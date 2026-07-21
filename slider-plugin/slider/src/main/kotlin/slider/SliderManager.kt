package slider

import slider.SliderManager.Configuration.CONFIG_PATH_KEY
import slider.SliderManager.Configuration.localConf
import slider.SliderManager.Configuration.yamlMapper
import slider.SliderManager.Git.pushSlides
import slider.SliderManager.Tasks.registerAsciidoctorRevealJsTask
import slider.SliderManager.Tasks.registerCleanSlidesBuildTask
import slider.SliderManager.Tasks.registerTasks
import slider.SliderPlugin.SliderExtension
import slider.Slides.RevealJsSlides
import slider.Slides.RevealJsSlides.BUILD_GRADLE_KEY
import slider.Slides.RevealJsSlides.CODERAY_CSS_KEY
import slider.Slides.RevealJsSlides.ENDPOINT_URL_KEY
import slider.Slides.RevealJsSlides.GROUP_TASK_SLIDER
import slider.Slides.RevealJsSlides.SOURCE_HIGHLIGHTER_KEY
import slider.Slides.RevealJsSlides.TASK_ASCIIDOCTOR_REVEALJS
import slider.Slides.RevealJsSlides.TASK_CLEAN_SLIDES_BUILD
import slider.capsule.AsciidocSpeakerNoteParser
import slider.capsule.CapsuleScriptWriter
import slider.Slides.RevealJsSlides.TASK_DASHBOARD_SLIDES_BUILD
import slider.Slides.RevealJsSlides.TASK_PUBLISH_SLIDES
import slider.Slides.RevealJsSlides.TASK_SERVE_SLIDES
import slider.Slides.RevealJsSlides.TASK_VISUAL_TEST
import slider.Slides.RevealJsSlides.TASK_INSTALL_PLAYWRIGHT
import slider.Slides.RevealJsSlides.REVEAL_I18N_OUTPUT_DIR
import slider.Slides.RevealJsSlides.TASK_GENERATE_REVEAL_UI_MESSAGES
import slider.Slides.RevealJsSlides.TASK_TRANSLATE_DECK
import slider.config.SlidesConfigLoader
import slider.config.YamlMapperFactory
import slider.translation.registerTranslateDeckTask
import slider.Slides.Serve.SERVE_DEP
import slider.Slides.Slide.DEFAULT_SLIDES_FOLDER
import slider.Slides.Slide.IMAGES
import slider.Slides.Slide.SLIDES_CONTEXT_YML
import slider.Slides.Slide.SLIDES_FOLDER
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.gradle.node.npm.task.NpxTask
import org.asciidoctor.gradle.jvm.AbstractAsciidoctorTask.OUT_OF_PROCESS
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.asciidoctor.gradle.jvm.slides.AsciidoctorJRevealJSTask
import org.asciidoctor.gradle.jvm.slides.RevealJSExtension
import org.gradle.api.DefaultTask
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import java.io.File
import java.io.File.separator

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
     * The plugin requires Java 23+ due to asciidoctor-gradle OUT_OF_PROCESS
     * behaviour and Gradle 9 compatibility requirements.
     */
    object Prerequisites {

        /**
         * Fails fast with a clear message if the current JVM is below Java 23.
         * Called as the very first step in [SliderPlugin.apply].
         */
        internal fun checkJavaVersion() = JavaVersion
            .current()
            .majorVersion
            .toInt()
            .run {
                require(JavaVersion.current().majorVersion.toInt() >= 23) {
                    "education.cccp.slider requires Java 23+. Current: Java $this"
                }
            }
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
     * Repository routing strategy:
     * - rubygems group  → gem-capable mirrors only (xillio, jcenter-backup, rubygems Ivy)
     * - everything else → mavenCentral, plugins.gradle.org, repo.gradle.org
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
         * to prevent resolution conflicts between gem and JVM artifact mirrors.
         */
        internal fun Project.configureRepositories() {
            // Gradle plugin artifacts
            repositories.maven { it.url = uri("https://plugins.gradle.org/m2/") }
            // grolifant — transitive dep of jruby-gradle-core, only available here
            repositories.maven {
                it.url = uri("https://repo.gradle.org/gradle/libs-releases/")
                it.content { c -> c.excludeGroup("rubygems") }
            }
            // rubygems fallback mirror
            repositories.maven {
                it.url = uri("https://repo.gradle.org/ui/native/jcenter-backup/")
                it.content { c -> c.includeGroup("rubygems") }
            }
            // rubygems primary mirror
            repositories.maven {
                it.url = uri("https://maven.xillio.com/artifactory/libs-release/")
                it.content { c -> c.includeGroup("rubygems") }
            }
            // standard JVM artifacts — rubygems excluded to avoid interception
            repositories.mavenCentral { it.content { c -> c.excludeGroup("rubygems") } }
            // actual .gem file resolution via Ivy artifact layout
            repositories.ivy {
                it.url = uri("https://rubygems.org/gems/")
                it.patternLayout { layout -> layout.artifact("[module]-[revision].gem") }
                it.metadataSources { s -> s.artifact() }
                it.content { c -> c.includeGroup("rubygems") }
            }
        }
    }

