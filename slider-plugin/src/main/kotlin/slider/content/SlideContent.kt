package slider.content

/**
 * Aggregate root modeling the content of a single AsciiDoc slide — the title,
 * optional subtitles, paragraphs, bullet lists, and an optional speaker note.
 *
 * Pure value object — no Gradle, no Playwright, no I/O. Built by
 * [AsciidocContentParser] from a `.adoc` source, consumed by rendering
 * assertions (overflow, notes, font size, contrast).
 *
 * @param title        slide title (level-2 heading `==`), must be non-blank.
 * @param subtitles    level-3 headings (`===`), each must be non-blank.
 * @param paragraphs   paragraph bodies, each must be non-blank.
 * @param lists        bullet lists, each a non-empty list of non-blank items.
 * @param speakerNote  speaker note extracted from a `[NOTE.speaker]` block,
 *                     `null` when the slide has no note. Must be non-blank
 *                     when present.
 */
data class SlideContent(
    val title: String,
    val subtitles: List<String> = emptyList(),
    val paragraphs: List<String> = emptyList(),
    val lists: List<List<String>> = emptyList(),
    val speakerNote: String? = null,
) {
    init {
        require(title.isNotBlank()) { "SlideContent.title must not be blank" }
        subtitles.forEachIndexed { i, s ->
            require(s.isNotBlank()) { "SlideContent.subtitles[$i] must not be blank" }
        }
        paragraphs.forEachIndexed { i, p ->
            require(p.isNotBlank()) { "SlideContent.paragraphs[$i] must not be blank" }
        }
        lists.forEachIndexed { i, items ->
            require(items.isNotEmpty()) { "SlideContent.lists[$i] must not be empty" }
            items.forEachIndexed { j, item ->
                require(item.isNotBlank()) { "SlideContent.lists[$i] item $j must not be blank" }
            }
        }
        require(speakerNote?.isNotBlank() ?: true) { "SlideContent.speakerNote must not be blank" }
    }

    /** `true` when the slide has a non-null speaker note. */
    fun hasSpeakerNote(): Boolean = speakerNote != null

    /** `true` when the slide has no subtitles, paragraphs, lists, or speaker note. */
    fun isEmpty(): Boolean =
        subtitles.isEmpty() && paragraphs.isEmpty() && lists.isEmpty() && speakerNote == null
}