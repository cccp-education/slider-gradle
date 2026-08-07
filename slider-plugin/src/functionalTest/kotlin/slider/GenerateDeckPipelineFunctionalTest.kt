package slider

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Functional tests for the `generateDeckPipeline` Gradle task — baby-step 8.3c
 * (SLD-8 US-8.3).
 *
 * `generateDeckPipeline` is the thin Gradle adapter that orchestrates the koog
 * [slider.pipeline.DeckPipelineGraph] (propose-context → validate-context →
 * generate-deck) in a single invocation. These tests verify the Gradle surface
 * of the task — registration, i18n descriptions, and input guards — without
 * contacting any LLM (the pipeline domain logic is covered by the unit tests
 * of baby-steps 8.3a/8.3b).
 */
class GenerateDeckPipelineFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun writeBuildFile() {
        projectDir.resolve("settings.gradle.kts").writeText("""
            rootProject.name = "generate-deck-pipeline-test"
        """.trimIndent())
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("education.cccp.slider")
            }
        """.trimIndent())
    }

    @Test
    fun `generateDeckPipeline task should be registered`() {
        writeBuildFile()

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--all", "--quiet")
            .forwardOutput()
            .build()

        assertThat(result.output).contains("generateDeckPipeline")
    }

    @Test
    fun `generateDeckPipeline task should be in the generate group in English`() {
        writeBuildFile()

        val output = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--group", "generate", "-Planguage=en", "--quiet")
            .forwardOutput()
            .build()
            .output

        assertThat(output).contains("generateDeckPipeline")
    }

    @Test
    fun `generateDeckPipeline task should be in the generate group in French`() {
        writeBuildFile()

        val output = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--group", "generate", "-Planguage=fr", "--quiet")
            .forwardOutput()
            .build()
            .output

        assertThat(output).contains("generateDeckPipeline")
    }

    @Test
    fun `generateDeckPipeline task should have an i18n description in English`() {
        writeBuildFile()

        val output = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--group", "generate", "-Planguage=en", "--quiet")
            .forwardOutput()
            .build()
            .output

        assertThat(output).contains("generateDeckPipeline")
        assertThat(output).contains("koog pipeline")
    }

    @Test
    fun `generateDeckPipeline task should have an i18n description in French`() {
        writeBuildFile()

        val output = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--group", "generate", "-Planguage=fr", "--quiet")
            .forwardOutput()
            .build()
            .output

        assertThat(output).contains("generateDeckPipeline")
        assertThat(output).contains("pipeline koog")
    }

    @Test
    fun `generateDeckPipeline without -Psubject should fail with a clear message`() {
        writeBuildFile()

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("generateDeckPipeline", "--quiet")
            .forwardOutput()
            .buildAndFail()

        assertThat(result.output).contains("subject")
        assertThat(result.task(":generateDeckPipeline")?.outcome).isEqualTo(TaskOutcome.FAILED)
    }

    @Test
    fun `generateDeckPipeline with -Psubject but no LLM should fail on the propose node`() {
        writeBuildFile()

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(
                "generateDeckPipeline",
                "-Psubject=Kotlin Coroutines",
                "-Pollama.baseUrl=http://localhost:1",
                "-Pollama.timeout=1",
                "--quiet",
            )
            .forwardOutput()
            .buildAndFail()

        assertThat(result.output).contains("generateDeckPipeline")
        assertThat(result.task(":generateDeckPipeline")?.outcome).isEqualTo(TaskOutcome.FAILED)
    }
}