// =========================================================================
// Plugins
// =========================================================================

    /**
     * Applies the external Gradle plugins required for slide generation.
     *
     * The `.classic` suffix is mandatory since asciidoctor-gradle 5.0.0-alpha.1
     * renamed the plugin IDs as part of a breaking API change.
     */
    object Plugins {

        /**
         * Applies:
         * - `com.github.node-gradle.node`        → npx/Node.js for serveSlides
         * - `org.asciidoctor.jvm.gems.classic`   → JRuby gem lifecycle (5.x API)
         * - `org.asciidoctor.jvm.revealjs.classic` → AsciidoctorJRevealJSTask (5.x API)
         */
        internal fun Project.applyPlugins() {
            plugins.apply("com.github.node-gradle.node")
            plugins.apply("org.asciidoctor.jvm.gems.classic")
            plugins.apply("org.asciidoctor.jvm.revealjs.classic")
        }
    }

// =========================================================================
// Dependencies
// =========================================================================

    /**
     * Declares the Ruby gem dependencies required for Reveal.js slide generation.
     */
    object Dependencies {

        /**
         * Adds the asciidoctor-revealjs gem to the asciidoctorGems configuration.
         * The `@gem` classifier is mandatory for Ivy-based gem resolution.
         */
        internal fun Project.configureDependencies() {
            dependencies.add(
                "asciidoctorGems",
                "rubygems:asciidoctor-revealjs:5.2.0@gem"
            )
        }
    }

