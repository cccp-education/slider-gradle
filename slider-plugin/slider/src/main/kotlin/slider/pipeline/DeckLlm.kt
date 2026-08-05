package slider.pipeline

/**
 * Port — the LLM provider consumed by [DeckPipelineGraph].
 *
 * Slider's existing LLM calls (ProposeDeckContextTask / GenerateDeckTask)
 * go through `slider.ai.LlmProviderChatModelAdapter` wrapping codebase's
 * `LlmProvider` (koog abstraction, suspend). [DeckPipelineGraph] uses this
 * synchronous port so it stays Gradle-free, coroutine-free, and unit-testable
 * with a plain fake — the adapter mapping `suspend` → blocking lives outside
 * the domain (`slider.pipeline` stays pure).
 *
 * Two operations match the two LLM-calling nodes of the pipeline:
 *  - [propose]  → the `propose-context` node (returns a DeckContext JSON blob).
 *  - [generate] → the `generate-deck` node  (returns an AsciiDoc string).
 */
interface DeckLlm {

    /** Calls the LLM with the propose-context prompt; returns a DeckContext JSON blob. */
    fun propose(prompt: String): String

    /** Calls the LLM with the generate-deck prompt; returns the AsciiDoc deck. */
    fun generate(prompt: String): String
}