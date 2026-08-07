package slider

import slider.Slides.RevealJsSlides.GROUP_TASK_SLIDER
import slider.Slides.RevealJsSlides.TASK_ASCIIDOCTOR_REVEALJS
import slider.Slides.RevealJsSlides.REVEAL_I18N_OUTPUT_DIR
import slider.Slides.RevealJsSlides.TASK_GENERATE_REVEAL_UI_MESSAGES
import slider.Slides.RevealJsSlides.TASK_TRANSLATE_DECK
import slider.i18n.SliderMessages
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
            registerReportTestsTask()
            registerReportFunctionalTestsTask()
            registerGenerateRevealUiMessagesTask()
            registerTranslateDeckTask()
        }
    }

    @Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")
    private fun Project.registerAsciidoctorTask() {
        val lang = SliderMessages.resolveLanguage(this)
        tasks.register<AsciidoctorTask>("asciidoctor") {
            group = SliderMessages.get("task.group.generate", lang)
            description = SliderMessages.get("task.asciidoctor.description", lang)
            dependsOn(TASK_ASCIIDOCTOR_REVEALJS)
        }
    }

    private fun Project.registerReportTestsTask() {
        val lang = SliderMessages.resolveLanguage(this)
        tasks.register<Exec>("reportTests") {
            group = SliderMessages.get("task.group.verify", lang)
            description = SliderMessages.get("task.reportTests.description", lang)
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
        val lang = SliderMessages.resolveLanguage(this)
        tasks.register<Exec>("reportFunctionalTests") {
            group = SliderMessages.get("task.group.verify", lang)
            description = SliderMessages.get("task.reportFunctionalTests.description", lang)
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
        val lang = SliderMessages.resolveLanguage(this)
        tasks.register<DefaultTask>(TASK_GENERATE_REVEAL_UI_MESSAGES) {
            group = GROUP_TASK_SLIDER
            description = SliderMessages.get("task.generateRevealUiMessages.description", lang)
            doLast {
                val outputDir = layout.buildDirectory.get().asFile.resolve(REVEAL_I18N_OUTPUT_DIR)
                outputDir.mkdirs()
                val written = slider.i18n.RevealUiMessagesWriter.writeAll(outputDir)
                logger.lifecycle(
                    SliderMessages.format("task.generateRevealUiMessages.written", lang, written.size, outputDir.absolutePath),
                )
            }
        }
    }
}
