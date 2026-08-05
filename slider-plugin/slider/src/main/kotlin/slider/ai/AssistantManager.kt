@file:Suppress("MemberVisibilityCanBePrivate")

package slider.ai

import codebase.koog.llm.service.LlmBuildService
import dev.langchain4j.model.chat.ChatModel
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildServiceSpec
import slider.DeckContext
import slider.ai.AssistantManager.PROP_AI_PROVIDER
import slider.ai.AssistantManager.PROVIDER_OLLAMA
import slider.i18n.SliderMessages

/**
 * Central AI orchestrator for the Slider Gradle plugin.
 *
 * EPIC SLD-8 (Decision 002): slider consumes codebase (N1) as the unified
 * LLM socle. The 4 provider factories (ollama/gemini/mistral/huggingface)
 * and the 8 hello* smoke-test tasks have been removed — provider resolution
 * now delegates to [LlmBuildService] via [LlmProviderChatModelAdapter].
 *
 * Responsibilities (post-SLD-8):
 * - [LlmBuildService] registration (Gradle-managed DI, Decision 001)
 * - [resolveModel] bridges codebase's [LlmProvider] to langchain4j [ChatModel]
 * - RAG task registration: reindexRag + proposeDeckContext + generateDeck
 * - [PromptManager]: system and user prompts for both pipeline steps
 *
 * Legacy ollama factories ([createOllamaChatModel],
 * [createOllamaStreamingChatModel], [generateStreamingResponse], [localModels])
 * are kept for the deprecated [slider.translate.TranslatorManager] (SLD-CR3)
 * and will be expelled in a future cleanup EPIC.
 */
object AssistantManager {

    // =========================================================================
    // Provider selection
    // =========================================================================

    const val PROP_AI_PROVIDER     = "ai.provider"
    const val PROVIDER_OLLAMA      = "ollama"
    const val PROVIDER_GEMINI      = "gemini"
    const val PROVIDER_MISTRAL     = "mistral"
    const val PROVIDER_HUGGINGFACE = "huggingface"

    /** Reads `-Pai.provider`, defaulting to `"ollama"` when absent or blank. */
    val Project.aiProvider: String
        get() = (findProperty(PROP_AI_PROVIDER) as? String
            ?: PROVIDER_OLLAMA).lowercase().trim()

    // =========================================================================
    // Local model catalog (legacy — used by deprecated TranslatorManager)
    // =========================================================================

    val localModels
        get() = setOf(
            "smollm:135m"                    to "SmollM",
            "llama3.2:3b-instruct-q8_0"      to "LlamaTiny",
            "smollm:135m-instruct-v0.2-q8_0" to "SmollMInstruct",
            "gemma3:1b-it-fp16"              to "Gemma3Instruct",
        )

    // =========================================================================
    // LlmBuildService registration + model resolution
    // =========================================================================

    /**
     * Registers the [LlmBuildService] (Gradle-managed DI) and returns its
     * [Provider]. The service exposes a codebase [codebase.koog.llm.LlmProvider]
     * resolved by [codebase.koog.llm.service.LlmServiceResolver].
     *
     * Call once per build (typically from [createChatTasks]); inject the
     * returned [Provider] into RAG tasks via `@ServiceReference`.
     */
    fun Project.registerLlmBuildService(): Provider<LlmBuildService> =
        gradle.sharedServices.registerIfAbsent(
            "sliderLlmService", LlmBuildService::class.java
        ) { spec: BuildServiceSpec<LlmBuildService.Params> ->
            spec.parameters.model.convention(project.aiProvider)
            spec.maxParallelUsages.set(1)
        }

    /**
     * Resolves the langchain4j [ChatModel] for the given [provider] by
     * delegating to the codebase [LlmBuildService] and wrapping the returned
     * [codebase.koog.llm.LlmProvider] in a [LlmProviderChatModelAdapter].
     *
     * This is the single resolution point used by [ProposeDeckContextTask]
     * and [GenerateDeckTask] (via [RagTasks]) and [slider.translation.TranslateDeckTask].
     *
     * ## Mock-LLM fallback (test compat)
     *
     * When `-Pollama.baseUrl` is set (typically to a test mock HTTP server),
     * resolution falls back to the legacy [createOllamaChatModel] which honors
     * that property. This keeps the GradleTestKit-based Cucumber scenarios
     * (feature `01_propose_deck_context.feature`) working without changes —
     * they inject a mock via `-Pollama.baseUrl`, not via [LlmBuildService].
     *
     * In production (no `-Pollama.baseUrl`), the codebase [LlmBuildService]
     * is used: `LlmProviderResolver` resolves the provider from the pool,
     * wrapped in a [LlmProviderChatModelAdapter].
     */
    fun Project.resolveModel(
        provider: String,
        serviceProvider: Provider<LlmBuildService>,
    ): ChatModel {
        val mockOllamaUrl = findProperty("ollama.baseUrl") as? String
        if (provider == PROVIDER_OLLAMA && mockOllamaUrl != null) {
            return createOllamaChatModel(findProperty("ollama.modelName") as? String ?: "smollm:135m")
        }
        val llmProvider = serviceProvider.get().provider()
        return LlmProviderChatModelAdapter(llmProvider)
    }

