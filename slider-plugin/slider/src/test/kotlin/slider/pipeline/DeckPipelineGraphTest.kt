package slider.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Unit tests for [DeckPipelineGraph] — the koog-orchestrated pipeline.
 *
 * Three nodes (propose-context → validate-context → generate-deck) chained
 * by conditional edges. The LLM is mocked via [FakeDeckPromptBuilder] +
 * [FakeDeckLlm] (no network, no key). The validator is the real
 * [DeckContextValidator] (pure domain service).
 *
 * Baby-step 8.3b (SLD-8 US-8.3): graph topology + happy path + failure modes.
 */
class DeckPipelineGraphTest {

    private val validDeckContextJson = """
        {
          "subject": "Kotlin Coroutines",
          "audience": "developers",
          "duration": 60,
          "languageCode": "fr",
          "outputFile": "kotlin-coroutines.adoc",
          "author": { "name": "Jane Doe", "email": "jane@example.com" }
        }
    """.trimIndent()

    private fun initialState(
        subject: String = "Kotlin Coroutines",
        language: String = "fr",
    ): DeckState = DeckState(
        subject = subject,
        language = language,
        authorName = "Jane Doe",
        authorEmail = "jane@example.com",
        ragContext = "Async programming on the JVM.",
        deckContextJson = "",
    )

    @Test
    fun `execute returns a DeckState with stage DECK_GENERATED on the happy path`() {
        val graph = DeckPipelineGraph(
            promptBuilder = FakeDeckPromptBuilder(),
            llm = FakeDeckLlm(
                proposeResponse = validDeckContextJson,
                generateResponse = "== Kotlin Coroutines\n\n== Introduction",
            ),
        )

        val result = graph.execute(initialState())

        assertThat(result.stage).isEqualTo(DeckStage.DECK_GENERATED)
        assertThat(result.deckAdoc).isEqualTo("== Kotlin Coroutines\n\n== Introduction")
        assertThat(result.contextValid).isTrue()
        assertThat(result.validationError).isNull()
        assertThat(result.error).isNull()
    }

    @Test
    fun `execute populates deckContextJson from the propose-context node`() {
        val graph = DeckPipelineGraph(
            promptBuilder = FakeDeckPromptBuilder(),
            llm = FakeDeckLlm(proposeResponse = validDeckContextJson, generateResponse = "== Deck"),
        )

        val result = graph.execute(initialState())

        assertThat(result.deckContextJson).isEqualTo(validDeckContextJson)
    }

    @Test
    fun `execute stops at CONTEXT_VALIDATED stage when the LLM proposes an invalid context`() {
        val graph = DeckPipelineGraph(
            promptBuilder = FakeDeckPromptBuilder(),
            llm = FakeDeckLlm(
                proposeResponse = """{"subject": "missing fields"}""",
                generateResponse = "== Should not be reached",
            ),
        )

        val result = graph.execute(initialState())

        assertThat(result.stage).isEqualTo(DeckStage.FAILED)
        assertThat(result.contextValid).isFalse()
        assertThat(result.validationError).isNotNull()
        assertThat(result.error).isNotNull()
        assertThat(result.deckAdoc).isEmpty()
    }

    @Test
    fun `execute reports the validator error when context is invalid`() {
        val graph = DeckPipelineGraph(
            promptBuilder = FakeDeckPromptBuilder(),
            llm = FakeDeckLlm(proposeResponse = "", generateResponse = ""),
        )

        val result = graph.execute(initialState())

        assertThat(result.stage).isEqualTo(DeckStage.FAILED)
        assertThat(result.error).contains("blank")
    }

    @Test
    fun `execute fails the pipeline when the propose-context LLM throws`() {
        val graph = DeckPipelineGraph(
            promptBuilder = FakeDeckPromptBuilder(),
            llm = FakeDeckLlm(proposeException = RuntimeException("LLM unavailable")),
        )

        val result = graph.execute(initialState())

        assertThat(result.stage).isEqualTo(DeckStage.FAILED)
        assertThat(result.error).isNotNull()
        assertThat(result.deckContextJson).isEmpty()
    }

