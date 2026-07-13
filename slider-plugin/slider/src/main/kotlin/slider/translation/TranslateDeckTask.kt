package slider.translation

import contracts.i18n.LanguageCatalog
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.register
import org.gradle.work.DisableCachingByDefault
import slider.DeckContext
import slider.Slides.RevealJsSlides.TASK_TRANSLATE_DECK
import slider.SliderManager.Configuration.yamlMapper
import slider.ai.AssistantManager.aiProvider
import slider.ai.AssistantManager.resolveModel
import java.io.File

/**
 * Gradle task: `translateDeck`
 *
 * Takes a source deck (deck-context.yml + associated .adoc file) and
 * translates it into the 10 supported languages (or a custom subset
 * via `-Ptarget.languages`).
 *
 * Required properties:
 *   `-Pdeck.context=<path>`     path to the source deck-context.yml
 *
 * Optional properties:
 *   `-Pdeck.adoc=<path>`        path to the source .adoc file (defaults
 *                               to the deck context's outputFile resolved
 *                               against slides/misc/)
 *   `-Ptarget.languages=en,zh`  comma-separated target ISO 639-1 codes
 *                               (defaults to all 10 LanguageCatalog codes)
 *   `-Pai.provider=<provider>`  ollama (default) | gemini | mistral | huggingface
 *
 * Usage:
 *   ./gradlew translateDeck -Pdeck.context=slides/misc/kotlin-deck-context.yml
 *   ./gradlew translateDeck -Pdeck.context=slides/misc/kotlin-deck-context.yml -Ptarget.languages=en,ar
 *
 * Outputs (in slides/misc/):
 *   - <slug>_<lang>-deck-context.yml  (translated DeckContext per language)
 *   - <slug>_<lang>-deck.adoc         (translated AsciiDoc per language)
 */
@DisableCachingByDefault(because = "LLM call is non-deterministic")
abstract class TranslateDeckTask : DefaultTask() {

    @set:Option(option = "deck.context", description = "Path to the source deck-context.yml")
    @get:Input
    lateinit var deckContextPath: String

    @set:Option(option = "deck.adoc", description = "Path to the source .adoc file (optional)")
    @get:Input
    @get:Optional
    var deckAdocPath: String? = null

    @set:Option(option = "target.languages", description = "Comma-separated target ISO 639-1 codes")
    @get:Input
    @get:Optional
    var targetLanguagesRaw: String? = null

    @TaskAction
    fun run() {
        val contextFile = File(deckContextPath)
            .also { require(it.exists()) { "Deck context file not found: $deckContextPath" } }

        val sourceDeck: DeckContext = yamlMapper.readValue(contextFile, DeckContext::class.java)
        sourceDeck.requireValidLanguage()

        val adocFile = deckAdocPath?.let(::File)
            ?: contextFile.parentFile.resolve(sourceDeck.outputFile)
        require(adocFile.exists()) {
            "Deck AsciiDoc file not found: ${adocFile.absolutePath}. " +
                "Specify it with -Pdeck.adoc=<path>"
        }
        val adocContent = adocFile.readText()

        val targetLanguages = targetLanguagesRaw
            ?.split(",")
            ?.map { it.trim().lowercase() }
            ?.filter { it.isNotEmpty() }
            ?: LanguageCatalog.supportedCodes().toList()

        val request = TranslationRequest(
            sourceDeck = sourceDeck,
            targetLanguages = targetLanguages,
        )
        val plan = DeckTranslationPlan.from(request)

        val provider = project.aiProvider
        val model = project.resolveModel(provider)
        val adapter = OllamaLanguageModelAdapter(model)
        val translator = DeckTranslator(adapter, adocContent)

        logger.lifecycle("🌍 Translating deck '${sourceDeck.subject}' from ${plan.sourceLanguage} to ${plan.tasks.size} languages")
        logger.lifecycle("   Provider: $provider")
        logger.lifecycle("   Targets: ${plan.tasks.map { it.to }}")

        val outcome = translator.translate(plan)

        val outputDir = contextFile.parentFile
        outcome.translatedResults().forEach { result ->
            val ctxFile = outputDir.resolve(
                result.translatedDeck.outputFile.replace(".adoc", "-context.yml")
            )
            yamlMapper.writeValue(ctxFile, result.translatedDeck)

            val adocOut = outputDir.resolve(result.translatedDeck.outputFile)
            adocOut.writeText(result.translatedAdoc)

            logger.lifecycle("✅ {} → {}", result.targetLanguage, adocOut.name)
        }

        outcome.failures().forEach { failure ->
            logger.error("❌ {} — {}", failure.targetLanguage, failure.errorMessage)
        }

        logger.lifecycle("📊 {}", outcome.summary())
    }
}

/**
 * Registers the `translateDeck` task on the project.
 */
fun Project.registerTranslateDeckTask() {
    tasks.register<TranslateDeckTask>(TASK_TRANSLATE_DECK) {
        group = "translator"
        description = "Translate a deck (deck-context.yml + .adoc) into multiple languages."

        project.findProperty("deck.context")?.let { deckContextPath = it.toString() }
        project.findProperty("deck.adoc")?.let { deckAdocPath = it.toString() }
        project.findProperty("target.languages")?.let { targetLanguagesRaw = it.toString() }
    }
}