    // =========================================================================
    // Task registration
    // =========================================================================

    fun Project.createChatTasks() {
        val pgServiceProvider = gradle.sharedServices.registerIfAbsent(
            "pgVectorService", PgVectorService::class.java
        ) { spec: BuildServiceSpec<PgVectorService.Params> ->
            spec.parameters.image.convention(PgVectorService.DEFAULT_IMAGE)
            spec.parameters.database.convention(PgVectorService.DEFAULT_DATABASE)
            spec.parameters.user.convention(PgVectorService.DEFAULT_USER)
            spec.parameters.password.convention(PgVectorService.DEFAULT_PASSWORD)
            spec.parameters.table.convention(PgVectorService.DEFAULT_TABLE)
            spec.parameters.startupTimeout.convention(PgVectorService.DEFAULT_TIMEOUT)
            // If -Ppgvector.port is provided, use external pgvector (e.g. Testcontainers)
            (project.findProperty("pgvector.port") as? String)?.toIntOrNull()?.let {
                spec.parameters.externalPort.set(it)
            }
            spec.maxParallelUsages.set(1)
        }

        val llmServiceProvider = registerLlmBuildService()

        registerReindexRagTask(pgServiceProvider, llmServiceProvider)
        registerProposeDeckContextTask(pgServiceProvider, llmServiceProvider)
        registerGenerateDeckTask(pgServiceProvider, llmServiceProvider)
    }

    // =========================================================================
    // RAG task registration
    // =========================================================================

    private fun Project.registerReindexRagTask(
        pgServiceProvider: Provider<PgVectorService>,
        llmServiceProvider: Provider<LlmBuildService>,
    ) {
        tasks.register("reindexRag", ReindexRagTask::class.java) {
            val lang = SliderMessages.resolveLanguage(this@registerReindexRagTask)
            it.group = SliderMessages.get("task.group.collect", lang)
            it.description = SliderMessages.get("task.reindexRag.description", lang)
            it.pgVectorService.set(pgServiceProvider)
            it.usesService(pgServiceProvider)
            it.llmService.set(llmServiceProvider)
            it.usesService(llmServiceProvider)
        }
    }

    private fun Project.registerProposeDeckContextTask(
        pgServiceProvider: Provider<PgVectorService>,
        llmServiceProvider: Provider<LlmBuildService>,
    ) {
        tasks.register("proposeDeckContext", ProposeDeckContextTask::class.java) {
            val lang = SliderMessages.resolveLanguage(this@registerProposeDeckContextTask)
            it.group = SliderMessages.get("task.group.generate", lang)
            it.description = SliderMessages.get("task.proposeDeckContext.description", lang)
            it.pgVectorService.set(pgServiceProvider)
            it.usesService(pgServiceProvider)
            it.llmService.set(llmServiceProvider)
            it.usesService(llmServiceProvider)
        }
    }

    private fun Project.registerGenerateDeckTask(
        pgServiceProvider: Provider<PgVectorService>,
        llmServiceProvider: Provider<LlmBuildService>,
    ) {
        tasks.register("generateDeck", GenerateDeckTask::class.java) {
            val lang = SliderMessages.resolveLanguage(this@registerGenerateDeckTask)
            it.group = SliderMessages.get("task.group.generate", lang)
            it.description = SliderMessages.get("task.generateDeck.description", lang)
            it.pgVectorService.set(pgServiceProvider)
            it.usesService(pgServiceProvider)
            it.llmService.set(llmServiceProvider)
            it.usesService(llmServiceProvider)
        }
    }

    // =========================================================================
    // Legacy Ollama factories — kept for deprecated TranslatorManager (SLD-CR3)
    // =========================================================================

    fun Project.createOllamaChatModel(model: String = "smollm:135m"): dev.langchain4j.model.ollama.OllamaChatModel =
        dev.langchain4j.model.ollama.OllamaChatModel.builder().apply {
            baseUrl(findProperty("ollama.baseUrl") as? String ?: "http://localhost:11439")
            modelName(findProperty("ollama.modelName") as? String ?: model)
            temperature(findProperty("ollama.temperature") as? Double ?: 0.8)
            java.time.Duration.ofSeconds(findProperty("ollama.timeout") as? Long ?: 6_000)
            logRequests(true)
            logResponses(true)
        }.build()

