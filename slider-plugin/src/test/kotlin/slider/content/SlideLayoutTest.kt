package slider.content

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class SlideLayoutTest {

    @Test
    fun `creates slide layout with viewport margins and font sizes`() {
        val layout = SlideLayout(
            viewportWidth = 1280,
            viewportHeight = 720,
            marginX = 40.0,
            marginY = 30.0,
            titleFontSize = 36.0,
            bodyFontSize = 18.0,
        )

        assertThat(layout.viewportWidth).isEqualTo(1280)
        assertThat(layout.viewportHeight).isEqualTo(720)
        assertThat(layout.marginX).isEqualTo(40.0)
        assertThat(layout.marginY).isEqualTo(30.0)
        assertThat(layout.titleFontSize).isEqualTo(36.0)
        assertThat(layout.bodyFontSize).isEqualTo(18.0)
    }

    @Test
    fun `rejects non-positive viewport width`() {
        assertThatThrownBy {
            SlideLayout(viewportWidth = 0, viewportHeight = 720, marginX = 40.0, marginY = 30.0, titleFontSize = 36.0, bodyFontSize = 18.0)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("viewportWidth")
    }

    @Test
    fun `rejects non-positive viewport height`() {
        assertThatThrownBy {
            SlideLayout(viewportWidth = 1280, viewportHeight = 0, marginX = 40.0, marginY = 30.0, titleFontSize = 36.0, bodyFontSize = 18.0)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("viewportHeight")
    }

    @Test
    fun `rejects negative horizontal margin`() {
        assertThatThrownBy {
            SlideLayout(viewportWidth = 1280, viewportHeight = 720, marginX = -10.0, marginY = 30.0, titleFontSize = 36.0, bodyFontSize = 18.0)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("marginX")
    }

    @Test
    fun `rejects negative vertical margin`() {
        assertThatThrownBy {
            SlideLayout(viewportWidth = 1280, viewportHeight = 720, marginX = 40.0, marginY = -5.0, titleFontSize = 36.0, bodyFontSize = 18.0)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("marginY")
    }

    @Test
    fun `rejects non-positive title font size`() {
        assertThatThrownBy {
            SlideLayout(viewportWidth = 1280, viewportHeight = 720, marginX = 40.0, marginY = 30.0, titleFontSize = 0.0, bodyFontSize = 18.0)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("titleFontSize")
    }

    @Test
    fun `rejects non-positive body font size`() {
        assertThatThrownBy {
            SlideLayout(viewportWidth = 1280, viewportHeight = 720, marginX = 40.0, marginY = 30.0, titleFontSize = 36.0, bodyFontSize = 0.0)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("bodyFontSize")
    }

    @Test
    fun `content width is viewport minus twice horizontal margin`() {
        val layout = SlideLayout(viewportWidth = 1280, viewportHeight = 720, marginX = 40.0, marginY = 30.0, titleFontSize = 36.0, bodyFontSize = 18.0)

        assertThat(layout.contentWidth).isEqualTo(1200.0)
    }

    @Test
    fun `content height is viewport minus twice vertical margin`() {
        val layout = SlideLayout(viewportWidth = 1280, viewportHeight = 720, marginX = 40.0, marginY = 30.0, titleFontSize = 36.0, bodyFontSize = 18.0)

        assertThat(layout.contentHeight).isEqualTo(660.0)
    }

    @Test
    fun `zero margins yield full viewport content area`() {
        val layout = SlideLayout(viewportWidth = 1024, viewportHeight = 768, marginX = 0.0, marginY = 0.0, titleFontSize = 24.0, bodyFontSize = 16.0)

        assertThat(layout.contentWidth).isEqualTo(1024.0)
        assertThat(layout.contentHeight).isEqualTo(768.0)
    }
}