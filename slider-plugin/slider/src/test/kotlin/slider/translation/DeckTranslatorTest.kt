package slider.translation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import slider.AuthorContext
import slider.DeckContext

class DeckTranslatorTest {

    private val validAuthor = AuthorContext(name = "Test", email = "test@example.com")

    private fun deck(languageCode: String = "fr") = DeckContext(
        subject = "Kotlin coroutines",
        audience = "developers",
        duration = 45,
        languageCode = languageCode,
        outputFile = "kotlin-coroutines-deck.adoc",
        author = validAuthor,
    )

    private val sampleAdoc = """= Kotlin Coroutines
:revealjs_theme: sky

== Introduction
Welcome to coroutines.

== Structured Concurrency
Let's dive in.
"""

    private class StubModelAdapter(
        private val responseForTarget: Map<String, String?>,
    ) : LanguageModelAdapter {
        override fun translate(
            deckContext: DeckContext,
            adocContent: String,
            sourceLanguage: String,
            targetLanguage: String,
        ): String? = responseForTarget[targetLanguage]
    }

    @Test
    fun `should translate all tasks when LLM returns content for every language`() {
        val adapter = StubModelAdapter(
            mapOf(
                "en" to "= Kotlin Coroutines\n\n== Introduction\nWelcome",
                "ar" to "= كوتلن كوروتين\n\n== مقدمة\nأهلا",
                "zh" to "= Kotlin 协程\n\n== 简介\n欢迎",
            )
        )
        val request = TranslationRequest(
            sourceDeck = deck("fr"),
            targetLanguages = listOf("en", "ar", "zh"),
        )
        val plan = DeckTranslationPlan.from(request)
        val translator = DeckTranslator(adapter, sampleAdoc)

        val outcome = translator.translate(plan)

        assertThat(outcome.translatedCount).isEqualTo(3)
        assertThat(outcome.failedCount).isZero()
        assertThat(outcome.skippedCount).isZero()
        assertThat(outcome.isAllTranslated()).isTrue()
    }

    @Test
    fun `should produce Failed result when LLM returns null`() {
        val adapter = StubModelAdapter(
            mapOf(
                "en" to "= Content",
                "ar" to null,
            )
        )
        val request = TranslationRequest(
            sourceDeck = deck("fr"),
            targetLanguages = listOf("en", "ar"),
        )
        val plan = DeckTranslationPlan.from(request)
        val translator = DeckTranslator(adapter, sampleAdoc)

        val outcome = translator.translate(plan)

        assertThat(outcome.translatedCount).isEqualTo(1)
        assertThat(outcome.failedCount).isEqualTo(1)
        assertThat(outcome.failures().first().targetLanguage).isEqualTo("ar")
        assertThat(outcome.failures().first().errorMessage).contains("ar")
    }

    @Test
    fun `should produce Failed result when LLM returns blank string`() {
        val adapter = StubModelAdapter(
            mapOf(
                "en" to "   ",
                "ar" to "= Content",
            )
        )
        val request = TranslationRequest(
            sourceDeck = deck("fr"),
            targetLanguages = listOf("en", "ar"),
        )
        val plan = DeckTranslationPlan.from(request)
        val translator = DeckTranslator(adapter, sampleAdoc)

        val outcome = translator.translate(plan)

        assertThat(outcome.failedCount).isEqualTo(1)
        assertThat(outcome.failures().first().targetLanguage).isEqualTo("en")
    }

    @Test
    fun `translated deck should have target language code and updated output file`() {
        val adapter = StubModelAdapter(mapOf("en" to "= Kotlin Coroutines"))
        val request = TranslationRequest(
            sourceDeck = deck("fr"),
            targetLanguages = listOf("en"),
        )
        val plan = DeckTranslationPlan.from(request)
        val translator = DeckTranslator(adapter, sampleAdoc)

        val outcome = translator.translate(plan)
        val translated = outcome.translatedResults().first()

        assertThat(translated.translatedDeck.languageCode).isEqualTo("en")
        assertThat(translated.translatedDeck.outputFile).isEqualTo("kotlin-coroutines_en-deck.adoc")
    }

    @Test
    fun `translated deck output file should replace existing language segment`() {
        val adapter = StubModelAdapter(mapOf("en" to "= Content"))
        val sourceDeck = deck("fr").copy(outputFile = "kotlin-coroutines_fr-deck.adoc")
        val request = TranslationRequest(
            sourceDeck = sourceDeck,
            targetLanguages = listOf("en"),
        )
        val plan = DeckTranslationPlan.from(request)
        val translator = DeckTranslator(adapter, sampleAdoc)

        val outcome = translator.translate(plan)
        val translated = outcome.translatedResults().first()

        assertThat(translated.translatedDeck.outputFile).isEqualTo("kotlin-coroutines_en-deck.adoc")
    }

    @Test
    fun `translated adoc content should match LLM response`() {
        val llmResponse = "= My Translated Deck\n\n== Slide 1\nHello"
        val adapter = StubModelAdapter(mapOf("es" to llmResponse))
        val request = TranslationRequest(
            sourceDeck = deck("fr"),
            targetLanguages = listOf("es"),
        )
        val plan = DeckTranslationPlan.from(request)
        val translator = DeckTranslator(adapter, sampleAdoc)

        val outcome = translator.translate(plan)
        val translated = outcome.translatedResults().first()

        assertThat(translated.translatedAdoc).isEqualTo(llmResponse)
    }

    @Test
    fun `should preserve source deck subject and audience in translated deck`() {
        val adapter = StubModelAdapter(mapOf("en" to "= Content"))
        val source = deck("fr").copy(subject = "My Subject", audience = "My Audience")
        val request = TranslationRequest(
            sourceDeck = source,
            targetLanguages = listOf("en"),
        )
        val plan = DeckTranslationPlan.from(request)
        val translator = DeckTranslator(adapter, sampleAdoc)

        val outcome = translator.translate(plan)
        val translated = outcome.translatedResults().first()

        assertThat(translated.translatedDeck.subject).isEqualTo("My Subject")
        assertThat(translated.translatedDeck.audience).isEqualTo("My Audience")
    }

    @Test
    fun `should translate all 9 tasks when source is fr and targets are all 10 minus fr`() {
        val allTargets = (contracts.i18n.LanguageCatalog.supportedCodes() - "fr").toList()
        val responses = allTargets.associateWith { "= Translated $it" }
        val adapter = StubModelAdapter(responses)
        val request = TranslationRequest(
            sourceDeck = deck("fr"),
            targetLanguages = allTargets,
        )
        val plan = DeckTranslationPlan.from(request)
        val translator = DeckTranslator(adapter, sampleAdoc)

        val outcome = translator.translate(plan)

        assertThat(outcome.totalCount).isEqualTo(9)
        assertThat(outcome.isAllTranslated()).isTrue()
    }

    @Test
    fun `outcome summary should reflect partial failure`() {
        val adapter = StubModelAdapter(
            mapOf(
                "en" to "= OK",
                "ar" to null,
                "zh" to "= OK",
            )
        )
        val request = TranslationRequest(
            sourceDeck = deck("fr"),
            targetLanguages = listOf("en", "ar", "zh"),
        )
        val plan = DeckTranslationPlan.from(request)
        val translator = DeckTranslator(adapter, sampleAdoc)

        val outcome = translator.translate(plan)
        val summary = outcome.summary()

        assertThat(summary).contains("2 translated")
        assertThat(summary).contains("1 failed")
    }
}