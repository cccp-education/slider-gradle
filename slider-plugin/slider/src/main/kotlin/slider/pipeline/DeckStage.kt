package slider.pipeline

/**
 * Stages of the deck generation pipeline — sequential progression from
 * initialization to the final generated deck (or failure).
 *
 * Pure enum — no Gradle, no LLM, no I/O. Drives the conditional edges of
 * [DeckPipelineGraph] and the invariants of [DeckState].
 */
enum class DeckStage {
    /** Fresh state — subject, language, author, ragContext set; no LLM call yet. */
    INITIALIZED,

    /** LLM has proposed a DeckContext JSON via the `propose-context` node. */
    CONTEXT_PROPOSED,

    /** The proposed DeckContext JSON has passed [DeckContextValidator]. */
    CONTEXT_VALIDATED,

    /** LLM has generated the final AsciiDoc deck via the `generate-deck` node. */
    DECK_GENERATED,

    /** Pipeline failed at any node — see [DeckState.error] for the reason. */
    FAILED,
}