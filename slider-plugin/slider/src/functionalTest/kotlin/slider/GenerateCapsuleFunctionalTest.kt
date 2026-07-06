package slider

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class GenerateCapsuleFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun writeBuildFile() {
        projectDir.resolve("settings.gradle.kts").writeText("""
            rootProject.name = "generate-capsule-test"
        """.trimIndent())
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("education.cccp.slider")
            }
        """.trimIndent())
    }

    private fun writeDeck(name: String, slides: List<Pair<String, String>>): File {
        val miscDir = projectDir.resolve("slides/misc").apply { mkdirs() }
        val sb = StringBuilder()
        sb.append("= $name\n")
        sb.append("author\n")
        sb.append(":revealjs_theme: sky\n\n")
        slides.forEach { (title, note) ->
            sb.append("== $title\n\n")
            sb.append("content\n\n")
            sb.append("[NOTE.speaker]\n")
            sb.append("--\n")
            sb.append("$note\n")
            sb.append("--\n\n")
        }
        return miscDir.resolve("$name.adoc").apply { writeText(sb.toString()) }
    }

    @Test
    fun `generateCapsule task should be registered in the slider group`() {
        writeBuildFile()

        val output = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--group", "slider", "--quiet")
            .forwardOutput()
            .build()
            .output

        assertThat(output).contains("generateCapsule")
    }

    @Test
    fun `generateCapsule should produce a dash-script txt matching the capsule-gradle contract`() {
        writeBuildFile()
        writeDeck(
            name = "demo-deck",
            slides = listOf(
                "Intro" to "Welcome to the demo.",
                "Topic" to "Today we cover capsule feed.",
            ),
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("generateCapsule", "--quiet")
            .forwardOutput()
            .build()

        assertThat(result.task(":generateCapsule")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        val scriptFile = projectDir.resolve("build/capsule/demo-deck-script.txt")
        assertThat(scriptFile).exists()

        val content = scriptFile.readText()
        assertThat(content).startsWith("=== CAPSULE SCRIPT : demo-deck ===")
        assertThat(content).contains("--- SLIDE 1 : Intro ---")
        assertThat(content).contains("--- SLIDE 2 : Topic ---")
        assertThat(content).contains("Welcome to the demo.")
        assertThat(content).contains("Today we cover capsule feed.")
    }

    @Test
    fun `generateCapsule should skip decks that have no speaker notes block`() {
        writeBuildFile()
        writeDeck(
            name = "with-notes",
            slides = listOf("Intro" to "Has a note."),
        )
        val miscDir = projectDir.resolve("slides/misc")
        miscDir.resolve("no-notes.adoc").writeText("""
            = no-notes
            author

            == Slide

            no speaker note here
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("generateCapsule", "--quiet")
            .forwardOutput()
            .build()

        assertThat(result.task(":generateCapsule")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(projectDir.resolve("build/capsule/with-notes-script.txt")).exists()
        assertThat(projectDir.resolve("build/capsule/no-notes-script.txt")).doesNotExist()
    }

    @Test
    fun `generateCapsule should emit 1-based slide indices in segment order`() {
        writeBuildFile()
        writeDeck(
            name = "ordered",
            slides = (1..3).map { i -> "Slide $i" to "Note $i." },
        )

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("generateCapsule", "--quiet")
            .forwardOutput()
            .build()

        val content = projectDir.resolve("build/capsule/ordered-script.txt").readText()
        assertThat(content).contains("--- SLIDE 1 : Slide 1 ---")
        assertThat(content).contains("--- SLIDE 2 : Slide 2 ---")
        assertThat(content).contains("--- SLIDE 3 : Slide 3 ---")
    }
}