    @Test
    fun `execute fails the pipeline when the generate-deck LLM throws`() {
        val graph = DeckPipelineGraph(
            promptBuilder = FakeDeckPromptBuilder(),
            llm = FakeDeckLlm(
                proposeResponse = validDeckContextJson,
                generateException = RuntimeException("Generation failed"),
            ),
        )

        val result = graph.execute(initialState())

        assertThat(result.stage).isEqualTo(DeckStage.FAILED)
        assertThat(result.error).isNotNull()
        assertThat(result.contextValid).isTrue()
    }

    @Test
    fun `execute calls the prompt builder with the running state for propose`() {
        val promptBuilder = FakeDeckPromptBuilder()
        val graph = DeckPipelineGraph(
            promptBuilder = promptBuilder,
            llm = FakeDeckLlm(proposeResponse = validDeckContextJson, generateResponse = ""),
        )

        graph.execute(initialState())

        assertThat(promptBuilder.proposeCalls).hasSize(1)
        assertThat(promptBuilder.proposeCalls[0].subject).isEqualTo("Kotlin Coroutines")
    }

    @Test
    fun `execute calls the prompt builder with the validated state for generate`() {
        val promptBuilder = FakeDeckPromptBuilder()
        val graph = DeckPipelineGraph(
            promptBuilder = promptBuilder,
            llm = FakeDeckLlm(proposeResponse = validDeckContextJson, generateResponse = "== Deck"),
        )

        graph.execute(initialState())

        assertThat(promptBuilder.generateCalls).hasSize(1)
        val generateState = promptBuilder.generateCalls[0]
        assertThat(generateState.contextValid).isTrue()
        assertThat(generateState.deckContextJson).isEqualTo(validDeckContextJson)
    }

    @Test
    fun `execute does not call generate prompt when context is invalid`() {
        val promptBuilder = FakeDeckPromptBuilder()
        val graph = DeckPipelineGraph(
            promptBuilder = promptBuilder,
            llm = FakeDeckLlm(proposeResponse = "{}", generateResponse = "== Should not be reached"),
        )

        graph.execute(initialState())

        assertThat(promptBuilder.generateCalls).isEmpty()
    }

    @Test
    fun `execute does not call generate LLM when context is invalid`() {
        val llm = FakeDeckLlm(proposeResponse = "{}", generateResponse = "== Should not be reached")
        val graph = DeckPipelineGraph(promptBuilder = FakeDeckPromptBuilder(), llm = llm)

        graph.execute(initialState())

        assertThat(llm.generateCallCount).isZero()
    }

    @Test
    fun `execute forwards the LLM propose prompt to the provider`() {
        val llm = FakeDeckLlm(proposeResponse = validDeckContextJson, generateResponse = "")
        val graph = DeckPipelineGraph(promptBuilder = FakeDeckPromptBuilder(), llm = llm)

        graph.execute(initialState())

        assertThat(llm.proposePrompts).hasSize(1)
        assertThat(llm.proposePrompts[0]).contains("Kotlin Coroutines")
    }

    @Test
    fun `execute forwards the LLM generate prompt to the provider`() {
        val llm = FakeDeckLlm(proposeResponse = validDeckContextJson, generateResponse = "== Deck")
        val graph = DeckPipelineGraph(promptBuilder = FakeDeckPromptBuilder(), llm = llm)

        graph.execute(initialState())

        assertThat(llm.generatePrompts).hasSize(1)
    }

    @Test
    fun `asMermaidDiagram returns a non-blank mermaid graph description`() {
        val graph = DeckPipelineGraph(
            promptBuilder = FakeDeckPromptBuilder(),
            llm = FakeDeckLlm(proposeResponse = validDeckContextJson, generateResponse = ""),
        )

        val mermaid = graph.asMermaidDiagram()

        assertThat(mermaid).isNotBlank()
    }

