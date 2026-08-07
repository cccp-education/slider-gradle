package slider.content

/**
 * Validates that a [SlideContent] renders correctly within a [SlideLayout].
 *
 * Pure domain logic — no Playwright, no Gradle, no I/O. Pattern aligned on
 * [slider.rtl.RtlSlideAssertion]: consumes pure value objects and returns a
 * [ContentAssertionResult].
 *
 * Assertions:
 * - **P0_OVERFLOW**      — a paragraph or the title overflows the content area.
 *   Uses [OverflowDetector] with a synthetic [TextBlock] built from the text
 *   and the slide content area (unit-pure estimation, no real rendering).
 * - **P0_MISSING_NOTES** — the slide has no speaker note.
 * - **P1_FONT_SIZE**     — the body font size is below the readable minimum
 *   ([MIN_BODY_FONT_SIZE] = 14 CSS px).
 * - **P1_CONTRAST**      — the title-to-body font ratio is below
 *   ([MIN_TITLE_BODY_RATIO] = 1.25), indicating a flat visual hierarchy.
 */
object ContentSlideAssertion {

    const val MIN_BODY_FONT_SIZE: Double = 14.0
    const val MIN_TITLE_BODY_RATIO: Double = 1.25

    fun assertAll(content: SlideContent, layout: SlideLayout): ContentAssertionResult {
        val failures = buildList {
            overflowFailures(content, layout).forEach(::add)
            if (!content.hasSpeakerNote()) {
                add(failure(ContentAssertionCode.P0_MISSING_NOTES, content, "slide has no speaker note"))
            }
            if (layout.bodyFontSize < MIN_BODY_FONT_SIZE) {
                add(
                    failure(
                        ContentAssertionCode.P1_FONT_SIZE,
                        content,
                        "bodyFontSize=${layout.bodyFontSize} < MIN_BODY_FONT_SIZE=$MIN_BODY_FONT_SIZE",
                    ),
                )
            }
            val ratio = layout.titleFontSize / layout.bodyFontSize
            if (ratio < MIN_TITLE_BODY_RATIO) {
                add(
                    failure(
                        ContentAssertionCode.P1_CONTRAST,
                        content,
                        "title/body ratio=$ratio < MIN_TITLE_BODY_RATIO=$MIN_TITLE_BODY_RATIO",
                    ),
                )
            }
        }
        return if (failures.isEmpty()) ContentAssertionResult.Passed
        else ContentAssertionResult.Failed(failures)
    }

    private fun overflowFailures(content: SlideContent, layout: SlideLayout): List<ContentAssertionFailure> {
        val failures = mutableListOf<ContentAssertionFailure>()
        titleOverflow(content, layout)?.let(failures::add)
        content.paragraphs.forEachIndexed { i, paragraph ->
            paragraphOverflow(content, paragraph, i, layout)?.let(failures::add)
        }
        return failures
    }

    private fun titleOverflow(content: SlideContent, layout: SlideLayout): ContentAssertionFailure? {
        val block = textBlock(content.title, layout.marginX, layout.marginY, layout.titleFontSize)
        return overflowFailure(block, layout, content, "title")
    }

    private fun paragraphOverflow(
        content: SlideContent,
        paragraph: String,
        index: Int,
        layout: SlideLayout,
    ): ContentAssertionFailure? {
        val y = layout.marginY + layout.titleFontSize
        val block = textBlock(paragraph, layout.marginX, y, layout.bodyFontSize)
        return overflowFailure(block, layout, content, "paragraph[$index]")
    }

    private fun textBlock(text: String, x: Double, y: Double, fontSize: Double): TextBlock =
        TextBlock(
            text = text,
            x = x,
            y = y,
            width = text.length * fontSize * 0.5,
            height = fontSize,
        )

    private fun overflowFailure(
        block: TextBlock,
        layout: SlideLayout,
        content: SlideContent,
        what: String,
    ): ContentAssertionFailure? {
        val result = OverflowDetector.detect(block, layout)
        return if (result is OverflowResult.Overflow) {
            failure(ContentAssertionCode.P0_OVERFLOW, content, "$what — ${result.message}")
        } else null
    }

    private fun failure(code: ContentAssertionCode, content: SlideContent, detail: String): ContentAssertionFailure =
        ContentAssertionFailure(code, "${code.name} — $detail", content.title)
}