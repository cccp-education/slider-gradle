package slider.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class DeckStateTest {

    @Test
    fun `creates a DeckState with all fields populated`() {
        val state = DeckState(
            subject = "Kotlin Coroutines",
            language = "fr",
            authorName = "Jane Doe",
            authorEmail = "jane@example.com",
            ragContext = "Async programming on the JVM.",
            deckContextJson = """{"subject":"Kotlin Coroutines"}""",
            contextValid = true,
            validationError = null,
            deckAdoc = "== Title",
            error = null,
            stage = DeckStage.DECK_GENERATED,
        )

        assertThat(state.subject).isEqualTo("Kotlin Coroutines")
        assertThat(state.language).isEqualTo("fr")
        assertThat(state.authorName).isEqualTo("Jane Doe")
        assertThat(state.authorEmail).isEqualTo("jane@example.com")
        assertThat(state.ragContext).isEqualTo("Async programming on the JVM.")
        assertThat(state.deckContextJson).isEqualTo("""{"subject":"Kotlin Coroutines"}""")
        assertThat(state.contextValid).isTrue()
        assertThat(state.validationError).isNull()
        assertThat(state.deckAdoc).isEqualTo("== Title")
        assertThat(state.error).isNull()
        assertThat(state.stage).isEqualTo(DeckStage.DECK_GENERATED)
    }

    @Test
    fun `creates a DeckState with defaults for a fresh pipeline run`() {
        val state = DeckState(
            subject = "Kotlin Coroutines",
            language = "fr",
            authorName = "Jane Doe",
            authorEmail = "jane@example.com",
            ragContext = "Async programming on the JVM.",
            deckContextJson = "",
        )

        assertThat(state.contextValid).isFalse()
        assertThat(state.validationError).isNull()
        assertThat(state.deckAdoc).isEmpty()
        assertThat(state.error).isNull()
        assertThat(state.stage).isEqualTo(DeckStage.INITIALIZED)
    }

    @Test
    fun `rejects a blank subject`() {
        assertThatThrownBy {
            DeckState(
                subject = "   ",
                language = "fr",
                authorName = "Jane Doe",
                authorEmail = "jane@example.com",
                ragContext = "ctx",
                deckContextJson = "",
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("subject")
    }

    @Test
    fun `rejects an empty subject`() {
        assertThatThrownBy {
            DeckState(
                subject = "",
                language = "fr",
                authorName = "Jane Doe",
                authorEmail = "jane@example.com",
                ragContext = "ctx",
                deckContextJson = "",
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `rejects a blank language`() {
        assertThatThrownBy {
            DeckState(
                subject = "Subject",
                language = "",
                authorName = "Jane Doe",
                authorEmail = "jane@example.com",
                ragContext = "ctx",
                deckContextJson = "",
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("language")
    }

    @Test
    fun `rejects a blank author name`() {
        assertThatThrownBy {
            DeckState(
                subject = "Subject",
                language = "fr",
                authorName = "  ",
                authorEmail = "jane@example.com",
                ragContext = "ctx",
                deckContextJson = "",
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("authorName")
    }

    @Test
    fun `rejects a blank author email`() {
        assertThatThrownBy {
            DeckState(
                subject = "Subject",
                language = "fr",
                authorName = "Jane Doe",
                authorEmail = "  ",
                ragContext = "ctx",
                deckContextJson = "",
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("authorEmail")
    }

    @Test
    fun `allows a blank ragContext — RAG is optional`() {
        val state = DeckState(
            subject = "Subject",
            language = "fr",
            authorName = "Jane Doe",
            authorEmail = "jane@example.com",
            ragContext = "",
            deckContextJson = "",
        )

        assertThat(state.ragContext).isEmpty()
    }

    @Test
    fun `rejects a non-null validationError when contextValid is true`() {
        assertThatThrownBy {
            DeckState(
                subject = "Subject",
                language = "fr",
                authorName = "Jane Doe",
                authorEmail = "jane@example.com",
                ragContext = "ctx",
                deckContextJson = """{"subject":"x"}""",
                contextValid = true,
                validationError = "Should not be set when valid",
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("validationError")
    }

    @Test
    fun `rejects a non-empty deckAdoc when stage is INITIALIZED`() {
        assertThatThrownBy {
            DeckState(
                subject = "Subject",
                language = "fr",
                authorName = "Jane Doe",
                authorEmail = "jane@example.com",
                ragContext = "ctx",
                deckContextJson = "",
                deckAdoc = "== Generated",
                stage = DeckStage.INITIALIZED,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("deckAdoc")
    }

    @Test
    fun `rejects a non-null error when stage is not FAILED`() {
        assertThatThrownBy {
            DeckState(
                subject = "Subject",
                language = "fr",
                authorName = "Jane Doe",
                authorEmail = "jane@example.com",
                ragContext = "ctx",
                deckContextJson = "",
                error = "Boom",
                stage = DeckStage.INITIALIZED,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("error")
    }

    @Test
    fun `rejects a blank deckContextJson when stage is CONTEXT_PROPOSED`() {
        assertThatThrownBy {
            DeckState(
                subject = "Subject",
                language = "fr",
                authorName = "Jane Doe",
                authorEmail = "jane@example.com",
                ragContext = "ctx",
                deckContextJson = "",
                stage = DeckStage.CONTEXT_PROPOSED,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("deckContextJson")
    }

    @Test
    fun `rejects contextValid true when stage is INITIALIZED`() {
        assertThatThrownBy {
            DeckState(
                subject = "Subject",
                language = "fr",
                authorName = "Jane Doe",
                authorEmail = "jane@example.com",
                ragContext = "ctx",
                deckContextJson = "",
                contextValid = true,
                stage = DeckStage.INITIALIZED,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("contextValid")
    }

    @Test
    fun `copy returns a new instance with the modified fields`() {
        val initial = DeckState(
            subject = "Subject",
            language = "fr",
            authorName = "Jane Doe",
            authorEmail = "jane@example.com",
            ragContext = "ctx",
            deckContextJson = "",
        )

        val proposed = initial.copy(
            deckContextJson = """{"subject":"Subject"}""",
            stage = DeckStage.CONTEXT_PROPOSED,
        )

        assertThat(proposed).isNotSameAs(initial)
        assertThat(proposed.subject).isEqualTo(initial.subject)
        assertThat(proposed.deckContextJson).isEqualTo("""{"subject":"Subject"}""")
        assertThat(proposed.stage).isEqualTo(DeckStage.CONTEXT_PROPOSED)
        assertThat(initial.stage).isEqualTo(DeckStage.INITIALIZED)
    }

    @Test
    fun `isImmutable — all fields are val via data class copy`() {
        val state = DeckState(
            subject = "S",
            language = "fr",
            authorName = "Jane",
            authorEmail = "jane@example.com",
            ragContext = "ctx",
            deckContextJson = "",
        )

        val newState = state.copy(contextValid = true, stage = DeckStage.CONTEXT_VALIDATED)

        assertThat(state.contextValid).isFalse()
        assertThat(newState.contextValid).isTrue()
    }
}