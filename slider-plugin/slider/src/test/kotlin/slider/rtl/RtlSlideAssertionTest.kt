package slider.rtl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RtlSlideAssertionTest {

    @Test
    fun `full RTL data passes all assertions`() {
        val slideData = SlideRenderData(
            revealRtlConfig = true,
            revealHasRtlClass = true,
            navNextLeft = true,
            viewportWidth = 1408,
            slideBoxX = 0.0,
            slideBoxWidth = 1408.0,
        )

        val result = RtlSlideAssertion.assertAll(slideData)

        assertThat(result.passed).isTrue()
        assertThat(result.failures).isEmpty()
    }

    @Test
    fun `P0 rtl config missing fails P0-RTL-CONFIG`() {
        val slideData = SlideRenderData(
            revealRtlConfig = false,
            revealHasRtlClass = true,
            navNextLeft = true,
            viewportWidth = 1280,
            slideBoxX = 0.0,
            slideBoxWidth = 1280.0,
        )

        val result = RtlSlideAssertion.assertAll(slideData)

        assertThat(result.passed).isFalse()
        assertThat(result.failureCodes()).contains(RtlAssertionCode.P0_RTL_CONFIG)
    }

    @Test
    fun `P0 rtl class missing fails P0-RTL-CLASS`() {
        val slideData = SlideRenderData(
            revealRtlConfig = true,
            revealHasRtlClass = false,
            navNextLeft = true,
            viewportWidth = 1280,
            slideBoxX = 0.0,
            slideBoxWidth = 1280.0,
        )

        val result = RtlSlideAssertion.assertAll(slideData)

        assertThat(result.passed).isFalse()
        assertThat(result.failureCodes()).contains(RtlAssertionCode.P0_RTL_CLASS)
    }

    @Test
    fun `P0 nav next not on left fails P0-NAV`() {
        val slideData = SlideRenderData(
            revealRtlConfig = true,
            revealHasRtlClass = true,
            navNextLeft = false,
            viewportWidth = 1280,
            slideBoxX = 0.0,
            slideBoxWidth = 1280.0,
        )

        val result = RtlSlideAssertion.assertAll(slideData)

        assertThat(result.passed).isFalse()
        assertThat(result.failureCodes()).contains(RtlAssertionCode.P0_NAV)
    }

    @Test
    fun `P1 slide overflows viewport fails P1-OVERFLOW`() {
        val slideData = SlideRenderData(
            revealRtlConfig = true,
            revealHasRtlClass = true,
            navNextLeft = true,
            viewportWidth = 1280,
            slideBoxX = -50.0,
            slideBoxWidth = 1400.0,
        )

        val result = RtlSlideAssertion.assertAll(slideData)

        assertThat(result.passed).isFalse()
        assertThat(result.failureCodes()).contains(RtlAssertionCode.P1_OVERFLOW)
    }

    @Test
    fun `P1 slide fits viewport passes`() {
        val slideData = SlideRenderData(
            revealRtlConfig = true,
            revealHasRtlClass = true,
            navNextLeft = true,
            viewportWidth = 1280,
            slideBoxX = 0.0,
            slideBoxWidth = 1280.0,
        )

        val result = RtlSlideAssertion.assertAll(slideData)

        assertThat(result.failureCodes()).doesNotContain(RtlAssertionCode.P1_OVERFLOW)
    }

    @Test
    fun `all P0 fail when no RTL data is present`() {
        val slideData = SlideRenderData(
            revealRtlConfig = false,
            revealHasRtlClass = false,
            navNextLeft = false,
            viewportWidth = 1280,
            slideBoxX = 0.0,
            slideBoxWidth = 1280.0,
        )

        val result = RtlSlideAssertion.assertAll(slideData)

        assertThat(result.passed).isFalse()
        assertThat(result.failureCodes())
            .containsExactlyInAnyOrder(
                RtlAssertionCode.P0_RTL_CONFIG,
                RtlAssertionCode.P0_RTL_CLASS,
                RtlAssertionCode.P0_NAV,
            )
    }

    @Test
    fun `failure messages contain the assertion code`() {
        val slideData = SlideRenderData(
            revealRtlConfig = false,
            revealHasRtlClass = false,
            navNextLeft = false,
            viewportWidth = 1280,
            slideBoxX = 0.0,
            slideBoxWidth = 1280.0,
        )

        val result = RtlSlideAssertion.assertAll(slideData)

        result.failures.forEach { failure ->
            assertThat(failure.message).contains(failure.code.name)
        }
    }
}