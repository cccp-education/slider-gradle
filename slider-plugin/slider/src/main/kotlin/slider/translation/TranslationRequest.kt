package slider.translation

import contracts.i18n.LanguageCatalog
import slider.DeckContext

/**
 * Immutable request to translate a deck into multiple target languages.
 *
 * This value object is the DDD boundary between the Gradle plugin layer
 * (which knows about `Project`, YAML config, file paths) and the pure
 * domain layer that performs the actual translation.
 *
 * Every field is validated at construction so that downstream code
 * (the `DeckTranslator`) can assume the request is well-formed.
 *
 * @param sourceDeck       the deck context to translate, must have a
 *                         valid `languageCode` (ISO 639-1 in
 *                         [LanguageCatalog.supportedCodes]).
 * @param targetLanguages  ISO 639-1 codes to translate into, each must
 *                         be in [LanguageCatalog.supportedCodes]. Defaults
 *                         to all 10 supported codes. Must not be empty,
 *                         must not contain duplicates.
 */
data class TranslationRequest(
    val sourceDeck: DeckContext,
    val targetLanguages: List<String> = LanguageCatalog.supportedCodes().toList(),
) {
    init {
        require(sourceDeck.languageCode in LanguageCatalog.supportedCodes()) {
            "TranslationRequest.sourceDeck.languageCode must be a valid ISO 639-1 code " +
                "in LanguageCatalog.supportedCodes(), but was '${sourceDeck.languageCode}'"
        }
        require(targetLanguages.isNotEmpty()) {
            "TranslationRequest.targetLanguages must not be empty"
        }
        targetLanguages.forEach { code ->
            require(code.isNotBlank()) {
                "TranslationRequest.targetLanguages must not contain blank codes"
            }
            require(code in LanguageCatalog.supportedCodes()) {
                "TranslationRequest.targetLanguages contains unknown code '$code' — " +
                    "must be one of ${LanguageCatalog.supportedCodes()}"
            }
        }
        require(targetLanguages.toSet().size == targetLanguages.size) {
            "TranslationRequest.targetLanguages must not contain duplicates: $targetLanguages"
        }
    }
}