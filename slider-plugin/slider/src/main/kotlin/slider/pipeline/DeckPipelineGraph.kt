package slider.pipeline

import ai.koog.agents.core.agent.asMermaidDiagram
import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.agent.entity.ToolSelectionStrategy
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Koog-orchestrated deck pipeline — the heart of EPIC SLD-8 US-8.3.
 *
 * Architecture ("koog orchestrates, langchain4j executes"):
 *  - The [graph] is a koog [AIAgentGraphStrategy] declaring 3 nodes wired by
 *    conditional edges. It captures the *topology* of the pipeline and is
 *    queryable via [asMermaidDiagram].
 *  - The [execute] method runs the pipeline sequentially with per-node
 *    try/catch, mirroring [codebase.koog.KoogAugmentedContextGraph.execute].
 *    This keeps failure modes explicit and the unit tests free from koog's
 *    async runtime.
 *
 * Nodes:
 *  1. [proposeContext] — calls [DeckLlm.propose] with the prompt built from
 *     the initial [DeckState]; stores the returned JSON in
 *     [DeckState.deckContextJson] and advances to [DeckStage.CONTEXT_PROPOSED].
 *  2. [validateContext] — pure — delegates to [DeckContextValidator].
 *     On [ValidationResult.Valid] advances to [DeckStage.CONTEXT_VALIDATED]
 *     with `contextValid = true`. On [ValidationResult.Invalid] advances to
 *     [DeckStage.FAILED] with the validator error.
 *  3. [generateDeck] — calls [DeckLlm.generate] with the prompt built from
 *     the validated state; stores the AsciiDoc in [DeckState.deckAdoc] and
 *     advances to [DeckStage.DECK_GENERATED].
 *
 * Conditional edges:
 *  - `validateContext → generateDeck  onCondition { it.contextValid }`
 *  - `validateContext → nodeFinish    onCondition { !it.contextValid }`
 *
 * Non-périmètre (v1): no retry loop on validate, no Checkpoints, no
 * Self-Reflection, no RAG inside the graph (RAG stays in the existing tasks).
 *
 * @param promptBuilder builds the LLM prompts from the running [DeckState].
 * @param llm           the LLM provider (production: adapter on codebase's
 *                      `LlmBuildService`; tests: a fake).
 */
class DeckPipelineGraph(
    private val promptBuilder: DeckPromptBuilder,
    private val llm: DeckLlm,
) {

    private val log = LoggerFactory.getLogger(DeckPipelineGraph::class.java)

    val graph: AIAgentGraphStrategy<DeckState, DeckState> = strategy<DeckState, DeckState>(
        name = "deck-pipeline",
        toolSelectionStrategy = ToolSelectionStrategy.NONE,
    ) {
        val proposeContext by node<DeckState, DeckState> { state ->
            proposeContextNode(state)
        }
        val validateContext by node<DeckState, DeckState> { state ->
            validateContextNode(state)
        }
        val generateDeck by node<DeckState, DeckState> { state ->
            generateDeckNode(state)
        }

        edge(nodeStart forwardTo proposeContext onCondition { _ -> true } transformed { it })
        edge(proposeContext forwardTo validateContext onCondition { _ -> true } transformed { it })
        edge(validateContext forwardTo generateDeck onCondition { it.contextValid } transformed { it })
        edge(validateContext forwardTo nodeFinish onCondition { !it.contextValid } transformed { it })
        edge(generateDeck forwardTo nodeFinish onCondition { _ -> true } transformed { it })
    }

    /**
     * Runs the pipeline sequentially from [initialState] to a final
     * [DeckState]. Each node is wrapped in try/catch; failures surface as
     * [DeckStage.FAILED] with a non-null [DeckState.error].
     */
    fun execute(initialState: DeckState): DeckState {
        var state = try {
            proposeContextNode(initialState)
        } catch (e: Exception) {
            log.warn("[DeckPipelineGraph] propose-context failed: {}", e.message)
            initialState.copy(
                stage = DeckStage.FAILED,
                error = "ProposeContextFailed: ${e.message}",
            )
        }

        if (state.stage == DeckStage.FAILED) return state

        state = validateContextNode(state)
        if (!state.contextValid) return state

        return try {
            generateDeckNode(state)
        } catch (e: Exception) {
            log.error("[DeckPipelineGraph] generate-deck failed: {}", e.message)
            state.copy(
                stage = DeckStage.FAILED,
                error = "GenerateDeckFailed: ${e.message}",
            )
        }
    }

    fun asMermaidDiagram(): String = runBlocking { graph.asMermaidDiagram() }

    private fun proposeContextNode(state: DeckState): DeckState {
        val prompt = promptBuilder.buildProposePrompt(state)
        val deckContextJson = llm.propose(prompt)
        require(deckContextJson.isNotBlank()) { "LLM returned a blank DeckContext JSON" }
        return state.copy(
            deckContextJson = deckContextJson,
            stage = DeckStage.CONTEXT_PROPOSED,
        )
    }

    private fun validateContextNode(state: DeckState): DeckState {
        return when (val result = DeckContextValidator.validate(state.deckContextJson)) {
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
    }

    private fun generateDeckNode(state: DeckState): DeckState {
        val prompt = promptBuilder.buildGeneratePrompt(state)
        val deckAdoc = llm.generate(prompt)
        require(deckAdoc.isNotBlank()) { "LLM returned a blank AsciiDoc deck" }
        return state.copy(
            deckAdoc = deckAdoc,
            stage = DeckStage.DECK_GENERATED,
        )
    }
}