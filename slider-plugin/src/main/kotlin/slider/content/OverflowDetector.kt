package slider.content

/**
 * Predicts whether a [TextBlock] overflows the content area of a [SlideLayout].
 *
 * Pure domain service — no Gradle, no Playwright, no I/O. The detector checks
 * three conditions in order:
 *
 * 1. **Horizontal overflow** — the block left edge is before the left margin
 *    or the block right edge is past the viewport width.
 * 2. **Vertical overflow** — the block top edge is above the top margin or
 *    the block bottom edge is past the viewport height.
 * 3. **Estimated text too long** — the estimated rendered width of the text
 *    (characters × average glyph width at the body font size) exceeds the
 *    slide content width. This catches the common case where a long string
 *    wraps off-screen even though its bounding box is correctly positioned.
 *
 * A block that satisfies all three checks [Fits].
 *
 * The average glyph width is approximated as `bodyFontSize * 0.5` — a
 * conservative estimate for proportional fonts (Latin average is ~0.45-0.5em).
 */
object OverflowDetector {

    private const val AVERAGE_GLYPH_WIDTH_RATIO = 0.5

    fun detect(block: TextBlock, layout: SlideLayout): OverflowResult {
        horizontalOverflow(block, layout)?.let { return it }
        verticalOverflow(block, layout)?.let { return it }
        estimatedTextOverflow(block, layout)?.let { return it }
        return OverflowResult.Fits
    }

    fun overflows(block: TextBlock, layout: SlideLayout): Boolean =
        detect(block, layout) is OverflowResult.Overflow

    private fun horizontalOverflow(
        block: TextBlock,
        layout: SlideLayout,
    ): OverflowResult.Overflow? {
        val leftEdge = layout.marginX
        val rightEdge = layout.viewportWidth.toDouble()
        if (block.x < leftEdge || block.rightEdge > rightEdge) {
            return OverflowResult.Overflow(
                OverflowReason.HORIZONTAL_OVERFLOW,
                "${OverflowReason.HORIZONTAL_OVERFLOW.name} — block x=${block.x}, rightEdge=${block.rightEdge}, " +
                    "leftMargin=${leftEdge}, viewportWidth=${rightEdge}",
            )
        }
        return null
    }

    private fun verticalOverflow(
        block: TextBlock,
        layout: SlideLayout,
    ): OverflowResult.Overflow? {
        val topEdge = layout.marginY
        val bottomEdge = layout.viewportHeight.toDouble()
        if (block.y < topEdge || block.bottomEdge > bottomEdge) {
            return OverflowResult.Overflow(
                OverflowReason.VERTICAL_OVERFLOW,
                "${OverflowReason.VERTICAL_OVERFLOW.name} — block y=${block.y}, bottomEdge=${block.bottomEdge}, " +
                    "topMargin=${topEdge}, viewportHeight=${bottomEdge}",
            )
        }
        return null
    }

    private fun estimatedTextOverflow(
        block: TextBlock,
        layout: SlideLayout,
    ): OverflowResult.Overflow? {
        val glyphWidth = layout.bodyFontSize * AVERAGE_GLYPH_WIDTH_RATIO
        val estimatedWidth = block.characterCount * glyphWidth
        if (estimatedWidth > layout.contentWidth) {
            return OverflowResult.Overflow(
                OverflowReason.ESTIMATED_TEXT_TOO_LONG,
                "${OverflowReason.ESTIMATED_TEXT_TOO_LONG.name} — estimatedWidth=$estimatedWidth, " +
                    "contentWidth=${layout.contentWidth} (chars=${block.characterCount}, glyphWidth=$glyphWidth)",
            )
        }
        return null
    }
}