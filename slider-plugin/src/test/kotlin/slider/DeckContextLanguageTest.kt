package slider

import contracts.i18n.LanguageCatalog
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class DeckContextLanguageTest {

    private val validAuthor = AuthorContext(name = "Test", email = "test@example.com")

    @Test
    fun `DeckContext default languageCode should be fr`() {
        val ctx = DeckContext(
            subject = "Kotlin coroutines",
            audience = "devs",
            duration = 45,
            outputFile = "kotlin-deck.adoc",
            author = validAuthor,
        )
        assertThat(ctx.languageCode).isEqualTo("fr")
    }

    @Test
    fun `DeckContext languageCode should accept all LanguageCatalog supported codes`() {
        LanguageCatalog.supportedCodes().forEach { code ->
            val ctx = DeckContext(
                subject = "subject",
                audience = "audience",
                duration = 30,
                languageCode = code,
                outputFile = "out.adoc",
                author = validAuthor,
            )
            assertThat(ctx.languageCode).isEqualTo(code)
        }
    }

    @Test
    fun `DeckContext languageCode should reject legacy display name 'French'`() {
        assertThatThrownBy {
            DeckContext(
                subject = "s",
                audience = "a",
                duration = 1,
                languageCode = "French",
                outputFile = "o.adoc",
                author = validAuthor,
            ).requireValidLanguage()
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("French")
    }

    @Test
    fun `DeckContext languageCode should reject unknown ISO code`() {
        assertThatThrownBy {
            DeckContext(
                subject = "s",
                audience = "a",
                duration = 1,
                languageCode = "xx",
                outputFile = "o.adoc",
                author = validAuthor,
            ).requireValidLanguage()
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("xx")
    }

    @Test
    fun `DeckContext requireValidLanguage should pass for fr`() {
        val ctx = DeckContext(
            subject = "s",
            audience = "a",
            duration = 1,
            languageCode = "fr",
            outputFile = "o.adoc",
            author = validAuthor,
        )
        ctx.requireValidLanguage()
    }
}