package slider

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TranslateAndGenerateCapsuleFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun writeBuildFile() {
        projectDir.resolve("settings.gradle.kts").writeText("""
            rootProject.name = "translate-and-generate-capsule-test"
        """.trimIndent())
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("education.cccp.slider")
            }
        """.trimIndent())
    }

    @Test
    fun `translateAndGenerateCapsule task should be registered in the slider group`() {
        writeBuildFile()

        val output = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--group", "slider", "--quiet")
            .forwardOutput()
            .build()
            .output

        assertThat(output).contains("translateAndGenerateCapsule")
    }

    @Test
    fun `translateAndGenerateCapsule should depend on translateDeck and generateCapsule`() {
        writeBuildFile()

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("translateAndGenerateCapsule", "--dry-run", "--quiet")
            .forwardOutput()
            .build()

        val output = result.output
        assertThat(output).contains("translateDeck")
        assertThat(output).contains("generateCapsule")
    }
}
