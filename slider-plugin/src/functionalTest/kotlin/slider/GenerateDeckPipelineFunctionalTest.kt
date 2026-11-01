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
 * generate-deck) in a single invocation. Two concerns only need Gradle:
 *
 *  * the **published surface** — the task is registered in the `generate` group
 *    with a localized description (one TestKit build);
 *  * the **input guards** — a missing `-Psubject` and an unreachable LLM both fail
 *    the task with a clear outcome (one build each).
 *
 * The description matrix (24 keys × 10 languages) is asserted purely by
 * [slider.i18n.TaskDescriptionsI18nCatalogTest], and the `-Planguage` wiring is
 * proven by the single smoke of [slider.i18n.TaskDescriptionsI18nFunctionalTest]
 * (XB-FP-6, S-322). The pipeline domain logic is covered by the unit tests of
 * baby-steps 8.3a/8.3b. Measured before: 7 TestKit builds for this class.
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
    fun `generateDeckPipeline task is registered in the generate group with a localized description`() {
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
