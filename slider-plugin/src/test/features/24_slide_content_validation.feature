@content @validation
Feature: Slide content validation (SLD-10.6)

  The slider.content domain validates that a slide renders correctly.
  Two assertion strategies coexist, sharing the same four assertion codes:

  - ContentSlideAssertion  — a priori estimation from SlideContent + SlideLayout
                             (parsed from the .adoc source, no real rendering).
  - ContentRenderAssertion — a posteriori measurement from ContentRenderData
                             (a snapshot DTO extracted from the rendered DOM).

  Both return a ContentAssertionResult (sealed Passed / Failed) and reuse
  the same ContentAssertionCode enum:
    P0_OVERFLOW      — text overflows the slide content area or viewport.
    P0_MISSING_NOTES — the slide has no speaker note (a priori) or the note
                       is not rendered in the DOM (a posteriori).
    P1_FONT_SIZE     — the body font size is below the readable minimum (14px).
    P1_CONTRAST      — the title/body font ratio is too flat (a priori) or the
                       computed WCAG contrast ratio is below 4.5 (a posteriori).

  # --- A priori: ContentSlideAssertion -------------------------------------

  Scenario: A valid slide passes all a priori assertions
    Given a slide content with title "Kotlin Coroutines" and a speaker note "Introduce coroutines"
    And a slide layout with viewport 1280x720, margin 40, title font 42, body font 18
    When the content slide assertion is evaluated
    Then all content assertions should pass

  Scenario: A slide without speaker note fails P0_MISSING_NOTES a priori
    Given a slide content with title "Kotlin Coroutines" and no speaker note
    And a slide layout with viewport 1280x720, margin 40, title font 42, body font 18
    When the content slide assertion is evaluated
    Then the content assertion "P0_MISSING_NOTES" should fail

  Scenario: A title too long fails P0_OVERFLOW a priori
    Given a slide content with title "This is a very long title that will overflow the slide content area" and a speaker note "note"
    And a slide layout with viewport 1280x720, margin 40, title font 42, body font 18
    When the content slide assertion is evaluated
    Then the content assertion "P0_OVERFLOW" should fail

  Scenario: A body font size below 14 fails P1_FONT_SIZE a priori
    Given a slide content with title "Kotlin Coroutines" and a speaker note "note"
    And a slide layout with viewport 1280x720, margin 40, title font 42, body font 12
    When the content slide assertion is evaluated
    Then the content assertion "P1_FONT_SIZE" should fail

  Scenario: A flat title/body ratio fails P1_CONTRAST a priori
    Given a slide content with title "Kotlin Coroutines" and a speaker note "note"
    And a slide layout with viewport 1280x720, margin 40, title font 18, body font 16
    When the content slide assertion is evaluated
    Then the content assertion "P1_CONTRAST" should fail

  # --- A posteriori: ContentRenderAssertion --------------------------------

  Scenario: A valid render snapshot passes all a posteriori assertions
    Given a render snapshot with title "Kotlin Coroutines", body font 18, title font 42, contrast 7.0, notes in DOM "true", viewport 1280x720
    When the content render assertion is evaluated
    Then all content render assertions should pass

  Scenario: A snapshot without notes in DOM fails P0_MISSING_NOTES a posteriori
    Given a render snapshot with title "Kotlin Coroutines", body font 18, title font 42, contrast 7.0, notes in DOM "false", viewport 1280x720
    When the content render assertion is evaluated
    Then the content render assertion "P0_MISSING_NOTES" should fail

  Scenario: A text block overflowing the viewport fails P0_OVERFLOW a posteriori
    Given a render snapshot with title "Kotlin Coroutines", body font 18, title font 42, contrast 7.0, notes in DOM "true", viewport 1280x720
    And a text block at x 0, y 0, width 1400, height 18
    When the content render assertion is evaluated
    Then the content render assertion "P0_OVERFLOW" should fail

  Scenario: A computed body font below 14 fails P1_FONT_SIZE a posteriori
    Given a render snapshot with title "Kotlin Coroutines", body font 12, title font 42, contrast 7.0, notes in DOM "true", viewport 1280x720
    When the content render assertion is evaluated
    Then the content render assertion "P1_FONT_SIZE" should fail

  Scenario: A contrast ratio below 4.5 fails P1_CONTRAST a posteriori
    Given a render snapshot with title "Kotlin Coroutines", body font 18, title font 42, contrast 3.0, notes in DOM "true", viewport 1280x720
    When the content render assertion is evaluated
    Then the content render assertion "P1_CONTRAST" should fail

  # --- Cross-validation ----------------------------------------------------

  Scenario: A slide valid a priori and a posteriori passes both assertions
    Given a slide content with title "Kotlin Coroutines" and a speaker note "Introduce coroutines"
    And a slide layout with viewport 1280x720, margin 40, title font 42, body font 18
    And a render snapshot with title "Kotlin Coroutines", body font 18, title font 42, contrast 7.0, notes in DOM "true", viewport 1280x720
    When the content slide assertion is evaluated
    And the content render assertion is evaluated
    Then all content assertions should pass
    And all content render assertions should pass