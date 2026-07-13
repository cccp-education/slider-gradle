package slider.translation

import slider.DeckContext

/**
 * Status of a single language translation within a [TranslationOutcome].
 */
enum class TranslationStatus {
    TRANSLATED,
    SKIPPED,
    FAILED,
}

/**
 * Result of translating a deck into a single target language.
 *
 * Sealed type with three variants:
 * - [Translated] — successful translation, holds the translated deck + adoc.
 * - [Skipped]    — intentionally skipped (e.g. identity translation).
 * - [Failed]     — translation failed, holds the error message.
 *
 * Every variant exposes a [targetLanguage] (ISO 639-1) and a [status].
 */
sealed class TranslationResult {

    abstract val targetLanguage: String
    abstract val status: TranslationStatus

    /**
     * Successful translation.
     *
     * @param targetLanguage  ISO 639-1 code of the produced deck.
     * @param translatedDeck  the translated [DeckContext] (metadata).
     * @param translatedAdoc  the translated AsciiDoc source content.
     */
    data class Translated(
        override val targetLanguage: String,
        val translatedDeck: DeckContext,
        val translatedAdoc: String,
    ) : TranslationResult() {
        override val status: TranslationStatus = TranslationStatus.TRANSLATED

        init {
            require(targetLanguage.isNotBlank()) {
                "TranslationResult.Translated.targetLanguage must not be blank"
            }
            require(translatedAdoc.isNotBlank()) {
                "TranslationResult.Translated.translatedAdoc must not be blank"
            }
        }
    }

    /**
     * Intentionally skipped translation.
     *
     * @param targetLanguage  ISO 639-1 code that was skipped.
     * @param reason          human-readable explanation.
     */
    data class Skipped(
        override val targetLanguage: String,
        val reason: String,
    ) : TranslationResult() {
        override val status: TranslationStatus = TranslationStatus.SKIPPED

        init {
            require(targetLanguage.isNotBlank()) {
                "TranslationResult.Skipped.targetLanguage must not be blank"
            }
        }
    }

    /**
     * Failed translation.
     *
     * @param targetLanguage  ISO 639-1 code that failed.
     * @param errorMessage    human-readable error description.
     */
    data class Failed(
        override val targetLanguage: String,
        val errorMessage: String,
    ) : TranslationResult() {
        override val status: TranslationStatus = TranslationStatus.FAILED

        init {
            require(targetLanguage.isNotBlank()) {
                "TranslationResult.Failed.targetLanguage must not be blank"
            }
        }
    }
}