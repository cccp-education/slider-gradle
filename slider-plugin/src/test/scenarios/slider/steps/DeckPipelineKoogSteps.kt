package slider.steps

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.pipeline.DeckLlm
import slider.pipeline.DeckPipelineGraph
import slider.pipeline.DeckPromptBuilder
import slider.pipeline.DeckStage
import slider.pipeline.DeckState

/**
 * Cucumber step definitions for the koog-orchestrated deck pipeline feature
 * (SLD-8.3 baby-step 8.3d — `26_deck_pipeline_koog.feature`).
 *
 * Pure BDD — no production code is modified by this baby-step. The pipeline
 * ([DeckPipelineGraph]) is exercised with in-memory fakes implementing the
 * [DeckLlm] and [DeckPromptBuilder] ports, mirroring the unit tests of 8.3b
 * (`DeckPipelineGraphTest`) and the BDD pattern of `DeckTranslationSteps`
 * (SLD-5) / `ContentValidationSteps` (SLD-10.6).
 *
 * Scenarios cover the happy path, invalid-context failures (validator), LLM
 * failure modes (propose / generate throwing or returning blank), edge
 * conditions (no generate call on invalid context, stage transitions), and
 * the Mermaid diagram exposure of the koog graph.
 */
class DeckPipelineKoogSteps : En {

    private var promptBuilder: RecordingPromptBuilder? = null
    private var llm: FakeDeckLlm? = null
    private var graph: DeckPipelineGraph? = null
    private var initialState: DeckState? = null
    private var result: DeckState? = null

    private val validDeckContextJson: String = """
        {
          "subject": "Kotlin Coroutines",
          "audience": "developers",
          "duration": 60,
          "languageCode": "fr",
          "outputFile": "kotlin-coroutines.adoc",
          "author": { "name": "Jane Doe", "email": "jane@example.com" }
        }
    """.trimIndent()

