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
import slider.SliderConfig.yamlMapper
import slider.ai.AssistantManager.aiProvider
import slider.ai.AssistantManager.resolveModel
import slider.i18n.SliderMessages
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
        val lang = SliderMessages.resolveLanguage(project)

        val contextFile = File(deckContextPath)
            .also { require(it.exists()) { SliderMessages.format("translate.error.deckContextNotFound", lang, deckContextPath) } }

        val sourceDeck: DeckContext = yamlMapper.readValue(contextFile, DeckContext::class.java)
        sourceDeck.requireValidLanguage()

        val adocFile = deckAdocPath?.let(::File)
            ?: contextFile.parentFile.resolve(sourceDeck.outputFile)
        require(adocFile.exists()) {
            SliderMessages.format("translate.error.adocNotFound", lang, adocFile.absolutePath)
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

        if (plan.tasks.isEmpty()) {
            logger.lifecycle(SliderMessages.format("task.translateDeck.noTargets", lang, sourceDeck.languageCode))
            return
        }

        val provider = project.aiProvider
        val model = project.resolveModel(provider)
        val adapter = OllamaLanguageModelAdapter(model)
        val translator = DeckTranslator(adapter, adocContent)

        logger.lifecycle(SliderMessages.format("task.translateDeck.translating", lang, sourceDeck.subject, plan.sourceLanguage, plan.tasks.size))
        logger.lifecycle(SliderMessages.format("task.translateDeck.provider", lang, provider))
        logger.lifecycle(SliderMessages.format("task.translateDeck.targets", lang, plan.tasks.map { it.to }))

        val outcome = translator.translate(plan)

        val outputDir = contextFile.parentFile
        outcome.translatedResults().forEach { result ->
            val ctxFile = outputDir.resolve(
                result.translatedDeck.outputFile.replace(".adoc", "-context.yml")
            )
            yamlMapper.writeValue(ctxFile, result.translatedDeck)

            val adocOut = outputDir.resolve(result.translatedDeck.outputFile)
            adocOut.writeText(result.translatedAdoc)

            logger.lifecycle(SliderMessages.format("task.translateDeck.translated", lang, result.targetLanguage, adocOut.name))
        }

        outcome.failures().forEach { failure ->
            logger.error(SliderMessages.format("task.translateDeck.failed", lang, failure.targetLanguage, failure.errorMessage))
        }

        logger.lifecycle(SliderMessages.format("task.translateDeck.summary", lang, outcome.summary()))
    }
}

/**
 * Registers the `translateDeck` task on the project.
 */
fun Project.registerTranslateDeckTask() {
    val lang = SliderMessages.resolveLanguage(this)
    tasks.register<TranslateDeckTask>(TASK_TRANSLATE_DECK) {
        group = SliderMessages.get("task.group.translator", lang)
        description = SliderMessages.get("task.translateDeck.description", lang)

        project.findProperty("deck.context")?.let { deckContextPath = it.toString() }
        project.findProperty("deck.adoc")?.let { deckAdocPath = it.toString() }
        project.findProperty("target.languages")?.let { targetLanguagesRaw = it.toString() }
    }
}