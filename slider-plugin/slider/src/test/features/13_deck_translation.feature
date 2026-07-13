@translation @i18n
Feature: Deck translation pipeline produces 10 localized variants from a source deck

  As a slider-gradle producer
  I want to translate a deck into the 10 most spoken languages
  So that my presentation reaches a global audience

  Scenario: A valid translation request defaults to all 10 supported languages
    Given a source deck in language "fr"
    When a translation request is created with default targets
    Then the request should target all 10 LanguageCatalog supported codes

  Scenario: A translation request with explicit target languages
    Given a source deck in language "fr"
    When a translation request is created with targets "en,ar,zh"
    Then the request should target exactly 3 languages
    And the request should target "en"
    And the request should target "ar"
    And the request should target "zh"

  Scenario: A translation plan excludes identity translation
    Given a source deck in language "fr"
    When a translation plan is built from a request targeting "fr,en,ar"
    Then the plan should contain 2 tasks
    And the plan should not contain a task from "fr" to "fr"

  Scenario: A translation plan covers all 9 non-source languages
    Given a source deck in language "fr"
    When a translation plan is built from a request targeting all 10 languages
    Then the plan should contain 9 tasks
    And the plan source language should be "fr"

  Scenario: An invalid source language is rejected
    Given a source deck in language "xx"
    When the translation request creation is attempted
    Then the translation creation should fail with a message containing "sourceDeck"

  Scenario: An invalid target language is rejected
    Given a source deck in language "fr"
    When the translation request creation is attempted with targets "en,xx"
    Then the translation creation should fail with a message containing "xx"

  Scenario: An empty target languages list is rejected
    Given a source deck in language "fr"
    When the translation request creation is attempted with empty targets
    Then the translation creation should fail with a message containing "targetLanguages"

  Scenario: Duplicate target languages are rejected
    Given a source deck in language "fr"
    When the translation request creation is attempted with targets "en,en,ar"
    Then the translation creation should fail with a message containing "duplicate"

  Scenario: A successful translation produces Translated results
    Given a source deck in language "fr"
    And a stub LLM that returns content for "en" and "ar"
    When the deck is translated into "en" and "ar"
    Then the outcome should have 2 translated results
    And the outcome should have 0 failed results
    And the outcome should be all translated

  Scenario: A failed LLM call produces a Failed result
    Given a source deck in language "fr"
    And a stub LLM that returns content for "en" but null for "zh"
    When the deck is translated into "en" and "zh"
    Then the outcome should have 1 translated results
    And the outcome should have 1 failed results
    And the outcome should not be all translated

  Scenario: A translated deck has the target language code and updated output file
    Given a source deck in language "fr" with output file "kotlin-deck.adoc"
    And a stub LLM that returns content for "en"
    When the deck is translated into "en"
    Then the translated deck for "en" should have language code "en"
    And the translated deck for "en" should have output file "kotlin_en-deck.adoc"

  Scenario: A translated deck preserves source subject and audience
    Given a source deck in language "fr" with subject "Kotlin Coroutines"
    And a stub LLM that returns content for "en"
    When the deck is translated into "en"
    Then the translated deck for "en" should have subject "Kotlin Coroutines"

  Scenario: Outcome summary reports counts
    Given a source deck in language "fr"
    And a stub LLM that returns content for "en" and "ar" but null for "zh"
    When the deck is translated into "en", "ar", and "zh"
    Then the outcome summary should contain "2 translated"
    And the outcome summary should contain "1 failed"