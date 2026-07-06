package slider.capsule

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Smoke test against the real committed deck `slides/misc/kotlin-intro-deck.adoc`.
 * Validates the parser + writer round-trip on a non-trivial deck (10 slides).
 *
 * NOTE: this test runs from the plugin module which has no direct access to the
 * repo root `slides/`. It resolves the deck by walking up from `projectDir`.
 */
class CapsuleSmokeRealDeckTest {

    @Test
    fun `parse kotlin-intro-deck then write should produce a contract-consumable script`() {
        val repoRoot = resolveRepoRoot()
        val deck = File(repoRoot, "slides/misc/kotlin-intro-deck.adoc")
        if (!deck.exists()) {
            // Smoke test is opportunistic — skip silently if the deck isn't reachable.
            return
        }

        val script = AsciidocSpeakerNoteParser.parse(deck.readText(), deckName = "kotlin-intro-deck")

        assertThat(script.segments).hasSize(10)
        assertThat(script.segments.map { it.index }).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        val rendered = CapsuleScriptWriter.write(script)
        assertThat(rendered).startsWith("=== CAPSULE SCRIPT : kotlin-intro-deck ===")
        assertThat(rendered.lines().count { it.startsWith("--- SLIDE") }).isEqualTo(10)

        // Contract markers expected by capsule-gradle CapsuleManager.parseScript:
        // first line = header, then "--- SLIDE n : title ---" headers, body lines non-blank.
        val lines = rendered.lines()
        assertThat(lines[0]).isEqualTo("=== CAPSULE SCRIPT : kotlin-intro-deck ===")
        assertThat(lines[1]).isEqualTo("")
        assertThat(lines[2]).startsWith("--- SLIDE 1 : ")
    }

    private fun resolveRepoRoot(): File {
        var dir = File(".").absoluteFile
        while (dir != null && dir.parentFile != null) {
            if (File(dir, "slides/misc/kotlin-intro-deck.adoc").exists()) return dir
            if (File(dir, "foundry/public/slider-gradle/slides/misc/kotlin-intro-deck.adoc").exists()) return dir
            dir = dir.parentFile
        }
        return File(".").absoluteFile
    }
}