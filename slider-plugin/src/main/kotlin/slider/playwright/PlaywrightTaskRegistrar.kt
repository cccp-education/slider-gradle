package slider.playwright

import com.github.gradle.node.npm.task.NpxTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import slider.i18n.SliderMessages
import slider.revealjs.RevealJsOutputDir
import slider.revealjs.RevealJsTaskRegistrar
import slider.Slides.Serve.SERVE_DEP
import slider.Slides.RevealJsSlides.GROUP_TASK_SLIDER

/**
 * Thin Gradle adapter that registers the three Playwright-related tasks
 * previously declared in `SliderManager.Tasks`:
 *
 * - `serveSlides`        → serves the generated slides via `npx serve`
 * - `installPlaywright`  → installs the Playwright browsers via
 *   `npx playwright install`
 * - `visualTest`         → runs the Playwright visual snapshot tests via
 *   `npx playwright test`
 *
 * Pure command data lives in the `slider.playwright` domain value objects
 * ([ServeCommand], [InstallCommand], [VisualTestCommand], [PlaywrightDir]);
 * this object only translates that data into Gradle `NpxTask` declarations.
 */
object PlaywrightTaskRegistrar {

    /**
     * Registers the three Playwright tasks on [project], wired in the
     * dependency order: `serveSlides` and `visualTest` both depend on the
     * `asciidoctorRevealJs` task; `installPlaywright` is standalone.
     */
    fun register(project: Project) {
        registerServeSlidesTask(project)
        registerInstallPlaywrightTask(project)
        registerVisualTestTask(project)
    }

    /**
     * Registers the `serveSlides` task — serves the generated slides via
     * `npx <SERVE_DEP> <outputDir>` using [ServeCommand].
     */
    fun registerServeSlidesTask(project: Project) {
        val outputDir = RevealJsOutputDir(project.layout.buildDirectory.get().asFile).asFile()
        val serveCommand = ServeCommand(
            packageName = SERVE_DEP,
            servedDir = outputDir,
        )
        val lang = SliderMessages.resolveLanguage(project)
        project.tasks.register<NpxTask>(PlaywrightTaskNames.SERVE_SLIDES) {
            group = SliderMessages.get("task.group.info", lang)
            description = SliderMessages.get("task.serveSlides.description", lang)
            dependsOn(RevealJsTaskRegistrar.TASK_ASCIIDOCTOR_REVEALJS)
            command.set(serveCommand.packageName)
            args.set(serveCommand.npxArgs())
            workingDir.set(project.layout.projectDirectory.asFile)
            doFirst { println(SliderMessages.get("task.serveSlides.serving", lang)) }
        }
    }

    /**
     * Registers the `installPlaywright` task — installs the Playwright
     * browsers via `npx playwright install <browsers>` using
     * [InstallCommand.DEFAULT].
     */
    fun registerInstallPlaywrightTask(project: Project) {
        val playwrightDir = PlaywrightDir(project.layout.projectDirectory.asFile)
        val installCommand = InstallCommand.DEFAULT
        val lang = SliderMessages.resolveLanguage(project)
        project.tasks.register<NpxTask>(PlaywrightTaskNames.INSTALL_PLAYWRIGHT) {
            group = SliderMessages.get("task.group.setup", lang)
            description = SliderMessages.get("task.installPlaywright.description", lang)
            command.set(installCommand.binary)
            args.set(installCommand.npxArgs())
            workingDir.set(project.file(playwrightDir.asFile()))
        }
    }

    /**
     * Registers the `visualTest` task — runs the Playwright visual snapshot
     * tests via `npx playwright test --config <configPath>` using
     * [VisualTestCommand].
     */
    fun registerVisualTestTask(project: Project) {
        val playwrightDir = PlaywrightDir(project.layout.projectDirectory.asFile)
        val visualCommand = VisualTestCommand(
            binary = "playwright",
            configPath = playwrightDir.configPath(),
        )
        val lang = SliderMessages.resolveLanguage(project)
        project.tasks.register<NpxTask>(PlaywrightTaskNames.VISUAL_TEST) {
            group = GROUP_TASK_SLIDER
            description = SliderMessages.get("task.visualTest.description", lang)
            dependsOn(RevealJsTaskRegistrar.TASK_ASCIIDOCTOR_REVEALJS)
            command.set(visualCommand.binary)
            args.set(visualCommand.npxArgs())
            workingDir.set(project.file(playwrightDir.asFile()))
        }
    }
}