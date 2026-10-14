package slider.pipeline

import codebase.koog.pipeline.PipelineDefinition

/**
 * Domain port implementation driving the shared N1 pipeline skeleton
 * `codebase.koog.pipeline.PipelineGraph` for the deck generation pipeline
 * (EPIC KOOG-PIPELINE-DEDUP, BS-2 adoption).
 *
 * Only the slider-specific knowledge lives here — the LLM calls, the prompt
 * builder, the [DeckContextValidator] delegation and the failure labels. The
 * orchestration (koog topology, sequential fail-fast `execute`, mermaid
 * diagram) is the shared `PipelineGraph`.
 *
 * Nodes:
 *  1. [propose] — calls [DeckLlm.propose] with the prompt built from the
 *     [DeckState]; stores the returned JSON and advances to
 *     [DeckStage.CONTEXT_PROPOSED].
 *  2. [validate] — pure — delegates to [DeckContextValidator]. Valid advances
 *     to [DeckStage.CONTEXT_VALIDATED] with `contextValid = true`; Invalid
 *     advances to [DeckStage.FAILED] with the validator error.
 *  3. [generate] — calls [DeckLlm.generate] with the validated state, stores
 *     the AsciiDoc and advances to [DeckStage.DECK_GENERATED].
 *
 * @param promptBuilder builds the LLM prompts from the running [DeckState].
 * @param llm           the LLM provider (production: adapter on codebase's
 *                      `LlmBuildService`; tests: a fake).
 */
class DeckPipelineDefinition(
    private val promptBuilder: DeckPromptBuilder,
    private val llm: DeckLlm,
) : PipelineDefinition<DeckState> {

    override val name: String = NAME

    override fun propose(state: DeckState): DeckState {
        val prompt = promptBuilder.buildProposePrompt(state)
        val deckContextJson = llm.propose(prompt)
        require(deckContextJson.isNotBlank()) { "LLM returned a blank DeckContext JSON" }
        return state.copy(
            deckContextJson = deckContextJson,
            stage = DeckStage.CONTEXT_PROPOSED,
        )
    }

    override fun validate(state: DeckState): DeckState =
        when (val result = DeckContextValidator.validate(state.deckContextJson)) {
            is ValidationResult.Valid -> state.copy(
                contextValid = true,
                stage = DeckStage.CONTEXT_VALIDATED,
            )
            is ValidationResult.Invalid -> state.copy(
                contextValid = false,
                validationError = result.error,
                error = result.error,
                stage = DeckStage.FAILED,
            )
        }

    override fun generate(state: DeckState): DeckState {
        val prompt = promptBuilder.buildGeneratePrompt(state)
        val deckAdoc = llm.generate(prompt)
        require(deckAdoc.isNotBlank()) { "LLM returned a blank AsciiDoc deck" }
        return state.copy(
            deckAdoc = deckAdoc,
            stage = DeckStage.DECK_GENERATED,
        )
    }

    override fun isValid(state: DeckState): Boolean = state.contextValid

    override fun isFailed(state: DeckState): Boolean = state.stage == DeckStage.FAILED

    override fun onProposeFailure(state: DeckState, cause: Throwable): DeckState =
        state.copy(
            stage = DeckStage.FAILED,
            error = "ProposeContextFailed: ${cause.message}",
        )

    override fun onGenerateFailure(state: DeckState, cause: Throwable): DeckState =
        state.copy(
            stage = DeckStage.FAILED,
            error = "GenerateDeckFailed: ${cause.message}",
        )

    companion object {
        /** Strategy name exposed by the built koog graph. */
        const val NAME: String = "deck-pipeline"
    }
}
