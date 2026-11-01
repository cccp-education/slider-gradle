package slider.content

/**
 * Parses an AsciiDoc deck source into a list of [SlideContent] — one per
 * level-2 heading (`==`). Uses a line-by-line pattern extended to paragraphs,
 * subtitles (`===`), bullet lists (`* `), and `[NOTE.speaker]` blocks.
 *
 * Recognised elements within a slide:
 * - `== Title`           — starts a new slide (title trimmed).
 * - `=== Subtitle`       — appends a subtitle to the current slide.
 * - `* item`             — appends an item to the current bullet list.
 *   A blank line or a non-bullet line closes the current list.
 * - `[NOTE.speaker]`     — starts a speaker note open-block (`--` delimited).
 * - Other non-blank lines — collected as paragraphs (one paragraph per
 *   contiguous block of non-blank, non-structural lines).
 *
 * Ignored lines:
 * - document header (`= Title`) and `author <email>` line following it,
 * - attribute entries (`:key: value`),
 * - attribute lists preceding a heading (`[.slide,...]`),
 * - single-line comments (`// ...`).
 *
 * Pure domain service — no Gradle, no Playwright, no I/O. Consumes a
 * string and returns pure value objects.
 */
object AsciidocContentParser {
    fun parseDeck(adocContent: String): List<SlideContent> {
        require(adocContent.isNotBlank()) { "adocContent must not be blank" }

        return adocContent
            .lines()
            .withIndex()
            .fold(ParseState(), ::reduce)
            .finish()
    }

    /**
     * Immutable accumulator threaded through [reduce] — replaces the former
     * mutable buffers and flags. The completed [slides] are never mutated;
     * each transition returns a new state.
     *
     * @param paragraphBuffer lines of the paragraph being accumulated.
     * @param listBuffer      items of the bullet list being accumulated.
     * @param afterDocumentHeader set once a level-1 heading is seen, so the
     *        author line right under the document title can be skipped.
     */
    private data class ParseState(
        val slides: List<SlideContent> = emptyList(),
        val title: String? = null,
        val subtitles: List<String> = emptyList(),
        val paragraphs: List<String> = emptyList(),
        val lists: List<List<String>> = emptyList(),
        val speakerNote: String? = null,
        val paragraphBuffer: List<String> = emptyList(),
        val listBuffer: List<String> = emptyList(),
        val inNote: Boolean = false,
        val noteOpened: Boolean = false,
        val noteLines: List<String> = emptyList(),
        val afterDocumentHeader: Boolean = false,
    ) {
        /** Flushes the trailing paragraph, list and slide, then exposes the result. */
        fun finish(): List<SlideContent> = flushList().flushParagraph().flushSlide().slides
    }

    /** Pure state transition for one source line. */
    private fun reduce(
        state: ParseState,
        indexedLine: IndexedValue<String>,
    ): ParseState {
        val line = indexedLine.value
        return when {
            isLevel1Heading(line) ->
                state
                    .flushList()
                    .flushParagraph()
                    .copy(afterDocumentHeader = true)

            state.afterDocumentHeader && indexedLine.index == 1 && isAuthorLine(line) -> state

            isSpeakerNoteStart(line) ->
                state
                    .flushList()
                    .flushParagraph()
                    .copy(inNote = true, noteOpened = false, noteLines = emptyList())

            isAttributeEntry(line) || isAttributeList(line) || isComment(line) -> state

            isLevel2Heading(line) ->
                state
                    .flushSlide()
                    .copy(
                        title = line.removePrefix("==").trim(),
                        subtitles = emptyList(),
                        paragraphs = emptyList(),
                        lists = emptyList(),
                        speakerNote = null,
                        paragraphBuffer = emptyList(),
                        listBuffer = emptyList(),
                        inNote = false,
                        noteOpened = false,
                        noteLines = emptyList(),
                    )

            isLevel3Heading(line) ->
                state
                    .flushList()
                    .flushParagraph()
                    .appendSubtitle(line.removePrefix("===").trim())

            state.inNote && !state.noteOpened && line.trimStart().startsWith("--") ->
                state.copy(noteOpened = true)

            state.inNote && state.noteOpened && line.trimStart().startsWith("--") -> state.closeNote()

            state.inNote && state.noteOpened -> state.appendNoteLine(line)

            isBulletItem(line) ->
                state
                    .flushParagraph()
                    .appendListItem(line.trimStart().removePrefix("*").trim())

            line.isBlank() -> state.flushList().flushParagraph()

            isLevel4PlusHeading(line) ->
                state
                    .flushList()
                    .flushParagraph()
                    .appendSubtitle(line.replaceFirst(level4Prefix, "").trim())

            else -> state.flushList().appendParagraphLine(line.trim())
        }
    }

