package slider.content

/**
 * Layout dimensions of a Reveal.js slide — viewport size, margins, and
 * font sizes for title and body text.
 *
 * Pure value object — no Gradle, no Playwright, no I/O. Consumed by
 * [OverflowDetector] to predict whether a [TextBlock] fits within the
 * slide content area.
 *
 * @param viewportWidth  slide viewport width in CSS pixels, must be positive.
 * @param viewportHeight slide viewport height in CSS pixels, must be positive.
 * @param marginX        horizontal margin on each side, must be non-negative.
 * @param marginY        vertical margin on top and bottom, must be non-negative.
 * @param titleFontSize  title font size in CSS pixels, must be positive.
 * @param bodyFontSize   body font size in CSS pixels, must be positive.
 */
data class SlideLayout(
    val viewportWidth: Int,
    val viewportHeight: Int,
    val marginX: Double,
    val marginY: Double,
    val titleFontSize: Double,
    val bodyFontSize: Double,
) {
    init {
        require(viewportWidth > 0) { "SlideLayout.viewportWidth must be positive, got $viewportWidth" }
        require(viewportHeight > 0) { "SlideLayout.viewportHeight must be positive, got $viewportHeight" }
        require(marginX >= 0.0) { "SlideLayout.marginX must be non-negative, got $marginX" }
        require(marginY >= 0.0) { "SlideLayout.marginY must be non-negative, got $marginY" }
        require(titleFontSize > 0.0) { "SlideLayout.titleFontSize must be positive, got $titleFontSize" }
        require(bodyFontSize > 0.0) { "SlideLayout.bodyFontSize must be positive, got $bodyFontSize" }
    }

    /** Content area width = `viewportWidth - 2 * marginX`. */
    val contentWidth: Double get() = viewportWidth.toDouble() - 2.0 * marginX

    /** Content area height = `viewportHeight - 2 * marginY`. */
    val contentHeight: Double get() = viewportHeight.toDouble() - 2.0 * marginY
}