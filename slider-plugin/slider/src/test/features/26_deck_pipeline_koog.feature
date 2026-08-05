@pipeline @koog
Feature: Deck pipeline koog orchestration (SLD-8.3 — baby-step 8.3d)

  The slider.pipeline domain orchestrates the deck generation pipeline as a
  koog graph (DeckPipelineGraph) chaining three nodes:
    propose-context -> validate-context -> generate-deck

  koog orchestrates the topology (nodes + conditional edges); langchain4j
  executes the LLM calls through the DeckLlm port. The pipeline runs in a
  single invocation (generateDeckPipeline task) and produces a DeckState
  whose stage ends at DECK_GENERATED on the happy path, or FAILED with a
  human-readable error on any failure mode.

  Conditional edges:
    validateContext -> generateDeck   onCondition { it.contextValid }
    validateContext -> nodeFinish     onCondition { !it.contextValid }

  The LLM is mocked in these scenarios via a fake DeckLlm — no network, no
  key. The validator (DeckContextValidator) is the real pure domain service.

  # --- Happy path ----------------------------------------------------------

  Scenario: The pipeline generates a deck on the happy path
    Given a deck pipeline with a stub LLM proposing a valid DeckContext and generating an AsciiDoc deck
    And an initial deck state with subject "Kotlin Coroutines" and language "fr"
    When the pipeline is executed
    Then the final stage should be "DECK_GENERATED"
    And the generated deck should not be blank

  Scenario: The proposed DeckContext JSON is preserved in the final state
    Given a deck pipeline with a stub LLM proposing a valid DeckContext and generating an AsciiDoc deck
    And an initial deck state with subject "Kotlin Coroutines" and language "fr"
    When the pipeline is executed
    Then the final stage should be "DECK_GENERATED"
    And the final deck context JSON should be the stub proposal

  Scenario: The subject, language and RAG context are preserved through the pipeline
    Given a deck pipeline with a stub LLM proposing a valid DeckContext and generating an AsciiDoc deck
    And an initial deck state with subject "Reactive Streams" and language "en"
    And the RAG context "async streams notes"
    When the pipeline is executed
    Then the final stage should be "DECK_GENERATED"
    And the subject should be preserved as "Reactive Streams"
    And the language should be preserved as "en"
    And the RAG context should be preserved as "async streams notes"

  # --- Invalid context -> FAILED, no deck ----------------------------------

  Scenario: A DeckContext missing the subject field fails validation
    Given a deck pipeline with a stub LLM proposing an invalid DeckContext missing the subject and generating nothing
    And an initial deck state with subject "Kotlin Coroutines" and language "fr"
    When the pipeline is executed
    Then the final stage should be "FAILED"
    And the generated deck should be blank
    And the error should mention "subject"

  Scenario: A DeckContext with a non-positive duration fails validation
    Given a deck pipeline with a stub LLM proposing an invalid DeckContext with duration 0 and generating nothing
    And an initial deck state with subject "Kotlin Coroutines" and language "fr"
    When the pipeline is executed
    Then the final stage should be "FAILED"
    And the error should mention "duration"

  Scenario: A DeckContext with an unsupported language code fails validation
    Given a deck pipeline with a stub LLM proposing an invalid DeckContext with language code "xx" and generating nothing
    And an initial deck state with subject "Kotlin Coroutines" and language "fr"
    When the pipeline is executed
    Then the final stage should be "FAILED"
    And the error should mention "languageCode"

  # --- LLM failure modes -> FAILED ----------------------------------------

  Scenario: A propose-context LLM exception fails the pipeline before validation
    Given a deck pipeline with a stub LLM that throws on propose
    And an initial deck state with subject "Kotlin Coroutines" and language "fr"
    When the pipeline is executed
    Then the final stage should be "FAILED"
    And the error should mention "ProposeContextFailed"
    And the final deck context JSON should be blank

  Scenario: A blank LLM proposal fails the pipeline
    Given a deck pipeline with a stub LLM proposing a blank DeckContext and generating nothing
    And an initial deck state with subject "Kotlin Coroutines" and language "fr"
    When the pipeline is executed
    Then the final stage should be "FAILED"
    And the final deck context JSON should be blank

  Scenario: A generate-deck LLM exception fails the pipeline after a valid context
    Given a deck pipeline with a stub LLM proposing a valid DeckContext and throwing on generate
    And an initial deck state with subject "Kotlin Coroutines" and language "fr"
    When the pipeline is executed
    Then the final stage should be "FAILED"
    And the context should be valid
    And the error should mention "GenerateDeckFailed"
    And the generated deck should be blank

  # --- Edge conditions -----------------------------------------------------

  Scenario: The generate-deck node is not called when the context is invalid
    Given a deck pipeline with a stub LLM proposing an invalid DeckContext missing the subject and generating an unreachable deck
    And an initial deck state with subject "Kotlin Coroutines" and language "fr"
    When the pipeline is executed
    Then the final stage should be "FAILED"
    And the generate node should not have been called

  Scenario: The pipeline transitions through all expected stages on the happy path
    Given a deck pipeline with a stub LLM proposing a valid DeckContext and generating an AsciiDoc deck
    And an initial deck state with subject "Kotlin Coroutines" and language "fr"
    When the pipeline is executed
    Then the final stage should be "DECK_GENERATED"
    And the propose node should have received the state at stage "INITIALIZED"
    And the generate node should have received the state at stage "CONTEXT_VALIDATED"

  Scenario: The koog graph exposes a non-blank Mermaid diagram
    Given a deck pipeline with a stub LLM proposing a valid DeckContext and generating an AsciiDoc deck
    Then the Mermaid diagram of the graph should not be blank