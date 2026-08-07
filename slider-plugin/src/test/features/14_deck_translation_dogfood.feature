@translation @dogfood @i18n
Feature: Deck translation dogfooding — 3 demo decks translated to 10 languages

  As a slider-gradle maintainer
  I want the 3 demo decks (fr/en/ar) to be translated into all 10 supported languages
  So that the translateDeck pipeline is validated end-to-end on real content

  Scenario: All 3 demo source decks have a deck-context.yml file
    Given the demo deck context for "fr" exists
    And the demo deck context for "en" exists
    And the demo deck context for "ar" exists

  Scenario: Each demo deck context has a valid ISO 639-1 language code
    Given the demo deck context for "fr" exists
    Then the context language code should be "fr"
    Given the demo deck context for "en" exists
    Then the context language code should be "en"
    Given the demo deck context for "ar" exists
    Then the context language code should be "ar"

  Scenario: Each demo deck context references an existing adoc file
    Given the demo deck context for "fr" exists
    Then the context output file should exist as adoc
    Given the demo deck context for "en" exists
    Then the context output file should exist as adoc
    Given the demo deck context for "ar" exists
    Then the context output file should exist as adoc

  Scenario Outline: Each source deck has 9 translated adoc files
    Given the source deck "<source>" has been translated
    Then there should be 9 translated adoc files for source "<source>"
    And there should be 9 translated context files for source "<source>"

    Examples:
      | source |
      | fr     |
      | en     |
      | ar     |

  Scenario Outline: Each translated adoc file preserves AsciiDoc structure
    Given the translated adoc from "<source>" to "<target>" exists
    Then the adoc should contain a title starting with "="
    And the adoc should contain a section header "=="
    And the adoc should contain "[NOTE.speaker]"
    And the adoc should contain "[%step]"
    And the adoc should contain ":revealjs_theme:"
    And the adoc should be non-empty

    Examples:
      | source | target |
      | fr     | en     |
      | fr     | ar     |
      | en     | fr     |
      | en     | zh     |
      | ar     | en     |
      | ar     | ur     |

  Scenario Outline: Each translated context has the correct target language code
    Given the translated context from "<source>" to "<target>" exists
    Then the context language code should be "<target>"

    Examples:
      | source | target |
      | fr     | en     |
      | fr     | ar     |
      | en     | fr     |
      | ar     | en     |
      | ar     | ur     |

  Scenario Outline: RTL target decks contain revealjs_direction rtl attribute
    Given the translated adoc from "<source>" to "<target>" exists
    Then the adoc should contain ":revealjs_direction: rtl"

    Examples:
      | source | target |
      | fr     | ar     |
      | fr     | ur     |
      | en     | ar     |
      | en     | ur     |
      | ar     | ur     |

  Scenario Outline: LTR target decks from RTL source do not contain revealjs_direction rtl
    Given the translated adoc from "<source>" to "<target>" exists
    Then the adoc should not contain ":revealjs_direction: rtl"

    Examples:
      | source | target |
      | ar     | en     |
      | ar     | fr     |
      | ar     | zh     |
      | ar     | es     |

  Scenario: Total translated files count across all 3 sources
    Then there should be 27 translated adoc files total
    And there should be 27 translated context files total