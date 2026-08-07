package slider.rtl

/**
 * Validates that a rendered Reveal.js slide deck is correctly laid out
 * in Right-To-Left mode.
 *
 * Pure domain logic — no Playwright, no Gradle, no I/O. It consumes a
 * [SlideRenderData] snapshot and returns an [RtlAssertionResult].
 *
 * Reveal.js handles RTL via `rtl: true` in its JS config and a `.rtl`
 * class on `.reveal` (applied at runtime), NOT via `<html dir="rtl">`.
 * The navigation is mirrored: "next" appears on the left.
 *
 * Assertions:
 * - **P0_RTL_CONFIG**  — Reveal.js JS config contains `rtl: true`.
 * - **P0_RTL_CLASS**   — `.reveal` has the `rtl` class (applied by JS at runtime).
 * - **P0_NAV**         — the "next" navigation control is on the left (RTL mirror).
 * - **P1_OVERFLOW**    — the slide bounding box fits within the viewport width.
 */
object RtlSlideAssertion {

    fun assertAll(data: SlideRenderData): RtlAssertionResult {
        val failures = buildList {
            if (!hasRtlConfig(data)) add(failure(RtlAssertionCode.P0_RTL_CONFIG, data.revealRtlConfig.toString()))
            if (!hasRtlClass(data)) add(failure(RtlAssertionCode.P0_RTL_CLASS, data.revealHasRtlClass.toString()))
            if (!isNavNextOnLeft(data)) add(failure(RtlAssertionCode.P0_NAV, data.navNextLeft.toString()))
            if (!isWithinViewport(data)) add(failure(RtlAssertionCode.P1_OVERFLOW, "${data.slideBoxX} + ${data.slideBoxWidth} > ${data.viewportWidth}"))
        }
        return RtlAssertionResult.of(failures)
    }

    private fun hasRtlConfig(data: SlideRenderData): Boolean = data.revealRtlConfig

    private fun hasRtlClass(data: SlideRenderData): Boolean = data.revealHasRtlClass

    private fun isNavNextOnLeft(data: SlideRenderData): Boolean = data.navNextLeft

    private fun isWithinViewport(data: SlideRenderData): Boolean =
        data.slideBoxX >= 0.0 && data.slideBoxX + data.slideBoxWidth <= data.viewportWidth.toDouble()

    private fun failure(code: RtlAssertionCode, actual: String): RtlAssertionFailure =
        RtlAssertionFailure(code, "${code.name} — expected RTL layout, got: $actual")
}