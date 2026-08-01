package slider

import slider.Slides.RevealJsSlides.GROUP_TASK_SLIDER
import slider.Slides.RevealJsSlides.TASK_ASCIIDOCTOR_REVEALJS
import slider.Slides.RevealJsSlides.REVEAL_I18N_OUTPUT_DIR
import slider.Slides.RevealJsSlides.TASK_GENERATE_REVEAL_UI_MESSAGES
import slider.Slides.RevealJsSlides.TASK_TRANSLATE_DECK
import slider.capsule.CapsuleTaskRegistrar
import slider.translation.registerTranslateDeckTask
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register

object SliderTasks {

    fun registerTasks(project: Project) {
        with(project) {
            slider.revealjs.RevealJsTaskRegistrar.register(this)
            registerAsciidoctorTask()
            slider.playwright.PlaywrightTaskRegistrar.register(this)
            slider.repository.PublishTaskRegistrar.register(this)
            slider.capsule.CapsuleTaskRegistrar.register(this)
            slider.capsule.CapsuleTaskRegistrar.registerTranslateAndGenerateCapsule(this)
            registerReportTestsTask()
            registerReportFunctionalTestsTask()
            registerGenerateRevealUiMessagesTask()
            registerTranslateDeckTask()
        }
    }

    @Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")
    private fun Project.registerAsciidoctorTask() {
        tasks.register<AsciidoctorTask>("asciidoctor") {
            group = "generate"
            dependsOn(TASK_ASCIIDOCTOR_REVEALJS)
        }
    }

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
