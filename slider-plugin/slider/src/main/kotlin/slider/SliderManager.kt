package slider

import slider.SliderManager.Configuration.CONFIG_PATH_KEY
import slider.SliderManager.Configuration.localConf
import slider.SliderManager.Configuration.yamlMapper
import slider.SliderManager.Git.pushSlides
import slider.SliderManager.Tasks.registerTasks
import slider.Slides.RevealJsSlides.GROUP_TASK_SLIDER
import slider.Slides.RevealJsSlides.TASK_ASCIIDOCTOR_REVEALJS
import slider.Slides.RevealJsSlides.TASK_PUBLISH_SLIDES
import slider.Slides.RevealJsSlides.REVEAL_I18N_OUTPUT_DIR
import slider.Slides.RevealJsSlides.TASK_GENERATE_REVEAL_UI_MESSAGES
import slider.Slides.RevealJsSlides.TASK_TRANSLATE_DECK
import slider.capsule.CapsuleTaskRegistrar
import slider.config.SlidesConfigLoader
import slider.config.YamlMapperFactory
import slider.translation.registerTranslateDeckTask
import slider.Slides.Slide.DEFAULT_SLIDES_FOLDER
import slider.Slides.Slide.SLIDES_CONTEXT_YML
import slider.Slides.Slide.SLIDES_FOLDER
import com.fasterxml.jackson.databind.ObjectMapper
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.gradle.api.DefaultTask
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * Root manager object for the Slider plugin.
 *
 * Shared constants and Project-level extension properties live here.
 * All responsibilities are delegated to focused nested objects:
 * - [Prerequisites] — Java version guard
 * - [Repositories]  — Maven/Ivy repository configuration
 * - [Plugins]       — plugin application
 * - [Dependencies]  — gem dependency declaration
 * - [Extensions]    — DSL extension + RevealJS configuration
 * - [Tasks]         — task registration orchestration
 * - [Git]           — Git commit/push operations (delegates file-system
 *                    preparation to [slider.repository.SlideDeployer])
 */
object SliderManager {

    object Configuration {
        const val CONFIG_PATH_KEY = "managed_config_path"
        // -------------------------------------------------------------------------
        // Shared Project extensions
        // -------------------------------------------------------------------------

        /**
         * Reads and returns the slides YAML configuration bound to this project.
         *
         * Thin adapter over [slider.config.SlidesConfigLoader] — resolves the
         * config path declared via the `managed_config_path` project property
         * (using the non-deprecated `findProperty` API) and delegates the
         * deserialisation to the domain loader.
         */
        val Project.localConf: SlidesConfiguration
            get() = SlidesConfigLoader.load(
                configPath = findProperty(CONFIG_PATH_KEY)?.toString().orEmpty(),
                baseDir = rootDir.absolutePath,
                mapper = yamlMapper,
            )

        /**
         * Shared Jackson `ObjectMapper` for YAML serialisation.
         *
         * Thin adapter over [slider.config.YamlMapperFactory] — kept as an
         * extension property so existing call sites (scaffold, publish,
         * translateDeck, RAG) continue to resolve via `Configuration.yamlMapper`
         * until each nested object is extracted in its own US-6.x.
         */
        val yamlMapper: ObjectMapper
            get() = YamlMapperFactory.create()

        /**
         * Reads and deserialises the slides configuration YAML file.
         * Delegates to [slider.config.SlidesConfigLoader] which returns an
         * empty [SlidesConfiguration] on any parsing failure.
         */
        fun readSlidesConfigurationFile(
            configPath: () -> String
        ): SlidesConfiguration = SlidesConfigLoader.load(
            configFile = File(configPath()),
            mapper = yamlMapper,
        )
    }

    // =========================================================================
    // Prerequisites
    // =========================================================================

    /**
     * Guards the build against unsupported Java versions.
     *
     * Thin Gradle adapter — pure validation logic lives in the `slider.prerequisite`
     * domain: [slider.prerequisite.JavaVersionGuard]. This object only bridges the
     * Gradle [org.gradle.api.JavaVersion] API to the domain guard.
     */
    object Prerequisites {