    @Test
    fun `execute preserves the initial subject and language through the pipeline`() {
        val graph = DeckPipelineGraph(
            promptBuilder = FakeDeckPromptBuilder(),
            llm = FakeDeckLlm(proposeResponse = validDeckContextJson, generateResponse = "== Deck"),
        )

        val result = graph.execute(initialState(subject = "Reactive Streams", language = "en"))

        assertThat(result.subject).isEqualTo("Reactive Streams")
        assertThat(result.language).isEqualTo("en")
    }

    @Test
    fun `execute preserves ragContext through the pipeline`() {
        val graph = DeckPipelineGraph(
            promptBuilder = FakeDeckPromptBuilder(),
            llm = FakeDeckLlm(proposeResponse = validDeckContextJson, generateResponse = "== Deck"),
        )

        val result = graph.execute(initialState().copy(ragContext = "custom RAG context"))

        assertThat(result.ragContext).isEqualTo("custom RAG context")
    }

    @Test
    fun `execute transitions through CONTEXT_PROPOSED before CONTEXT_VALIDATED`() {
        val promptBuilder = TrackingPromptBuilder()
        val graph = DeckPipelineGraph(
            promptBuilder = promptBuilder,
            llm = FakeDeckLlm(proposeResponse = validDeckContextJson, generateResponse = "== Deck"),
        )

        graph.execute(initialState())

        val proposeState = promptBuilder.proposeStates[0]
        assertThat(proposeState.stage).isEqualTo(DeckStage.INITIALIZED)
        val generateState = promptBuilder.generateStates[0]
        assertThat(generateState.stage).isEqualTo(DeckStage.CONTEXT_VALIDATED)
    }

    @Test
    fun `execute sets stage FAILED when generate throws despite valid context`() {
        val graph = DeckPipelineGraph(
            promptBuilder = FakeDeckPromptBuilder(),
            llm = FakeDeckLlm(
                proposeResponse = validDeckContextJson,
                generateException = IllegalStateException("timeout"),
            ),
        )

        val result = graph.execute(initialState())

        assertThat(result.stage).isEqualTo(DeckStage.FAILED)
        assertThat(result.contextValid).isTrue()
        assertThat(result.error).contains("timeout")
    }

    /**
     * Deterministic prompt builder stub — records the states it sees and
     * returns prompts derived from the subject so tests can assert on them.
     */
    private class FakeDeckPromptBuilder : DeckPromptBuilder {
        val proposeCalls = mutableListOf<DeckState>()
        val generateCalls = mutableListOf<DeckState>()

        override fun buildProposePrompt(state: DeckState): String {
            proposeCalls.add(state)
            return "Propose a DeckContext JSON for subject='${state.subject}' in language='${state.language}'."
        }

        override fun buildGeneratePrompt(state: DeckState): String {
            generateCalls.add(state)
            return "Generate an AsciiDoc deck from this DeckContext: ${state.deckContextJson}."
        }
    }

    /**
     * Prompt builder that captures the full [DeckState] at each call point
     * to verify stage transitions without exposing intermediate state.
     */
    private class TrackingPromptBuilder : DeckPromptBuilder {
        val proposeStates = mutableListOf<DeckState>()
        val generateStates = mutableListOf<DeckState>()

        override fun buildProposePrompt(state: DeckState): String {
            proposeStates.add(state)
            return "propose"
        }

        override fun buildGeneratePrompt(state: DeckState): String {
            generateStates.add(state)
            return "generate"
        }
    }

    /**
     * Fake LLM — no network, no key. Returns canned responses (or throws).
     */
    private class FakeDeckLlm(
        private val proposeResponse: String = "",
        private val generateResponse: String = "",
        private val proposeException: RuntimeException? = null,
        private val generateException: RuntimeException? = null,
    ) : DeckLlm {
        val proposePrompts = mutableListOf<String>()
        val generatePrompts = mutableListOf<String>()
        var proposeCallCount = 0
            private set
        var generateCallCount = 0
            private set

        override fun propose(prompt: String): String {
            proposeCallCount++
            proposePrompts.add(prompt)
            proposeException?.let { throw it }
            return proposeResponse
        }

        override fun generate(prompt: String): String {
            generateCallCount++
            generatePrompts.add(prompt)
            generateException?.let { throw it }
            return generateResponse
        }
    }
}