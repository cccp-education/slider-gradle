package slider.pipeline

/**
 * Outcome of validating a DeckContext JSON blob proposed by the LLM.
 * Sealed hierarchy: either [Valid] (the JSON is well-formed and complete)
 * or [Invalid] with a human-readable error message.
 *
 * Pure value object — no Gradle, no LLM, no I/O. Pattern aligned on
 * [slider.content.ContentAssertionResult] (sealed for exhaustive `when` matching).
 */
sealed interface ValidationResult {

    /**
     * The JSON is a well-formed DeckContext with all required fields.
     *
     * @param json the raw JSON string that was validated.
     */
    data class Valid(val json: String) : ValidationResult {
        init {
            require(json.isNotBlank()) { "ValidationResult.Valid.json must not be blank" }
        }
    }

    /**
     * The JSON is missing, malformed, or incomplete.
     *
     * @param error human-readable explanation of what failed.
     */
    data class Invalid(val error: String) : ValidationResult {
        init {
            require(error.isNotBlank()) { "ValidationResult.Invalid.error must not be blank" }
        }
    }
}