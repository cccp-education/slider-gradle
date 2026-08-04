package slider.content

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OverflowDetectorTest {

    private val layout = SlideLayout(
        viewportWidth = 1280,
        viewportHeight = 720,
        marginX = 40.0,
        marginY = 30.0,
        titleFontSize = 36.0,
        bodyFontSize = 18.0,
    )

    @Test
    fun `block inside content area fits`() {
        val block = TextBlock(text = "Hello", x = 40.0, y = 30.0, width = 200.0, height = 30.0)

        val result = OverflowDetector.detect(block, layout)

        assertThat(result).isInstanceOf(OverflowResult.Fits::class.java)
    }

    @Test
    fun `block starting before left margin overflows horizontally`() {
        val block = TextBlock(text = "Hi", x = 10.0, y = 30.0, width = 200.0, height = 30.0)

        val result = OverflowDetector.detect(block, layout)

        assertThat(result).isInstanceOf(OverflowResult.Overflow::class.java)
        assertThat((result as OverflowResult.Overflow).reason).isEqualTo(OverflowReason.HORIZONTAL_OVERFLOW)
    }

    @Test
    fun `block extending past right margin overflows horizontally`() {
        val block = TextBlock(text = "Hi", x = 1100.0, y = 30.0, width = 250.0, height = 30.0)

        val result = OverflowDetector.detect(block, layout)

        assertThat(result).isInstanceOf(OverflowResult.Overflow::class.java)
        assertThat((result as OverflowResult.Overflow).reason).isEqualTo(OverflowReason.HORIZONTAL_OVERFLOW)
    }

    @Test
    fun `block starting above top margin overflows vertically`() {
        val block = TextBlock(text = "Hi", x = 40.0, y = 10.0, width = 200.0, height = 30.0)

        val result = OverflowDetector.detect(block, layout)

        assertThat(result).isInstanceOf(OverflowResult.Overflow::class.java)
        assertThat((result as OverflowResult.Overflow).reason).isEqualTo(OverflowReason.VERTICAL_OVERFLOW)
    }

    @Test
    fun `block extending past bottom margin overflows vertically`() {
        val block = TextBlock(text = "Hi", x = 40.0, y = 690.0, width = 200.0, height = 50.0)

        val result = OverflowDetector.detect(block, layout)

        assertThat(result).isInstanceOf(OverflowResult.Overflow::class.java)
        assertThat((result as OverflowResult.Overflow).reason).isEqualTo(OverflowReason.VERTICAL_OVERFLOW)
    }

    @Test
    fun `block extending past viewport width overflows horizontally`() {
        val block = TextBlock(text = "Hi", x = 1200.0, y = 30.0, width = 200.0, height = 30.0)

        val result = OverflowDetector.detect(block, layout)

        assertThat(result).isInstanceOf(OverflowResult.Overflow::class.java)
        assertThat((result as OverflowResult.Overflow).reason).isEqualTo(OverflowReason.HORIZONTAL_OVERFLOW)
    }

    @Test
    fun `block extending past viewport height overflows vertically`() {
        val block = TextBlock(text = "Hi", x = 40.0, y = 700.0, width = 200.0, height = 100.0)

        val result = OverflowDetector.detect(block, layout)

        assertThat(result).isInstanceOf(OverflowResult.Overflow::class.java)
        assertThat((result as OverflowResult.Overflow).reason).isEqualTo(OverflowReason.VERTICAL_OVERFLOW)
    }

    @Test
    fun `short text estimated width fits within content width`() {
        val block = TextBlock(text = "Hello", x = 40.0, y = 30.0, width = 100.0, height = 18.0)

        val result = OverflowDetector.detect(block, layout)

        assertThat(result).isInstanceOf(OverflowResult.Fits::class.java)
    }

    @Test
    fun `long text estimated width exceeds content width overflows text`() {
        val longText = "word ".repeat(400)
        val block = TextBlock(text = longText.trim(), x = 40.0, y = 30.0, width = 100.0, height = 18.0)

        val result = OverflowDetector.detect(block, layout)

        assertThat(result).isInstanceOf(OverflowResult.Overflow::class.java)
        assertThat((result as OverflowResult.Overflow).reason).isEqualTo(OverflowReason.ESTIMATED_TEXT_TOO_LONG)
    }

    @Test
    fun `overflows helper returns true for overflow and false for fit`() {
        val fitting = TextBlock(text = "Hi", x = 40.0, y = 30.0, width = 200.0, height = 30.0)
        val overflowing = TextBlock(text = "Hi", x = 10.0, y = 30.0, width = 200.0, height = 30.0)

        assertThat(OverflowDetector.overflows(fitting, layout)).isFalse()
        assertThat(OverflowDetector.overflows(overflowing, layout)).isTrue()
    }

    @Test
    fun `overflow message contains the reason name`() {
        val block = TextBlock(text = "Hi", x = 10.0, y = 30.0, width = 200.0, height = 30.0)

        val result = OverflowDetector.detect(block, layout) as OverflowResult.Overflow

        assertThat(result.message).contains(result.reason.name)
    }

    @Test
    fun `block exactly at content bounds fits`() {
        val block = TextBlock(
            text = "Hi",
            x = 40.0,
            y = 30.0,
            width = 1200.0,
            height = 660.0,
        )

        val result = OverflowDetector.detect(block, layout)

        assertThat(result).isInstanceOf(OverflowResult.Fits::class.java)
    }
}