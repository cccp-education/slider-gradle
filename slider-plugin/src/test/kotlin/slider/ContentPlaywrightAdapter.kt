package slider

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import slider.content.ContentRenderData
import slider.content.TextBlock

/**
 * Playwright adapter that extracts a [ContentRenderData] snapshot from a
 * rendered Reveal.js slide. Mirrors the extraction pattern of
 * [RtlPlaywrightFunctionalTest] (S-021).
 *
 * This is the I/O half of the hexagonal pattern — the pure
 * [slider.content.ContentRenderAssertion] consumes the snapshot without
 * any Playwright dependency.
 *
 * The adapter is a stateless `object`: the [Page] is passed as a parameter,
 * not stored. [extract] takes a [slideIndex] (default 0) so that each slide
 * can be validated individually in a test loop.
 */
object ContentPlaywrightAdapter {

    /**
     * Extracts a [ContentRenderData] snapshot from the slide at [slideIndex]
     * in the rendered Reveal.js deck loaded in [page].
     *
     * The contrast ratio is computed in the browser via injected JavaScript
     * (WCAG relative luminance formula) because the resolved colors depend on
     * the CSS cascade and are too costly to re-implement in Kotlin.
     *
     * Returns `computedContrastRatio = 0.0` when the title or body elements
     * are not found — this triggers `P1_CONTRAST` (0 < 4.5) downstream.
     */
    fun extract(page: Page, slideIndex: Int = 0): ContentRenderData {
        page.waitForSelector(".reveal .slides section")

        val slide = page.locator(".reveal .slides section").nth(slideIndex)

        val slideTitle = slide.locator("h2, h1").first().textContent() ?: ""

        val realTextBlocks = extractTextBlocks(slide)

        val computedTitleFontSize = computedFontSize(page, ".reveal .slides section h2, .reveal .slides section h1")
        val computedBodyFontSize = computedFontSize(page, ".reveal .slides section p, .reveal .slides section li")

        val computedContrastRatio = computedContrast(page)

        val hasNotesInDom = page.locator("aside.notes").count() > 0

        val viewport = page.viewportSize()!!

        return ContentRenderData(
            slideTitle = slideTitle.trim(),
            realTextBlocks = realTextBlocks,
            computedTitleFontSize = computedTitleFontSize,
            computedBodyFontSize = computedBodyFontSize,
            computedContrastRatio = computedContrastRatio,
            hasNotesInDom = hasNotesInDom,
            viewportWidth = viewport.width,
            viewportHeight = viewport.height,
        )
    }

    private fun extractTextBlocks(slide: Locator): List<TextBlock> {
        val elements = slide.locator("h2, h3, p, li").all()
        return elements.mapNotNull { el ->
            val box = el.boundingBox() ?: return@mapNotNull null
            val text = el.textContent() ?: return@mapNotNull null
            if (text.isBlank()) return@mapNotNull null
            TextBlock(
                text = text.trim(),
                x = box.x,
                y = box.y,
                width = box.width,
                height = box.height,
            )
        }
    }

    private fun computedFontSize(page: Page, selector: String): Double {
        val px = page.evalOnSelector(selector, "el => getComputedStyle(el).fontSize") as String
        return px.removeSuffix("px").toDouble()
    }

    private fun computedContrast(page: Page): Double {
        val js = """
            () => {
                const title = document.querySelector('.reveal .slides section h2, .reveal .slides section h1');
                const body = document.querySelector('.reveal .slides section p, .reveal .slides section li');
                if (!title || !body) return 0.0;
                const ts = getComputedStyle(title);
                const bs = getComputedStyle(body);
                return wcagContrast(ts.color, bs.backgroundColor);
            }
            function wcagContrast(c1, c2) {
                const l1 = relativeLuminance(c1);
                const l2 = relativeLuminance(c2);
                const lighter = Math.max(l1, l2);
                const darker = Math.min(l1, l2);
                return (lighter + 0.05) / (darker + 0.05);
            }
            function relativeLuminance(rgb) {
                const m = rgb.match(/\d+/g);
                if (!m) return 0.0;
                const [r, g, b] = m.map(Number).map(c => {
                    c = c / 255;
                    return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
                });
                return 0.2126 * r + 0.7152 * g + 0.0722 * b;
            }
        """.trimIndent()
        return (page.evaluate(js) as Number).toDouble()
    }
}