        /**
         * Fails fast with a clear message if the current JVM is below Java 23.
         * Called as the very first step in [SliderPlugin.apply].
         */
        internal fun checkJavaVersion() =
            slider.prerequisite.JavaVersionGuard.requireJava23FromMajor(
                JavaVersion.current().majorVersion,
            )
    }


    // =========================================================================
    // Scaffold
    // =========================================================================

    /**
     * Handles first-use initialisation of the consumer project's slides/ directory.
     *
     * Thin Gradle adapter — all pure logic (completeness check, zip extraction,
     * default factories) lives in the `slider.scaffold` domain:
     * - [slider.scaffold.SlidesScaffolder] — completeness check + zip extraction
     * - [slider.scaffold.ScaffoldDefaults] — default SlidesConfiguration + DeckContext factories
     *
     * The plugin bundles a default slides.zip in its classpath resources
     * (src/main/resources/slides.zip). On first use, if the slides/ directory
     * is absent or incomplete, the zip is extracted into the project directory
     * to provide a ready-to-use slide structure.
     *
     * This follows the scaffolding pattern used by plugins like Quarkus and
     * Spring Initializr — the consumer gets a working default without any
     * manual setup.
     */
    object Scaffold {

        internal fun Project.scaffoldSlidesIfAbsent() {
            val slidesDir = layout.projectDirectory.asFile.resolve(SLIDES_FOLDER)
            val miscDir = slidesDir.resolve(DEFAULT_SLIDES_FOLDER)

            // slides/ exists and all required files are present — do nothing
            if (slidesDir.exists() && slider.scaffold.SlidesScaffolder.isSlidesConfigComplete(miscDir)) return

            val zip = SliderPlugin::class.java
                .classLoader
                .getResourceAsStream("slides.zip")
                ?: error(
                    "slides.zip not found in plugin classpath. " +
                            "Please report this issue at https://github.com/cheroliv/slider-gradle"
                )

            val result = slider.scaffold.SlidesScaffolder.extractSlidesZip(zip, layout.projectDirectory.asFile)
            when (result) {
                is slider.scaffold.ScaffoldResult.Created -> {
                    println("✅ slides/ directory initialised from plugin defaults.")
                    println("📁 Edit slides/${DEFAULT_SLIDES_FOLDER}/*-deck.adoc to get started.")
                }
                is slider.scaffold.ScaffoldResult.Failed -> error("Cannot extract slides.zip: ${result.reason}")
                is slider.scaffold.ScaffoldResult.Skipped -> { /* no-op — never returned by extractSlidesZip */ }
            }
        }


        /**
         * Generates a default slides-context.yml in the consumer project directory
         * if the file does not already exist.
         *
         * Delegates the default model construction to [slider.scaffold.ScaffoldDefaults];
         * only the YAML serialisation and Gradle `Project` wiring remain here.
         */
        internal fun Project.scaffoldSlidesContextIfAbsent() {
            val slidesContext = layout.projectDirectory.asFile.resolve(SLIDES_CONTEXT_YML)

            // slides-context.yml already exists — do nothing
            if (slidesContext.exists()) return

            val default = slider.scaffold.ScaffoldDefaults.defaultSlidesConfiguration()
            yamlMapper.writeValue(slidesContext, default)

            println("✅ slides-context.yml generated with default values.")
            println("✏️  Edit slides-context.yml with your actual Git repository configuration.")
        }

        /**
         * Generates a default example-deck-context.yml in slides/misc/
         * if the file does not already exist.
         *
         * Delegates the default model construction to [slider.scaffold.ScaffoldDefaults];
         * only the YAML serialisation and Gradle `Project` wiring remain here.
         */
        internal fun Project.scaffoldDeckContextIfAbsent() {
            val miscDir = layout.projectDirectory.asFile
                .resolve(SLIDES_FOLDER)
                .resolve(DEFAULT_SLIDES_FOLDER)
            val deckContext = miscDir.resolve("example-deck-context.yml")

            // example-deck-context.yml already exists — do nothing
            if (deckContext.exists()) return

            // Ensure misc/ directory exists (slides scaffold may not have run yet)
            miscDir.mkdirs()

            val default = slider.scaffold.ScaffoldDefaults.defaultDeckContext()
            yamlMapper.writeValue(deckContext, default)

            println("✅ example-deck-context.yml generated in slides/misc/.")
            println("✏️  Edit slides/misc/example-deck-context.yml with your deck details.")
        }
    }


// =========================================================================
// Repositories
// =========================================================================

