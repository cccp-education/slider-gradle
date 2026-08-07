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

    @Test
    fun `translateDeck with all target languages equal to source should skip without LLM`() {
        writeBuildFile()
        val deckDir = projectDir.resolve("fixtures/decks").apply { mkdirs() }
        deckDir.resolve("demo-deck-context.yml").writeText("""
            subject: "Demo"
            audience: "Testers"
            duration: 10
            languageCode: "fr"
            outputFile: "demo-fr-deck.adoc"
            author:
              name: "tester"
              email: "t@example.com"
        """.trimIndent())
        deckDir.resolve("demo-fr-deck.adoc").writeText("""
            = Demo

            == Slide
            Content
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(
                "translateDeck",
                "-Pdeck.context=${deckDir.resolve("demo-deck-context.yml").absolutePath}",
                "-Ptarget.languages=fr",
            )
            .forwardOutput()
            .build()

        assertThat(result.task(":translateDeck")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output).contains("No target languages to translate into")
        assertThat(deckDir.listFiles()).hasSize(2)
    }
}