package slider.translation

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import slider.AuthorContext
import slider.DeckContext

class TranslationResultTest {

    private val validAuthor = AuthorContext(name = "Test", email = "test@example.com")

    private fun deck(languageCode: String, subject: String = "Kotlin coroutines") = DeckContext(
        subject = subject,
        audience = "developers",
        duration = 45,
        languageCode = languageCode,
        outputFile = "kotlin-coroutines-${languageCode}-deck.adoc",
        author = validAuthor,
    )

    @Test
    fun `Translated result should expose target language and translated deck`() {
        val translatedDeck = deck("en")
        val result = TranslationResult.Translated(
            targetLanguage = "en",
            translatedDeck = translatedDeck,
            translatedAdoc = "= Kotlin Coroutines\n\n== Intro\n",
        )

        assertThat(result.targetLanguage).isEqualTo("en")
        assertThat(result.translatedDeck).isEqualTo(translatedDeck)
        assertThat(result.translatedAdoc).contains("Kotlin Coroutines")
        assertThat(result.status).isEqualTo(TranslationStatus.TRANSLATED)
    }

    @Test
    fun `Translated result should reject blank target language`() {
        assertThatThrownBy {
            TranslationResult.Translated(
                targetLanguage = "",
                translatedDeck = deck("en"),
                translatedAdoc = "content",
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("targetLanguage")
    }

    @Test
    fun `Translated result should reject blank translated adoc`() {
        assertThatThrownBy {
            TranslationResult.Translated(
                targetLanguage = "en",
                translatedDeck = deck("en"),
                translatedAdoc = "",
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("translatedAdoc")
    }

    @Test
    fun `Skipped result should expose target language and reason`() {
        val result = TranslationResult.Skipped(
            targetLanguage = "fr",
            reason = "Source and target language are the same",
        )

        assertThat(result.targetLanguage).isEqualTo("fr")
        assertThat(result.reason).contains("same")
        assertThat(result.status).isEqualTo(TranslationStatus.SKIPPED)
    }

    @Test
    fun `Skipped result should reject blank target language`() {
        assertThatThrownBy {
            TranslationResult.Skipped(targetLanguage = "", reason = "x")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `Failed result should expose target language and error message`() {
        val result = TranslationResult.Failed(
            targetLanguage = "zh",
            errorMessage = "LLM returned empty response",
        )

        assertThat(result.targetLanguage).isEqualTo("zh")
        assertThat(result.errorMessage).contains("empty")
        assertThat(result.status).isEqualTo(TranslationStatus.FAILED)
    }

    @Test
    fun `Failed result should reject blank target language`() {
        assertThatThrownBy {
            TranslationResult.Failed(targetLanguage = "", errorMessage = "x")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `status property should distinguish the three variants`() {
        val translated = TranslationResult.Translated(
            targetLanguage = "en",
            translatedDeck = deck("en"),
            translatedAdoc = "content",
        )
        val skipped = TranslationResult.Skipped("fr", "identity")
        val failed = TranslationResult.Failed("zh", "error")

        assertThat(translated.status).isEqualTo(TranslationStatus.TRANSLATED)
        assertThat(skipped.status).isEqualTo(TranslationStatus.SKIPPED)
        assertThat(failed.status).isEqualTo(TranslationStatus.FAILED)
    }
}