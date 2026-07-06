@i18n @fallback
Feature: i18n fallback and configuration validation

  Scenario: I18nConfig fallback language should always be the default
    When the i18n config is resolved with default language "fr"
    Then the fallback language should be "fr"

  Scenario: Invalid CLI language code should fall back to default
    When the i18n config is resolved with CLI language "xx" and default "fr"
    Then the active language should fall back to "fr"

  Scenario: Unknown CLI language code falls back to default when gradle properties are empty
    When the i18n config is resolved with CLI language "unknown" and default "es"
    Then the active language should fall back to "es"

  Scenario: Empty CLI language code falls back to gradle properties
    When the i18n config is resolved with empty CLI language and gradle properties language "en"
    Then the active language should be "en"

  Scenario: Supported languages fall back to active language singleton when empty
    When the i18n config is resolved with CLI language "en" and empty supported languages
    Then the supported languages should contain only "en"

  Scenario: Resolved i18n config for supported code should validate successfully
    When the i18n config is resolved with CLI language "fr" and default "fr"
    Then the config should validate successfully