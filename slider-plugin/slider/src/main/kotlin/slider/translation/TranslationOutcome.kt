package slider.translation

/**
 * Aggregated outcome of a deck translation pipeline run.
 *
 * Wraps the list of [TranslationResult]s (one per target language) and
 * exposes convenience queries for counting, filtering, and summarising.
 *
 * @param results  one result per target language, must not be empty,
 *                 must not contain duplicate target languages.
 */
data class TranslationOutcome(
    val results: List<TranslationResult>,
) {
    init {
        require(results.isNotEmpty()) {
            "TranslationOutcome.results must not be empty"
        }
        val targetLanguages = results.map { it.targetLanguage }
        require(targetLanguages.toSet().size == targetLanguages.size) {
            "TranslationOutcome.results must not contain duplicate target languages: $targetLanguages"
        }
    }

    companion object {

        /**
         * Factory that validates and wraps a list of results.
         */
        fun of(results: List<TranslationResult>): TranslationOutcome = TranslationOutcome(results)
    }

    val totalCount: Int get() = results.size

    val translatedCount: Int
        get() = results.count { it.status == TranslationStatus.TRANSLATED }

    val skippedCount: Int
        get() = results.count { it.status == TranslationStatus.SKIPPED }

    val failedCount: Int
        get() = results.count { it.status == TranslationStatus.FAILED }

    /**
     * Returns only the [TranslationResult.Translated] results.
     */
    fun translatedResults(): List<TranslationResult.Translated> =
        results.filterIsInstance<TranslationResult.Translated>()

    /**
     * Returns only the [TranslationResult.Failed] results.
     */
    fun failures(): List<TranslationResult.Failed> =
        results.filterIsInstance<TranslationResult.Failed>()

    /**
     * Returns the target languages of all successfully translated decks.
     */
    fun translatedLanguages(): List<String> =
        translatedResults().map { it.targetLanguage }

    /**
     * True when every result is [TranslationStatus.TRANSLATED].
     */
    fun isAllTranslated(): Boolean =
        results.all { it.status == TranslationStatus.TRANSLATED }

    /**
     * Human-readable summary line with counts.
     */
    fun summary(): String =
        "$translatedCount translated, $skippedCount skipped, $failedCount failed (total $totalCount)"
}