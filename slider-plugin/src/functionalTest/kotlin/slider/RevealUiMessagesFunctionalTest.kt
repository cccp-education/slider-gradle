package slider

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class RevealUiMessagesFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun writeBuildFile() {
        projectDir.resolve("settings.gradle.kts").writeText("""
            rootProject.name = "reveal-ui-i18n-test"
        """.trimIndent())
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("education.cccp.slider")
            }
        """.trimIndent())
    }

    @Test
    fun `generateRevealUiMessages task should be registered and run successfully`() {
        writeBuildFile()

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("generateRevealUiMessages", "--quiet")
            .forwardOutput()
            .build()

        assertThat(result.task(":generateRevealUiMessages")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `generateRevealUiMessages should produce one messages_{code} js file per supported language`() {
        writeBuildFile()

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("generateRevealUiMessages", "--quiet")
            .forwardOutput()
            .build()

        val outputDir = projectDir.resolve("build/reveal-i18n")
        listOf(
            "en", "zh", "hi", "es", "fr", "ar", "bn", "pt", "ru", "ur"
        ).forEach { code ->
            val file = outputDir.resolve("messages_$code.js")
            assertThat(file)
                .withFailMessage("messages_$code.js should be generated under build/reveal-i18n")
                .exists()
        }
    }

    @Test
    fun `generateRevealUiMessages task should belong to the slider group`() {
        writeBuildFile()

        val output = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--group", "slider", "--quiet")
            .forwardOutput()
            .build()
            .output

        assertThat(output).contains("generateRevealUiMessages")
    }
}