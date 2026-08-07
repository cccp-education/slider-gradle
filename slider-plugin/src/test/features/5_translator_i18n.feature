@translator @i18n
Feature: TranslatorManager supports 10 languages via LanguageCatalog

  Scenario: Supported languages match LanguageCatalog ISO codes
    When the translator supported languages are queried
    Then they should contain all LanguageCatalog supported codes

  Scenario: Supported languages do not contain legacy display names
    When the translator supported languages are queried
    Then they should not contain "French" or "English"

  Scenario: Translation tasks exclude identity permutations
    When translation tasks are generated from supported languages
    Then no task should translate a language to itself

  Scenario: Translation tasks produce N times N minus 1 permutations
    When translation tasks are generated from supported languages
    Then the number of tasks should be 10 times 9

  Scenario: Translation prompt mentions target language native name
    When a translation prompt is generated from "fr" to "ar" for text "Bonjour"
    Then the prompt should contain the native name of "ar"

  Scenario: Translation prompt mentions source language native name
    When a translation prompt is generated from "fr" to "en" for text "Bonjour"
    Then the prompt should contain the native name of "fr"

  Scenario: Translation prompt contains the source text
    When a translation prompt is generated from "fr" to "zh" for text "Bonjour le monde"
    Then the prompt should contain "Bonjour le monde"