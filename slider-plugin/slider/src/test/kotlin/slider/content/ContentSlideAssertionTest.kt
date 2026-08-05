package slider.content

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentSlideAssertionTest {

    private val layout = SlideLayout(
        viewportWidth = 1280,
        viewportHeight = 720,
        marginX = 40.0,
        marginY = 40.0,
        titleFontSize = 40.0,
        bodyFontSize = 20.0,
    )

    @Test
    fun `a complete slide with speaker note passes all assertions`() {
        val content = SlideContent(
            title = "Introduction",
            paragraphs = listOf("Short paragraph."),
            speakerNote = "Welcome the audience.",
        )

        val result = ContentSlideAssertion.assertAll(content, layout)

        assertThat(result).isEqualTo(ContentAssertionResult.Passed)
    }

    @Test
    fun `P0 overflow fails when a paragraph is too long for the content width`() {
        val longText = "x".repeat(200)
        val content = SlideContent(
            title = "Overflow",
            paragraphs = listOf(longText),
            speakerNote = "Note",
        )

        val result = ContentSlideAssertion.assertAll(content, layout)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        assertThat(failed.failureCodes()).contains(ContentAssertionCode.P0_OVERFLOW)
    }

    @Test
    fun `P0 missing notes fails when slide has no speaker note`() {
        val content = SlideContent(
            title = "No Notes",
            paragraphs = listOf("Some text."),
        )

        val result = ContentSlideAssertion.assertAll(content, layout)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        assertThat(failed.failureCodes()).contains(ContentAssertionCode.P0_MISSING_NOTES)
    }

    @Test
    fun `P1 font size fails when body font size is below minimum`() {
        val smallFontLayout = SlideLayout(
            viewportWidth = 1280,
            viewportHeight = 720,
            marginX = 40.0,
            marginY = 40.0,
            titleFontSize = 40.0,
            bodyFontSize = 8.0,
        )
        val content = SlideContent(
            title = "Tiny",
            paragraphs = listOf("Text."),
            speakerNote = "Note",
        )

        val result = ContentSlideAssertion.assertAll(content, smallFontLayout)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        assertThat(failed.failureCodes()).contains(ContentAssertionCode.P1_FONT_SIZE)
    }

    @Test
    fun `P1 contrast fails when title-body font ratio is too low`() {
        val flatRatioLayout = SlideLayout(
            viewportWidth = 1280,
            viewportHeight = 720,
            marginX = 40.0,
            marginY = 40.0,
            titleFontSize = 22.0,
            bodyFontSize = 20.0,
        )
        val content = SlideContent(
            title = "Flat",
            paragraphs = listOf("Text."),
            speakerNote = "Note",
        )

        val result = ContentSlideAssertion.assertAll(content, flatRatioLayout)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        assertThat(failed.failureCodes()).contains(ContentAssertionCode.P1_CONTRAST)
    }

    @Test
    fun `a title-only slide with no speaker note fails only missing notes`() {
        val content = SlideContent(title = "Title Only")

        val result = ContentSlideAssertion.assertAll(content, layout)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        assertThat(failed.failureCodes()).containsExactly(ContentAssertionCode.P0_MISSING_NOTES)
    }

    @Test
    fun `all failures are reported at once`() {
        val longText = "x".repeat(300)
        val tinyLayout = SlideLayout(
            viewportWidth = 1280,
            viewportHeight = 720,
            marginX = 40.0,
            marginY = 40.0,
            titleFontSize = 9.0,
            bodyFontSize = 8.0,
        )
        val content = SlideContent(
            title = longText,
            paragraphs = listOf(longText),
        )

        val result = ContentSlideAssertion.assertAll(content, tinyLayout)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        assertThat(failed.failureCodes()).contains(
            ContentAssertionCode.P0_OVERFLOW,
            ContentAssertionCode.P0_MISSING_NOTES,
            ContentAssertionCode.P1_FONT_SIZE,
            ContentAssertionCode.P1_CONTRAST,
        )
    }

    @Test
    fun `each failure carries the slide title as slideRef`() {
        val content = SlideContent(title = "My Slide Title")

        val result = ContentSlideAssertion.assertAll(content, layout)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        failed.failures.forEach { failure ->
            assertThat(failure.slideRef).isEqualTo("My Slide Title")
        }
    }

    @Test
    fun `failure messages contain the assertion code name`() {
        val content = SlideContent(title = "No Notes")

        val result = ContentSlideAssertion.assertAll(content, layout)

        assertThat(result).isInstanceOf(ContentAssertionResult.Failed::class.java)
        val failed = result as ContentAssertionResult.Failed
        failed.failures.forEach { failure ->
            assertThat(failure.message).contains(failure.code.name)
        }
    }

    @Test
    fun `ContentAssertionResult Passed has no failures`() {
        assertThat(ContentAssertionResult.Passed).isInstanceOf(ContentAssertionResult.Passed::class.java)
    }

    @Test
    fun `ContentAssertionResult Failed exposes failures list`() {
        val failure = ContentAssertionFailure(
            code = ContentAssertionCode.P0_MISSING_NOTES,
            message = "P0_MISSING_NOTES — slide has no speaker note",
            slideRef = "Title",
        )

        val failed = ContentAssertionResult.Failed(listOf(failure))

        assertThat(failed.failures).hasSize(1)
        assertThat(failed.failures.first().code).isEqualTo(ContentAssertionCode.P0_MISSING_NOTES)
    }
}