package slider.content

/**
 * Snapshot of a rendered slide page — the data Playwright-jvm extracts
 * from the browser so that [ContentRenderAssertion] can validate it
 * without any dependency on the Playwright API.
 *
 * Pure data carrier: tests build it from fake values (unit), functional
 * tests build it from Playwright [com.microsoft.playwright.Page] queries,
 * and Cucumber steps build it from world state.
 *
 * Mirrors [slider.rtl.SlideRenderData] — the boundary contract between
 * the I/O adapter (Playwright) and the pure domain.
 *
 * @param slideTitle       text content of the slide title (h2/h1).
 * @param realTextBlocks   bounding boxes of every text block on the slide,
 *                         extracted via Playwright `boundingBox()`. Each
 *                         [TextBlock] carries the real geometry (not
 *                         synthesized like in [ContentSlideAssertion]).
 * @param computedTitleFontSize  title font size in CSS pixels (getComputedStyle).
 * @param computedBodyFontSize   body font size in CSS pixels (getComputedStyle).
 * @param computedContrastRatio  WCAG contrast ratio between title and body
 *                               text colors (relative luminance formula).
 * @param hasNotesInDom     true when the speaker note is rendered in the DOM
 *                          (Reveal.js `aside.notes` or `[NOTE.speaker]` rendered).
 * @param viewportWidth     browser viewport width in CSS pixels.
 * @param viewportHeight    browser viewport height in CSS pixels.
 */
data class ContentRenderData(
    val slideTitle: String,
    val realTextBlocks: List<TextBlock>,
    val computedTitleFontSize: Double,
    val computedBodyFontSize: Double,
    val computedContrastRatio: Double,
    val hasNotesInDom: Boolean,
    val viewportWidth: Int,
    val viewportHeight: Int,
    @Suppress("unused") private val bypassInvariants: Boolean = false,
) {
    init {
        if (!bypassInvariants) {
            require(slideTitle.isNotBlank()) { "ContentRenderData.slideTitle must not be blank" }
            require(computedTitleFontSize > 0.0) {
                "ContentRenderData.computedTitleFontSize must be positive, got $computedTitleFontSize"
            }
            require(computedBodyFontSize > 0.0) {
                "ContentRenderData.computedBodyFontSize must be positive, got $computedBodyFontSize"
            }
            require(computedContrastRatio >= 0.0) {
                "ContentRenderData.computedContrastRatio must be non-negative, got $computedContrastRatio"
            }
            require(viewportWidth > 0) { "ContentRenderData.viewportWidth must be positive, got $viewportWidth" }
            require(viewportHeight > 0) { "ContentRenderData.viewportHeight must be positive, got $viewportHeight" }
        }
    }

    companion object {
        /**
         * Empty snapshot for tests/Cucumber world state where the snapshot
         * is built progressively. Bypasses [init] invariants — do not use
         * for real validation.
         */
        val EMPTY: ContentRenderData = ContentRenderData(
            slideTitle = "",
            realTextBlocks = emptyList(),
            computedTitleFontSize = 0.0,
            computedBodyFontSize = 0.0,
            computedContrastRatio = 0.0,
            hasNotesInDom = false,
            viewportWidth = 0,
            viewportHeight = 0,
            bypassInvariants = true,
        )
    }
}