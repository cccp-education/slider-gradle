package slider.i18n

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Functional smoke for the i18n task descriptions (XB-FP-6, S-322).
 *
 * The exhaustive matrix (24 task keys × 10 languages, translations and fallbacks)
 * is covered by the pure [TaskDescriptionsI18nCatalogTest]. This suite keeps a
 * single TestKit build to prove the wiring end to end: the plugin applies, the
 * `-Planguage` property reaches the task descriptions, and every documented task
 * is listed.
 */
class TaskDescriptionsI18nFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File

    private fun runTasksWithLanguage(language: String): String {
        testProjectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("education.cccp.slider")
            }
            """.trimIndent(),
        )
        testProjectDir.resolve("settings.gradle.kts").writeText(
            """
            rootProject.name = "task-descriptions-i18n-test"
            """.trimIndent(),
        )
        return GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--all", "-Planguage=$language")
            .withPluginClasspath()
            .forwardOutput()
            .build()
            .output
    }

    @Test
    fun `plugin lists every documented task with its localized description`() {
        val output = runTasksWithLanguage("fr")

        val taskNames = listOf(
            "cleanBuild",
            "asciidoctorRevealJs",
            "generateDashboard",
            "asciidoctor",
            "serveSlides",
            "installPlaywright",
            "visualTest",
            "deploySlides",
            "reportTests",
            "reportFunctionalTests",
            "generateRevealUiMessages",
            "translateDeck",
            "reindexRag",
            "proposeDeckContext",
            "generateDeck",
            "generateDeckPipeline",
        )
        taskNames.forEach { taskName ->
            assertThat(output)
                .describedAs("Task '$taskName' should be listed")
                .contains(taskName)
        }

        // Two descriptions from the French bundle prove the -Planguage wiring.
        assertThat(output).contains("Supprime les artefacts de présentation")
        assertThat(output).contains("Compile les sources AsciiDoc en présentation HTML Reveal.js")
    }
}
