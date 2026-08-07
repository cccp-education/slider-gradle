package slider.content

/**
 * Reason a [TextBlock] overflows the slide content area.
 *
 * HORIZONTAL_OVERFLOW — the block bounding box crosses the left margin
 * or extends past the right edge of the viewport.
 * VERTICAL_OVERFLOW   — the block bounding box crosses the top margin
 * or extends past the bottom edge of the viewport.
 * ESTIMATED_TEXT_TOO_LONG — the estimated rendered width of the text
 * (characters × average glyph width at the body font size) exceeds the
 * slide content width, even when the block bounding box itself is
 * correctly positioned.
 */
enum class OverflowReason {
    HORIZONTAL_OVERFLOW,
    VERTICAL_OVERFLOW,
    ESTIMATED_TEXT_TOO_LONG,
}

/**
 * Result of evaluating a [TextBlock] against a [SlideLayout] via
 * [OverflowDetector]. Sealed hierarchy: the block either [Fits] or
 * [Overflow] with a [OverflowReason].
 */
sealed interface OverflowResult {

    /**
     * The block fits within the slide content area.
     */
    data object Fits : OverflowResult

    /**
     * The block overflows the slide content area for [reason].
     *
     * @param reason  why the block overflows.
     * @param message human-readable message including the reason name.
     */
    data class Overflow(
        val reason: OverflowReason,
        val message: String,
    ) : OverflowResult
}