@i18n @rtl
Feature: Reveal.js RTL layout for Arabic, Urdu and Persian decks

  Scenario: Arabic language code resolves to RTL layout
    When the deck language is "ar"
    Then the RTL layout should be enabled

  Scenario: Urdu language code resolves to RTL layout
    When the deck language is "ur"
    Then the RTL layout should be enabled

  Scenario: Persian language code resolves to RTL layout
    When the deck language is "fa"
    Then the RTL layout should be enabled

  Scenario: French language code resolves to LTR layout
    When the deck language is "fr"
    Then the RTL layout should be disabled

  Scenario: English language code resolves to LTR layout
    When the deck language is "en"
    Then the RTL layout should be disabled

  Scenario: Unknown language code defaults to LTR layout
    When the deck language is "xx"
    Then the RTL layout should be disabled

  Scenario: RTL languages are exactly the catalog RTL set
    When the RTL languages are listed
    Then they should be exactly the catalog RTL languages