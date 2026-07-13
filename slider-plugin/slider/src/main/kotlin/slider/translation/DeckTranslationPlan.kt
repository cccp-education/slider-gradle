package slider.translation

import slider.DeckContext

/**
 * A single translation task within a [DeckTranslationPlan].
 *
 * Pairs the source deck with a target language code. The [DeckTranslator]
 * consumes one task at a time and produces a [TranslationResult].
 *
 * @param from       source language ISO 639-1 code.
 * @param to         target language ISO 639-1 code.
 * @param sourceDeck the deck context to translate from.
 */
data class TranslationTask(
    val from: String,
    val to: String,
    val sourceDeck: DeckContext,
)

/**
 * Immutable plan describing how a [TranslationRequest] will be executed.
 *
 * The plan expands the request into a list of [TranslationTask]s, one per
 * target language. Identity tasks (where `from == to`) are excluded —
 * translating a deck into its own language is a no-op.
 *
 * @param sourceLanguage   the source deck's ISO 639-1 code.
 * @param targetLanguages  the target languages to translate into.
 * @param tasks            the individual translation tasks to execute.
 */
data class DeckTranslationPlan(
    val sourceLanguage: String,
    val targetLanguages: List<String>,
    val tasks: List<TranslationTask>,
) {

    companion object {

        /**
         * Builds a plan from a [TranslationRequest].
         *
         * Identity tasks (source → source) are filtered out so that if the
         * source language happens to be in the target list, it is skipped
         * rather than producing a no-op translation.
         */
        fun from(request: TranslationRequest): DeckTranslationPlan {
            val from = request.sourceDeck.languageCode
            val tasks = request.targetLanguages
                .filter { it != from }
                .map { to -> TranslationTask(from = from, to = to, sourceDeck = request.sourceDeck) }
            return DeckTranslationPlan(
                sourceLanguage = from,
                targetLanguages = request.targetLanguages,
                tasks = tasks,
            )
        }
    }
}