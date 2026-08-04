package slider.content

/**
 * Parses an AsciiDoc deck source into a list of [SlideContent] — one per
 * level-2 heading (`==`). Reuses the line-by-line pattern of
 * [slider.capsule.AsciidocSpeakerNoteParser] and extends it to paragraphs,
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

        val lines = adocContent.lines()
        val slides = mutableListOf<SlideContent>()

        var currentTitle: String? = null
        val subtitles = mutableListOf<String>()
        val paragraphs = mutableListOf<String>()
        val lists = mutableListOf<List<String>>()
        var speakerNote: String? = null

        val currentParagraph = mutableListOf<String>()
        val currentList = mutableListOf<String>()

        var inNote = false
        var noteOpened = false
        val noteLines = mutableListOf<String>()

        var afterDocumentHeader = false

        for ((index, line) in lines.withIndex()) {
            when {
                isLevel1Heading(line) -> {
                    flushList(currentList, lists)
                    flushParagraph(currentParagraph, paragraphs)
                    afterDocumentHeader = true
                }

                afterDocumentHeader && index == 1 && isAuthorLine(line) -> {
                    // author line following `= Title`, skip
                }

                isSpeakerNoteStart(line) -> {
                    flushList(currentList, lists)
                    flushParagraph(currentParagraph, paragraphs)
                    inNote = true
                    noteOpened = false
                    noteLines.clear()
                }

                isAttributeEntry(line) || isAttributeList(line) || isComment(line) -> {
                    // ignored structural lines
                }

                isLevel2Heading(line) -> {
                    flush(slideSink = slides, currentTitle, subtitles, paragraphs, lists, speakerNote)
                    currentTitle = line.removePrefix("==").trim()
                    subtitles.clear()
                    paragraphs.clear()
                    lists.clear()
                    speakerNote = null
                    currentParagraph.clear()
                    currentList.clear()
                    inNote = false
                    noteOpened = false
                    noteLines.clear()
                }

                isLevel3Heading(line) -> {
                    flushList(currentList, lists)
                    flushParagraph(currentParagraph, paragraphs)
                    subtitles.add(line.removePrefix("===").trim())
                }

                inNote && !noteOpened && line.trimStart().startsWith("--") -> {
                    noteOpened = true
                }

                inNote && noteOpened && line.trimStart().startsWith("--") -> {
                    val note = noteLines.joinToString("\n").trim()
                    if (note.isNotBlank()) speakerNote = note
                    inNote = false
                    noteOpened = false
                    noteLines.clear()
                }

                inNote && noteOpened -> noteLines.add(line)

                isBulletItem(line) -> {
                    flushParagraph(currentParagraph, paragraphs)
                    currentList.add(line.trimStart().removePrefix("*").trim())
                }

                line.isBlank() -> {
                    flushList(currentList, lists)
                    flushParagraph(currentParagraph, paragraphs)
                }

                isLevel4PlusHeading(line) -> {
                    flushList(currentList, lists)
                    flushParagraph(currentParagraph, paragraphs)
                    subtitles.add(line.replaceFirst(Regex("^={4,}\\s*"), "").trim())
                }

                else -> {
                    flushList(currentList, lists)
                    currentParagraph.add(line.trim())
                }
            }
        }
        flushList(currentList, lists)
        flushParagraph(currentParagraph, paragraphs)
        flush(slideSink = slides, currentTitle, subtitles, paragraphs, lists, speakerNote)

        return slides.toList()
    }

    private fun flush(
        slideSink: MutableList<SlideContent>,
        title: String?,
        subtitles: MutableList<String>,
        paragraphs: MutableList<String>,
        lists: MutableList<List<String>>,
        speakerNote: String?,
    ) {
        if (title == null) return
        // Skip slides with no content at all? No — a title-only slide is valid.
        slideSink.add(
            SlideContent(
                title = title,
                subtitles = subtitles.toList(),
                paragraphs = paragraphs.toList(),
                lists = lists.toList(),
                speakerNote = speakerNote,
            ),
        )
        subtitles.clear()
        paragraphs.clear()
        lists.clear()
    }

    private fun flushParagraph(sink: MutableList<String>, paragraphs: MutableList<String>) {
        if (sink.isEmpty()) return
        val joined = sink.joinToString(" ").trim()
        if (joined.isNotBlank()) paragraphs.add(joined)
        sink.clear()
    }

    private fun flushList(sink: MutableList<String>, lists: MutableList<List<String>>) {
        if (sink.isEmpty()) return
        lists.add(sink.toList())
        sink.clear()
    }

    private fun isLevel1Heading(line: String): Boolean =
        line.startsWith("= ") && !line.startsWith("== ")

    private fun isLevel2Heading(line: String): Boolean =
        line.startsWith("== ") && !line.startsWith("=== ")

    private fun isLevel3Heading(line: String): Boolean =
        line.startsWith("=== ") && !line.startsWith("==== ")

    private fun isLevel4PlusHeading(line: String): Boolean =
        line.startsWith("==== ")

    private fun isAuthorLine(line: String): Boolean =
        line.isNotBlank() && !line.startsWith(":") && !line.startsWith("=") && !line.startsWith("[")

    private fun isAttributeEntry(line: String): Boolean =
        line.trimStart().startsWith(":")

    private fun isAttributeList(line: String): Boolean =
        line.trimStart().startsWith("[") && line.trimEnd().endsWith("]")

    private fun isComment(line: String): Boolean =
        line.trimStart().startsWith("//")

    private fun isBulletItem(line: String): Boolean =
        line.trimStart().startsWith("* ") || line.trimStart() == "*"

    private fun isSpeakerNoteStart(line: String): Boolean {
        val t = line.trimStart()
        return t.startsWith("[NOTE.speaker]") || t.startsWith("[NOTE.speaker,")
    }
}