    /**
     * Configures all Maven and Ivy repositories required by the plugin.
     *
     * Thin Gradle adapter — pure repository declarations live in the
     * `slider.wiring` domain: [slider.wiring.GradleWiring] (driven by
     * [slider.wiring.WiringSpec.DEFAULT]). Repository routing strategy
     * (rubygems include/exclude per mirror) is encoded as
     * [slider.wiring.GroupRouting] in the spec.
     */
    object Repositories {

        /**
         * Declares the full repository set needed to resolve:
         * - Gradle plugin artifacts (plugins.gradle.org)
         * - grolifant transitive dependency (repo.gradle.org/libs-releases)
         * - Ruby gems via Ivy layout (rubygems.org)
         * - Standard JVM artifacts (mavenCentral)
         *
         * The rubygems group is explicitly included/excluded per repository
         * via [slider.wiring.GroupRouting] to prevent resolution conflicts
         * between gem and JVM artifact mirrors.
         */
        internal fun Project.configureRepositories() =
            slider.wiring.GradleWiring.configureRepositories(
                project = this,
                repositories = slider.wiring.WiringSpec.DEFAULT.repositories,
            )
    }

// =========================================================================
// Plugins
// =========================================================================

    /**
     * Applies the external Gradle plugins required for slide generation.
     *
     * Thin Gradle adapter — plugin ids live in the `slider.wiring` domain
     * ([slider.wiring.WiringSpec.DEFAULT.plugins]). The `.classic` suffix is
     * mandatory since asciidoctor-gradle 5.0.0-alpha.1 renamed the plugin IDs
     * as part of a breaking API change.
     */
    object Plugins {

        /**
         * Applies:
         * - `com.github.node-gradle.node`        → npx/Node.js for serveSlides
         * - `org.asciidoctor.jvm.gems.classic`   → JRuby gem lifecycle (5.x API)
         * - `org.asciidoctor.jvm.revealjs.classic` → AsciidoctorJRevealJSTask (5.x API)
         */
        internal fun Project.applyPlugins() =
            slider.wiring.GradleWiring.applyPlugins(
                project = this,
                plugins = slider.wiring.WiringSpec.DEFAULT.plugins,
            )
    }

// =========================================================================
// Dependencies
// =========================================================================

    /**
     * Declares the Ruby gem dependencies required for Reveal.js slide generation.
     *
     * Thin Gradle adapter — gem coordinates live in the `slider.wiring`
     * domain ([slider.wiring.WiringSpec.DEFAULT.gems]).
     */
    object Dependencies {

        /**
         * Adds the asciidoctor-revealjs gem to the asciidoctorGems configuration.
         * The `@gem` classifier is mandatory for Ivy-based gem resolution.
         */
        internal fun Project.configureDependencies() =
            slider.wiring.GradleWiring.configureDependencies(
                project = this,
                gems = slider.wiring.WiringSpec.DEFAULT.gems,
            )
    }

// =========================================================================
// Extensions
// =========================================================================

    /**
     * Registers and configures Gradle extensions consumed by the plugin and its tasks.
     *
     * Thin Gradle adapter — pure registration logic lives in the
     * `slider.extension` domain: [slider.extension.SliderExtensionRegistrar]
     * (registers the `slider {}` DSL + pins the Reveal.js template).
     */
    object Extensions {

        /**
         * Registers the `slider {}` DSL extension for consumer configuration,
         * and pins the RevealJS template to reveal.js 5.2.1 from the hakimel/reveal.js
         * GitHub repository.
         */
        internal fun Project.configureExtensions() =
            slider.extension.SliderExtensionRegistrar.configure(this)
    }

// =========================================================================
// Tasks
// =========================================================================

