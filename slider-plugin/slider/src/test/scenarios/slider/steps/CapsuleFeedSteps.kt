package slider.steps

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.capsule.AsciidocSpeakerNoteParser
import slider.capsule.CapsuleScript
import slider.capsule.CapsuleScriptWriter
import java.io.File

class CapsuleFeedSteps : En {

    private var deckName: String = ""
    private var adocContent: String = ""
    private var script: CapsuleScript? = null
    private var rendered: String = ""
    private var caught: Throwable? = null

    init {

        Given("an AsciiDoc deck {string} with content") { name: String, content: String ->
            deckName = name
            adocContent = content.trimIndent()
        }

        Given("the real demo deck {string} is loaded") { deckBasename: String ->
            deckName = deckBasename
            val repoRoot = resolveRepoRoot()
            val deck = File(repoRoot, "slides/misc/$deckBasename.adoc")
                .takeIf { it.exists() }
                ?: File(repoRoot, "foundry/public/slider-gradle/slides/misc/$deckBasename.adoc")
                    .takeIf { it.exists() }
                    ?: error("Demo deck not found: $deckBasename")
            adocContent = deck.readText()
        }

        Given("a blank deck name") {
            deckName = "   "
        }

        When("the capsule script is generated from the deck") {
            script = AsciidocSpeakerNoteParser.parse(adocContent, deckName.trim().ifBlank { deckName })
        }

        When("the capsule script generation is attempted") {
            try {
                script = AsciidocSpeakerNoteParser.parse(adocContent, deckName)
            } catch (t: Throwable) {
                caught = t
            }
        }

        When("the script is rendered as plain text") {
            rendered = script?.let { CapsuleScriptWriter.write(it) } ?: ""
        }

        Then("the script deck name should be {string}") { expected: String ->
            assertThat(script?.deckName).isEqualTo(expected)
        }

        Then("the script should contain {int} segment(s)") { count: Int ->
            assertThat(script?.segments).hasSize(count)
        }

        Then("the segment {int} should have title {string}") { idx: Int, title: String ->
            assertThat(script?.segments?.get(idx - 1)?.title).isEqualTo(title)
        }

        Then("the segment {int} should have speakerNote {string}") { idx: Int, note: String ->
            assertThat(script?.segments?.get(idx - 1)?.speakerNote).isEqualTo(note)
        }

        Then("the segment {int} should have index {int}") { idx: Int, expected: Int ->
            assertThat(script?.segments?.get(idx - 1)?.index).isEqualTo(expected)
        }

        Then("the script should be empty") {
            assertThat(script?.isEmpty).isTrue()
        }

        Then("the first line should be {string}") { expected: String ->
            assertThat(rendered.lines().firstOrNull()).isEqualTo(expected)
        }

        Then("the second line should be blank") {
            val lines = rendered.lines()
            assertThat(lines.getOrNull(1)).isEqualTo("")
        }

        Then("the text should contain {string}") { expected: String ->
            assertThat(rendered).contains(expected)
        }

        Then("the generation should fail with a message containing {string}") { fragment: String ->
            assertThat(caught)
                .withFailMessage("Expected a failure containing '$fragment'")
                .isNotNull()
            assertThat(caught!!.message).contains(fragment)
        }
    }

    private fun resolveRepoRoot(): File {
        var dir = File(".").absoluteFile
        while (dir != null && dir.parentFile != null) {
            if (File(dir, "slides/misc/capsule-feed-demo-fr-deck.adoc").exists()) return dir
            if (File(dir, "foundry/public/slider-gradle/slides/misc/capsule-feed-demo-fr-deck.adoc").exists()) return dir
            dir = dir.parentFile
        }
        return File(".").absoluteFile
    }
}