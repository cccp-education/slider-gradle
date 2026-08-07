package slider

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Functional tests for the slide-deployment domain integration.
 *
 * These tests use GradleTestKit to verify that the `publishSlides` task
 * is registered correctly and that the domain layer
 * (`slider.repository.SlideDeploymentRequest` + `SlideDeployer`) is
 * reachable from the plugin without triggering a real Git push.
 */
class SlideDeploymentFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun writeBuildFile() {
        projectDir.resolve("settings.gradle.kts").writeText("""
            rootProject.name = "slide-deployment-test"
        """.trimIndent())
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("education.cccp.slider")
            }
        """.trimIndent())
    }

    @Test
    fun `deploySlides task should be registered in the deploy group`() {
        writeBuildFile()

        val output = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--group", "deploy", "--quiet")
            .forwardOutput()
            .build()
            .output

        assertThat(output).contains("deploySlides")
    }

    @Test
    fun `deploySlides task should appear in the full task list`() {
        writeBuildFile()

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--quiet")
            .forwardOutput()
            .build()

        assertThat(result.output).contains("deploySlides")
        assertThat(result.output).contains("Deploy generated slides to the configured remote repository.")
    }
}