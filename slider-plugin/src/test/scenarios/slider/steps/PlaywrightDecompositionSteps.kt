package slider.steps

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.playwright.InstallCommand
import slider.playwright.PlaywrightDir
import slider.playwright.PlaywrightTaskNames
import slider.playwright.ServeCommand
import slider.playwright.VisualTestCommand
import java.io.File

class PlaywrightDecompositionSteps : En {

    private var playwrightDir: PlaywrightDir? = null
    private var playwrightDirError: Throwable? = null
    private var serveCommand: ServeCommand? = null
    private var serveError: Throwable? = null
    private var installCommand: InstallCommand? = null
    private var installError: Throwable? = null
    private var visualCommand: VisualTestCommand? = null
    private var visualError: Throwable? = null

    init {

        // -------------------------------------------------------------------------
        // PlaywrightDir
        // -------------------------------------------------------------------------

        When("a playwright dir is built from project dir {string}") { projectDir: String ->
            playwrightDir = PlaywrightDir(File(projectDir))
            playwrightDirError = null
        }

        When("a playwright dir is built with a blank project dir") {
            try {
                PlaywrightDir(File(""))
            } catch (e: Throwable) {
                playwrightDirError = e
            }
        }

        Then("the playwright dir path should be {string}") { expected: String ->
            assertThat(playwrightDir?.asFile()?.path).isEqualTo(expected)
        }

        Then("the playwright config path should end with {string}") { expected: String ->
            assertThat(playwrightDir?.configPath()).endsWith(expected)
        }

        Then("the playwright dir construction should fail with a validation error") {
            assertThat(playwrightDirError)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        // -------------------------------------------------------------------------
        // ServeCommand
        // -------------------------------------------------------------------------

        When("a serve command is built with package {string} and served dir {string}") {
            packageName: String, servedDir: String ->
            serveCommand = ServeCommand(packageName = packageName, servedDir = File(servedDir))
            serveError = null
        }

        When("a serve command is built with a blank package name") {
            try {
                ServeCommand(packageName = "", servedDir = File("/tmp/out"))
            } catch (e: Throwable) {
                serveError = e
            }
        }

        When("a serve command is built with a blank served dir") {
            try {
                ServeCommand(packageName = "serve", servedDir = File(""))
            } catch (e: Throwable) {
                serveError = e
            }
        }

        Then("the serve package name should be {string}") { expected: String ->
            assertThat(serveCommand?.packageName).isEqualTo(expected)
        }

        Then("the serve served dir should be {string}") { expected: String ->
            assertThat(serveCommand?.servedDir?.path).isEqualTo(expected)
        }

        Then("the serve npx args should contain {string}") { expected: String ->
            assertThat(serveCommand?.npxArgs()).contains(expected)
        }

        Then("the serve command construction should fail with a validation error") {
            assertThat(serveError)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        // -------------------------------------------------------------------------
        // InstallCommand
        // -------------------------------------------------------------------------

        When("the default install command is built") {
            installCommand = InstallCommand.DEFAULT
            installError = null
        }

        When("an install command is built with browsers {string} and {string}") {
            b1: String, b2: String ->
            installCommand = InstallCommand(binary = "playwright", browsers = listOf(b1, b2))
            installError = null
        }

        When("an install command is built with a blank binary") {
            try {
                InstallCommand(binary = "", browsers = listOf("chromium"))
            } catch (e: Throwable) {
                installError = e
            }
        }

        When("an install command is built with an empty browser list") {
            try {
                InstallCommand(binary = "playwright", browsers = emptyList())
            } catch (e: Throwable) {
                installError = e
            }
        }

        Then("the install binary should be {string}") { expected: String ->
            assertThat(installCommand?.binary).isEqualTo(expected)
        }

        Then("the install npx args should be {string}") { expected: String ->
            val expectedList = expected.split(",")
            assertThat(installCommand?.npxArgs()).isEqualTo(expectedList)
        }

        Then("the install command construction should fail with a validation error") {
            assertThat(installError)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        // -------------------------------------------------------------------------
        // VisualTestCommand
        // -------------------------------------------------------------------------

        When("a visual test command is built with binary {string} and config path {string}") {
            binary: String, configPath: String ->
            visualCommand = VisualTestCommand(binary = binary, configPath = configPath)
            visualError = null
        }

        When("a visual test command is built with a blank binary") {
            try {
                VisualTestCommand(binary = "", configPath = "/tmp/cfg.ts")
            } catch (e: Throwable) {
                visualError = e
            }
        }

        When("a visual test command is built with a blank config path") {
            try {
                VisualTestCommand(binary = "playwright", configPath = "")
            } catch (e: Throwable) {
                visualError = e
            }
        }

        Then("the visual test binary should be {string}") { expected: String ->
            assertThat(visualCommand?.binary).isEqualTo(expected)
        }

        Then("the visual test config path should be {string}") { expected: String ->
            assertThat(visualCommand?.configPath).isEqualTo(expected)
        }

        Then("the visual test npx args should be {string}") { expected: String ->
            val expectedList = expected.split(",")
            assertThat(visualCommand?.npxArgs()).isEqualTo(expectedList)
        }

        Then("the visual test construction should fail with a validation error") {
            assertThat(visualError)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        // -------------------------------------------------------------------------
        // Task names
        // -------------------------------------------------------------------------

        When("the playwright task names are read") {
            // No-op — constants read in the Then steps
        }

        Then("the serve slides task name should be {string}") { expected: String ->
            assertThat(PlaywrightTaskNames.SERVE_SLIDES).isEqualTo(expected)
        }

        Then("the visual test task name should be {string}") { expected: String ->
            assertThat(PlaywrightTaskNames.VISUAL_TEST).isEqualTo(expected)
        }

        Then("the install playwright task name should be {string}") { expected: String ->
            assertThat(PlaywrightTaskNames.INSTALL_PLAYWRIGHT).isEqualTo(expected)
        }
    }
}