    init {

        // -------------------------------------------------------------------------
        // Pipeline construction — stub LLM configurations
        // -------------------------------------------------------------------------

        Given("a deck pipeline with a stub LLM proposing a valid DeckContext and generating an AsciiDoc deck") {
            promptBuilder = RecordingPromptBuilder()
            llm = FakeDeckLlm(
                proposeResponse = validDeckContextJson,
                generateResponse = "== Kotlin Coroutines\n\n== Introduction",
            )
            graph = DeckPipelineGraph(promptBuilder!!, llm!!)
        }

        Given("a deck pipeline with a stub LLM proposing an invalid DeckContext missing the subject and generating nothing") {
            promptBuilder = RecordingPromptBuilder()
            llm = FakeDeckLlm(
                proposeResponse = """{"audience": "developers", "duration": 60, "languageCode": "fr"}""",
                generateResponse = "",
            )
            graph = DeckPipelineGraph(promptBuilder!!, llm!!)
        }

        Given("a deck pipeline with a stub LLM proposing an invalid DeckContext missing the subject and generating an unreachable deck") {
            promptBuilder = RecordingPromptBuilder()
            llm = FakeDeckLlm(
                proposeResponse = """{"audience": "developers", "duration": 60, "languageCode": "fr"}""",
                generateResponse = "== Should not be reached",
            )
            graph = DeckPipelineGraph(promptBuilder!!, llm!!)
        }

        Given("a deck pipeline with a stub LLM proposing an invalid DeckContext with duration {int} and generating nothing") { duration: Int ->
            promptBuilder = RecordingPromptBuilder()
            llm = FakeDeckLlm(
                proposeResponse = """{"subject": "Kotlin Coroutines", "audience": "developers", "duration": $duration, "languageCode": "fr", "outputFile": "k.adoc", "author": {"name": "Jane", "email": "j@example.com"}}""",
                generateResponse = "",
            )
            graph = DeckPipelineGraph(promptBuilder!!, llm!!)
        }

        Given("a deck pipeline with a stub LLM proposing an invalid DeckContext with language code {string} and generating nothing") { code: String ->
            promptBuilder = RecordingPromptBuilder()
            llm = FakeDeckLlm(
                proposeResponse = """{"subject": "Kotlin Coroutines", "audience": "developers", "duration": 60, "languageCode": "$code", "outputFile": "k.adoc", "author": {"name": "Jane", "email": "j@example.com"}}""",
                generateResponse = "",
            )
            graph = DeckPipelineGraph(promptBuilder!!, llm!!)
        }

        Given("a deck pipeline with a stub LLM proposing a blank DeckContext and generating nothing") {
            promptBuilder = RecordingPromptBuilder()
            llm = FakeDeckLlm(proposeResponse = "   ", generateResponse = "")
            graph = DeckPipelineGraph(promptBuilder!!, llm!!)
        }

        Given("a deck pipeline with a stub LLM that throws on propose") {
            promptBuilder = RecordingPromptBuilder()
            llm = FakeDeckLlm(proposeException = RuntimeException("LLM unavailable"))
            graph = DeckPipelineGraph(promptBuilder!!, llm!!)
        }

        Given("a deck pipeline with a stub LLM proposing a valid DeckContext and throwing on generate") {
            promptBuilder = RecordingPromptBuilder()
            llm = FakeDeckLlm(
                proposeResponse = validDeckContextJson,
                generateException = RuntimeException("Generation timed out"),
            )
            graph = DeckPipelineGraph(promptBuilder!!, llm!!)
        }

        // -------------------------------------------------------------------------
        // Initial state
        // -------------------------------------------------------------------------

        Given("an initial deck state with subject {string} and language {string}") { subject: String, language: String ->
            initialState = DeckState(
                subject = subject,
                language = language,
                authorName = "Jane Doe",
                authorEmail = "jane@example.com",
                ragContext = "",
                deckContextJson = "",
            )
        }

        Given("the RAG context {string}") { ragContext: String ->
            initialState = initialState!!.copy(ragContext = ragContext)
        }

        // -------------------------------------------------------------------------
        // When
        // -------------------------------------------------------------------------

        When("the pipeline is executed") {
            result = graph!!.execute(initialState!!)
        }

        // -------------------------------------------------------------------------
        // Then — happy path
        // -------------------------------------------------------------------------

        Then("the final stage should be {string}") { stageName: String ->
            val expected = DeckStage.valueOf(stageName)
            assertThat(result!!.stage)
                .withFailMessage("Expected stage $expected but got ${result!!.stage} (error=${result!!.error})")
                .isEqualTo(expected)
        }

        Then("the generated deck should not be blank") {
            assertThat(result!!.deckAdoc).isNotBlank()
        }

        Then("the generated deck should be blank") {
            assertThat(result!!.deckAdoc).isBlank()
        }

        Then("the final deck context JSON should be the stub proposal") {
            assertThat(result!!.deckContextJson).isEqualTo(validDeckContextJson)
        }

        Then("the final deck context JSON should be blank") {
            assertThat(result!!.deckContextJson).isBlank()
        }

        Then("the subject should be preserved as {string}") { subject: String ->
            assertThat(result!!.subject).isEqualTo(subject)
        }

        Then("the language should be preserved as {string}") { language: String ->
            assertThat(result!!.language).isEqualTo(language)
        }

        Then("the RAG context should be preserved as {string}") { ragContext: String ->
            assertThat(result!!.ragContext).isEqualTo(ragContext)
        }

        // -------------------------------------------------------------------------
        // Then — failures
        // -------------------------------------------------------------------------

        Then("the error should mention {string}") { fragment: String ->
            assertThat(result!!.error)
                .withFailMessage("Expected error to mention '$fragment' but was: ${result!!.error}")
                .contains(fragment)
        }

        Then("the context should be valid") {
            assertThat(result!!.contextValid).isTrue()
        }

        // -------------------------------------------------------------------------
        // Then — edge conditions
        // -------------------------------------------------------------------------

        Then("the generate node should not have been called") {
            assertThat(llm!!.generateCallCount)
                .withFailMessage("Expected generate node not to be called but it was called ${llm!!.generateCallCount} time(s)")
                .isZero()
        }

        Then("the propose node should have received the state at stage {string}") { stageName: String ->
            val expected = DeckStage.valueOf(stageName)
            val proposeState = promptBuilder!!.proposeStates.firstOrNull()
            assertThat(proposeState)
                .withFailMessage("Expected a propose call but none was recorded")
                .isNotNull()
            assertThat(proposeState!!.stage).isEqualTo(expected)
        }

        Then("the generate node should have received the state at stage {string}") { stageName: String ->
            val expected = DeckStage.valueOf(stageName)
            val generateState = promptBuilder!!.generateStates.firstOrNull()
            assertThat(generateState)
                .withFailMessage("Expected a generate call but none was recorded")
                .isNotNull()
            assertThat(generateState!!.stage).isEqualTo(expected)
        }

        Then("the Mermaid diagram of the graph should not be blank") {
            val mermaid = graph!!.asMermaidDiagram()
            assertThat(mermaid).isNotBlank()
        }
    }

    /**
     * Prompt builder that records every [DeckState] it receives, so BDD
     * scenarios can assert on stage transitions. Mirrors the
     * `TrackingPromptBuilder` of `DeckPipelineGraphTest`.
     */
    private class RecordingPromptBuilder : DeckPromptBuilder {
        val proposeStates = mutableListOf<DeckState>()
        val generateStates = mutableListOf<DeckState>()

        override fun buildProposePrompt(state: DeckState): String {
            proposeStates.add(state)
            return "Propose a DeckContext JSON for subject='${state.subject}' in language='${state.language}'."
        }

        override fun buildGeneratePrompt(state: DeckState): String {
            generateStates.add(state)
            return "Generate an AsciiDoc deck from this DeckContext: ${state.deckContextJson}."
        }
    }

    /**
     * Fake LLM — no network, no key. Returns canned responses (or throws).
     * Mirrors the `FakeDeckLlm` of `DeckPipelineGraphTest`.
     */
    private class FakeDeckLlm(
        private val proposeResponse: String = "",
        private val generateResponse: String = "",
        private val proposeException: RuntimeException? = null,
        private val generateException: RuntimeException? = null,
    ) : DeckLlm {
        var generateCallCount = 0
            private set

        override fun propose(prompt: String): String {
            proposeException?.let { throw it }
            return proposeResponse
        }

        override fun generate(prompt: String): String {
            generateCallCount++
            generateException?.let { throw it }
            return generateResponse
        }
    }
}