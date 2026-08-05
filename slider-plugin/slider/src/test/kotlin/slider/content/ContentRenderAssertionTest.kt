package slider.content

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentRenderAssertionTest {

    private fun validTextBlock(): TextBlock = TextBlock(
        text = "Hello",
        x = 10.0,
        y = 10.0,
        width = 100.0,
        height = 30.0,
    )

    private fun validRenderData(): ContentRenderData = ContentRenderData(
        slideTitle = "Introduction",
        realTextBlocks = listOf(validTextBlock()),
        computedTitleFontSize = 36.0,
        computedBodyFontSize = 18.0,
        computedContrastRatio = 7.2,
        hasNotesInDom = true,
        viewportWidth = 1280,
        viewportHeight = 720,
    )

    @Test
    fun `full valid render data passes all assertions`() {
        val result = ContentRenderAssertion.assertAll(validRenderData())

        assertThat(result).isEqualTo(ContentAssertionResult.Passed)
    }

    @Test
    fun `P0 overflow fails when a real text block overflows viewport`() {
        val overflowingBlock = TextBlock(
            text = "Hello",
            x = -10.0,
            y = 10.0,
            width = 100.0,
            height = 30.0,
        )
        val data = validRenderData().copy(realTextBlocks = listOf(overflowingBlock))

        val result = ContentRenderAssertion.assertAll(data)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        assertThat(failed.failureCodes()).contains(ContentAssertionCode.P0_OVERFLOW)
    }

    @Test
    fun `P0 missing notes fails when hasNotesInDom is false`() {
        val data = validRenderData().copy(hasNotesInDom = false)

        val result = ContentRenderAssertion.assertAll(data)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        assertThat(failed.failureCodes()).contains(ContentAssertionCode.P0_MISSING_NOTES)
    }

    @Test
    fun `P1 font size fails when computed body font is below minimum`() {
        val data = validRenderData().copy(computedBodyFontSize = 8.0)

        val result = ContentRenderAssertion.assertAll(data)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        assertThat(failed.failureCodes()).contains(ContentAssertionCode.P1_FONT_SIZE)
    }

    @Test
    fun `P1 contrast fails when computed contrast ratio is below WCAG AA`() {
        val data = validRenderData().copy(computedContrastRatio = 2.0)

        val result = ContentRenderAssertion.assertAll(data)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        assertThat(failed.failureCodes()).contains(ContentAssertionCode.P1_CONTRAST)
    }

    @Test
    fun `all failures are reported at once`() {
        val overflowingBlock = TextBlock(
            text = "Hello",
            x = -10.0,
            y = 10.0,
            width = 100.0,
            height = 30.0,
        )
        val data = ContentRenderData(
            slideTitle = "Broken Slide",
            realTextBlocks = listOf(overflowingBlock),
            computedTitleFontSize = 36.0,
            computedBodyFontSize = 8.0,
            computedContrastRatio = 2.0,
            hasNotesInDom = false,
            viewportWidth = 1280,
            viewportHeight = 720,
        )

        val result = ContentRenderAssertion.assertAll(data)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        assertThat(failed.failureCodes()).containsExactlyInAnyOrder(
            ContentAssertionCode.P0_OVERFLOW,
            ContentAssertionCode.P0_MISSING_NOTES,
            ContentAssertionCode.P1_FONT_SIZE,
            ContentAssertionCode.P1_CONTRAST,
        )
    }

    @Test
    fun `each failure carries the slide title as slideRef`() {
        val data = validRenderData().copy(
            slideTitle = "My Rendered Slide",
            hasNotesInDom = false,
        )

        val result = ContentRenderAssertion.assertAll(data)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        failed.failures.forEach { failure ->
            assertThat(failure.slideRef).isEqualTo("My Rendered Slide")
        }
    }

    @Test
    fun `failure messages contain the assertion code name`() {
        val data = validRenderData().copy(hasNotesInDom = false)

        val result = ContentRenderAssertion.assertAll(data)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        failed.failures.forEach { failure ->
            assertThat(failure.message).contains(failure.code.name)
        }
    }

    @Test
    fun `MIN_CONTRAST_RATIO is 4_5 for WCAG AA`() {
        assertThat(ContentRenderAssertion.MIN_CONTRAST_RATIO).isEqualTo(4.5)
    }
}