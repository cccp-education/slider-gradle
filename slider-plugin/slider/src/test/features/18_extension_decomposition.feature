@extension @decomposition
Feature: Extension domain decomposition (SLD-6.4)

  As a slider-gradle maintainer
  I want the Extensions logic extracted into the slider.extension domain
  So that SliderManager.Extensions becomes a thin Gradle adapter delegating pure logic

  # RevealJsPin -------------------------------------------------------------------------------

  Scenario: The default Reveal.js pin targets tag 5.2.1
    When the default Reveal.js pin is built
    Then the pin tag should be "5.2.1"

  Scenario: The default Reveal.js pin points to the hakimel organisation
    When the default Reveal.js pin is built
    Then the pin organisation should be "hakimel"

  Scenario: The default Reveal.js pin points to the reveal.js repository
    When the default Reveal.js pin is built
    Then the pin repository should be "reveal.js"

  Scenario: The default Reveal.js pin sets the gem version to 5.2.0
    When the default Reveal.js pin is built
    Then the pin version should be "5.2.0"

  Scenario: A blank version is rejected
    When a Reveal.js pin is built with a blank version
    Then the pin construction should fail with a validation error

  Scenario: A blank tag is rejected
    When a Reveal.js pin is built with a blank tag
    Then the pin construction should fail with a validation error

  Scenario: A blank organisation is rejected
    When a Reveal.js pin is built with a blank organisation
    Then the pin construction should fail with a validation error

  Scenario: A blank repository is rejected
    When a Reveal.js pin is built with a blank repository
    Then the pin construction should fail with a validation error

  Scenario: A custom pin preserves all supplied values
    When a Reveal.js pin is built with version "6.0.0" organisation "custom-org" repository "custom-repo" tag "6.0.0"
    Then the pin version should be "6.0.0"
    And the pin organisation should be "custom-org"
    And the pin repository should be "custom-repo"
    And the pin tag should be "6.0.0"