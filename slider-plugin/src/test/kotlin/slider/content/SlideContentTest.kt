package slider.content

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class SlideContentTest {

    @Test
    fun `creates slide content with title and empty collections by default`() {
        val content = SlideContent(title = "Introduction")

        assertThat(content.title).isEqualTo("Introduction")
        assertThat(content.subtitles).isEmpty()
        assertThat(content.paragraphs).isEmpty()
        assertThat(content.lists).isEmpty()
        assertThat(content.speakerNote).isNull()
    }

    @Test
    fun `creates slide content with all fields populated`() {
        val content = SlideContent(
            title = "Kotlin Coroutines",
            subtitles = listOf("Suspending functions", "Structured concurrency"),
            paragraphs = listOf("Coroutines are lightweight.", "They suspend without blocking threads."),
            lists = listOf(listOf("launch", "async"), listOf("Job", "Deferred")),
            speakerNote = "Emphasize the difference between launch and async.",
        )

        assertThat(content.title).isEqualTo("Kotlin Coroutines")
        assertThat(content.subtitles).containsExactly("Suspending functions", "Structured concurrency")
        assertThat(content.paragraphs).hasSize(2)
        assertThat(content.lists).hasSize(2)
        assertThat(content.speakerNote).isEqualTo("Emphasize the difference between launch and async.")
    }

    @Test
    fun `rejects blank title`() {
        assertThatThrownBy {
            SlideContent(title = "   ")
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("title")
    }

    @Test
    fun `rejects empty title`() {
        assertThatThrownBy {
            SlideContent(title = "")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `rejects blank subtitle in list`() {
        assertThatThrownBy {
            SlideContent(
                title = "Slide",
                subtitles = listOf("Valid", "   "),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("subtitle")
    }

    @Test
    fun `rejects blank paragraph in list`() {
        assertThatThrownBy {
            SlideContent(
                title = "Slide",
                paragraphs = listOf("Valid paragraph", ""),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("paragraph")
    }

    @Test
    fun `rejects empty list item collection`() {
        assertThatThrownBy {
            SlideContent(
                title = "Slide",
                lists = listOf(listOf("item one"), emptyList()),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("list")
    }

    @Test
    fun `rejects blank item in list`() {
        assertThatThrownBy {
            SlideContent(
                title = "Slide",
                lists = listOf(listOf("item one", "  ")),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("item")
    }

    @Test
    fun `rejects blank speaker note`() {
        assertThatThrownBy {
            SlideContent(
                title = "Slide",
                speakerNote = "   ",
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("speakerNote")
    }

    @Test
    fun `two slide contents with same fields are equal`() {
        val a = SlideContent(
            title = "Title",
            subtitles = listOf("Sub"),
            paragraphs = listOf("Para"),
            lists = listOf(listOf("item")),
            speakerNote = "Note",
        )
        val b = SlideContent(
            title = "Title",
            subtitles = listOf("Sub"),
            paragraphs = listOf("Para"),
            lists = listOf(listOf("item")),
            speakerNote = "Note",
        )

        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun `has speaker note returns true when note is non-null`() {
        val withNote = SlideContent(title = "Slide", speakerNote = "A note")
        val withoutNote = SlideContent(title = "Slide")

        assertThat(withNote.hasSpeakerNote()).isTrue()
        assertThat(withoutNote.hasSpeakerNote()).isFalse()
    }

    @Test
    fun `is empty returns true when no subtitles paragraphs lists or note`() {
        val empty = SlideContent(title = "Title")
        val rich = SlideContent(
            title = "Title",
            paragraphs = listOf("content"),
        )

        assertThat(empty.isEmpty()).isTrue()
        assertThat(rich.isEmpty()).isFalse()
    }
}