    /** Appends the pending subtitles/paragraphs/lists as a slide (no-op without a title). */
    private fun ParseState.flushSlide(): ParseState {
        val currentTitle = title ?: return this
        // A title-only slide is valid.
        return copy(
            slides =
                slides +
                    SlideContent(
                        title = currentTitle,
                        subtitles = subtitles,
                        paragraphs = paragraphs,
                        lists = lists,
                        speakerNote = speakerNote,
                    ),
            subtitles = emptyList(),
            paragraphs = emptyList(),
            lists = emptyList(),
        )
    }

    /** Joins the pending paragraph lines into one paragraph (no-op when empty). */
    private fun ParseState.flushParagraph(): ParseState {
        if (paragraphBuffer.isEmpty()) return this
        val joined = paragraphBuffer.joinToString(" ").trim()
        return copy(
            paragraphs = if (joined.isNotBlank()) paragraphs + joined else paragraphs,
            paragraphBuffer = emptyList(),
        )
    }

    /** Closes the pending bullet list (no-op when empty). */
    private fun ParseState.flushList(): ParseState {
        if (listBuffer.isEmpty()) return this
        return copy(lists = lists + listOf(listBuffer), listBuffer = emptyList())
    }

    private fun ParseState.closeNote(): ParseState {
        val note = noteLines.joinToString("\n").trim()
        return copy(
            speakerNote = if (note.isNotBlank()) note else speakerNote,
            inNote = false,
            noteOpened = false,
            noteLines = emptyList(),
        )
    }

    private fun ParseState.appendSubtitle(subtitle: String): ParseState = copy(subtitles = subtitles + subtitle)

    private fun ParseState.appendNoteLine(line: String): ParseState = copy(noteLines = noteLines + line)

    private fun ParseState.appendListItem(item: String): ParseState = copy(listBuffer = listBuffer + item)

    private fun ParseState.appendParagraphLine(line: String): ParseState = copy(paragraphBuffer = paragraphBuffer + line)

    private val level4Prefix = Regex("^={4,}\\s*")

    private fun isLevel1Heading(line: String): Boolean = line.startsWith("= ") && !line.startsWith("== ")

    private fun isLevel2Heading(line: String): Boolean = line.startsWith("== ") && !line.startsWith("=== ")

    private fun isLevel3Heading(line: String): Boolean = line.startsWith("=== ") && !line.startsWith("==== ")

    private fun isLevel4PlusHeading(line: String): Boolean = line.startsWith("==== ")

    private fun isAuthorLine(line: String): Boolean = line.isNotBlank() && !line.startsWith(":") && !line.startsWith("=") && !line.startsWith("[")

    private fun isAttributeEntry(line: String): Boolean = line.trimStart().startsWith(":")

    private fun isAttributeList(line: String): Boolean = line.trimStart().startsWith("[") && line.trimEnd().endsWith("]")

    private fun isComment(line: String): Boolean = line.trimStart().startsWith("//")

    private fun isBulletItem(line: String): Boolean = line.trimStart().startsWith("* ") || line.trimStart() == "*"

    private fun isSpeakerNoteStart(line: String): Boolean {
        val t = line.trimStart()
        return t.startsWith("[NOTE.speaker]") || t.startsWith("[NOTE.speaker,")
    }
}
