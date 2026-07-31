@capsule @translation @pipeline
Feature: Translate and generate capsule pipeline (SLD-7)

  As a slider-gradle producer
  I want a single composite task that translates a deck and generates capsule scripts
  So that the multi-language capsule feed pipeline is a one-command operation

  Scenario: The composite task name is stable
    When the translateAndGenerateCapsule task name is read
    Then the task name should be "translateAndGenerateCapsule"

  Scenario: The composite task description mentions both sub-tasks
    When the translateAndGenerateCapsule task description is read
    Then the description should mention "translateDeck"
    And the description should mention "generateCapsule"

  Scenario: The composite task group is slider
    When the translateAndGenerateCapsule task group is read
    Then the task group should be "slider"
