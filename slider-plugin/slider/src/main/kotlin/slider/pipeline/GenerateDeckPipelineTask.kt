package slider.pipeline

import codebase.koog.llm.service.LlmBuildService
import slider.ai.AssistantManager.aiProvider
import slider.ai.AssistantManager.resolveModel
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Gradle task: `generateDeckPipeline`
 *
 * Thin adapter that orchestrates the koog [DeckPipelineGraph]
 * (propose-context → validate-context → generate-deck) in a single
 * invocation — the SLD-8 US-8.3 pipeline entry point.
 *
 * Unlike [slider.ai.ProposeDeckContextTask] + [slider.ai.GenerateDeckTask]
 * (the two-step RAG pipeline), this task runs the whole graph in one shot
 * and does not touch pgvector — RAG context can be supplied via
 * `-Pdeck.ragContext` for tests but the pipeline itself stays RAG-free
 * (the graph is pure LLM orchestration).
 *
 * Required properties:
 *   `-Psubject=<text>`
 *
 * Optional properties:
 *   `-Planguage=<lang>`           ISO 639-1 code, default: fr
 *   `-Pauthor.name=<text>`        default: git config user.name
 *   `-Pauthor.email=<text>`       default: git config user.email
 *   `-Pdeck.output=<path>`        overrides the auto-generated path
 *   `-Pdeck.ragContext=<text>`    RAG context to feed the LLM prompts
 *   `-Pai.provider=<provider>`   ollama (default) | gemini | mistral | huggingface
 *
 * Usage:
 *   ./gradlew generateDeckPipeline -Psubject="Kotlin coroutines"
 */
@DisableCachingByDefault(because = "LLM orchestration")
abstract class GenerateDeckPipelineTask : DefaultTask() {

    @get:ServiceReference
    abstract val llmService: Property<LlmBuildService>

    @TaskAction
    fun run() {
        val subject = project.findProperty("subject") as? String
            ?: error(
                "Missing required property -Psubject.\n" +
                    "Usage: ./gradlew generateDeckPipeline -Psubject=\"Introduction to Kotlin\""
            )

        val language = (project.findProperty("language") as? String)
            ?.takeIf { it.isNotBlank() }
            ?: "fr"

        val authorName = project.findProperty("author.name") as? String
            ?: runCatching {
                ProcessBuilder("git", "config", "user.name")
                    .directory(project.projectDir)
                    .start().inputStream.bufferedReader().readLine()
            }.getOrNull() ?: "Unknown"
        val authorEmail = project.findProperty("author.email") as? String
            ?: runCatching {
                ProcessBuilder("git", "config", "user.email")
                    .directory(project.projectDir)
                    .start().inputStream.bufferedReader().readLine()
            }.getOrNull() ?: "unknown@example.com"

        val ragContext = (project.findProperty("deck.ragContext") as? String) ?: ""
        val provider = project.aiProvider

        println("🎨 [pipeline] generateDeckPipeline — provider: $provider — subject: \"$subject\" — lang: $language")

        val model = project.resolveModel(provider, llmService)
        val graph = DeckPipelineGraph(
            promptBuilder = PromptManagerDeckPromptBuilder(),
            llm = ChatModelDeckLlm(model),
        )

        val initial = DeckState(
            subject = subject,
            language = language,
            authorName = authorName,
            authorEmail = authorEmail,
            ragContext = ragContext,
            deckContextJson = "",
        )

        val result = graph.execute(initial)

        if (result.stage == DeckStage.FAILED) {
            error("generateDeckPipeline failed at stage ${result.stage}: ${result.error}")
        }

        val outputFile = (project.findProperty("deck.output") as? String)
            ?.let { project.layout.projectDirectory.asFile.resolve(it) }
            ?: project.layout.projectDirectory.asFile
                .resolve("slides/misc")
                .resolve(subject.toSlug() + "-deck_${language}.adoc")
        outputFile.parentFile.mkdirs()
        outputFile.writeText(result.deckAdoc)

        println("✅ [pipeline] Deck generated → ${outputFile.absolutePath}")
    }

    private fun String.toSlug(): String =
        lowercase()
            .replace(Regex("[àáâãäå]"), "a")
            .replace(Regex("[èéêë]"), "e")
            .replace(Regex("[ìíîï]"), "i")
            .replace(Regex("[òóôõö]"), "o")
            .replace(Regex("[ùúûü]"), "u")
            .replace(Regex("ç"), "c")
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
}