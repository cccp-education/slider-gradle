@playwright @decomposition
Feature: Playwright domain decomposition (SLD-6.7)

  As a slider-gradle maintainer
  I want the serveSlides, installPlaywright and visualTest logic extracted into the slider.playwright domain
  So that SliderManager.Tasks registers tasks through a thin Gradle adapter delegating pure logic

  # PlaywrightDir -----------------------------------------------------------------------------

  Scenario: A playwright dir resolves project_src_test_playwright
    When a playwright dir is built from project dir "/tmp/proj"
    Then the playwright dir path should be "/tmp/proj/src/test/playwright"

  Scenario: A playwright dir config path resolves playwright_config_ts
    When a playwright dir is built from project dir "/tmp/proj"
    Then the playwright config path should end with "/src/test/playwright/playwright.config.ts"

  Scenario: A blank project dir is rejected
    When a playwright dir is built with a blank project dir
    Then the playwright dir construction should fail with a validation error

  # ServeCommand -----------------------------------------------------------------------------

  Scenario: A serve command keeps its package name and served dir
    When a serve command is built with package "serve@14" and served dir "/tmp/out"
    Then the serve package name should be "serve@14"
    And the serve served dir should be "/tmp/out"

  Scenario: A serve command rejects a blank package name
    When a serve command is built with a blank package name
    Then the serve command construction should fail with a validation error

  Scenario: A serve command rejects a blank served dir
    When a serve command is built with a blank served dir
    Then the serve command construction should fail with a validation error

  Scenario: A serve command renders its npx args as a single-element list
    When a serve command is built with package "serve@14" and served dir "/tmp/out"
    Then the serve npx args should contain "/tmp/out"

  # InstallCommand ---------------------------------------------------------------------------

  Scenario: The default install command targets the chromium browser
    When the default install command is built
    Then the install binary should be "playwright"
    And the install npx args should be "install,chromium"

  Scenario: A custom install command keeps its browsers
    When an install command is built with browsers "firefox" and "webkit"
    Then the install npx args should be "install,firefox,webkit"

  Scenario: An install command rejects a blank binary
    When an install command is built with a blank binary
    Then the install command construction should fail with a validation error

  Scenario: An install command rejects an empty browser list
    When an install command is built with an empty browser list
    Then the install command construction should fail with a validation error

  # VisualTestCommand ------------------------------------------------------------------------

  Scenario: A visual test command keeps its binary and config path
    When a visual test command is built with binary "playwright" and config path "/tmp/cfg.ts"
    Then the visual test binary should be "playwright"
    And the visual test config path should be "/tmp/cfg.ts"

  Scenario: A visual test command rejects a blank binary
    When a visual test command is built with a blank binary
    Then the visual test construction should fail with a validation error

  Scenario: A visual test command rejects a blank config path
    When a visual test command is built with a blank config path
    Then the visual test construction should fail with a validation error

  Scenario: A visual test command renders its npx args as test --config path
    When a visual test command is built with binary "playwright" and config path "/tmp/cfg.ts"
    Then the visual test npx args should be "test,--config,/tmp/cfg.ts"

  # Task names --------------------------------------------------------------------------------

  Scenario: The playwright task names are stable
    When the playwright task names are read
    Then the serve slides task name should be "serveSlides"
    And the visual test task name should be "visualTest"
    And the install playwright task name should be "installPlaywright"