// =========================================================================
// Extensions
// =========================================================================

    /**
     * Registers and configures Gradle extensions consumed by the plugin and its tasks.
     */
    object Extensions {

        /**
         * Registers the `slider {}` DSL extension for consumer configuration,
         * and pins the RevealJS template to reveal.js 5.2.1 from the hakimel/reveal.js
         * GitHub repository.
         */
        internal fun Project.configureExtensions() {
            // Expose the slider {} DSL block to the consumer
            extensions.create(GROUP_TASK_SLIDER, SliderExtension::class.java)

            // Pin reveal.js version and GitHub template source
            extensions.getByType(RevealJSExtension::class.java).apply {
                version = "5.2.0"
                templateGitHub { gh ->
                    gh.setOrganisation("hakimel")
                    gh.setRepository("reveal.js")
                    gh.setTag("5.2.1")
                }
            }
        }
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
            registerCleanSlidesBuildTask()
            registerAsciidoctorRevealJsTask()
            registerAsciidoctorTask()
            registerServeSlidesTask()
            registerDashboardTask()
            registerPublishSlidesTask()
            registerAsciidocCapsuleTask()
            registerReportTestsTask()
            registerReportFunctionalTestsTask()
            registerVisualTestTask()
            registerInstallPlaywrightTask()
            registerGenerateRevealUiMessagesTask()
            registerTranslateDeckTask()
        }

        /**
         * Deletes previously generated presentation artifacts from the build output:
         * - slides.json
         * - images/ directory
         * - all .html files
         *
         * Always runs before asciidoctorRevealJs to guarantee a clean output.
         */
        private fun Project.registerCleanSlidesBuildTask() {
            tasks.register<DefaultTask>(TASK_CLEAN_SLIDES_BUILD) {
                group = "build"
                description = "Delete generated presentation artifacts from the build directory."
                doFirst {
                    layout.buildDirectory.get().asFile
                        .resolve("docs")
                        .resolve("asciidocRevealJs")
                        .run {
                            resolve("slides.json").run { if (exists()) delete() }
                            resolve("images").deleteRecursively()
                            listFiles()
                                ?.filter { file -> file.isFile && file.name.endsWith(".html") }
                                ?.forEach { file -> file.delete() }
                        }
                }
            }
        }

        /**
         * Core task — compiles AsciiDoc sources into a Reveal.js HTML presentation.
         *
         * Key decisions:
         * - OUT_OF_PROCESS: JAVA_EXEC was removed in asciidoctor-gradle 5.0.0-alpha.1
         *   due to Gradle closure serialisation changes. OUT_OF_PROCESS is the replacement.
         * - Source dir: resolved from slides/misc/ inside the consumer project.
         * - Images: copied alongside generated HTML under an images/ subdirectory.
         * - Attributes: configure syntax highlighting, Reveal.js theme, transitions,
         *   history, and slide numbering.
         */
        @Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")
        private fun Project.registerAsciidoctorRevealJsTask() {
            tasks.getByName<AsciidoctorJRevealJSTask>(TASK_ASCIIDOCTOR_REVEALJS) {
                group = "generate"
                description = "Compile AsciiDoc sources into a Reveal.js HTML presentation."
                setExecutionMode(OUT_OF_PROCESS)
                dependsOn(TASK_CLEAN_SLIDES_BUILD)
                finalizedBy(TASK_DASHBOARD_SLIDES_BUILD)
                // RTL — driven by -Planguage=<code> when the resolved
                // language is a RTL one (Arabic, Urdu). Defaults to LTR.
                val resolvedLanguage = project.findProperty("language") as? String
                    ?: ""
                if (slider.i18n.RevealRtlResolver.resolveRtl(resolvedLanguage)) {
                    revealjsOptions.setRightToLeft(true)
                }
                doFirst {
                    val cssOutput = layout.buildDirectory.get().asFile
                        .resolve("docs")
                        .resolve("asciidocRevealJs")
                    cssOutput.mkdirs()
                    javaClass.getResourceAsStream("/revealjs/theme/talaria.css")?.use { input ->
                        cssOutput.resolve("talaria.css").outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                revealjsOptions {
                    // Resolve and log the AsciiDoc source directory
                    layout.projectDirectory.asFile
                        .resolve(SLIDES_FOLDER)
                        .resolve(DEFAULT_SLIDES_FOLDER)
                        .apply { println("Slide source absolute path: $absolutePath") }
                        .let(::setSourceDir)
                    // Output path mirrors the source file location
                    baseDirFollowsSourceFile()
                    // Copy images and theme CSS alongside the generated HTML
                    resources { spec ->
                        spec.from(sourceDir.resolve(IMAGES)) { copy ->
                            copy.include("**")
                            copy.into(IMAGES)
                        }
                    }
                    // Asciidoctor + Reveal.js rendering attributes
                    attributes(
                        mapOf(
                            BUILD_GRADLE_KEY to layout.projectDirectory.asFile.resolve("build.gradle.kts"),
                            ENDPOINT_URL_KEY to "https://github.com/pages-content/slides/",
                            SOURCE_HIGHLIGHTER_KEY to "coderay",
                            CODERAY_CSS_KEY to "style",
                            RevealJsSlides.IMAGEDIR_KEY to ".${separator}images",
                            RevealJsSlides.TOC_KEY to "left",
                            RevealJsSlides.ICONS_KEY to "font",
                            RevealJsSlides.SETANCHORS_KEY to "",
                            RevealJsSlides.IDPREFIX_KEY to "slide-",
                            RevealJsSlides.IDSEPARATOR_KEY to "-",
                            RevealJsSlides.DOCINFO_KEY to "shared",
                            RevealJsSlides.REVEALJS_THEME_KEY to "black",
                            RevealJsSlides.REVEALJS_CUSTOMCSS_KEY to "talaria.css",
                            RevealJsSlides.REVEALJS_TRANSITION_KEY to "slide",
                            RevealJsSlides.REVEALJS_HISTORY_KEY to "true",
                            RevealJsSlides.REVEALJS_SLIDENUMBER_KEY to "true"
                        )
                    )
                }
            }
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
         * Serves the generated presentation locally via the `serve` npm package
         * executed through npx. No browser is launched automatically.
         * Depends on asciidoctorRevealJs to ensure slides are built first.
         */
        private fun Project.registerServeSlidesTask() {
            tasks.register<NpxTask>(TASK_SERVE_SLIDES) {
                group = "info"
                description = "Serve slides using the serve package executed via npx."
                dependsOn(TASK_ASCIIDOCTOR_REVEALJS)
                command.set(SERVE_DEP)
                layout.buildDirectory.get().asFile
                    .resolve("docs")
                    .resolve("asciidocRevealJs")
                    .absolutePath
                    .run(::listOf)
                    .run(args::set)
                workingDir.set(layout.projectDirectory.asFile)
                doFirst { println("Serving slides via npx serve...") }
            }
        }

        /**
         * Generates the presentation dashboard in the build output directory:
         * - slides.json  → metadata list of all available presentations
         * - index.html   → copied from slides/misc/index.html
         *
         * Scans slides/misc/ for .adoc files to populate slides.json.
         * Finalises asciidoctorRevealJs so it always runs after generation.
         */
        private fun Project.registerDashboardTask() {
            tasks.register<DefaultTask>(TASK_DASHBOARD_SLIDES_BUILD) {
                group = "generate"
                description = "Generate index.html and slides.json listing all Reveal.js presentations."
                doLast {
                    val slidesDir = layout.projectDirectory.asFile
                        .resolve(SLIDES_FOLDER)
                        .resolve(DEFAULT_SLIDES_FOLDER)
                        .apply {
                            // Log the source index.html content for traceability
                            listFiles()?.find { it.name == "index.html" }
                                ?.readText()?.trimIndent()
                                ?.run { "index.html:\n$this" }
                                ?.run(logger::info)
                        }

                    val outputDir = layout.buildDirectory.get().asFile
                        .resolve("docs")
                        .resolve("asciidocRevealJs")
                        .also { logger.info("output dir path: $it") }

                    val slidesJsonFile = outputDir.resolve("slides.json")

                    // Ensure the output directory exists before writing
                    outputDir.mkdirs()

                    // Scan .adoc files and build the slides metadata list
                    val adocFiles = slidesDir.listFiles { file ->
                        file.isFile && file.extension == "adoc"
                    }?.map { file ->
                        mapOf(
                            "name" to file.nameWithoutExtension,
                            "filename" to "${file.nameWithoutExtension}.html"
                        )
                    }.also { println(it) } ?: emptyList()

                    // Serialise slides metadata to JSON manually (no extra dependency)
                    buildString {
                        appendLine("[")
                        adocFiles.forEachIndexed { index, slide ->
                            append("  {")
                            append("\"name\": \"${slide["name"]}\", ")
                            append("\"filename\": \"${slide["filename"]}\"")
                            append("}")
                            if (index < adocFiles.size - 1) append(",")
                            appendLine()
                        }
                        appendLine("]")
                    }.run(slidesJsonFile::writeText)

                    // Copy index.html from source to build output
                    val indexFile = slidesDir.resolve("index.html")
                    indexFile.copyTo(outputDir.resolve("index.html"), overwrite = true)

                    println("✅ Dashboard generated successfully!")
                    println("📁 Generated files:")
                    println("   - ${indexFile.absolutePath}")
                    println("   - ${slidesJsonFile.absolutePath}")
                    println("📊 ${adocFiles.size} presentation(s) found")
                }
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


        @Suppress("UnstableApiUsage")
        private fun Project.registerAsciidocCapsuleTask() {
            val adocDir = projectDir.resolve("slides/misc")
            val buildDir = layout.buildDirectory

            tasks.register<DefaultTask>("generateCapsule") {
                group = "slider"
                description = "Extract speaker notes from AsciiDoc decks and generate a capsule script (consumed by capsule-gradle)."
                outputs.upToDateWhen { false }

                doLast {
                    val scriptDir = buildDir.get().asFile.resolve("capsule")
                    scriptDir.mkdirs()

                    adocDir.listFiles { f -> f.extension == "adoc" }?.forEach { adoc ->
                        val script = AsciidocSpeakerNoteParser.parse(
                            adocContent = adoc.readText(),
                            deckName = adoc.nameWithoutExtension,
                        )
                        if (script.isEmpty) {
                            logger.lifecycle("⚠ Capsule script skipped for ${adoc.name} (no speaker notes found)")
                            return@forEach
                        }
                        val scriptFile = scriptDir.resolve("${adoc.nameWithoutExtension}-script.txt")
                        scriptFile.writeText(CapsuleScriptWriter.write(script))
                        logger.lifecycle("✅ Capsule script → ${scriptFile.name} (${script.segments.size} slides)")
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

        private fun Project.playwrightDir(): String =
            projectDir.resolve("src/test/playwright").absolutePath

        private fun Project.registerInstallPlaywrightTask() {
            tasks.register<NpxTask>(TASK_INSTALL_PLAYWRIGHT) {
                group = "setup"
                description = "Install Playwright browsers (chromium) for visual testing."
                command.set("playwright")
                args.set(listOf("install", "chromium"))
                workingDir.set(file(playwrightDir()))
            }
        }

        private fun Project.registerVisualTestTask() {
            tasks.register<NpxTask>(TASK_VISUAL_TEST) {
                group = GROUP_TASK_SLIDER
                description = "Run Playwright visual snapshot tests on generated slides."
                dependsOn(TASK_ASCIIDOCTOR_REVEALJS)
                command.set("playwright")
                args.set(listOf(
                    "test",
                    "--config", "${playwrightDir()}/playwright.config.ts"
                ))
                workingDir.set(file(playwrightDir()))
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