@deployment @repository @publish
Feature: Publish task decomposition (SLD-6.9)

  As a slider-gradle maintainer
  I want the publishSlides task and Git object extracted into the slider.repository domain
  So that SliderManager.Tasks delegates publish registration to PublishTaskRegistrar

  Scenario: The deploySlides task is registered in the deploy group
    Given a Gradle project with the slider plugin applied
    When the project tasks are listed
    Then the task "deploySlides" should be in the "deploy" group

  Scenario: The deploySlides task depends on asciidoctor
    Given a Gradle project with the slider plugin applied
    When the project tasks are listed
    Then the task "deploySlides" should depend on "asciidoctor"

  Scenario: The SlidePublisher orchestrates the full pipeline
    Given a slides directory with 2 files
    And a repo directory path "repo"
    And a bare git remote is available
    And a branch "gh-pages"
    And a commit message "deploy slides"
    And credentials "user" and "token"
    When the repo directory is created
    And the slides are copied
    And the slides are committed via JGitSlidePusher
    And the slides are pushed via JGitSlidePusher
    Then the push should succeed
