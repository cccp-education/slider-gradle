package slider.content

/**
 * A block of text rendered on a slide, with its estimated position and
 * bounding box in CSS pixels.
 *
 * Pure value object — no Gradle, no Playwright, no I/O. Tests build it
 * from fake values (unit), functional tests build it from Playwright
 * [com.microsoft.playwright.Page] queries.
 *
 * @param text   rendered text content, must be non-blank.
 * @param x      x-coordinate of the block top-left corner.
 * @param y      y-coordinate of the block top-left corner.
 * @param width  block width in CSS pixels, must be non-negative.
 * @param height block height in CSS pixels, must be non-negative.
 */
data class TextBlock(
    val text: String,
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
) {
    init {
        require(text.isNotBlank()) { "TextBlock.text must not be blank" }
        require(width >= 0.0) { "TextBlock.width must be non-negative, got $width" }
        require(height >= 0.0) { "TextBlock.height must be non-negative, got $height" }
    }

    /** Right edge x-coordinate (`x + width`). */
    val rightEdge: Double get() = x + width

    /** Bottom edge y-coordinate (`y + height`). */
    val bottomEdge: Double get() = y + height

    /** Number of characters in [text]. */
    val characterCount: Int get() = text.length
}