package slider.translation

import contracts.i18n.LanguageCatalog
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import slider.AuthorContext
import slider.DeckContext

class TranslationRequestTest {

    private val validAuthor = AuthorContext(name = "Test", email = "test@example.com")

    private fun validDeck(
        subject: String = "Kotlin coroutines",
        languageCode: String = "fr",
        outputFile: String = "kotlin-coroutines-deck.adoc",
    ) = DeckContext(
        subject = subject,
        audience = "developers",
        duration = 45,
        languageCode = languageCode,
        outputFile = outputFile,
        author = validAuthor,
    )

    private val allCodes = LanguageCatalog.supportedCodes().toList()

    @Test
    fun `should construct with valid deck and default target languages`() {
        val req = TranslationRequest(
            sourceDeck = validDeck(),
        )

        assertThat(req.sourceDeck.subject).isEqualTo("Kotlin coroutines")
        assertThat(req.sourceDeck.languageCode).isEqualTo("fr")
        assertThat(req.targetLanguages).containsExactlyInAnyOrderElementsOf(allCodes)
    }

    @Test
    fun `should construct with explicit target languages`() {
        val req = TranslationRequest(
            sourceDeck = validDeck(),
            targetLanguages = listOf("en", "ar", "zh"),
        )

        assertThat(req.targetLanguages).containsExactly("en", "ar", "zh")
    }

    @Test
    fun `default target languages should cover all 10 LanguageCatalog supported codes`() {
        val req = TranslationRequest(sourceDeck = validDeck())

        assertThat(req.targetLanguages).hasSize(10)
        LanguageCatalog.supportedCodes().forEach { code ->
            assertThat(req.targetLanguages).contains(code)
        }
    }

    @Test
    fun `should reject target language not in LanguageCatalog`() {
        assertThatThrownBy {
            TranslationRequest(
                sourceDeck = validDeck(),
                targetLanguages = listOf("en", "xx"),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("xx")
    }

    @Test
    fun `should reject empty target languages list`() {
        assertThatThrownBy {
            TranslationRequest(
                sourceDeck = validDeck(),
                targetLanguages = emptyList(),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("targetLanguages")
    }

    @Test
    fun `should reject blank target language code`() {
        assertThatThrownBy {
            TranslationRequest(
                sourceDeck = validDeck(),
                targetLanguages = listOf("en", ""),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `should reject duplicate target languages`() {
        assertThatThrownBy {
            TranslationRequest(
                sourceDeck = validDeck(),
                targetLanguages = listOf("en", "en", "ar"),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("duplicate")
    }

    @Test
    fun `should reject deck with invalid source language`() {
        assertThatThrownBy {
            TranslationRequest(
                sourceDeck = validDeck(languageCode = "zz"),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("sourceDeck")
    }

    @Test
    fun `should accept source language not in target list (translate to all 10`() {
        val req = TranslationRequest(
            sourceDeck = validDeck(languageCode = "fr"),
            targetLanguages = (allCodes - "fr"),
        )

        assertThat(req.targetLanguages).hasSize(9)
        assertThat(req.targetLanguages).doesNotContain("fr")
    }

    @Test
    fun `should accept source language also in target list (no special handling)`() {
        val req = TranslationRequest(
            sourceDeck = validDeck(languageCode = "fr"),
            targetLanguages = allCodes,
        )

        assertThat(req.targetLanguages).contains("fr")
    }
}