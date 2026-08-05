@i18n @messages
Feature: SliderMessages resolution and formatting

  Background:
    Given a slider message resolver

  Scenario: Resolve a known key in English
    When the i18n message key "task.reindexRag.description" is resolved in language "en"
    Then the resolved message should contain "Force a full rebuild"

  Scenario: Resolve a known key in French
    When the i18n message key "task.reindexRag.description" is resolved in language "fr"
    Then the resolved message should contain "Forcer une reconstruction"

  Scenario: Resolve a known key in Arabic
    When the i18n message key "task.reindexRag.description" is resolved in language "ar"
    Then the resolved message should contain "إجبار إعادة بناء"

  Scenario: Resolve a known key in Chinese
    When the i18n message key "task.reindexRag.description" is resolved in language "zh"
    Then the resolved message should contain "强制完整重建"

  Scenario: Format a parameterized description with model name in English
    When the i18n message key "task.helloOllama.description" is formatted in language "en" with args "gpt-oss:120b-cloud"
    Then the resolved message should contain "gpt-oss:120b-cloud"
    And the resolved message should contain "smoke test"

  Scenario: Format a parameterized description with model name in French
    When the i18n message key "task.helloOllama.description" is formatted in language "fr" with args "gpt-oss:120b-cloud"
    Then the resolved message should contain "gpt-oss:120b-cloud"
    And the resolved message should contain "test de fumée"

  Scenario: Format a multi-argument cleaned message in English
    When the i18n message key "task.cleanBuild.cleaned" is formatted in language "en" with args "3|/tmp/build|5|2|1"
    Then the resolved message should contain "3"
    And the resolved message should contain "/tmp/build"
    And the resolved message should contain "5"

  Scenario: Format a multi-argument cleaned message in French
    When the i18n message key "task.cleanBuild.cleaned" is formatted in language "fr" with args "3|/tmp/build|5|2|1"
    Then the resolved message should contain "3"
    And the resolved message should contain "artefacts nettoyés"

  Scenario: Missing key throws an error
    When the i18n message key "task.nonexistent.key" is resolved in language "en"
    Then the resolution should fail with a missing resource error

  Scenario: Unsupported language falls back to English
    When the i18n message key "task.reindexRag.description" is resolved in language "xx"
    Then the message should be the English fallback for "task.reindexRag.description"

  Scenario Outline: Resolve task description in all supported languages
    When the i18n message key "<key>" is resolved in language "<lang>"
    Then the message should not be blank

    Examples:
      | key                            | lang |
      | task.reindexRag.description    | en   |
      | task.reindexRag.description    | fr   |
      | task.reindexRag.description    | zh   |
      | task.reindexRag.description    | hi   |
      | task.reindexRag.description    | es   |
      | task.reindexRag.description    | ar   |
      | task.reindexRag.description    | bn   |
      | task.reindexRag.description    | pt   |
      | task.reindexRag.description    | ru   |
      | task.reindexRag.description    | ur   |
      | task.generateDeck.description  | en   |
      | task.generateDeck.description  | fr   |
      | task.generateDeck.description  | zh   |
      | task.generateDeck.description  | hi   |
      | task.generateDeck.description  | es   |
      | task.generateDeck.description  | ar   |
      | task.generateDeck.description  | bn   |
      | task.generateDeck.description  | pt   |
      | task.generateDeck.description  | ru   |
      | task.generateDeck.description  | ur   |