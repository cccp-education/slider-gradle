package slider.i18n

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class AssistantManagerI18nFunctionalTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `RAG tasks have i18n descriptions in English`() {
        writeBuildFile()
        writeSettingsFile()

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--group", "collect", "-Planguage=en")
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertThat(result.output).contains("reindexRag")
        assertThat(result.output).contains("Force a full rebuild of the RAG embedding index")
    }

    @Test
    fun `RAG tasks have i18n descriptions in French`() {
        writeBuildFile()
        writeSettingsFile()

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--group", "collect", "-Planguage=fr")
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertThat(result.output).contains("reindexRag")
        assertThat(result.output).contains("Forcer une reconstruction complète")
    }

    @Test
    fun `smoke test tasks have i18n descriptions in English`() {
        writeBuildFile()
        writeSettingsFile()

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--group", "slider-ai", "-Planguage=en")
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertThat(result.output).contains("helloOllama")
        assertThat(result.output).contains("smoke test")
    }

    @Test
    fun `smoke test tasks have i18n descriptions in French`() {
        writeBuildFile()
        writeSettingsFile()

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--group", "slider-ai", "-Planguage=fr")
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertThat(result.output).contains("helloOllama")
        assertThat(result.output).contains("test de fumée")
    }

    @Test
    fun `generate tasks have i18n descriptions in English`() {
        writeBuildFile()
        writeSettingsFile()

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--group", "generate", "-Planguage=en")
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertThat(result.output).contains("proposeDeckContext")
        assertThat(result.output).contains("generateDeck")
        assertThat(result.output).contains("RAG + LLM")
    }

    @Test
    fun `generate tasks have i18n descriptions in French`() {
        writeBuildFile()
        writeSettingsFile()

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--group", "generate", "-Planguage=fr")
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertThat(result.output).contains("proposeDeckContext")
        assertThat(result.output).contains("generateDeck")
        assertThat(result.output).contains("RAG + LLM")
    }

    private fun writeBuildFile() {
        testProjectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("education.cccp.slider")
            }
        """.trimIndent())
    }

    private fun writeSettingsFile() {
        testProjectDir.resolve("settings.gradle.kts").writeText("""
            rootProject.name = "i18n-functional-test"
        """.trimIndent())
    }
}
