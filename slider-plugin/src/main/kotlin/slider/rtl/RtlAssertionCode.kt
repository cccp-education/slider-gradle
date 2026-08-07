package slider.rtl

/**
 * Assertion codes for RTL visual validation of a Reveal.js deck.
 *
 * P0 assertions are critical — the deck is not usable in RTL if any
 * of them fails. P1 assertions are quality — the deck is usable but
 * the layout may be degraded.
 */
enum class RtlAssertionCode {
    P0_RTL_CONFIG,
    P0_RTL_CLASS,
    P0_NAV,
    P1_OVERFLOW,
}