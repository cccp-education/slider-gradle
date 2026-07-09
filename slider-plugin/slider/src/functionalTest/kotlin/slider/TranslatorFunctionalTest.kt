package slider

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TranslatorFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun writeBuildFile() {
        projectDir.resolve("settings.gradle.kts").writeText("""
            rootProject.name = "translator-test"
        """.trimIndent())
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("education.cccp.slider.translator")
            }
        """.trimIndent())
    }

    private fun runTask(task: String): String =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(task, "--quiet")
            .forwardOutput()
            .build()
            .output

    @Test
    fun `displaySupportedLanguages should list all 10 ISO codes`() {
        writeBuildFile()

        val output = runTask("displaySupportedLanguages")

        listOf("en", "zh", "hi", "es", "fr", "ar", "bn", "pt", "ru", "ur").forEach { code ->
            assertThat(output)
                .withFailMessage("displaySupportedLanguages output should contain ISO code '$code'")
                .contains(code)
        }
    }

    @Test
    fun `displaySupportedLanguages should list native names not English display names`() {
        writeBuildFile()

        val output = runTask("displaySupportedLanguages")

        assertThat(output).contains("Français")
        assertThat(output).contains("العربية")
        assertThat(output).contains("中文")
    }

    @Test
    fun `displaySupportedLanguages should not contain legacy English-only display names`() {
        writeBuildFile()

        val output = runTask("displaySupportedLanguages")

        assertThat(output).doesNotContain("French(fr)")
    }

    @Test
    fun `translate task should be registered for all 10 languages permutations`() {
        writeBuildFile()

        val output = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--group", "translator", "--quiet")
            .forwardOutput()
            .build()
            .output

        assertThat(output).contains("translateFrToEn")
        assertThat(output).contains("translateEnToZh")
        assertThat(output).contains("translateFrToAr")
        assertThat(output).contains("translateZhToFr")
    }
}