    /**
     * Orchestrates the registration of all Slider plugin tasks.
     *
     * Task dependency graph:
     * ```
     * cleanBuild
     *   └── generateSlides ──┐
     *         └── asciidoctor│ finalizedBy
     *         └── serveSlides │
     *                         ▼
     *                generateDashboard
     * ```
     */
    object Tasks {

        /**
         * Registers all tasks in dependency order.
         * [registerCleanSlidesBuildTask] must come first so the task name
         * is resolvable when [registerAsciidoctorRevealJsTask] calls dependsOn.
         */
        internal fun Project.registerTasks() {
            // Reveal.js tasks (cleanBuild + asciidoctorRevealJs + generateDashboard)
            // are registered by the slider.revealjs domain adapter.
            slider.revealjs.RevealJsTaskRegistrar.register(this)
            registerAsciidoctorTask()
            // Playwright tasks (serveSlides + installPlaywright + visualTest)
            // are registered by the slider.playwright domain adapter.
            slider.playwright.PlaywrightTaskRegistrar.register(this)
            registerPublishSlidesTask()
            slider.capsule.CapsuleTaskRegistrar.register(this)
            registerReportTestsTask()
            registerReportFunctionalTestsTask()
            registerGenerateRevealUiMessagesTask()
            registerTranslateDeckTask()
        }

        /**
         * Standard Asciidoctor HTML conversion task.
         * Depends on asciidoctorRevealJs so both outputs are always in sync.
         */
        @Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")
        private fun Project.registerAsciidoctorTask() {
            tasks.register<AsciidoctorTask>("asciidoctor") {
                group = "generate"
                dependsOn(TASK_ASCIIDOCTOR_REVEALJS)
            }
        }

        /**
         * Deploys the generated slides to a remote Git repository.
         *
         * Reads target repository, branch, credentials, and commit message
         * from the YAML configuration file declared via the slider DSL (configPath).
         * Delegates the actual Git operations to [Git].
         */
        private fun Project.registerPublishSlidesTask() {
            tasks.register<DefaultTask>(TASK_PUBLISH_SLIDES) {
                group = "deploy"
                description = "Deploy generated slides to the configured remote repository."
                dependsOn("asciidoctor")
                doFirst { task ->
                    logger.info("Task description :\n\t${task.description}")
                }
                doLast {
                    // Reuse the shared Configuration.localConf adapter (delegates
                    // to slider.config.SlidesConfigLoader) instead of re-deserialising
                    // the YAML here — fixes the properties[...] deprecation warning.
                    val localConf: SlidesConfiguration = this@registerPublishSlidesTask.localConf

                    val repoDir = layout.buildDirectory.get().asFile
                        .resolve(localConf.pushSlides!!.to)

                    // Delegate file copy + Git push to SliderManager.Git
                    with(Git) {
                        pushSlides(
                            slidesDirPath = {
                                layout.buildDirectory.get().asFile
                                    .resolve(localConf.srcPath!!)
                                    .absolutePath
                            },
                            pathTo = { repoDir.absolutePath }
                        )
                    }
                }
            }
        }


        /**
         * Runs all checks and opens the unit test HTML report in Firefox.
         * Useful for quick post-build test review without manual file navigation.
         */
        private fun Project.registerReportTestsTask() {
            tasks.register<Exec>("reportTests") {
                group = "verify"
                description = "Run checks and open the unit test report in Firefox."
                dependsOn("check")
                commandLine(
                    "firefox", "--new-tab",
                    layout.buildDirectory.asFile.get()
                        .resolve("reports")
                        .resolve("tests")
                        .resolve("test")
                        .resolve("index.html")
                        .absolutePath
                )
            }
        }

        /**
         * Runs all checks and opens the functional test HTML report in Firefox.
         * Useful for reviewing GradleTestKit functional test results.
         */
        private fun Project.registerReportFunctionalTestsTask() {
            tasks.register<Exec>("reportFunctionalTests") {
                group = "verify"
                description = "Run checks and open the functional test report in Firefox."
                dependsOn("check")
                commandLine(
                    "firefox", "--new-tab",
                    layout.buildDirectory.get().asFile
                        .resolve("reports")
                        .resolve("tests")
                        .resolve("functionalTest")
                        .resolve("index.html")
                        .absolutePath
                )
            }
        }

