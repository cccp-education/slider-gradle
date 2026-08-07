package slider.content

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class AsciidocContentParserTest {

    @Test
    fun `parseDeck should reject blank adoc content`() {
        assertThatThrownBy {
            AsciidocContentParser.parseDeck("   ")
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("adoc")
    }

    @Test
    fun `parseDeck should return empty list when no level-2 heading found`() {
        val adoc = """
            = Deck title
            :revealjs_theme: sky

            No slide heading here, just a paragraph.
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides).isEmpty()
    }

    @Test
    fun `parseDeck should extract a single slide with title only`() {
        val adoc = """
            = Deck

            == Introduction
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides).hasSize(1)
        assertThat(slides[0].title).isEqualTo("Introduction")
        assertThat(slides[0].isEmpty()).isTrue()
    }

    @Test
    fun `parseDeck should extract multiple slides`() {
        val adoc = """
            = Deck

            == First Slide

            Content one.

            == Second Slide

            Content two.
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides).hasSize(2)
        assertThat(slides.map { it.title }).containsExactly("First Slide", "Second Slide")
    }

    @Test
    fun `parseDeck should collect paragraphs between headings`() {
        val adoc = """
            = Deck

            == Slide

            First paragraph.

            Second paragraph.
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides).hasSize(1)
        assertThat(slides[0].paragraphs).containsExactly("First paragraph.", "Second paragraph.")
    }

    @Test
    fun `parseDeck should collect subtitles from level-3 headings`() {
        val adoc = """
            = Deck

            == Slide

            === Subtitle One

            Content.

            === Subtitle Two

            More content.
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides[0].subtitles).containsExactly("Subtitle One", "Subtitle Two")
    }

    @Test
    fun `parseDeck should collect bullet lists`() {
        val adoc = """
            = Deck

            == Slide

            * item one
            * item two
            * item three
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides[0].lists).hasSize(1)
        assertThat(slides[0].lists[0]).containsExactly("item one", "item two", "item three")
    }

    @Test
    fun `parseDeck should collect multiple consecutive bullet lists separated by blank line`() {
        val adoc = """
            = Deck

            == Slide

            * first list item

            * second list item
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides[0].lists).hasSize(2)
        assertThat(slides[0].lists[0]).containsExactly("first list item")
        assertThat(slides[0].lists[1]).containsExactly("second list item")
    }

    @Test
    fun `parseDeck should extract speaker note from NOTE speaker block`() {
        val adoc = """
            = Deck

            == Slide

            Content.

            [NOTE.speaker]
            --
            Remember to emphasize the introduction.
            --
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides[0].hasSpeakerNote()).isTrue()
        assertThat(slides[0].speakerNote).isEqualTo("Remember to emphasize the introduction.")
    }

    @Test
    fun `parseDeck should preserve multi-line speaker notes`() {
        val adoc = """
            = Deck

            == Slide

            [NOTE.speaker]
            --
            Line one.
            Line two.
            --
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides[0].speakerNote).isEqualTo("Line one.\nLine two.")
    }

    @Test
    fun `parseDeck should handle NOTE speaker with extra attributes`() {
        val adoc = """
            = Deck

            == Slide

            [NOTE.speaker,style=foo]
            --
            Note with attributes.
            --
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides[0].speakerNote).isEqualTo("Note with attributes.")
    }

    @Test
    fun `parseDeck should treat slide without NOTE speaker block as having null note`() {
        val adoc = """
            = Deck

            == Slide

            No note here.
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides[0].hasSpeakerNote()).isFalse()
        assertThat(slides[0].speakerNote).isNull()
    }

    @Test
    fun `parseDeck should ignore level-3 speaker note blocks for slide segmentation`() {
        val adoc = """
            = Deck

            == Slide

            === Subtitle

            [NOTE.speaker]
            --
            A note under a subtitle.
            --
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides).hasSize(1)
        assertThat(slides[0].subtitles).containsExactly("Subtitle")
        assertThat(slides[0].hasSpeakerNote()).isTrue()
        assertThat(slides[0].speakerNote).isEqualTo("A note under a subtitle.")
    }

    @Test
    fun `parseDeck should ignore attribute lines preceding the level-2 heading`() {
        val adoc = """
            = Deck

            [.slide,data-transition="zoom"]
            == Titled Slide

            Content.
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides[0].title).isEqualTo("Titled Slide")
    }

    @Test
    fun `parseDeck should only treat level-2 headings as slides`() {
        val adoc = """
            = Deck Title

            == Real Slide

            === Subsection

            content

            == Another Slide
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides).hasSize(2)
        assertThat(slides.map { it.title }).containsExactly("Real Slide", "Another Slide")
        assertThat(slides[0].subtitles).containsExactly("Subsection")
    }

    @Test
    fun `parseDeck should trim whitespace in titles`() {
        val adoc = """
            = Deck

            ==   Spaced Title  
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides[0].title).isEqualTo("Spaced Title")
    }

    @Test
    fun `parseDeck should handle a slide with all content types`() {
        val adoc = """
            = Deck

            == Full Slide

            Intro paragraph.

            === Subtitle

            Sub-content paragraph.

            * bullet one
            * bullet two

            [NOTE.speaker]
            --
            Speaker note text.
            --
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        val slide = slides[0]
        assertThat(slide.title).isEqualTo("Full Slide")
        assertThat(slide.subtitles).containsExactly("Subtitle")
        assertThat(slide.paragraphs).containsExactly("Intro paragraph.", "Sub-content paragraph.")
        assertThat(slide.lists).hasSize(1)
        assertThat(slide.lists[0]).containsExactly("bullet one", "bullet two")
        assertThat(slide.speakerNote).isEqualTo("Speaker note text.")
    }

    @Test
    fun `parseDeck should skip document header level-1 heading`() {
        val adoc = """
            = Document Title
            author <author@example.com>
            :revealjs_theme: sky

            == First Slide

            content
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides).hasSize(1)
        assertThat(slides[0].title).isEqualTo("First Slide")
    }

    @Test
    fun `parseDeck should not treat the level-1 heading as a slide`() {
        val adoc = """
            = Only Document Title

            No slide here.
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides).isEmpty()
    }

    @Test
    fun `parseDeck should separate consecutive bullet items with no blank line as one list`() {
        val adoc = """
            = Deck

            == Slide

            * one
            * two
            * three
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides[0].lists).hasSize(1)
        assertThat(slides[0].lists[0]).hasSize(3)
    }

    @Test
    fun `parseDeck should ignore comment lines`() {
        val adoc = """
            = Deck

            == Slide

            // This is a comment

            Real paragraph.
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides[0].paragraphs).containsExactly("Real paragraph.")
    }

    @Test
    fun `parseDeck should ignore attribute entries starting with colon`() {
        val adoc = """
            = Deck
            :revealjs_theme: sky

            == Slide

            content
        """.trimIndent()

        val slides = AsciidocContentParser.parseDeck(adoc)

        assertThat(slides).hasSize(1)
        assertThat(slides[0].paragraphs).containsExactly("content")
    }
}