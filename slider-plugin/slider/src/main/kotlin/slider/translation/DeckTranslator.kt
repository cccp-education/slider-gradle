package slider.translation

import slider.DeckContext

/**
 * Domain service that orchestrates the translation of a deck into
 * multiple target languages.
 *
 * This is a pure domain object: it depends only on the [LanguageModelAdapter]
 * port (passed in at construction) and on the value objects from the
 * `slider.translation` package. It never touches Gradle, the filesystem,
 * or langchain4j directly.
 *
 * The translator iterates over the [DeckTranslationPlan] tasks, calls the
 * LLM adapter for each, and aggregates the results into a
 * [TranslationOutcome].
 *
 * @param modelAdapter  the LLM port used for actual text translation.
 * @param adocContent   the AsciiDoc source content of the deck to translate.
 */
class DeckTranslator(
    private val modelAdapter: LanguageModelAdapter,
    private val adocContent: String,
) {

    /**
     * Executes all tasks in the given plan and returns the aggregated outcome.
     *
     * For each [TranslationTask]:
     * - Calls [LanguageModelAdapter.translate] with the source deck, adoc
     *   content, and language pair.
     * - If the adapter returns a non-blank translation, wraps it in a
     *   [TranslationResult.Translated] with a copied [DeckContext] whose
     *   `languageCode` and `outputFile` are updated for the target language.
     * - If the adapter returns null or blank, produces a
     *   [TranslationResult.Failed].
     *
     * @param plan  the translation plan to execute.
     * @return the aggregated [TranslationOutcome].
     */
    fun translate(plan: DeckTranslationPlan): TranslationOutcome {
        val results = plan.tasks.map { task ->
            val translatedAdoc = modelAdapter.translate(
                deckContext = task.sourceDeck,
                adocContent = adocContent,
                sourceLanguage = task.from,
                targetLanguage = task.to,
            )

            if (translatedAdoc.isNullOrBlank()) {
                TranslationResult.Failed(
                    targetLanguage = task.to,
                    errorMessage = "LLM returned empty or null response for '${task.to}'",
                )
            } else {
                val translatedDeck = task.sourceDeck.copy(
                    languageCode = task.to,
                    outputFile = buildOutputFile(task.sourceDeck, task.to),
                )
                TranslationResult.Translated(
                    targetLanguage = task.to,
                    translatedDeck = translatedDeck,
                    translatedAdoc = translatedAdoc,
                )
            }
        }

        return TranslationOutcome.of(results)
    }

    /**
     * Builds the output filename for a translated deck by injecting the
     * target language code before the `-deck.adoc` suffix.
     *
     * Example: `kotlin-coroutines-deck.adoc` → `kotlin-coroutines-en-deck.adoc`
     *
     * If the original filename already ends with `_{lang}-deck.adoc`, the
     * language segment is replaced rather than appended.
     */
    private fun buildOutputFile(sourceDeck: DeckContext, targetLanguage: String): String {
        val original = sourceDeck.outputFile
        val deckSuffix = "-deck.adoc"
        return if (original.endsWith(deckSuffix)) {
            val base = original.removeSuffix(deckSuffix)
            val langPattern = Regex("_[a-z]{2}$")
            val cleanBase = if (langPattern.containsMatchIn(base)) {
                langPattern.replace(base, "")
            } else {
                base
            }
            "${cleanBase}_$targetLanguage$deckSuffix"
        } else {
            original.replace(Regex("\\.adoc$"), "_$targetLanguage.adoc")
        }
    }
}