package slider.content

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ContentRenderDataTest {

    private fun validTextBlock(): TextBlock = TextBlock(
        text = "Hello",
        x = 10.0,
        y = 20.0,
        width = 100.0,
        height = 30.0,
    )

    @Test
    fun `data class ContentRenderData accepts valid fields`() {
        val data = ContentRenderData(
            slideTitle = "Introduction",
            realTextBlocks = listOf(validTextBlock()),
            computedTitleFontSize = 36.0,
            computedBodyFontSize = 18.0,
            computedContrastRatio = 7.2,
            hasNotesInDom = true,
            viewportWidth = 1280,
            viewportHeight = 720,
        )

        assertThat(data.slideTitle).isEqualTo("Introduction")
        assertThat(data.realTextBlocks).hasSize(1)
        assertThat(data.computedTitleFontSize).isEqualTo(36.0)
        assertThat(data.computedBodyFontSize).isEqualTo(18.0)
        assertThat(data.computedContrastRatio).isEqualTo(7.2)
        assertThat(data.hasNotesInDom).isTrue()
        assertThat(data.viewportWidth).isEqualTo(1280)
        assertThat(data.viewportHeight).isEqualTo(720)
    }

    @Test
    fun `rejects blank slideTitle`() {
        assertThatThrownBy {
            ContentRenderData(
                slideTitle = "   ",
                realTextBlocks = emptyList(),
                computedTitleFontSize = 36.0,
                computedBodyFontSize = 18.0,
                computedContrastRatio = 7.2,
                hasNotesInDom = true,
                viewportWidth = 1280,
                viewportHeight = 720,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("slideTitle")
    }

    @Test
    fun `rejects non-positive computedTitleFontSize`() {
        assertThatThrownBy {
            ContentRenderData(
                slideTitle = "Title",
                realTextBlocks = emptyList(),
                computedTitleFontSize = 0.0,
                computedBodyFontSize = 18.0,
                computedContrastRatio = 7.2,
                hasNotesInDom = true,
                viewportWidth = 1280,
                viewportHeight = 720,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("computedTitleFontSize")
    }

    @Test
    fun `rejects non-positive computedBodyFontSize`() {
        assertThatThrownBy {
            ContentRenderData(
                slideTitle = "Title",
                realTextBlocks = emptyList(),
                computedTitleFontSize = 36.0,
                computedBodyFontSize = 0.0,
                computedContrastRatio = 7.2,
                hasNotesInDom = true,
                viewportWidth = 1280,
                viewportHeight = 720,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("computedBodyFontSize")
    }

    @Test
    fun `rejects negative computedContrastRatio`() {
        assertThatThrownBy {
            ContentRenderData(
                slideTitle = "Title",
                realTextBlocks = emptyList(),
                computedTitleFontSize = 36.0,
                computedBodyFontSize = 18.0,
                computedContrastRatio = -1.0,
                hasNotesInDom = true,
                viewportWidth = 1280,
                viewportHeight = 720,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("computedContrastRatio")
    }

    @Test
    fun `rejects non-positive viewport dimensions`() {
        assertThatThrownBy {
            ContentRenderData(
                slideTitle = "Title",
                realTextBlocks = emptyList(),
                computedTitleFontSize = 36.0,
                computedBodyFontSize = 18.0,
                computedContrastRatio = 7.2,
                hasNotesInDom = true,
                viewportWidth = 0,
                viewportHeight = 720,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("viewportWidth")

        assertThatThrownBy {
            ContentRenderData(
                slideTitle = "Title",
                realTextBlocks = emptyList(),
                computedTitleFontSize = 36.0,
                computedBodyFontSize = 18.0,
                computedContrastRatio = 7.2,
                hasNotesInDom = true,
                viewportWidth = 1280,
                viewportHeight = 0,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("viewportHeight")
    }

    @Test
    fun `EMPTY factory bypasses invariants`() {
        val empty = ContentRenderData.EMPTY

        assertThat(empty.slideTitle).isEmpty()
        assertThat(empty.realTextBlocks).isEmpty()
        assertThat(empty.computedTitleFontSize).isEqualTo(0.0)
        assertThat(empty.computedBodyFontSize).isEqualTo(0.0)
        assertThat(empty.computedContrastRatio).isEqualTo(0.0)
        assertThat(empty.hasNotesInDom).isFalse()
        assertThat(empty.viewportWidth).isEqualTo(0)
        assertThat(empty.viewportHeight).isEqualTo(0)
    }

    @Test
    fun `accepts empty realTextBlocks list`() {
        val data = ContentRenderData(
            slideTitle = "Title",
            realTextBlocks = listOf(),
            computedTitleFontSize = 36.0,
            computedBodyFontSize = 18.0,
            computedContrastRatio = 7.2,
            hasNotesInDom = true,
            viewportWidth = 1280,
            viewportHeight = 720,
        )

        assertThat(data.realTextBlocks).isEmpty()
    }
}