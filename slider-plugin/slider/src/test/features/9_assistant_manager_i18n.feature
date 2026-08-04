@i18n @assistant-manager
Feature: AssistantManager i18n task descriptions

  Scenario: reindexRag task has i18n description in English
    When the i18n message key "task.reindexRag.description" is resolved in English
    Then the message should contain "Force a full rebuild"

  Scenario: reindexRag task has i18n description in French
    When the i18n message key "task.reindexRag.description" is resolved in French
    Then the message should contain "Forcer une reconstruction"

  Scenario: proposeDeckContext task has i18n description in English
    When the i18n message key "task.proposeDeckContext.description" is resolved in English
    Then the message should contain "RAG + LLM"

  Scenario: proposeDeckContext task has i18n description in French
    When the i18n message key "task.proposeDeckContext.description" is resolved in French
    Then the message should contain "RAG + LLM"

  Scenario: generateDeck task has i18n description in English
    When the i18n message key "task.generateDeck.description" is resolved in English
    Then the message should contain "AsciiDoc/Reveal.js"

  Scenario: generateDeck task has i18n description in French
    When the i18n message key "task.generateDeck.description" is resolved in French
    Then the message should contain "AsciiDoc/Reveal.js"

  Scenario: helloOllama smoke test has i18n description in English
    When the i18n message key "task.helloOllama.description" is formatted in English with model "gpt-oss:120b-cloud"
    Then the message should contain "gpt-oss:120b-cloud"
    And the message should contain "smoke test"

  Scenario: helloOllama smoke test has i18n description in French
    When the i18n message key "task.helloOllama.description" is formatted in French with model "gpt-oss:120b-cloud"
    Then the message should contain "gpt-oss:120b-cloud"
    And the message should contain "test de fumée"

  Scenario: helloGemini smoke test has i18n description in English
    When the i18n message key "task.helloGemini.description" is formatted in English with model "gemini-2.0-flash"
    Then the message should contain "gemini-2.0-flash"
    And the message should contain "smoke test"

  Scenario: helloHuggingFace smoke test has i18n description in English
    When the i18n message key "task.helloHuggingFace.description" is formatted in English with model "meta-llama/Llama-3.1-8B"
    Then the message should contain "meta-llama/Llama-3.1-8B"
    And the message should contain "smoke test"

  Scenario: task group collect resolves in English
    When the i18n message key "task.group.collect" is resolved in English
    Then the message should be "collect"

  Scenario: task group slider-ai resolves in English
    When the i18n message key "task.group.slider-ai" is resolved in English
    Then the message should be "slider-ai"
