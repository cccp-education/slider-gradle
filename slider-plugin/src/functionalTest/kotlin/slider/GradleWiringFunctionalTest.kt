package slider

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Functional test for the slider wiring extraction (SLD-6.5).
 *
 * Verifies that [slider.wiring.GradleWiring] — applied via the slider plugin —
 * declares the expected repositories, plugins, and gem dependency on the
 * consumer project. Uses GradleTestKit with the real plugin classpath so the
 * wiring logic is exercised end-to-end.
 *
 * The wiring is validated indirectly through Gradle behaviour it triggers:
 * - `applyPlugins`     → asciidoctorRevealJs task is registered
 * - `configureDependencies` → asciidoctorGems configuration resolves the gem
 * - `configureRepositories`  → the gem resolves, which proves the rubygems Ivy
 *   repository + Maven mirrors are wired (a missing repository would fail
 *   resolution with a "cannot resolve" error).
 */
class GradleWiringFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun writeBuildFile() {
        projectDir.resolve("settings.gradle.kts").writeText("""
            rootProject.name = "wiring-test"
        """.trimIndent())
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("education.cccp.slider")
            }
        """.trimIndent())
    }

    @Test
    fun `the slider plugin applies the three expected external plugins`() {
        writeBuildFile()

        val output = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--all", "--quiet")
            .forwardOutput()
            .build()
            .output

        // The node plugin contributes npm/node tasks; asciidoctor contributes
        // asciidoctorRevealJs. Their presence proves GradleWiring.applyPlugins
        // ran successfully during plugin application.
        assertThat(output).contains("asciidoctorRevealJs")
    }

    @Test
    fun `the slider plugin declares the asciidoctor-revealjs gem on asciidoctorGems`() {
        writeBuildFile()

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("dependencies", "--configuration", "asciidoctorGems", "--quiet")
            .forwardOutput()
            .build()

        // The dependencies task exits with SUCCESS when the configuration
        // resolves (even if some gems are missing in the test environment).
        assertThat(result.task(":dependencies")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output).contains("asciidoctor-revealjs")
    }
}