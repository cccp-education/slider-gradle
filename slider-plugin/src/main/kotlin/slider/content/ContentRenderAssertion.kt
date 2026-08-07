package slider.content

/**
 * Validates that a rendered Reveal.js slide deck is correctly rendered
 * in terms of content geometry, font readability, contrast, and speaker
 * notes presence.
 *
 * Pure domain logic — no Playwright, no Gradle, no I/O. It consumes a
 * [ContentRenderData] snapshot (extracted from a real browser by the
 * Playwright adapter) and returns a [ContentAssertionResult].
 *
 * Mirrors [slider.rtl.RtlSlideAssertion] — the pure-assertion half of
 * the hexagonal pattern. The other half (Playwright adapter) lives in
 * the test source set.
 *
 * Assertions (reuse [ContentAssertionCode] from S-051):
 * - **P0_OVERFLOW**     — any real [TextBlock] overflows the viewport.
 * - **P0_MISSING_NOTES** — the speaker note is not rendered in the DOM.
 * - **P1_FONT_SIZE**    — the computed body font size is below [MIN_BODY_FONT_SIZE].
 * - **P1_CONTRAST**     — the computed contrast ratio is below [MIN_CONTRAST_RATIO].
 *
 * [MIN_BODY_FONT_SIZE] and [MIN_TITLE_BODY_RATIO] are reused from
 * [ContentSlideAssertion]. [MIN_CONTRAST_RATIO] is new (WCAG AA = 4.5).
 */
object ContentRenderAssertion {

    /** WCAG AA minimum contrast ratio for normal text. */
    const val MIN_CONTRAST_RATIO: Double = 4.5

    fun assertAll(data: ContentRenderData): ContentAssertionResult {
        val failures = buildList {
            addAll(overflowFailures(data))
            if (!data.hasNotesInDom) {
                add(
                    failure(
                        ContentAssertionCode.P0_MISSING_NOTES,
                        "slide has no speaker note rendered in DOM",
                        data.slideTitle,
                    ),
                )
            }
            if (data.computedBodyFontSize < ContentSlideAssertion.MIN_BODY_FONT_SIZE) {
                add(
                    failure(
                        ContentAssertionCode.P1_FONT_SIZE,
                        "computed body font size ${data.computedBodyFontSize} < ${ContentSlideAssertion.MIN_BODY_FONT_SIZE}",
                        data.slideTitle,
                    ),
                )
            }
            if (data.computedContrastRatio < MIN_CONTRAST_RATIO) {
                add(
                    failure(
                        ContentAssertionCode.P1_CONTRAST,
                        "computed contrast ratio ${data.computedContrastRatio} < $MIN_CONTRAST_RATIO (WCAG AA)",
                        data.slideTitle,
                    ),
                )
            }
        }
        return if (failures.isEmpty()) ContentAssertionResult.Passed
        else ContentAssertionResult.Failed(failures)
    }

    private fun overflowFailures(data: ContentRenderData): List<ContentAssertionFailure> =
        data.realTextBlocks.mapIndexedNotNull { index, block ->
            val layout = SlideLayout(
                viewportWidth = data.viewportWidth,
                viewportHeight = data.viewportHeight,
                marginX = 0.0,
                marginY = 0.0,
                titleFontSize = data.computedTitleFontSize,
                bodyFontSize = data.computedBodyFontSize,
            )
            val result = OverflowDetector.detect(block, layout)
            if (result is OverflowResult.Overflow) {
                failure(
                    ContentAssertionCode.P0_OVERFLOW,
                    "text block $index — ${result.message}",
                    data.slideTitle,
                )
            } else null
        }

    private fun failure(
        code: ContentAssertionCode,
        detail: String,
        slideRef: String,
    ): ContentAssertionFailure =
        ContentAssertionFailure(code, "${code.name} — $detail", slideRef)
}