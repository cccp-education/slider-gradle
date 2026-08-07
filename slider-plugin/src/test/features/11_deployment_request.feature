@deployment @repository
Feature: Slide deployment request validation

  As a slider-gradle producer
  I want to validate the deployment request before pushing to Git
  So that misconfiguration fails fast with a clear message

  Scenario: A valid request with all fields populated is accepted
    Given a slides directory with 2 files
    And a repo directory path "repo"
    And a remote url "https://github.com/org/slides.git"
    And a branch "main"
    And a commit message "deploy slides"
    And credentials "user" and "token"
    When the deployment request is created
    Then the request should be valid
    And the request credentials should have username "user"
    And the request credentials should have password "token"

  Scenario: A blank remote url is rejected
    Given a slides directory with 1 file
    And a repo directory path "repo"
    And a remote url "  "
    And a branch "main"
    And a commit message "deploy slides"
    And credentials "user" and "token"
    When the deployment request creation is attempted
    Then the creation should fail with a message containing "remoteUrl"

  Scenario: A blank branch is rejected
    Given a slides directory with 1 file
    And a repo directory path "repo"
    And a remote url "https://github.com/org/slides.git"
    And a branch ""
    And a commit message "deploy slides"
    And credentials "user" and "token"
    When the deployment request creation is attempted
    Then the creation should fail with a message containing "branch"

  Scenario: A blank commit message is rejected
    Given a slides directory with 1 file
    And a repo directory path "repo"
    And a remote url "https://github.com/org/slides.git"
    And a branch "main"
    And a commit message ""
    And credentials "user" and "token"
    When the deployment request creation is attempted
    Then the creation should fail with a message containing "commitMessage"

  Scenario: Blank credentials are rejected
    Given a slides directory with 1 file
    And a repo directory path "repo"
    And a remote url "https://github.com/org/slides.git"
    And a branch "main"
    And a commit message "deploy"
    And credentials "" and "token"
    When the deployment request creation is attempted
    Then the creation should fail with a message containing "username"

  Scenario: A non-existent slides directory is rejected
    Given a non-existent slides directory
    And a repo directory path "repo"
    And a remote url "https://github.com/org/slides.git"
    And a branch "main"
    And a commit message "deploy"
    And credentials "user" and "token"
    When the deployment request creation is attempted
    Then the creation should fail with a message containing "slidesDir"

  Scenario: Creating a repo directory succeeds when none exists
    Given a slides directory with 1 file
    And a repo directory path "fresh-repo"
    When the repo directory is created
    Then the repo directory should exist and be empty

  Scenario: Creating a repo directory removes an existing directory
    Given a slides directory with 1 file
    And a repo directory path "stale-repo" with a stale file
    When the repo directory is created
    Then the repo directory should exist and be empty

  Scenario: Copying slides into the repo directory succeeds
    Given a slides directory with 2 files
    And a repo directory path "repo"
    When the repo directory is created
    And the slides are copied
    Then the repo directory should contain 2 files

  Scenario: Copying slides from an empty directory fails
    Given a slides directory with 0 files
    And a repo directory path "repo"
    When the repo directory is created
    And the slides are copied
    Then the copy should fail