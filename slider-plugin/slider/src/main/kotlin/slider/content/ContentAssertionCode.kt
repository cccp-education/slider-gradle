package slider.content

/**
 * Assertion codes for content validation of a [SlideContent] rendered on a
 * [SlideLayout].
 *
 * P0 assertions are critical — the slide is not usable if any of them fails:
 * - **P0_OVERFLOW**     — a paragraph or title overflows the content area.
 * - **P0_MISSING_NOTES** — the slide has no speaker note.
 *
 * P1 assertions are quality — the slide is usable but readability is degraded:
 * - **P1_FONT_SIZE** — the body font size is below the readable minimum.
 * - **P1_CONTRAST**  — the title-to-body font ratio is too flat for hierarchy.
 */
enum class ContentAssertionCode {
    P0_OVERFLOW,
    P0_MISSING_NOTES,
    P1_FONT_SIZE,
    P1_CONTRAST,
}