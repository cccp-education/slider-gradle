@prerequisite @decomposition
Feature: Prerequisite domain decomposition (SLD-6.3)

  As a slider-gradle maintainer
  I want the Prerequisites logic extracted into the slider.prerequisite domain
  So that SliderManager.Prerequisites becomes a thin Gradle adapter delegating pure logic

  # JavaVersionGuard.requireJava23 -------------------------------------------------------------

  Scenario: A Java version above the minimum is accepted
    When the guard is checked against major version 24
    Then the guard should pass

  Scenario: A Java version exactly at the minimum is accepted
    When the guard is checked against major version 23
    Then the guard should pass

  Scenario: A Java version below the minimum is rejected with a clear message
    When the guard is checked against major version 22
    Then the guard should fail with message "education.cccp.slider requires Java 23+. Current: Java 22"

  Scenario: A very old Java version is rejected with the current value in the message
    When the guard is checked against major version 17
    Then the guard should fail with message "education.cccp.slider requires Java 23+. Current: Java 17"

  # JavaVersionGuard.requireJava23FromMajor ----------------------------------------------------

  Scenario: A numeric major version string above the minimum is accepted
    When the guard is checked against major version string "25"
    Then the guard should pass

  Scenario: A numeric major version string exactly at the minimum is accepted
    When the guard is checked against major version string "23"
    Then the guard should pass

  Scenario: A numeric major version string below the minimum is rejected
    When the guard is checked against major version string "21"
    Then the guard should fail with message "education.cccp.slider requires Java 23+. Current: Java 21"

  Scenario: A non-numeric major version string is rejected
    When the guard is checked against major version string "foo"
    Then the guard should fail with a version parse error

  Scenario: A blank major version string is rejected
    When the guard is checked against major version string "   "
    Then the guard should fail with a version parse error