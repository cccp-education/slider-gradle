package slider.steps

import contracts.i18n.LanguageCatalog
import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import slider.AuthorContext
import slider.DeckContext
import slider.translation.DeckTranslationPlan
import slider.translation.DeckTranslator
import slider.translation.LanguageModelAdapter
import slider.translation.TranslationOutcome
import slider.translation.TranslationRequest
import slider.translation.TranslationResult

class DeckTranslationSteps : En {

    private lateinit var sourceDeck: DeckContext
    private var caught: Throwable? = null
    private var request: TranslationRequest? = null
    private var plan: DeckTranslationPlan? = null
    private var outcome: TranslationOutcome? = null
    private var stubResponses: Map<String, String?> = emptyMap()

    private val validAuthor = AuthorContext(name = "Test", email = "test@example.com")

    private fun deck(
        languageCode: String = "fr",
        subject: String = "Kotlin Coroutines",
        outputFile: String = "kotlin-deck.adoc",
    ) = DeckContext(
        subject = subject,
        audience = "developers",
        duration = 45,
        languageCode = languageCode,
        outputFile = outputFile,
        author = validAuthor,
    )

    private val sampleAdoc = """= Kotlin Coroutines
== Introduction
Welcome.
"""

    init {

        Given("a source deck in language {string}") { lang: String ->
            sourceDeck = deck(languageCode = lang)
        }

        Given("a source deck in language {string} with output file {string}") { lang: String, outFile: String ->
            sourceDeck = deck(languageCode = lang, outputFile = outFile)
        }

        Given("a source deck in language {string} with subject {string}") { lang: String, subject: String ->
            sourceDeck = deck(languageCode = lang, subject = subject)
        }

        Given("a stub LLM that returns content for {string}") { lang: String ->
            stubResponses = mapOf(lang to "= Translated content for $lang")
        }

        Given("a stub LLM that returns content for {string} and {string}") { lang1: String, lang2: String ->
            stubResponses = mapOf(
                lang1 to "= Translated content for $lang1",
                lang2 to "= Translated content for $lang2",
            )
        }

        Given("a stub LLM that returns content for {string} but null for {string}") { lang1: String, lang2: String ->
            stubResponses = mapOf(
                lang1 to "= Translated content for $lang1",
                lang2 to null,
            )
        }

        Given("a stub LLM that returns content for {string} and {string} but null for {string}") { lang1: String, lang2: String, lang3: String ->
            stubResponses = mapOf(
                lang1 to "= Translated content for $lang1",
                lang2 to "= Translated content for $lang2",
                lang3 to null,
            )
        }

        Given("a stub LLM that returns null for {string}") { lang: String ->
            stubResponses = mapOf(lang to null)
        }

        When("a translation request is created with default targets") {
            request = TranslationRequest(sourceDeck = sourceDeck)
        }

        When("a translation request is created with targets {string}") { targets: String ->
            val langs = targets.split(",").map { it.trim() }
            request = TranslationRequest(sourceDeck = sourceDeck, targetLanguages = langs)
        }

        When("the translation request creation is attempted") {
            try {
                request = TranslationRequest(sourceDeck = sourceDeck)
            } catch (t: Throwable) {
                caught = t
            }
        }

        When("the translation request creation is attempted with targets {string}") { targets: String ->
            try {
                val langs = targets.split(",").map { it.trim() }
                request = TranslationRequest(sourceDeck = sourceDeck, targetLanguages = langs)
            } catch (t: Throwable) {
                caught = t
            }
        }

        When("the translation request creation is attempted with empty targets") {
            try {
                request = TranslationRequest(sourceDeck = sourceDeck, targetLanguages = emptyList())
            } catch (t: Throwable) {
                caught = t
            }
        }

        When("a translation plan is built from a request targeting {string}") { targets: String ->
            val langs = targets.split(",").map { it.trim() }
            val req = TranslationRequest(sourceDeck = sourceDeck, targetLanguages = langs)
            plan = DeckTranslationPlan.from(req)
        }

        When("a translation plan is built from a request targeting all 10 languages") {
            val req = TranslationRequest(sourceDeck = sourceDeck)
            plan = DeckTranslationPlan.from(req)
        }

        When("the deck is translated into {string}") { targets: String ->
            val langs = targets.split(",").map { it.trim() }
            val req = TranslationRequest(sourceDeck = sourceDeck, targetLanguages = langs)
            val p = DeckTranslationPlan.from(req)
            val adapter = StubAdapter(stubResponses)
            val translator = DeckTranslator(adapter, sampleAdoc)
            outcome = translator.translate(p)
        }

        When("the deck is translated into {string} and {string}") { lang1: String, lang2: String ->
            val req = TranslationRequest(sourceDeck = sourceDeck, targetLanguages = listOf(lang1, lang2))
            val p = DeckTranslationPlan.from(req)
            val adapter = StubAdapter(stubResponses)
            val translator = DeckTranslator(adapter, sampleAdoc)
            outcome = translator.translate(p)
        }

        When("the deck is translated into {string}, {string}, and {string}") { lang1: String, lang2: String, lang3: String ->
            val req = TranslationRequest(sourceDeck = sourceDeck, targetLanguages = listOf(lang1, lang2, lang3))
            val p = DeckTranslationPlan.from(req)
            val adapter = StubAdapter(stubResponses)
            val translator = DeckTranslator(adapter, sampleAdoc)
            outcome = translator.translate(p)
        }

        Then("the request should target all 10 LanguageCatalog supported codes") {
            assertThat(request!!.targetLanguages).containsExactlyInAnyOrderElementsOf(
                LanguageCatalog.supportedCodes().toList()
            )
        }

        Then("the request should target exactly {int} languages") { count: Int ->
            assertThat(request!!.targetLanguages).hasSize(count)
        }

        Then("the request should target {string}") { lang: String ->
            assertThat(request!!.targetLanguages).contains(lang)
        }

        Then("the plan should contain {int} tasks") { count: Int ->
            assertThat(plan!!.tasks).hasSize(count)
        }

        Then("the plan should not contain a task from {string} to {string}") { from: String, to: String ->
            assertThat(plan!!.tasks.none { it.from == from && it.to == to }).isTrue()
        }

        Then("the plan source language should be {string}") { lang: String ->
            assertThat(plan!!.sourceLanguage).isEqualTo(lang)
        }

        Then("the translation creation should fail with a message containing {string}") { fragment: String ->
            assertThat(caught).isNotNull()
            assertThat(caught!!.message).contains(fragment)
        }

        Then("the outcome should have {int} translated results") { count: Int ->
            assertThat(outcome!!.translatedCount).isEqualTo(count)
        }

        Then("the outcome should have {int} failed results") { count: Int ->
            assertThat(outcome!!.failedCount).isEqualTo(count)
        }

        Then("the outcome should be all translated") {
            assertThat(outcome!!.isAllTranslated()).isTrue()
        }

        Then("the outcome should not be all translated") {
            assertThat(outcome!!.isAllTranslated()).isFalse()
        }

        Then("the translated deck for {string} should have language code {string}") { target: String, expectedCode: String ->
            val translated = outcome!!.translatedResults().find { it.targetLanguage == target }
            assertThat(translated).isNotNull()
            assertThat(translated!!.translatedDeck.languageCode).isEqualTo(expectedCode)
        }

        Then("the translated deck for {string} should have output file {string}") { target: String, expectedFile: String ->
            val translated = outcome!!.translatedResults().find { it.targetLanguage == target }
            assertThat(translated).isNotNull()
            assertThat(translated!!.translatedDeck.outputFile).isEqualTo(expectedFile)
        }

        Then("the translated deck for {string} should have subject {string}") { target: String, expectedSubject: String ->
            val translated = outcome!!.translatedResults().find { it.targetLanguage == target }
            assertThat(translated).isNotNull()
            assertThat(translated!!.translatedDeck.subject).isEqualTo(expectedSubject)
        }

        Then("the outcome summary should contain {string}") { fragment: String ->
            assertThat(outcome!!.summary()).contains(fragment)
        }
    }

    private class StubAdapter(
        private val responses: Map<String, String?>,
    ) : LanguageModelAdapter {
        override fun translate(
            deckContext: DeckContext,
            adocContent: String,
            sourceLanguage: String,
            targetLanguage: String,
        ): String? = responses[targetLanguage]
    }
}