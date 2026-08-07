@wiring @decomposition
Feature: Wiring domain decomposition (SLD-6.5)

  As a slider-gradle maintainer
  I want the Repositories, Plugins, and Dependencies logic extracted into the slider.wiring domain
  So that SliderManager nested objects become thin Gradle adapters delegating pure logic

  # GroupRouting ------------------------------------------------------------------------------

  Scenario: An include group routing keeps its group name
    When an include group routing is built for "rubygems"
    Then the routing group should be "rubygems"

  Scenario: An exclude group routing keeps its group name
    When an exclude group routing is built for "rubygems"
    Then the routing group should be "rubygems"

  Scenario: A blank include group is rejected
    When an include group routing is built with a blank group
    Then the routing construction should fail with a validation error

  Scenario: A blank exclude group is rejected
    When an exclude group routing is built with a blank group
    Then the routing construction should fail with a validation error

  # RepositorySpec ---------------------------------------------------------------------------

  Scenario: A Maven repository spec keeps its url and routing
    When a Maven repository spec is built with url "https://plugins.gradle.org/m2/" and no routing
    Then the repository url should be "https://plugins.gradle.org/m2/"
    And the repository kind should be "MAVEN"

  Scenario: A MavenCentral repository spec has no url
    When a MavenCentral repository spec is built excluding "rubygems"
    Then the repository url should be null
    And the repository kind should be "MAVEN_CENTRAL"

  Scenario: A Maven repository rejects a blank url
    When a Maven repository spec is built with a blank url
    Then the repository construction should fail with a validation error

  Scenario: A MavenCentral repository rejects a non-null url
    When a MavenCentral repository spec is built with url "https://example.com"
    Then the repository construction should fail with a validation error

  # PluginSpec -------------------------------------------------------------------------------

  Scenario: A plugin spec keeps its id
    When a plugin spec is built with id "com.github.node-gradle.node"
    Then the plugin id should be "com.github.node-gradle.node"

  Scenario: A blank plugin id is rejected
    When a plugin spec is built with a blank id
    Then the plugin construction should fail with a validation error

  # GemDependency ----------------------------------------------------------------------------

  Scenario: A gem dependency renders its ivy coordinates
    When a gem dependency is built with group "rubygems" name "asciidoctor-revealjs" version "5.2.0" classifier "gem"
    Then the gem coordinates should be "rubygems:asciidoctor-revealjs:5.2.0@gem"

  Scenario: A gem dependency rejects a blank name
    When a gem dependency is built with a blank name
    Then the gem construction should fail with a validation error

  # WiringSpec DEFAULT -----------------------------------------------------------------------

  Scenario: The default wiring spec declares six repositories
    When the default wiring spec is built
    Then the spec should declare 6 repositories

  Scenario: The default wiring spec declares three plugins
    When the default wiring spec is built
    Then the spec should declare 3 plugins
    And the first plugin id should be "com.github.node-gradle.node"

  Scenario: The default wiring spec declares one gem dependency
    When the default wiring spec is built
    Then the spec should declare 1 gem

  Scenario: The default wiring spec routes rubygems with both include and exclude groups
    When the default wiring spec is built
    Then the spec should include the "rubygems" group on at least one repository
    And the spec should exclude the "rubygems" group on at least one repository

  Scenario: The default wiring spec targets the rubygems Ivy repository last
    When the default wiring spec is built
    Then the last repository should be an Ivy repository
    And the last repository url should be "https://rubygems.org/gems/"