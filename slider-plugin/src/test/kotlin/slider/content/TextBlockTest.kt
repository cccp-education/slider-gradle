package slider.content

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class TextBlockTest {

    @Test
    fun `creates text block with text position and bounding box`() {
        val block = TextBlock(
            text = "Hello world",
            x = 0.0,
            y = 0.0,
            width = 200.0,
            height = 30.0,
        )

        assertThat(block.text).isEqualTo("Hello world")
        assertThat(block.x).isEqualTo(0.0)
        assertThat(block.y).isEqualTo(0.0)
        assertThat(block.width).isEqualTo(200.0)
        assertThat(block.height).isEqualTo(30.0)
    }

    @Test
    fun `rejects blank text`() {
        assertThatThrownBy {
            TextBlock(text = "   ", x = 0.0, y = 0.0, width = 100.0, height = 20.0)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("text")
    }

    @Test
    fun `rejects empty text`() {
        assertThatThrownBy {
            TextBlock(text = "", x = 0.0, y = 0.0, width = 100.0, height = 20.0)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `rejects negative width`() {
        assertThatThrownBy {
            TextBlock(text = "Hi", x = 0.0, y = 0.0, width = -10.0, height = 20.0)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("width")
    }

    @Test
    fun `rejects negative height`() {
        assertThatThrownBy {
            TextBlock(text = "Hi", x = 0.0, y = 0.0, width = 100.0, height = -5.0)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("height")
    }

    @Test
    fun `accepts zero width and zero height as degenerate block`() {
        val block = TextBlock(text = "Hi", x = 10.0, y = 20.0, width = 0.0, height = 0.0)

        assertThat(block.width).isEqualTo(0.0)
        assertThat(block.height).isEqualTo(0.0)
    }

    @Test
    fun `right edge is x plus width`() {
        val block = TextBlock(text = "Hi", x = 100.0, y = 50.0, width = 250.0, height = 30.0)

        assertThat(block.rightEdge).isEqualTo(350.0)
    }

    @Test
    fun `bottom edge is y plus height`() {
        val block = TextBlock(text = "Hi", x = 100.0, y = 50.0, width = 250.0, height = 30.0)

        assertThat(block.bottomEdge).isEqualTo(80.0)
    }

    @Test
    fun `two blocks with same fields are equal`() {
        val a = TextBlock(text = "Hi", x = 1.0, y = 2.0, width = 3.0, height = 4.0)
        val b = TextBlock(text = "Hi", x = 1.0, y = 2.0, width = 3.0, height = 4.0)

        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun `estimated character count equals text length`() {
        val block = TextBlock(text = "Hello", x = 0.0, y = 0.0, width = 100.0, height = 20.0)

        assertThat(block.characterCount).isEqualTo(5)
    }
}