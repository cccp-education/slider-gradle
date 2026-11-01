package slider.pipeline

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.right
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
 * Functional shape (XB-FP-2, arrow-kt): the checks are **independent pure
 * functions** returning a list of errors (empty = ok); they are concatenated —
 * no mutable accumulator — and wrapped in `Either<NonEmptyList<String>, String>`
 * so that **every** problem is reported at once (the former implementation
 * short-circuited on the first one). [validate] keeps the historical
 * [ValidationResult] boundary: the sealed type stays the domain vocabulary and
 * Arrow is an internal composition tool (D3/D4).
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
     *         [ValidationResult.Invalid] with **all** the violations joined by
     *         `"; "` otherwise.
     */
    fun validate(json: String): ValidationResult =
        validateEither(json).fold(
            { errors -> ValidationResult.Invalid(errors.joinToString("; ")) },
            { validJson -> ValidationResult.Valid(validJson) },
        )

    /**
     * Arrow-shaped validation: `Left` carries the non-empty list of violations,
     * `Right` the validated JSON. Exposed to the module (internal) so that the
     * composition can be exercised without the sealed boundary.
     */
    internal fun validateEither(json: String): Either<NonEmptyList<String>, String> {
        if (json.isBlank()) {
            return NonEmptyList("DeckContext JSON is blank", emptyList()).left()
        }

        val root: JsonNode =
            when (val parsed = Either.catch { jsonMapper.readTree(json) }) {
                is Either.Left -> {
                    return NonEmptyList("DeckContext JSON is malformed: ${parsed.value.message}", emptyList()).left()
                }
                is Either.Right -> parsed.value
            }

        if (root.isMissingNode || !root.isObject) {
            return NonEmptyList("DeckContext JSON must be a JSON object", emptyList()).left()
        }

        // Independent checks concatenated — no short-circuit, no mutable list.
        val errors: List<String> =
            root.requiredText("subject") +
                root.requiredText("audience") +
                root.durationErrors() +
                root.languageErrors() +
                root.requiredText("outputFile") +
                root.authorErrors()

        return when {
            errors.isEmpty() -> json.right()
            else -> NonEmptyList(errors.first(), errors.drop(1)).left()
        }
    }

    /** Missing / null / blank violation for a required text field. */
    private fun JsonNode.requiredText(name: String): List<String> =
        when {
            !has(name) || get(name).isNull -> listOf("DeckContext.$name is missing")
            get(name).asText().isBlank() -> listOf("DeckContext.$name is blank")
            else -> emptyList()
        }

    /** `duration` must be a present positive number. */
    private fun JsonNode.durationErrors(): List<String> {
        val duration = get("duration")
        return if (duration == null || !duration.isNumber || duration.asInt() <= 0) {
            listOf("DeckContext.duration must be a positive integer")
        } else {
            emptyList()
        }
    }

    /** `languageCode` must be present and belong to the shared N0 catalog. */
    private fun JsonNode.languageErrors(): List<String> {
        val languageCode = get("languageCode")
        return when {
            languageCode == null || languageCode.asText().isBlank() ->
                listOf("DeckContext.languageCode is missing or blank")

            languageCode.asText() !in LanguageCatalog.supportedCodes() ->
                listOf(
                    "DeckContext.languageCode '${languageCode.asText()}' is not a supported language code",
                )

            else -> emptyList()
        }
    }

    /** `author` must be an object with non-blank `name` and `email`. */
    private fun JsonNode.authorErrors(): List<String> {
        val author = get("author")
        if (author == null || !author.isObject) {
            return listOf("DeckContext.author is missing or not an object")
        }
        return author.requiredText("name") + author.requiredText("email")
    }
}
