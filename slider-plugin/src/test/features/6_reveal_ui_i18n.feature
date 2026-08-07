@i18n @revealjs
Feature: Reveal.js UI i18n messages generation for 10 languages

  Scenario: RevealUiMessageCatalog exposes one entry per LanguageCatalog language
    When the Reveal UI message catalog is queried
    Then it should contain one entry per LanguageCatalog language

  Scenario: RevealUiMessageCatalog covers all 10 ISO codes
    When the Reveal UI message catalog is queried
    Then it should cover all LanguageCatalog supported codes

  Scenario: Reveal UI messages for Arabic should be RTL
    When the Reveal UI messages for "ar" are queried
    Then the RTL flag should be true

  Scenario: Reveal UI messages for Urdu should be RTL
    When the Reveal UI messages for "ur" are queried
    Then the RTL flag should be true

  Scenario: Reveal UI messages for French should not be RTL
    When the Reveal UI messages for "fr" are queried
    Then the RTL flag should be false

  Scenario: Reveal UI messages writer produces one file per supported language
    When the Reveal UI messages writer writes all messages to the output directory
    Then one messages js file should exist for each supported language

  Scenario: Written messages_fr js file contains French navigation labels
    When the Reveal UI messages writer writes all messages to the output directory
    Then the messages js file for "fr" should contain "Diapositive précédente"
    And the messages js file for "fr" should contain "Diapositive suivante"

  Scenario: Written messages_ar js file embeds the RTL flag
    When the Reveal UI messages writer writes all messages to the output directory
    Then the messages js file for "ar" should contain "rtl: true"

  Scenario: Written messages_en js file does not embed the RTL flag
    When the Reveal UI messages writer writes all messages to the output directory
    Then the messages js file for "en" should not contain "rtl: true"

  Scenario: Each written file starts with the RevealI18n assignment
    When the Reveal UI messages writer writes all messages to the output directory
    Then each written file should start with "RevealI18n"