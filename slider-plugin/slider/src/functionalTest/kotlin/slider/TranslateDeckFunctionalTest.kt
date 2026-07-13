package slider

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TranslateDeckFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun writeBuildFile() {
        projectDir.resolve("settings.gradle.kts").writeText("""
            rootProject.name = "translate-deck-test"
        """.trimIndent())
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("education.cccp.slider")
            }
        """.trimIndent())
    }

    @Test
    fun `translateDeck task should be registered in the translator group`() {
        writeBuildFile()

        val output = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--group", "translator", "--quiet")
            .forwardOutput()
            .build()
            .output

        assertThat(output).contains("translateDeck")
    }

    @Test
    fun `translateDeck task should appear in the full task list`() {
        writeBuildFile()

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--quiet")
            .forwardOutput()
            .build()

        assertThat(result.output).contains("translateDeck")
        assertThat(result.output).contains("Translate a deck")
    }
}