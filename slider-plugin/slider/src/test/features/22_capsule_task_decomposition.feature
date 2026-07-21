@capsule @decomposition
Feature: Capsule task decomposition (SLD-6.8)

  As a slider-gradle maintainer
  I want the generateCapsule task registration extracted into the slider.capsule domain
  So that SliderManager.Tasks registers the task through a thin Gradle adapter delegating pure logic

  # CapsuleAdocDir ---------------------------------------------------------------------------

  Scenario: A capsule adoc dir resolves project_slides_misc
    When a capsule adoc dir is built from project dir "/tmp/proj"
    Then the capsule adoc dir path should be "/tmp/proj/slides/misc"

  Scenario: A capsule adoc dir rejects a blank project dir
    When a capsule adoc dir is built with a blank project dir
    Then the capsule adoc dir construction should fail with a validation error

  Scenario: A capsule adoc dir lists only adoc files sorted by name
    When a capsule adoc dir is built with adoc files "deck-b.adoc" and "deck-a.adoc" plus a "notes.txt"
    Then the capsule adoc dir should list files "deck-a.adoc,deck-b.adoc"

  Scenario: A capsule adoc dir returns no files when misc is missing
    When a capsule adoc dir is built from a project dir without a slides_misc directory
    Then the capsule adoc dir should list no files

  # CapsuleScriptDir -------------------------------------------------------------------------

  Scenario: A capsule script dir resolves build_capsule
    When a capsule script dir is built from build dir "/tmp/build"
    Then the capsule script dir path should be "/tmp/build/capsule"

  Scenario: A capsule script dir rejects a blank build dir
    When a capsule script dir is built with a blank build dir
    Then the capsule script dir construction should fail with a validation error

  Scenario: A capsule script dir ensureCreated creates the directory
    When a capsule script dir is ensured created from a fresh build dir
    Then the capsule script directory should exist

  Scenario: A capsule script dir scriptFile resolves name_without_extension-script_txt
    When a capsule script dir is built from build dir "/tmp/build"
    Then the capsule script file for deck "kotlin-intro" should be "/tmp/build/capsule/kotlin-intro-script.txt"

  # Task names --------------------------------------------------------------------------------

  Scenario: The capsule task names are stable
    When the capsule task names are read
    Then the generate capsule task name should be "generateCapsule"
    And the capsule task group should be "slider"
    And the capsule task description should mention "speaker notes"
    And the capsule task description should mention "capsule-gradle"