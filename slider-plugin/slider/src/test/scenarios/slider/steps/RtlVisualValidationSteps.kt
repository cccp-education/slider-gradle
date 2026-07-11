package slider.steps

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.rtl.RtlAssertionCode
import slider.rtl.RtlAssertionResult
import slider.rtl.RtlSlideAssertion
import slider.rtl.SlideRenderData

class RtlVisualValidationSteps : En {

    private var renderData: SlideRenderData? = null
    private var assertionResult: RtlAssertionResult? = null

    init {

        Given("a slide render data with rtl config {string}, rtl class {string}, nav next left {string}, viewport {int}, slide x {int}, slide width {int}") {
            rtlConfig: String, rtlClass: String, navNextLeft: String, viewport: Int, slideX: Int, slideWidth: Int ->
            renderData = SlideRenderData(
                revealRtlConfig = rtlConfig.toBoolean(),
                revealHasRtlClass = rtlClass.toBoolean(),
                navNextLeft = navNextLeft.toBoolean(),
                viewportWidth = viewport,
                slideBoxX = slideX.toDouble(),
                slideBoxWidth = slideWidth.toDouble(),
            )
            assertionResult = RtlSlideAssertion.assertAll(renderData!!)
        }

        Then("all RTL assertions should pass") {
            assertThat(assertionResult!!.passed)
                .withFailMessage("Expected all RTL assertions to pass but got: ${assertionResult!!.failures}")
                .isTrue()
        }

        Then("the RTL assertion {string} should fail") { codeName: String ->
            val code = RtlAssertionCode.valueOf(codeName)
            assertThat(assertionResult!!.failureCodes())
                .withFailMessage("Expected $codeName to fail. Failures: ${assertionResult!!.failures}")
                .contains(code)
        }
    }
}