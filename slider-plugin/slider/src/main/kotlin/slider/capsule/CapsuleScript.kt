package slider.capsule

/**
 * One slide segment inside a capsule script.
 *
 * @param index 1-based slide position.
 * @param title slide title (AsciiDoc `== ` heading text), must be non-blank.
 * @param speakerNote narration body, must be non-blank.
 */
data class SlideSegment(
    val index: Int,
    val title: String,
    val speakerNote: String,
) {
    init {
        require(title.isNotBlank()) { "SlideSegment.title must not be blank (index=$index)" }
        require(speakerNote.isNotBlank()) { "SlideSegment.speakerNote must not be blank (index=$index)" }
    }
}

/**
 * Aggregate root — the full capsule script for one deck.
 *
 * @param deckName deck name (without extension), must be non-blank.
 * @param segments ordered slide segments, must be non-empty.
 */
data class CapsuleScript(
    val deckName: String,
    val segments: List<SlideSegment>,
) {
    init {
        require(deckName.isNotBlank()) { "CapsuleScript.deckName must not be blank" }
    }

    val isEmpty: Boolean get() = segments.isEmpty()
}

/**
 * Serializes a [CapsuleScript] into the plain-text contract consumed by
 * capsule-gradle (`CapsuleManager.parseScript`).
 *
 * Contract format:
 * ```
 * === CAPSULE SCRIPT : <deckName> ===
 *
 * --- SLIDE <n> : <title> ---
 * <speakerNote>
 *
 * --- SLIDE <n+1> : <title> ---
 * <speakerNote>
 * ```
 */
object CapsuleScriptWriter {

    fun write(script: CapsuleScript): String {
        require(script.segments.isNotEmpty()) { "CapsuleScript.segments must not be empty" }
        val sb = StringBuilder()
        sb.append("=== CAPSULE SCRIPT : ${script.deckName} ===")
        sb.append("\n")
        script.segments.forEach { seg ->
            sb.append("\n")
            sb.append("--- SLIDE ${seg.index} : ${seg.title.trim()} ---\n")
            sb.append(seg.speakerNote.trim())
            sb.append("\n")
        }
        return sb.toString().trimEnd() + "\n"
    }
}