        /**
         * Generates `messages_{code}.js` files for every supported language
         * under `build/reveal-i18n/`. Each file declares a
         * `RevealI18n.messages["{code}"] = { ... }` assignment exposing the
         * navigation and control labels consumed by the Reveal.js i18n plugin.
         *
         * The generated files are loaded as `<script>` tags in the deck HTML
         * and combined with the `lang` Reveal.js config option to localise the
         * on-screen UI (prev/next/up/help tooltips, overview, speaker notes,
         * fullscreen). RTL languages (ar, ur) emit a `rtl: true` flag so the
         * Reveal.js layout can flip direction.
         */
        private fun Project.registerGenerateRevealUiMessagesTask() {
            tasks.register<DefaultTask>(TASK_GENERATE_REVEAL_UI_MESSAGES) {
                group = GROUP_TASK_SLIDER
                description = "Generate Reveal.js UI i18n messages_{code}.js files for all 10 supported languages."
                doLast {
                    val outputDir = layout.buildDirectory.get().asFile.resolve(REVEAL_I18N_OUTPUT_DIR)
                    outputDir.mkdirs()
                    val written = slider.i18n.RevealUiMessagesWriter.writeAll(outputDir)
                    logger.lifecycle("✅ ${written.size} Reveal.js i18n message files written to {}", outputDir.absolutePath)
                }
            }
        }
    }

// =========================================================================
// Git
// =========================================================================

    /**
     * Handles all Git operations required to publish slides to a remote repository.
     *
     * The full pipeline is delegated to the `slider.repository` domain:
     * - [slider.repository.SlideDeployer] handles file-system preparation
     * - [slider.repository.JGitSlidePusher] handles Git init/commit/push
     *
     * This object is now a thin Gradle adapter that builds the
     * [slider.repository.SlideDeploymentRequest] from YAML config and
     * delegates everything else to the domain layer.
     */
    object Git {

        /**
         * Full publish pipeline:
         * - Creates a clean temporary repo directory at [pathTo]
         * - Copies slides from [slidesDirPath] into it via [SlideDeployer]
         * - Commits and pushes if the copy succeeds
         * - Cleans up both the repo dir and the source slides dir on success
         */
        fun Project.pushSlides(
            slidesDirPath: () -> String,
            pathTo: () -> String
        ) {
            val conf = localConf
            val slidesDir = File(slidesDirPath())
            val repoDir = File(pathTo())
            val pushConf = conf.pushSlides ?: return
            val request = slider.repository.SlideDeploymentRequest(
                slidesDir = slidesDir,
                repoDir = repoDir,
                remoteUrl = pushConf.repo.repository,
                branch = pushConf.branch,
                commitMessage = pushConf.message,
                username = pushConf.repo.credentials.username,
                password = pushConf.repo.credentials.password,
            )

            val repoResult = slider.repository.SlideDeployer.createRepoDir(repoDir)
            if (repoResult is slider.repository.RepoDirResult.Failure) {
                logger.error("Cannot create repo dir: ${repoResult.error}")
                return
            }

            val copyResult = slider.repository.SlideDeployer.copySlides(request)
            if (copyResult is slider.repository.CopyResult.Failure) {
                logger.error("Cannot copy slides: ${copyResult.error}")
                slider.repository.SlideDeployer.cleanupRepoDir(request)
                return
            }

            val commitResult = slider.repository.JGitSlidePusher.initAndCommit(request)
            if (commitResult is slider.repository.CommitResult.Failure) {
                logger.error("Cannot init and commit: ${commitResult.error}")
                slider.repository.SlideDeployer.cleanupRepoDir(request)
                return
            }

            val pushResult = slider.repository.JGitSlidePusher.push(request)
            if (pushResult is slider.repository.SlidePushResult.Failure) {
                logger.error("Cannot push slides: ${pushResult.error}")
                slider.repository.SlideDeployer.cleanupRepoDir(request)
                return
            }

            slider.repository.SlideDeployer.cleanupRepoDir(request)
            slider.repository.SlideDeployer.cleanupSlidesDir(request)
        }
    }

}