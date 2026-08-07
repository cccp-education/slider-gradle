package slider.playwright

import java.io.File

/**
 * Value object describing the Playwright directory under a consumer project
 * (`<projectDir>/src/test/playwright`).
 *
 * Centralises the path resolution so the adapter [PlaywrightTaskRegistrar] no
 * longer needs a private `Project.playwrightDir()` extension.
 */
data class PlaywrightDir(val projectDir: File) {
    init {
        require(projectDir.path.isNotBlank()) { "projectDir must not be blank" }
    }

    /**
     * Resolves the concrete Playwright directory file.
     */
    fun asFile(): File = projectDir.resolve("src").resolve("test").resolve("playwright")

    /**
     * Resolves the absolute path of the `playwright.config.ts` file used as
     * the `--config` argument of the `playwright test` command.
     */
    fun configPath(): String = asFile().resolve("playwright.config.ts").absolutePath
}

/**
 * Value object describing the `npx serve` command used by the `serveSlides`
 * task.
 *
 * @property packageName  npm package spec executed via npx (e.g. `serve@14`)
 * @property servedDir     directory served by the `serve` package
 */
data class ServeCommand(
    val packageName: String,
    val servedDir: File,
) {
    init {
        require(packageName.isNotBlank()) { "packageName must not be blank" }
        require(servedDir.path.isNotBlank()) { "servedDir must not be blank" }
    }

    /**
     * Renders the npx args passed to `NpxTask.args` — a single-element list
     * containing the absolute path of the served directory.
     */
    fun npxArgs(): List<String> = listOf(servedDir.absolutePath)
}

/**
 * Value object describing the `npx playwright install` command used by the
 * `installPlaywright` task.
 *
 * @property binary   npm binary to execute (always `playwright`)
 * @property browsers list of browsers to install (e.g. `["chromium"]`)
 */
data class InstallCommand(
    val binary: String,
    val browsers: List<String>,
) {
    init {
        require(binary.isNotBlank()) { "binary must not be blank" }
        require(browsers.isNotEmpty()) { "browsers must not be empty" }
    }

    companion object {

        /**
         * Default install command — installs only the chromium browser.
         */
        val DEFAULT: InstallCommand = InstallCommand(
            binary = "playwright",
            browsers = listOf("chromium"),
        )
    }

    /**
     * Renders the npx args passed to `NpxTask.args` — `install <browsers>`.
     */
    fun npxArgs(): List<String> = listOf("install") + browsers
}

/**
 * Value object describing the `npx playwright test` command used by the
 * `visualTest` task.
 *
 * @property binary     npm binary to execute (always `playwright`)
 * @property configPath absolute path of the `playwright.config.ts` file
 */
data class VisualTestCommand(
    val binary: String,
    val configPath: String,
) {
    init {
        require(binary.isNotBlank()) { "binary must not be blank" }
        require(configPath.isNotBlank()) { "configPath must not be blank" }
    }

    /**
     * Renders the npx args passed to `NpxTask.args` —
     * `test --config <configPath>`.
     */
    fun npxArgs(): List<String> = listOf("test", "--config", configPath)
}

/**
 * Stable task names owned by the `slider.playwright` domain.
 *
 * Kept here so the adapter [PlaywrightTaskRegistrar] no longer imports the
 * `RevealJsSlides` constants just for these three task identifiers.
 */
object PlaywrightTaskNames {
    const val SERVE_SLIDES = "serveSlides"
    const val VISUAL_TEST = "visualTest"
    const val INSTALL_PLAYWRIGHT = "installPlaywright"
}