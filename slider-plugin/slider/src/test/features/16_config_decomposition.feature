@config @decomposition
Feature: Config domain decomposition (SLD-6.2)

  As a slider-gradle maintainer
  I want the Configuration logic extracted into the slider.config domain
  So that SliderManager.Configuration becomes a thin Gradle adapter delegating pure logic

  # ConfigPath ---------------------------------------------------------------------------

  Scenario: A blank config path is rejected
    Given a blank config path string
    When the ConfigPath is constructed
    Then the construction should fail with a validation error

  Scenario: A non-blank config path is accepted
    Given the config path string "managed_config_path"
    When the ConfigPath is constructed
    Then the ConfigPath value should be "managed_config_path"

  Scenario: A relative path is resolved against the base directory
    Given the config path string "slides-context.yml"
    And the base directory "/tmp/slider"
    When the path is resolved against the base directory
    Then the resolved path should end with "slides-context.yml"

  Scenario: An absolute path under the base directory is returned unchanged
    Given the config path string "/tmp/slider/slides-context.yml"
    And the base directory "/tmp/slider"
    When the path is resolved against the base directory
    Then the resolved path should be "/tmp/slider/slides-context.yml"

  # SlidesConfigLoader -------------------------------------------------------------------

  Scenario: A valid YAML file is loaded into a populated SlidesConfiguration
    Given a slides-context.yml file with a valid configuration
    When the configuration is loaded
    Then the config source path should be "docs/asciidocRevealJs"
    And the config push to should be "build/slides-repo"
    And the config repo name should be "slides"

  Scenario: A missing YAML file falls back to an empty configuration
    Given no slides-context.yml file exists
    When the configuration is loaded
    Then the fallback configuration should be returned
    And the config source path should be empty

  Scenario: A malformed YAML file falls back to an empty configuration
    Given a slides-context.yml file with malformed YAML content
    When the configuration is loaded
    Then the fallback configuration should be returned

  Scenario: Loading by path and base dir resolves a relative path
    Given a slides-context.yml file in the base directory with srcPath "out"
    When the configuration is loaded by path and base dir
    Then the config source path should be "out"

  # YamlMapperFactory --------------------------------------------------------------------

  Scenario: The YAML mapper reads a simple mapping
    Given a YAML mapping with name "slider" and version "0.0.11"
    When the mapping is read by the YAML mapper
    Then the parsed name should be "slider"
    And the parsed version should be "0.0.11"

  Scenario: The YAML mapper supports Kotlin data classes
    Given a YAML mapping with name "Alice" and age 30
    When the mapping is read by the YAML mapper as a Person
    Then the person name should be "Alice"
    And the person age should be 30

  Scenario: The YAML mapper disables WRITE_DATES_AS_TIMESTAMPS
    When the YAML mapper is created
    Then WRITE_DATES_AS_TIMESTAMPS should be disabled