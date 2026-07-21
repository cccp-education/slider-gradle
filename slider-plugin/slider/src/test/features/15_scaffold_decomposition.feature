@scaffold @decomposition
Feature: Scaffold domain decomposition (SLD-6.1)

  As a slider-gradle maintainer
  I want the Scaffold logic extracted into the slider.scaffold domain
  So that SliderManager.Scaffold becomes a thin Gradle adapter delegating pure logic

  # SlidesScaffolder.isSlidesConfigComplete ----------------------------------------------------

  Scenario: An empty misc directory is not a complete slides configuration
    Given an empty slides misc directory
    When the slides configuration completeness is checked
    Then the configuration should be incomplete

  Scenario: A misc directory without index html is not complete
    Given a slides misc directory with a deck file "intro-deck.adoc" but no index html
    When the slides configuration completeness is checked
    Then the configuration should be incomplete

  Scenario: A misc directory without any deck adoc file is not complete
    Given a slides misc directory with index html but no deck adoc file
    When the slides configuration completeness is checked
    Then the configuration should be incomplete

  Scenario: A misc directory with index html and one deck adoc file is complete
    Given a slides misc directory with index html and a deck file "intro-deck.adoc"
    When the slides configuration completeness is checked
    Then the configuration should be complete

  Scenario: A misc directory with index html and several deck adoc files is complete
    Given a slides misc directory with index html and 3 deck files
    When the slides configuration completeness is checked
    Then the configuration should be complete

  Scenario: A directory named like a deck adoc is not a deck file
    Given a slides misc directory with index html and a directory named "fake-deck.adoc"
    When the slides configuration completeness is checked
    Then the configuration should be incomplete

  # SlidesScaffolder.extractSlidesZip --------------------------------------------------------

  Scenario: Extracting a non-empty slides zip creates the expected files
    Given a slides zip containing "slides/misc/index.html" and "slides/misc/intro-deck.adoc"
    When the zip is extracted into a target project directory
    Then the extraction result should be Created
    And the target directory should contain "slides/misc/index.html"
    And the target directory should contain "slides/misc/intro-deck.adoc"

  Scenario: Extracting a slides zip with directory entries skips them
    Given a slides zip containing directory entries and the file "slides/misc/index.html"
    When the zip is extracted into a target project directory
    Then the extraction result should be Created
    And the target directory should contain "slides/misc/index.html"

  Scenario: Extracting an empty slides zip reports a failure
    Given an empty slides zip
    When the zip is extracted into a target project directory
    Then the extraction result should be Failed

  # ScaffoldDefaults ------------------------------------------------------------------------

  Scenario: The default SlidesConfiguration has placeholder git push values
    When the default SlidesConfiguration is built
    Then the source path should be "docs/asciidocRevealJs"
    And the push from should be "build/docs/asciidocRevealJs"
    And the push to should be "build/slides-repo"
    And the push branch should be "main"
    And the push message should be "deploy slides"
    And the repo name should be "slides"
    And the repo url should be "https://github.com/your-org/your-slides-repo.git"
    And the credentials username should be "your-username"
    And the credentials password should be "your-token"

  Scenario: The default SlidesConfiguration has placeholder AI provider keys
    When the default SlidesConfiguration is built
    Then the gemini key list should contain "your-gemini-api-key"
    And the mistral key list should contain "your-mistral-api-key"
    And the huggingface key list should contain "your-huggingface-api-key"

  Scenario: The default DeckContext has placeholder presentation values
    When the default DeckContext is built
    Then the subject should be "Your presentation subject"
    And the audience should be "Your target audience"
    And the duration should be 45
    And the language code should be "fr"
    And the output file should be "example-deck.adoc"
    And the author name should be "Your Name"
    And the author email should be "your.email@example.com"

  Scenario: The default DeckContext exposes a sky revealjs theme
    When the default DeckContext is built
    Then the revealjs theme should be "sky"
    And the revealjs slide number should be "c/t"
    And the revealjs width should be 1408
    And the revealjs height should be 792
    And the revealjs controls should be enabled
    And the revealjs controls layout should be "edges"
    And the revealjs history should be enabled
    And the revealjs fragment in URL should be enabled

  Scenario: The default DeckContext includes detailed notes
    When the default DeckContext is built
    Then the speaker notes should be enabled
    And the page notes should be enabled
    And the page notes style should be DETAILED

  Scenario: The default DeckContext ships with three starter slides
    When the default DeckContext is built
    Then the slides count should be 3
    And the slide 1 title should be "Agenda"
    And the slide 2 title should be "First Topic"
    And the slide 3 title should be "Summary and Next Steps"