    fun Project.createOllamaStreamingChatModel(model: String = "smollm:135m"): dev.langchain4j.model.ollama.OllamaStreamingChatModel =
        dev.langchain4j.model.ollama.OllamaStreamingChatModel.builder().apply {
            baseUrl(findProperty("ollama.baseUrl") as? String ?: "http://localhost:11439")
            modelName(findProperty("ollama.modelName") as? String ?: model)
            temperature(findProperty("ollama.temperature") as? Double ?: 0.8)
            java.time.Duration.ofSeconds(findProperty("ollama.timeout") as? Long ?: 6_000)
            logRequests(true)
            logResponses(true)
        }.build()

    suspend fun generateStreamingResponse(
        model: dev.langchain4j.model.chat.StreamingChatModel,
        promptMessage: String
    ): arrow.core.Either<Throwable, dev.langchain4j.model.chat.response.ChatResponse> =
        arrow.core.Either.Companion.catch {
            kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                model.chat(promptMessage, object : dev.langchain4j.model.chat.response.StreamingChatResponseHandler {
                    override fun onPartialResponse(partialResponse: String) = print(partialResponse)
                    override fun onCompleteResponse(response: dev.langchain4j.model.chat.response.ChatResponse) = continuation.resumeWith(Result.success(response))
                    override fun onError(error: Throwable) { continuation.cancel(error) }
                })
            }
        }

    // =========================================================================
    // PromptManager
    // =========================================================================

    object PromptManager {

        // ---------------------------------------------------------------------
        // Step 1 — DeckContext proposal
        // ---------------------------------------------------------------------

        val contextSystemPrompt = """
You are E-3PO, an expert instructional designer for adult technical training.

Your task is to propose a structured DeckContext for a slide deck.

## OUTPUT CONTRACT
Return ONLY a valid JSON object matching this exact structure — no markdown fences,
no explanation, no preamble:

{
  "subject": "string",
  "audience": "string",
  "duration": <integer minutes>,
  "language": "string",
  "outputFile": "string (kebab-case, ends with -deck_<lang>.adoc where <lang> is the ISO 639-1 language code, e.g. kotlin-coroutines-deck_fr.adoc)",
  "author": { "name": "string", "email": "string" },
  "revealjs": {
    "theme": "string",
    "transition": "slide",
    "slideNumber": "c/t",
    "width": 1408,
    "height": 792,
    "controls": true,
    "controlsLayout": "edges",
    "history": true,
    "fragmentInURL": true
  },
  "notes": {
    "speakerNotes": true,
    "pageNotes": true,
    "pageNotesStyle": "DETAILED"
  },
  "slides": [
    { "title": "string", "speakerHint": "string", "pageNotesHint": "string", "transition": "string", "autoAnimate": false, "autoAnimateEasing": "ease-in-out", "autoAnimateDuration": 1.0 }
  ]
}

## SLIDE PLANNING RULES
- First slide after title: always an Agenda slide
- Last slide: always a Summary/Conclusion slide
- Duration guideline: ~2 minutes per content slide
- Adapt depth and number of slides to the audience level
- speakerHint: what the presenter should say/demonstrate on that slide
- pageNotesHint: references, exercises or deeper content for learners
- outputFile: must follow the pattern <subject-slug>-deck_<lang>.adoc (e.g. kotlin-coroutines-deck_fr.adoc, spring-boot-intro-deck_en.adoc)
""".trimIndent()

        fun contextUserMessage(
            subject: String,
            language: String,
            authorName: String,
            authorEmail: String,
            ragContext: String,
        ): String = buildString {
            appendLine("Propose a complete DeckContext JSON for the following subject:")
            appendLine()
            appendLine("Subject : $subject")
            appendLine("Language: $language")
            appendLine("The outputFile MUST follow the pattern: <subject-slug>_${language}-deck.adoc")
            appendLine("Author name : $authorName")
            appendLine("Author email: $authorEmail")
            appendLine()
            if (ragContext.isNotEmpty()) {
                appendLine("## Relevant examples from the project (use as structural reference)")
                appendLine()
                appendLine(ragContext)
                appendLine()
            }
            appendLine("Return ONLY the JSON object. No explanation, no markdown fences.")
        }

        // ---------------------------------------------------------------------
        // Step 2 — Deck generation
        // ---------------------------------------------------------------------

        val deckSystemPrompt = """
You are E-3PO, an expert at generating AsciiDoc/Reveal.js slide decks.

## OUTPUT CONTRACT
Output raw AsciiDoc ONLY — no markdown, no explanations, no code fences around the deck.

## REQUIRED HEADER
= <subject>
<author name> <<author email>>
:revealjs_theme: <theme>
:revealjs_transition: <transition>
:revealjs_slideNumber: <slideNumber>
:revealjs_width: <width>
:revealjs_height: <height>
:revealjs_controls: true
:revealjs_controlsLayout: edges
:revealjs_history: true
:revealjs_fragmentInURL: true
:source-highlighter: rouge

## SLIDE STRUCTURE
Each slide: == Slide Title
Speaker notes (if speakerNotes=true): [NOTE.speaker] -- … --
Page notes   (if pageNotes=true):     [.notes] -- … --

Per-slide transition override: [.slide, data-transition="zoom"]
Add this line right after the == heading on any slide.

pageNotesStyle controls [.notes] depth:
  MINIMAL        → one reference line
  DETAILED       → deep content + references + exercises
  EXERCISES_ONLY → practical exercises only

## SIZE CONSTRAINTS
- With [%step]    : max 5 bullet points per slide
- Without [%step] : max 7 bullet points per slide
- Code block      : max 10 lines per [source,...] block
- Prose           : max 3 sentences per slide
- NEVER mix a bullet list and a code block on the same slide

## ABSOLUTE RULES
1. Every slide must have a == heading
2. First slide after title: Agenda/Plan
3. Last slide: Summary/Conclusion
4. Follow slide hints order and titles exactly when provided
5. Content language must match the `language` field

## AUTO-ANIMATE (reveal.js 4.0+)
Two adjacent slides with [%auto-animate] will automatically animate
matched DOM elements between them.

Syntax:
[%auto-animate]
== Slide Title A
Content…

[%auto-animate]
== Slide Title B
Modified content — elements animate automatically

Options (on the same line as %auto-animate):
  [%auto-animate,auto-animate-easing="ease-in-out",auto-animate-duration=2.0]
  == Slide Title

Use auto-animate for:
- Code evolution (add/remove lines across slides)
- List growth (items appearing one by one)
- Element repositioning (title moves up, body appears)
- Style transitions (color, size, margin changes)
""".trimIndent()

        fun deckUserMessage(ctx: DeckContext, ragContext: String): String = buildString {
            appendLine("Generate a complete Reveal.js slide deck with the following context:")
            appendLine()
            appendLine("Subject   : ${ctx.subject}")
            appendLine("Audience  : ${ctx.audience}")
            appendLine("Duration  : ${ctx.duration} minutes")
            appendLine("Language  : ${ctx.languageCode}")
            appendLine("OutputFile: ${ctx.outputFile}")
            appendLine()
            appendLine("Author:")
            appendLine("  Name  : ${ctx.author.name}")
            appendLine("  Email : ${ctx.author.email}")
            appendLine()
            appendLine("Reveal.js configuration:")
            appendLine("  theme        : ${ctx.revealjs.theme}")
            appendLine("  transition   : ${ctx.revealjs.transition}")
            appendLine("  slideNumber  : ${ctx.revealjs.slideNumber}")
            appendLine("  width        : ${ctx.revealjs.width}")
            appendLine("  height       : ${ctx.revealjs.height}")
            appendLine()
            appendLine("Notes configuration:")
            appendLine("  speakerNotes   : ${ctx.notes.speakerNotes}")
            appendLine("  pageNotes      : ${ctx.notes.pageNotes}")
            appendLine("  pageNotesStyle : ${ctx.notes.pageNotesStyle}")
            appendLine()
            if (ctx.slides.isEmpty())
                appendLine("No slide hints provided — build a pedagogically appropriate structure.")
            else {
                appendLine("Slide hints (follow this order and these titles exactly):")
                ctx.slides.forEach { hint ->
                    appendLine("  - title: ${hint.title}")
                    hint.transition?.let   { appendLine("    transition: $it") }
                    if (hint.autoAnimate) appendLine("    autoAnimate: true")
                    hint.autoAnimateEasing?.let { appendLine("    autoAnimateEasing: $it") }
                    hint.autoAnimateDuration?.let { appendLine("    autoAnimateDuration: $it") }
                    hint.speakerHint?.let  { appendLine("    speakerHint: $it") }
                    hint.pageNotesHint?.let { appendLine("    pageNotesHint: $it") }
                }
            }
            if (ragContext.isNotEmpty()) {
                appendLine()
                appendLine("## AsciiDoc syntax reference examples (from project — use as style guide)")
                appendLine()
                appendLine(ragContext)
            }
        }
    }
}