package slider.revealjs

import org.asciidoctor.gradle.jvm.AbstractAsciidoctorTask.OUT_OF_PROCESS
import org.asciidoctor.gradle.jvm.slides.AsciidoctorJRevealJSTask
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import java.io.File
import java.io.File.separator

/**
 * Thin Gradle adapter that registers the three Reveal.js-related tasks
 * previously declared in `SliderManager.Tasks`:
 *
 * - `cleanBuild`             → deletes the previous build artifacts
 *   ([CleanBuildTarget] domain).
 * - `asciidoctorRevealJs`    → compiles AsciiDoc sources into a Reveal.js
 *   HTML presentation ([RevealJsAttributesSpec] + [SlideSourceDir] +
 *   [RevealJsOutputDir] domains).
 * - `generateDashboard`      → writes the `slides.json` dashboard payload
 *   and copies the source `index.html` ([SlideMetadataScanner] +
 *   [DashboardJsonSerializer] domains).
 *
 * Pure rendering data lives in [slider.revealjs] value objects; this object
 * only translates that data into Gradle task declarations.
 */
object RevealJsTaskRegistrar {

    /**
     * Registers the three Reveal.js tasks on [project], wired in dependency
     * order: `cleanBuild` → `asciidoctorRevealJs` → `generateDashboard`.
     *
     * Uses [WiringSpec.DEFAULT] for the rendering attributes (overridden with
     * the dynamic `build-gradle` and `endpoint-url` entries that depend on
     * the consumer project's layout).
     */
    fun register(project: Project) {
        registerCleanBuildTask(project)
        registerAsciidoctorRevealJsTask(project)
        registerDashboardTask(project)
    }

    /**
     * Registers the `cleanBuild` task — deletes previously generated
     * presentation artifacts via [CleanBuildTarget].
     */
    fun registerCleanBuildTask(project: Project) {
        project.tasks.register<DefaultTask>(TASK_CLEAN_SLIDES_BUILD) {
            group = "build"
            description = "Delete generated presentation artifacts from the build directory."
            doFirst {
                val outputDir = RevealJsOutputDir(project.layout.buildDirectory.get().asFile).asFile()
                val report = CleanBuildTarget(outputDir).collect()
                logger.lifecycle(
                    "Cleaned {} artifacts from {} (slides.json={}, images={}, html={})",
                    report.cleanedCount(),
                    outputDir.absolutePath,
                    report.slidesJsonDeleted,
                    report.imagesDirDeleted,
                    report.htmlFilesDeleted,
                )
            }
        }
    }

    /**
     * Registers the core `asciidoctorRevealJs` task — compiles AsciiDoc
     * sources into a Reveal.js HTML presentation using the configured
     * [RevealJsAttributesSpec] and the resolved source/output directories.
     */
    @Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")
    fun registerAsciidoctorRevealJsTask(project: Project) {
        val sourceDir = SlideSourceDir(project.layout.projectDirectory.asFile)
        val outputDir = RevealJsOutputDir(project.layout.buildDirectory.get().asFile)
        val attributes = RevealJsAttributesSpec.DEFAULT.withOverrides(
            mapOf(
                RevealJsAttributeKeys.BUILD_GRADLE to
                    project.layout.projectDirectory.asFile.resolve("build.gradle.kts").absolutePath,
                RevealJsAttributeKeys.ENDPOINT_URL to "https://github.com/pages-content/slides/",
            ),
        ).attributes

        project.tasks.getByName<AsciidoctorJRevealJSTask>(TASK_ASCIIDOCTOR_REVEALJS) {
            group = "generate"
            description = "Compile AsciiDoc sources into a Reveal.js HTML presentation."
            setExecutionMode(OUT_OF_PROCESS)
            dependsOn(TASK_CLEAN_SLIDES_BUILD)
            finalizedBy(TASK_DASHBOARD_SLIDES_BUILD)
            // RTL — driven by -Planguage=<code> when the resolved language
            // is a RTL one (Arabic, Urdu). Defaults to LTR.
            val resolvedLanguage = project.findProperty("language") as? String ?: ""
            if (slider.i18n.RevealRtlResolver.resolveRtl(resolvedLanguage)) {
                revealjsOptions.setRightToLeft(true)
            }
            doFirst {
                val cssOutput = outputDir.asFile()
                cssOutput.mkdirs()
                javaClass.getResourceAsStream("/revealjs/theme/talaria.css")?.use { input ->
                    cssOutput.resolve("talaria.css").outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            revealjsOptions {
                // Resolve and log the AsciiDoc source directory
                sourceDir.asFile()
                    .apply { println("Slide source absolute path: $absolutePath") }
                    .let(::setSourceDir)
                // Output path mirrors the source file location
                baseDirFollowsSourceFile()
                // Copy images and theme CSS alongside the generated HTML
                resources { spec ->
                    spec.from(sourceDir.imagesDir()) { copy ->
                        copy.include("**")
                        copy.into("images")
                    }
                }
                // Asciidoctor + Reveal.js rendering attributes
                attributes(attributes)
            }
        }
    }

    /**
     * Registers the `generateDashboard` task — writes `slides.json` and
     * copies the source `index.html` into the build output dir.
     */
    fun registerDashboardTask(project: Project) {
        val sourceDir = SlideSourceDir(project.layout.projectDirectory.asFile)
        val outputDir = RevealJsOutputDir(project.layout.buildDirectory.get().asFile)

        project.tasks.register<DefaultTask>(TASK_DASHBOARD_SLIDES_BUILD) {
            group = "generate"
            description = "Generate index.html and slides.json listing all Reveal.js presentations."
            doLast {
                // Log the source index.html content for traceability
                sourceDir.asFile().listFiles()?.find { it.name == "index.html" }
                    ?.readText()?.trimIndent()
                    ?.run { "index.html:\n$this" }
                    ?.run(logger::info)

                val output = outputDir.asFile()
                val slidesJsonFile = output.resolve("slides.json")

                // Ensure the output directory exists before writing
                output.mkdirs()

                // Scan .adoc files via the domain scanner
                val metas = SlideMetadataScanner.scan(sourceDir.asFile())
                println(metas)

                // Serialise slides metadata to JSON via the domain serializer
                DashboardJsonSerializer.serialize(metas).run(slidesJsonFile::writeText)

                // Copy index.html from source to build output
                val indexFile = sourceDir.asFile().resolve("index.html")
                indexFile.copyTo(output.resolve("index.html"), overwrite = true)

                println("✅ Dashboard generated successfully!")
                println("📁 Generated files:")
                println("   - ${indexFile.absolutePath}")
                println("   - ${slidesJsonFile.absolutePath}")
                println("📊 ${metas.size} presentation(s) found")
            }
        }
    }

    /**
     * Task names — kept here so the slider.revealjs domain owns the
     * identifiers of the tasks it registers.
     */
    const val TASK_CLEAN_SLIDES_BUILD = "cleanBuild"
    const val TASK_ASCIIDOCTOR_REVEALJS = "asciidoctorRevealJs"
    const val TASK_DASHBOARD_SLIDES_BUILD = "generateDashboard"
}