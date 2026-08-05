package slider.i18n

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

class TaskDescriptionsI18nFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File

    private fun writeBuildFile() {
        testProjectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("education.cccp.slider")
            }
        """.trimIndent())
    }

    private fun writeSettingsFile() {
        testProjectDir.resolve("settings.gradle.kts").writeText("""
            rootProject.name = "task-descriptions-i18n-test"
        """.trimIndent())
    }

    private fun runTasksWithLanguage(language: String): String {
        writeBuildFile()
        writeSettingsFile()
        return GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--all", "-Planguage=$language")
            .withPluginClasspath()
            .forwardOutput()
            .build()
            .output
    }

    @ParameterizedTest
    @ValueSource(strings = ["en", "fr", "zh", "hi", "es", "ar", "bn", "pt", "ru", "ur"])
    fun `all slider tasks have non-blank descriptions in language`(language: String) {
        val output = runTasksWithLanguage(language)

        val i18nTaskNames = listOf(
            "cleanBuild",
            "asciidoctorRevealJs",
            "generateDashboard",
            "asciidoctor",
            "serveSlides",
            "installPlaywright",
            "visualTest",
            "deploySlides",
            "generateCapsule",
            "translateAndGenerateCapsule",
            "reportTests",
            "reportFunctionalTests",
            "generateRevealUiMessages",
            "translateDeck",
            "reindexRag",
            "proposeDeckContext",
            "generateDeck",
        )

        for (taskName in i18nTaskNames) {
            assertThat(output)
                .describedAs("Task '$taskName' should be listed in language '$language'")
                .contains(taskName)
        }
    }

    @Test
    fun `cleanBuild task has i18n description in English`() {
        val output = runTasksWithLanguage("en")
        assertThat(output).contains("cleanBuild")
        assertThat(output).contains("Delete generated presentation artifacts")
    }

    @Test
    fun `cleanBuild task has i18n description in French`() {
        val output = runTasksWithLanguage("fr")
        assertThat(output).contains("cleanBuild")
        assertThat(output).contains("Supprime les artefacts de présentation")
    }

    @Test
    fun `asciidoctorRevealJs task has i18n description in English`() {
        val output = runTasksWithLanguage("en")
        assertThat(output).contains("asciidoctorRevealJs")
        assertThat(output).contains("Compile AsciiDoc sources into a Reveal.js HTML presentation")
    }

    @Test
    fun `asciidoctorRevealJs task has i18n description in French`() {
        val output = runTasksWithLanguage("fr")
        assertThat(output).contains("asciidoctorRevealJs")
        assertThat(output).contains("Compile les sources AsciiDoc en présentation HTML Reveal.js")
    }

    @Test
    fun `serveSlides task has i18n description in English`() {
        val output = runTasksWithLanguage("en")
        assertThat(output).contains("serveSlides")
        assertThat(output).contains("Serve slides using the serve package")
    }

    @Test
    fun `serveSlides task has i18n description in French`() {
        val output = runTasksWithLanguage("fr")
        assertThat(output).contains("serveSlides")
        assertThat(output).contains("Sert les diapositives")
    }

    @Test
    fun `visualTest task has i18n description in English`() {
        val output = runTasksWithLanguage("en")
        assertThat(output).contains("visualTest")
        assertThat(output).contains("Run Playwright visual snapshot tests")
    }

    @Test
    fun `visualTest task has i18n description in French`() {
        val output = runTasksWithLanguage("fr")
        assertThat(output).contains("visualTest")
        assertThat(output).contains("Exécute les tests de snapshot visuel Playwright")
    }

    @Test
    fun `translateDeck task has i18n description in English`() {
        val output = runTasksWithLanguage("en")
        assertThat(output).contains("translateDeck")
        assertThat(output).contains("Translate a deck")
    }

    @Test
    fun `translateDeck task has i18n description in French`() {
        val output = runTasksWithLanguage("fr")
        assertThat(output).contains("translateDeck")
        assertThat(output).contains("Traduit un deck")
    }
}