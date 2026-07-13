package slider.translation

import contracts.i18n.LanguageCatalog
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import slider.AuthorContext
import slider.DeckContext

class DeckTranslationPlanTest {

    private val validAuthor = AuthorContext(name = "Test", email = "test@example.com")

    private fun validDeck(languageCode: String = "fr") = DeckContext(
        subject = "Kotlin coroutines",
        audience = "developers",
        duration = 45,
        languageCode = languageCode,
        outputFile = "kotlin-coroutines-deck.adoc",
        author = validAuthor,
    )

    private val allCodes = LanguageCatalog.supportedCodes().toList()

    @Test
    fun `plan should generate one translation task per target language`() {
        val request = TranslationRequest(
            sourceDeck = validDeck("fr"),
            targetLanguages = (allCodes - "fr"),
        )
        val plan = DeckTranslationPlan.from(request)

        assertThat(plan.tasks).hasSize(9)
    }

    @Test
    fun `plan tasks should pair source language with each target language`() {
        val request = TranslationRequest(
            sourceDeck = validDeck("fr"),
            targetLanguages = listOf("en", "ar", "zh"),
        )
        val plan = DeckTranslationPlan.from(request)

        assertThat(plan.tasks).hasSize(3)
        assertThat(plan.tasks.map { it.from }).containsOnly("fr")
        assertThat(plan.tasks.map { it.to }).containsExactlyInAnyOrder("en", "ar", "zh")
    }

    @Test
    fun `plan should expose source language from deck`() {
        val request = TranslationRequest(
            sourceDeck = validDeck("es"),
            targetLanguages = (allCodes - "es"),
        )
        val plan = DeckTranslationPlan.from(request)

        assertThat(plan.sourceLanguage).isEqualTo("es")
    }

    @Test
    fun `plan should expose target languages from request`() {
        val request = TranslationRequest(
            sourceDeck = validDeck("fr"),
            targetLanguages = listOf("en", "ar"),
        )
        val plan = DeckTranslationPlan.from(request)

        assertThat(plan.targetLanguages).containsExactly("en", "ar")
    }

    @Test
    fun `plan should not generate identity task when source is in target list`() {
        val request = TranslationRequest(
            sourceDeck = validDeck("fr"),
            targetLanguages = allCodes,
        )
        val plan = DeckTranslationPlan.from(request)

        assertThat(plan.tasks.map { it.to }).doesNotContain("fr")
        assertThat(plan.tasks).hasSize(9)
    }

    @Test
    fun `plan task should expose source deck for context`() {
        val deck = validDeck("fr")
        val request = TranslationRequest(
            sourceDeck = deck,
            targetLanguages = listOf("en"),
        )
        val plan = DeckTranslationPlan.from(request)

        assertThat(plan.tasks).hasSize(1)
        assertThat(plan.tasks.first().sourceDeck).isSameAs(deck)
    }

    @Test
    fun `plan should handle all 10 targets when source is outside catalog`() {
        val deck = validDeck("fr")
        val request = TranslationRequest(
            sourceDeck = deck,
            targetLanguages = allCodes,
        )
        val plan = DeckTranslationPlan.from(request)

        assertThat(plan.tasks).hasSize(9)
        plan.tasks.forEach { task ->
            assertThat(task.from).isEqualTo("fr")
            assertThat(task.to).isNotEqualTo("fr")
        }
    }
}