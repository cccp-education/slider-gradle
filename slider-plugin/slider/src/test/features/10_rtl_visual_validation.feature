@i18n @rtl @visual
Feature: RTL visual validation for Reveal.js decks

  The RtlSlideAssertion domain validates that a rendered slide deck
  is correctly laid out in Right-To-Left mode. It checks the Reveal.js
  RTL config, the runtime .rtl class, the mirrored navigation, and the
  viewport overflow.

  Scenario: Full RTL deck passes all assertions
    Given a slide render data with rtl config "true", rtl class "true", nav next left "true", viewport 1280, slide x 0, slide width 1280
    Then all RTL assertions should pass

  Scenario: Missing RTL config fails P0-RTL-CONFIG
    Given a slide render data with rtl config "false", rtl class "true", nav next left "true", viewport 1280, slide x 0, slide width 1280
    Then the RTL assertion "P0_RTL_CONFIG" should fail

  Scenario: Missing RTL class fails P0-RTL-CLASS
    Given a slide render data with rtl config "true", rtl class "false", nav next left "true", viewport 1280, slide x 0, slide width 1280
    Then the RTL assertion "P0_RTL_CLASS" should fail

  Scenario: Navigation not mirrored fails P0-NAV
    Given a slide render data with rtl config "true", rtl class "true", nav next left "false", viewport 1280, slide x 0, slide width 1280
    Then the RTL assertion "P0_NAV" should fail

  Scenario: Slide overflowing viewport fails P1-OVERFLOW
    Given a slide render data with rtl config "true", rtl class "true", nav next left "true", viewport 1280, slide x -50, slide width 1400
    Then the RTL assertion "P1_OVERFLOW" should fail

  Scenario: LTR deck fails all P0 assertions
    Given a slide render data with rtl config "false", rtl class "false", nav next left "false", viewport 1280, slide x 0, slide width 1280
    Then the RTL assertion "P0_RTL_CONFIG" should fail
    And the RTL assertion "P0_RTL_CLASS" should fail
    And the RTL assertion "P0_NAV" should fail