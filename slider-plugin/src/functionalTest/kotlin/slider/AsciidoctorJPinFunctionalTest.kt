package slider

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Functional test for the AsciidoctorJ version pin (bug S-023/S-034).
 *
 * The `asciidoctorRevealJs` OUT_OF_PROCESS worker must run a single JRuby
 * runtime. Without the pin, asciidoctor-gradle resolves its default
 * AsciidoctorJ (`3.0.0`, `jruby-complete:9.4.8.0`) alongside the plugin
 * classpath AsciidoctorJ (`3.0.1`, `jruby-stdlib:9.4.14.0`): two
 * `jar-dependencies` gems (0.4.1 vs 0.5.4) end up on the same classpath, the
 * gems preparation aborts with `Gem::LoadError` and the gem jar stays empty.
 *
 * The pin is validated through the resolved AsciidoctorJ runtime classpath:
 * the asciidoctor-gradle default `3.0.0` must be upgraded to the pinned
 * `3.0.1` (the version carried by the plugin classpath). Without the pin the
 * worker keeps `3.0.0` and the duplicate JRuby reappears.
 */
class AsciidoctorJPinFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun writeBuildFile() {
        projectDir.resolve("settings.gradle.kts").writeText(
            """
            rootProject.name = "asciidoctorj-pin-test"
            """.trimIndent(),
        )
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("education.cccp.slider")
            }
            """.trimIndent(),
        )
    }

    @Test
    fun `the asciidoctorj version is pinned to the plugin classpath version`() {
        writeBuildFile()

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(
                "dependencies",
                "--configuration",
                "__\$\$asciidoctorj_asciidoctorRevealJs\$\$__r",
                "--quiet",
            )
            .forwardOutput()
            .build()

        assertThat(result.task(":dependencies")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        assertThat(result.output)
            .describedAs("asciidoctorj 3.0.0 must be upgraded to the pinned 3.0.1")
            .contains("org.asciidoctor:asciidoctorj:3.0.0 -> 3.0.1")
    }
}
