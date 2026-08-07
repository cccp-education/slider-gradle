package slider.translation

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import slider.AuthorContext
import slider.DeckContext

class TranslationOutcomeTest {

    private val validAuthor = AuthorContext(name = "Test", email = "test@example.com")

    private fun deck(languageCode: String) = DeckContext(
        subject = "Kotlin coroutines",
        audience = "developers",
        duration = 45,
        languageCode = languageCode,
        outputFile = "kotlin-coroutines-${languageCode}-deck.adoc",
        author = validAuthor,
    )

    private fun translated(target: String) = TranslationResult.Translated(
        targetLanguage = target,
        translatedDeck = deck(target),
        translatedAdoc = "= Content in $target\n",
    )

    private fun skipped(target: String) = TranslationResult.Skipped(
        targetLanguage = target,
        reason = "identity",
    )

    private fun failed(target: String) = TranslationResult.Failed(
        targetLanguage = target,
        errorMessage = "LLM error for $target",
    )

    @Test
    fun `outcome should aggregate results from a list`() {
        val results = listOf(
            translated("en"),
            translated("ar"),
            skipped("fr"),
            failed("zh"),
        )
        val outcome = TranslationOutcome.of(results)

        assertThat(outcome.results).hasSize(4)
    }

    @Test
    fun `outcome should count translated results`() {
        val outcome = TranslationOutcome.of(listOf(
            translated("en"), translated("ar"), skipped("fr"), failed("zh"),
        ))

        assertThat(outcome.translatedCount).isEqualTo(2)
    }

    @Test
    fun `outcome should count skipped results`() {
        val outcome = TranslationOutcome.of(listOf(
            translated("en"), skipped("fr"), skipped("es"), failed("zh"),
        ))

        assertThat(outcome.skippedCount).isEqualTo(2)
    }

    @Test
    fun `outcome should count failed results`() {
        val outcome = TranslationOutcome.of(listOf(
            translated("en"), failed("zh"), failed("hi"), failed("bn"),
        ))

        assertThat(outcome.failedCount).isEqualTo(3)
    }

    @Test
    fun `outcome total should equal sum of counts`() {
        val outcome = TranslationOutcome.of(listOf(
            translated("en"), translated("ar"), skipped("fr"), failed("zh"),
        ))

        assertThat(outcome.totalCount).isEqualTo(4)
        assertThat(outcome.translatedCount + outcome.skippedCount + outcome.failedCount)
            .isEqualTo(outcome.totalCount)
    }

    @Test
    fun `outcome should filter only translated results`() {
        val outcome = TranslationOutcome.of(listOf(
            translated("en"), skipped("fr"), failed("zh"),
        ))

        val translatedOnly = outcome.translatedResults()
        assertThat(translatedOnly).hasSize(1)
        assertThat(translatedOnly.first().targetLanguage).isEqualTo("en")
    }

    @Test
    fun `outcome should expose target languages of translated results`() {
        val outcome = TranslationOutcome.of(listOf(
            translated("en"), translated("ar"), skipped("fr"), failed("zh"),
        ))

        assertThat(outcome.translatedLanguages()).containsExactlyInAnyOrder("en", "ar")
    }

    @Test
    fun `outcome should report all translated when no failures`() {
        val outcome = TranslationOutcome.of(listOf(
            translated("en"), translated("ar"), translated("zh"),
        ))

        assertThat(outcome.isAllTranslated()).isTrue()
    }

    @Test
    fun `outcome should report not all translated when any failure`() {
        val outcome = TranslationOutcome.of(listOf(
            translated("en"), failed("ar"),
        ))

        assertThat(outcome.isAllTranslated()).isFalse()
    }

    @Test
    fun `outcome should accept empty results list`() {
        val outcome = TranslationOutcome.of(emptyList())

        assertThat(outcome.results).isEmpty()
        assertThat(outcome.totalCount).isZero()
        assertThat(outcome.translatedCount).isZero()
        assertThat(outcome.skippedCount).isZero()
        assertThat(outcome.failedCount).isZero()
        assertThat(outcome.isAllTranslated()).isTrue()
    }

    @Test
    fun `outcome should reject duplicate target languages in results`() {
        assertThatThrownBy {
            TranslationOutcome.of(listOf(
                translated("en"),
                translated("en"),
            ))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("duplicate")
    }

    @Test
    fun `outcome summary should contain counts`() {
        val outcome = TranslationOutcome.of(listOf(
            translated("en"), translated("ar"), skipped("fr"), failed("zh"),
        ))
        val summary = outcome.summary()

        assertThat(summary).contains("2")
        assertThat(summary).contains("translated")
        assertThat(summary).contains("skipped")
        assertThat(summary).contains("failed")
    }

    @Test
    fun `outcome should expose failed results with error messages`() {
        val outcome = TranslationOutcome.of(listOf(
            translated("en"), failed("zh"), failed("hi"),
        ))
        val failures = outcome.failures()

        assertThat(failures).hasSize(2)
        assertThat(failures.map { it.targetLanguage }).containsExactlyInAnyOrder("zh", "hi")
        assertThat(failures.map { it.errorMessage }).contains("LLM error for zh", "LLM error for hi")
    }
}