package slider.pipeline

/**
 * Port — builds the LLM prompts for the deck pipeline nodes.
 *
 * Implementations (e.g. `slider.ai.AssistantManager.PromptManager`) assemble
 * the system + user messages from the running [DeckState]. Pure — no LLM,
 * no I/O. Kept as a port so [DeckPipelineGraph] can be unit-tested with a
 * deterministic stub, without depending on `slider.ai` or Gradle.
 */
interface DeckPromptBuilder {

    /**
     * Prompt for the `propose-context` node — asks the LLM to propose a
     * DeckContext JSON blob from the subject, language, and RAG context.
     */
    fun buildProposePrompt(state: DeckState): String

    /**
     * Prompt for the `generate-deck` node — asks the LLM to generate the
     * AsciiDoc deck from the validated DeckContext JSON.
     */
    fun buildGeneratePrompt(state: DeckState): String
}