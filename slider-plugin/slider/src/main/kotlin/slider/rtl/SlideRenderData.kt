package slider.rtl

/**
 * Snapshot of a rendered slide page — the data Playwright-jvm extracts
 * from the browser so that [RtlSlideAssertion] can validate it without
 * any dependency on the Playwright API.
 *
 * This is a pure data carrier: tests build it from fake values (unit),
 * functional tests build it from Playwright [com.microsoft.playwright.Page]
 * queries, and Cucumber steps build it from world state.
 *
 * Reveal.js handles RTL via `rtl: true` in its JS config + a `.rtl` class
 * on `.reveal` (applied at runtime), NOT via `<html dir="rtl">`. The
 * navigation is mirrored: the "next" button appears on the left.
 *
 * @param revealRtlConfig   true when the Reveal.js JS config contains `rtl: true`.
 * @param revealHasRtlClass true when `.reveal` has the `rtl` class (applied by JS at runtime).
 * @param navNextLeft       true when the "next" navigation control is on the left (RTL mirror).
 * @param viewportWidth     browser viewport width in CSS pixels.
 * @param slideBoxX         x-coordinate of the slide bounding box.
 * @param slideBoxWidth     width of the slide bounding box in CSS pixels.
 */
data class SlideRenderData(
    val revealRtlConfig: Boolean,
    val revealHasRtlClass: Boolean,
    val navNextLeft: Boolean,
    val viewportWidth: Int,
    val slideBoxX: Double,
    val slideBoxWidth: Double,
)