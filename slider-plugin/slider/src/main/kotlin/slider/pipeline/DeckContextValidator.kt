package slider.pipeline

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import contracts.i18n.LanguageCatalog

/**
 * Pure domain service that validates a DeckContext JSON blob proposed by the
 * LLM `propose-context` node — before the `generate-deck` node runs.
 *
 * Checks (in order):
 *  1. Input is non-blank.
 *  2. Input is valid JSON.
 *  3. `subject` is present and non-blank.
 *  4. `audience` is present and non-blank.
 *  5. `duration` is present and a positive integer.
 *  6. `languageCode` is present and belongs to [LanguageCatalog.supportedCodes].
 *  7. `outputFile` is present and non-blank.
 *  8. `author` object is present with non-blank `name` and `email`.
 *
 * Returns a [ValidationResult] — [ValidationResult.Valid] when all checks pass,
 * [ValidationResult.Invalid] with a human-readable error otherwise.
 *
 * Pure — no Gradle, no LLM, no I/O. Uses Jackson (JSON parser) and the shared
 * N0 contract [LanguageCatalog]. Pattern aligned on
 * [slider.content.ContentSlideAssertion] (object pur, returns sealed result).
 */
object DeckContextValidator {

    private val jsonMapper: ObjectMapper = ObjectMapper()

    /**
     * Validates the given [json] DeckContext blob.
     *
     * @param json the raw DeckContext JSON string proposed by the LLM.
     * @return [ValidationResult.Valid] when the JSON is well-formed and complete,
     *         [ValidationResult.Invalid] with an explanation otherwise.
     */
    fun validate(json: String): ValidationResult {
        if (json.isBlank()) {
            return ValidationResult.Invalid("DeckContext JSON is blank")
        }

        val root: JsonNode = try {
            jsonMapper.readTree(json)
        } catch (e: Exception) {
            return ValidationResult.Invalid("DeckContext JSON is malformed: ${e.message}")
        }

        if (root.isMissingNode || !root.isObject) {
            return ValidationResult.Invalid("DeckContext JSON must be a JSON object")
        }

        root.textField("subject")?.let { return it }
        root.textField("audience")?.let { return it }

        val duration = root.get("duration")
        if (duration == null || !duration.isNumber || duration.asInt() <= 0) {
            return ValidationResult.Invalid("DeckContext.duration must be a positive integer")
        }

        val languageCode = root.get("languageCode")
        if (languageCode == null || languageCode.asText().isBlank()) {
            return ValidationResult.Invalid("DeckContext.languageCode is missing or blank")
        }
        if (languageCode.asText() !in LanguageCatalog.supportedCodes()) {
            return ValidationResult.Invalid(
                "DeckContext.languageCode '${languageCode.asText()}' is not a supported language code",
            )
        }

        root.textField("outputFile")?.let { return it }

        val author = root.get("author")
        if (author == null || !author.isObject) {
            return ValidationResult.Invalid("DeckContext.author is missing or not an object")
        }
        author.textField("name")?.let { return it }
        author.textField("email")?.let { return it }

        return ValidationResult.Valid(json)
    }

    /**
     * Returns an [ValidationResult.Invalid] when the field at [name] is missing,
     * null, or blank — otherwise null.
     */
    private fun JsonNode.textField(name: String): ValidationResult.Invalid? {
        val node = this.get(name)
        if (node == null || node.isNull) {
            return ValidationResult.Invalid("DeckContext.$name is missing")
        }
        if (node.asText().isBlank()) {
            return ValidationResult.Invalid("DeckContext.$name is blank")
        }
        return null
    }
}