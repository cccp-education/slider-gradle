package slider.pipeline

import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import codebase.koog.pipeline.pipelineGraph

/**
 * Koog-orchestrated deck pipeline — the heart of EPIC SLD-8 US-8.3.
 *
 * EPIC KOOG-PIPELINE-DEDUP (BS-2 adoption): this class is now a thin facade
 * over the shared N1 skeleton `codebase.koog.pipeline.PipelineGraph`. All the
 * slider-specific node logic lives in [DeckPipelineDefinition]; the koog
 * topology (3 nodes with the conditional `validate -> generate | nodeFinish`
 * edges) and the sequential fail-fast `execute` are inherited from the N1
 * skeleton — the exact same structure previously duplicated alongside
 * `capsule.pipeline.CapsulePipelineGraph`.
 *
 * Architecture ("koog orchestrates, langchain4j executes"):
 *  - [graph] is a koog [AIAgentGraphStrategy] declaring 3 nodes wired by
 *    conditional edges. It captures the *topology* of the pipeline and is
 *    queryable via [asMermaidDiagram].
 *  - [execute] runs the pipeline sequentially with per-node try/catch, keeping
 *    failure modes explicit and the unit tests free from koog's async runtime.
 *
 * Conditional edges (topology):
 *  - `validate -> generate    onCondition { it.contextValid }`
 *  - `validate -> nodeFinish  onCondition { !it.contextValid }`
 *
 * Failures surface as [DeckStage.FAILED] with a non-null [DeckState.error]
 * (`ProposeContextFailed:` / `GenerateDeckFailed:`).
 *
 * @param promptBuilder builds the LLM prompts from the running [DeckState].
 * @param llm           the LLM provider (production: adapter on codebase's
 *                      `LlmBuildService`; tests: a fake).
 */
class DeckPipelineGraph(
    promptBuilder: DeckPromptBuilder,
    llm: DeckLlm,
) {

    private val delegate = pipelineGraph(DeckPipelineDefinition(promptBuilder, llm))

    val graph: AIAgentGraphStrategy<DeckState, DeckState> = delegate.graph

    /**
     * Runs the pipeline sequentially from [initialState] to a final
     * [DeckState]. Each node is wrapped in try/catch; failures surface as
     * [DeckStage.FAILED] with a non-null [DeckState.error].
     */
    fun execute(initialState: DeckState): DeckState = delegate.execute(initialState)

    fun asMermaidDiagram(): String = delegate.asMermaidDiagram()
}
