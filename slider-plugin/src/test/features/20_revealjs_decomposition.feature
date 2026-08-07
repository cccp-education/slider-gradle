@revealjs @decomposition
Feature: Reveal.js domain decomposition (SLD-6.6)

  As a slider-gradle maintainer
  I want the asciidoctorRevealJs, cleanBuild and generateDashboard logic extracted into the slider.revealjs domain
  So that SliderManager.Tasks registers tasks through a thin Gradle adapter delegating pure logic

  # RevealJsAttributesSpec -------------------------------------------------------------------

  Scenario: The default attributes spec declares fourteen rendering attributes
    When the default attributes spec is built
    Then the spec should declare 14 attributes

  Scenario: The default attributes spec uses the coderay source highlighter
    When the default attributes spec is built
    Then the attribute "source-highlighter" should be "coderay"

  Scenario: The default attributes spec sets the talaria custom css
    When the default attributes spec is built
    Then the attribute "revealjs_customcss" should be "talaria.css"

  Scenario: The default attributes spec uses the black revealjs theme
    When the default attributes spec is built
    Then the attribute "revealjs_theme" should be "black"

  Scenario: A custom attributes spec overrides the defaults
    When a custom attributes spec overrides the default with "revealjs_theme" set to "white"
    Then the attribute "revealjs_theme" should be "white"
    And the attribute "source-highlighter" should be "coderay"

  Scenario: An attributes spec rejects a whitespace-only value
    When an attributes spec is built with "revealjs_theme" set to "   "
    Then the spec construction should fail with a validation error

  # RevealJsOutputDir -------------------------------------------------------------------------

  Scenario: An output dir resolves build_dir docs asciidocRevealJs
    When a revealjs output dir is built from build dir "/tmp/proj/build"
    Then the output dir path should be "/tmp/proj/build/docs/asciidocRevealJs"

  # SlideSourceDir ----------------------------------------------------------------------------

  Scenario: A slide source dir resolves project_slides_misc
    When a slide source dir is built from project dir "/tmp/proj"
    Then the source dir path should be "/tmp/proj/slides/misc"

  # SlideMetadata -----------------------------------------------------------------------------

  Scenario: A slide metadata keeps name and filename
    When a slide metadata is built with name "intro" filename "intro.html"
    Then the slide name should be "intro"
    And the slide filename should be "intro.html"

  Scenario: A blank slide name is rejected
    When a slide metadata is built with a blank name
    Then the slide construction should fail with a validation error

  # SlideMetadataScanner ---------------------------------------------------------------------

  Scenario: The scanner returns an empty list for a missing directory
    When the scanner scans a missing directory
    Then the scanner should return an empty list

  # DashboardJsonSerializer -------------------------------------------------------------------

  Scenario: The dashboard serializer renders an empty array for no slides
    When the dashboard serializer serialises no slides
    Then the json payload should be "[]"

  Scenario: The dashboard serializer renders a single slide
    When the dashboard serializer serialises a slide named "intro"
    Then the json payload should contain "intro"
    And the json payload should contain "intro.html"

  # CleanBuildTarget --------------------------------------------------------------------------

  Scenario: A clean build target reports no deletions for a missing output dir
    When a clean build target collects a missing output dir
    Then the cleaned count should be 0
    And the slides json should not be reported as deleted