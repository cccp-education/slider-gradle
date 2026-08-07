package slider.steps

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.content.ContentAssertionCode
import slider.content.ContentAssertionResult
import slider.content.ContentRenderAssertion
import slider.content.ContentRenderData
import slider.content.ContentSlideAssertion
import slider.content.SlideContent
import slider.content.SlideLayout
import slider.content.TextBlock

/**
 * Cucumber step definitions for the slide content validation feature
 * (SLD-10.6 — `24_slide_content_validation.feature`).
 *
 * Two assertion strategies share the same four [ContentAssertionCode]s:
 * - **A priori** — [ContentSlideAssertion] consumes [SlideContent] + [SlideLayout]
 *   (parsed from the .adoc source, no real rendering).
 * - **A posteriori** — [ContentRenderAssertion] consumes [ContentRenderData]
 *   (a snapshot DTO extracted from the rendered DOM).
 *
 * Pattern aligned on [RtlVisualValidationSteps] — build pure value objects,
 * call `assertAll`, then assert on the sealed [ContentAssertionResult].
 */
class ContentValidationSteps : En {

    private var slideContent: SlideContent? = null
    private var slideLayout: SlideLayout? = null
    private var renderData: ContentRenderData? = null
    private var slideAssertionResult: ContentAssertionResult? = null
    private var renderAssertionResult: ContentAssertionResult? = null

    init {

        // -------------------------------------------------------------------------
        // A priori — SlideContent + SlideLayout → ContentSlideAssertion
        // -------------------------------------------------------------------------

        Given("a slide content with title {string} and a speaker note {string}") {
            title: String, note: String ->
            slideContent = SlideContent(title = title, speakerNote = note)
        }

        Given("a slide content with title {string} and no speaker note") { title: String ->
            slideContent = SlideContent(title = title)
        }

        Given("a slide layout with viewport {int}x{int}, margin {int}, title font {int}, body font {int}") {
            viewportWidth: Int, viewportHeight: Int, margin: Int, titleFont: Int, bodyFont: Int ->
            slideLayout = SlideLayout(
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight,
                marginX = margin.toDouble(),
                marginY = margin.toDouble(),
                titleFontSize = titleFont.toDouble(),
                bodyFontSize = bodyFont.toDouble(),
            )
        }

        When("the content slide assertion is evaluated") {
            slideAssertionResult = ContentSlideAssertion.assertAll(slideContent!!, slideLayout!!)
        }

        Then("all content assertions should pass") {
            assertThat(slideAssertionResult)
                .withFailMessage("Expected all a priori assertions to pass but got: $slideAssertionResult")
                .isEqualTo(ContentAssertionResult.Passed)
        }

        Then("the content assertion {string} should fail") { codeName: String ->
            val code = ContentAssertionCode.valueOf(codeName)
            assertThat(slideAssertionResult)
                .withFailMessage("Expected $codeName to fail but got: $slideAssertionResult")
                .isInstanceOf(ContentAssertionResult.Failed::class.java)
            val failed = slideAssertionResult as ContentAssertionResult.Failed
            assertThat(failed.failureCodes())
                .withFailMessage("Expected $codeName in failures ${failed.failures}")
                .contains(code)
        }

        // -------------------------------------------------------------------------
        // A posteriori — ContentRenderData → ContentRenderAssertion
        // -------------------------------------------------------------------------

        Given("a render snapshot with title {string}, body font {double}, title font {double}, contrast {double}, notes in DOM {string}, viewport {int}x{int}") {
            title: String, bodyFont: Double, titleFont: Double, contrast: Double, notesInDom: String, viewportWidth: Int, viewportHeight: Int ->
            renderData = ContentRenderData(
                slideTitle = title,
                realTextBlocks = emptyList(),
                computedTitleFontSize = titleFont,
                computedBodyFontSize = bodyFont,
                computedContrastRatio = contrast,
                hasNotesInDom = notesInDom.toBoolean(),
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight,
            )
        }

        Given("a text block at x {int}, y {int}, width {int}, height {int}") {
            x: Int, y: Int, width: Int, height: Int ->
            val block = TextBlock(
                text = "overflowing block",
                x = x.toDouble(),
                y = y.toDouble(),
                width = width.toDouble(),
                height = height.toDouble(),
            )
            val current = renderData!!
            renderData = current.copy(realTextBlocks = current.realTextBlocks + block)
        }

        When("the content render assertion is evaluated") {
            renderAssertionResult = ContentRenderAssertion.assertAll(renderData!!)
        }

        Then("all content render assertions should pass") {
            assertThat(renderAssertionResult)
                .withFailMessage("Expected all a posteriori assertions to pass but got: $renderAssertionResult")
                .isEqualTo(ContentAssertionResult.Passed)
        }

        Then("the content render assertion {string} should fail") { codeName: String ->
            val code = ContentAssertionCode.valueOf(codeName)
            assertThat(renderAssertionResult)
                .withFailMessage("Expected $codeName to fail but got: $renderAssertionResult")
                .isInstanceOf(ContentAssertionResult.Failed::class.java)
            val failed = renderAssertionResult as ContentAssertionResult.Failed
            assertThat(failed.failureCodes())
                .withFailMessage("Expected $codeName in failures ${failed.failures}")
                .contains(code)
        }
    }
}