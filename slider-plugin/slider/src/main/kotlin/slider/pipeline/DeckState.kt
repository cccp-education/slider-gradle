package slider.pipeline

/**
 * Immutable state of the deck generation pipeline, flowing through the nodes
 * of [DeckPipelineGraph]. Each node produces a new [DeckState] via [copy],
 * following the koog state pattern (data class + immutable progression).
 *
 * Pure value object — no Gradle, no LLM, no koog, no I/O. The 11 fields model
 * the full lifecycle from initial inputs to the generated deck (or failure).
 *
 * Invariants — cross-field consistency validated in [init]:
 * - [contextValid] `true` requires [validationError] `null` and stage >= CONTEXT_VALIDATED.
 * - [stage] CONTEXT_PROPOSED requires a non-blank [deckContextJson].
 * - [deckAdoc] non-blank requires stage >= DECK_GENERATED.
 * - [error] non-null requires stage FAILED.
 *
 * @param subject         the deck subject (non-blank).
 * @param language        ISO 639-1 target language code (non-blank).
 * @param authorName      author display name (non-blank).
 * @param authorEmail     author email (non-blank).
 * @param ragContext      RAG context string — may be blank when RAG is disabled.
 * @param deckContextJson DeckContext JSON proposed by the LLM; blank until
 *                        the `propose-context` node runs.
 * @param contextValid    `true` only after [DeckContextValidator] passes.
 * @param validationError error message from [DeckContextValidator], or null.
 * @param deckAdoc        generated AsciiDoc deck; blank until `generate-deck` runs.
 * @param error           pipeline error message; null unless [stage] is FAILED.
 * @param stage           current [DeckStage] — defaults to [DeckStage.INITIALIZED].
 */
data class DeckState(
    val subject: String,
    val language: String,
    val authorName: String,
    val authorEmail: String,
    val ragContext: String,
    val deckContextJson: String,
    val contextValid: Boolean = false,
    val validationError: String? = null,
    val deckAdoc: String = "",
    val error: String? = null,
    val stage: DeckStage = DeckStage.INITIALIZED,
) {
    init {
        require(subject.isNotBlank()) { "DeckState.subject must not be blank" }
        require(language.isNotBlank()) { "DeckState.language must not be blank" }
        require(authorName.isNotBlank()) { "DeckState.authorName must not be blank" }
        require(authorEmail.isNotBlank()) { "DeckState.authorEmail must not be blank" }

        // contextValid true requires no validationError.
        require(!(contextValid && validationError != null)) {
            "DeckState.validationError must be null when contextValid is true"
        }

        // stage INITIALIZED cannot have contextValid true.
        require(!(contextValid && stage == DeckStage.INITIALIZED)) {
            "DeckState.contextValid cannot be true when stage is INITIALIZED"
        }

        // stage CONTEXT_PROPOSED requires a non-blank deckContextJson.
        require(!(stage == DeckStage.CONTEXT_PROPOSED && deckContextJson.isBlank())) {
            "DeckState.deckContextJson must not be blank when stage is CONTEXT_PROPOSED"
        }

        // deckAdoc non-blank requires stage DECK_GENERATED or FAILED.
        require(!(deckAdoc.isNotBlank() && stage != DeckStage.DECK_GENERATED && stage != DeckStage.FAILED)) {
            "DeckState.deckAdoc must be blank when stage is $stage"
        }

        // error non-null requires stage FAILED.
        require(!(error != null && stage != DeckStage.FAILED)) {
            "DeckState.error must be null when stage is $stage"
        }
    }
}