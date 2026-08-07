@deployment @repository @git
Feature: Git push adapter — JGitSlidePusher

  As a slider-gradle producer
  I want to init, commit, and push slides to a Git remote via the JGitSlidePusher adapter
  So that the domain layer handles Git wire operations without leaking JGit types

  Scenario: Initialising and committing creates a git repo with the configured commit message
    Given a slides directory with 2 files
    And a repo directory path "repo"
    And a remote url "file://localhost/dummy.git"
    And a branch "gh-pages"
    And a commit message "deploy slides"
    And credentials "user" and "token"
    When the repo directory is created
    And the slides are copied
    And the slides are committed via JGitSlidePusher
    Then the commit should succeed
    And the commit message should be "deploy slides"
    And the git branch should be "gh-pages"

  Scenario: Pushing to a local bare remote succeeds after init and commit
    Given a slides directory with 1 file
    And a repo directory path "repo"
    And a bare git remote is available
    And a branch "main"
    And a commit message "initial deploy"
    And credentials "user" and "token"
    When the repo directory is created
    And the slides are copied
    And the slides are committed via JGitSlidePusher
    And the slides are pushed via JGitSlidePusher
    Then the push should succeed

  Scenario: Pushing to a non-existent remote fails with a clear error
    Given a slides directory with 1 file
    And a repo directory path "repo"
    And a remote url "file:///nonexistent/path/to/repo.git"
    And a branch "main"
    And a commit message "deploy"
    And credentials "user" and "token"
    When the repo directory is created
    And the slides are copied
    And the slides are committed via JGitSlidePusher
    And the slides are pushed via JGitSlidePusher
    Then the push should fail with an error message