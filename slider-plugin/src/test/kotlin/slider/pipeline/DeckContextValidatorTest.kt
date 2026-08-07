package slider.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class DeckContextValidatorTest {

    @Test
    fun `validates a well-formed DeckContext JSON with all required fields`() {
        val json = """
            {
              "subject": "Kotlin Coroutines",
              "audience": "developers",
              "duration": 60,
              "languageCode": "fr",
              "outputFile": "kotlin-coroutines.adoc",
              "author": { "name": "Jane Doe", "email": "jane@example.com" }
            }
        """.trimIndent()

        val result = DeckContextValidator.validate(json)

        assertThat(result).isInstanceOf(ValidationResult.Valid::class.java)
    }

    @Test
    fun `rejects a blank JSON input`() {
        val result = DeckContextValidator.validate("   ")

        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val invalid = result as ValidationResult.Invalid
        assertThat(invalid.error).contains("blank")
    }

    @Test
    fun `rejects an empty JSON input`() {
        val result = DeckContextValidator.validate("")

        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
    }

    @Test
    fun `rejects malformed JSON`() {
        val result = DeckContextValidator.validate("""{"subject": "missing closing brace"""")

        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val invalid = result as ValidationResult.Invalid
        assertThat(invalid.error).contains("malformed")
    }

    @Test
    fun `rejects JSON missing the subject field`() {
        val json = """
            {
              "audience": "developers",
              "duration": 60,
              "languageCode": "fr",
              "outputFile": "deck.adoc",
              "author": { "name": "Jane Doe", "email": "jane@example.com" }
            }
        """.trimIndent()

        val result = DeckContextValidator.validate(json)

        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val invalid = result as ValidationResult.Invalid
        assertThat(invalid.error).contains("subject")
    }

    @Test
    fun `rejects JSON with a blank subject value`() {
        val json = """
            {
              "subject": "   ",
              "audience": "developers",
              "duration": 60,
              "languageCode": "fr",
              "outputFile": "deck.adoc",
              "author": { "name": "Jane Doe", "email": "jane@example.com" }
            }
        """.trimIndent()

        val result = DeckContextValidator.validate(json)

        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val invalid = result as ValidationResult.Invalid
        assertThat(invalid.error).contains("subject")
    }

    @Test
    fun `rejects JSON missing the audience field`() {
        val json = """
            {
              "subject": "Kotlin",
              "duration": 60,
              "languageCode": "fr",
              "outputFile": "deck.adoc",
              "author": { "name": "Jane Doe", "email": "jane@example.com" }
            }
        """.trimIndent()

        val result = DeckContextValidator.validate(json)

        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val invalid = result as ValidationResult.Invalid
        assertThat(invalid.error).contains("audience")
    }

    @Test
    fun `rejects JSON missing the duration field`() {
        val json = """
            {
              "subject": "Kotlin",
              "audience": "devs",
              "languageCode": "fr",
              "outputFile": "deck.adoc",
              "author": { "name": "Jane Doe", "email": "jane@example.com" }
            }
        """.trimIndent()

        val result = DeckContextValidator.validate(json)

        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val invalid = result as ValidationResult.Invalid
        assertThat(invalid.error).contains("duration")
    }

    @Test
    fun `rejects JSON with a non-positive duration`() {
        val json = """
            {
              "subject": "Kotlin",
              "audience": "devs",
              "duration": 0,
              "languageCode": "fr",
              "outputFile": "deck.adoc",
              "author": { "name": "Jane Doe", "email": "jane@example.com" }
            }
        """.trimIndent()

        val result = DeckContextValidator.validate(json)

        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val invalid = result as ValidationResult.Invalid
        assertThat(invalid.error).contains("duration")
    }

    @Test
    fun `rejects JSON with an invalid languageCode`() {
        val json = """
            {
              "subject": "Kotlin",
              "audience": "devs",
              "duration": 60,
              "languageCode": "xx",
              "outputFile": "deck.adoc",
              "author": { "name": "Jane Doe", "email": "jane@example.com" }
            }
        """.trimIndent()

        val result = DeckContextValidator.validate(json)

        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val invalid = result as ValidationResult.Invalid
        assertThat(invalid.error).contains("languageCode")
    }

    @Test
    fun `rejects JSON missing the outputFile field`() {
        val json = """
            {
              "subject": "Kotlin",
              "audience": "devs",
              "duration": 60,
              "languageCode": "fr",
              "author": { "name": "Jane Doe", "email": "jane@example.com" }
            }
        """.trimIndent()

        val result = DeckContextValidator.validate(json)

        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val invalid = result as ValidationResult.Invalid
        assertThat(invalid.error).contains("outputFile")
    }

    @Test
    fun `rejects JSON with a blank outputFile`() {
        val json = """
            {
              "subject": "Kotlin",
              "audience": "devs",
              "duration": 60,
              "languageCode": "fr",
              "outputFile": "  ",
              "author": { "name": "Jane Doe", "email": "jane@example.com" }
            }
        """.trimIndent()

        val result = DeckContextValidator.validate(json)

        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val invalid = result as ValidationResult.Invalid
        assertThat(invalid.error).contains("outputFile")
    }

    @Test
    fun `rejects JSON missing the author object`() {
        val json = """
            {
              "subject": "Kotlin",
              "audience": "devs",
              "duration": 60,
              "languageCode": "fr",
              "outputFile": "deck.adoc"
            }
        """.trimIndent()

        val result = DeckContextValidator.validate(json)

        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val invalid = result as ValidationResult.Invalid
        assertThat(invalid.error).contains("author")
    }

    @Test
    fun `rejects JSON with an author missing the name`() {
        val json = """
            {
              "subject": "Kotlin",
              "audience": "devs",
              "duration": 60,
              "languageCode": "fr",
              "outputFile": "deck.adoc",
              "author": { "email": "jane@example.com" }
            }
        """.trimIndent()

        val result = DeckContextValidator.validate(json)

        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val invalid = result as ValidationResult.Invalid
        assertThat(invalid.error).contains("name")
    }

    @Test
    fun `rejects JSON with an author missing the email`() {
        val json = """
            {
              "subject": "Kotlin",
              "audience": "devs",
              "duration": 60,
              "languageCode": "fr",
              "outputFile": "deck.adoc",
              "author": { "name": "Jane Doe" }
            }
        """.trimIndent()

        val result = DeckContextValidator.validate(json)

        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val invalid = result as ValidationResult.Invalid
        assertThat(invalid.error).contains("email")
    }

    @Test
    fun `ValidationResult Valid exposes the raw json`() {
        val json = """
            {
              "subject": "Kotlin",
              "audience": "devs",
              "duration": 60,
              "languageCode": "fr",
              "outputFile": "deck.adoc",
              "author": { "name": "Jane Doe", "email": "jane@example.com" }
            }
        """.trimIndent()

        val result = DeckContextValidator.validate(json)

        assertThat(result).isInstanceOf(ValidationResult.Valid::class.java)
        val valid = result as ValidationResult.Valid
        assertThat(valid.json).isEqualTo(json)
    }

    @Test
    fun `ValidationResult Invalid requires a non-blank error`() {
        assertThatThrownBy {
            ValidationResult.Invalid(error = "  ")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }
}