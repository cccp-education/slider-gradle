package slider.revealjs

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RevealJsDomainTest {

    // ---------------------------------------------------------------------------
    // RevealJsAttributesSpec
    // ---------------------------------------------------------------------------

    @Test
    fun `the default attributes spec declares the fourteen rendering attributes`() {
        val attrs = RevealJsAttributesSpec.DEFAULT.attributes
        assertEquals(14, attrs.size)
    }

    @Test
    fun `the default attributes spec uses the coderay source highlighter`() {
        assertEquals("coderay", RevealJsAttributesSpec.DEFAULT.attributes[RevealJsAttributeKeys.SOURCE_HIGHLIGHTER])
    }

    @Test
    fun `the default attributes spec sets the talaria custom css`() {
        assertEquals("talaria.css", RevealJsAttributesSpec.DEFAULT.attributes[RevealJsAttributeKeys.REVEALJS_CUSTOMCSS])
    }

    @Test
    fun `the default attributes spec uses the black revealjs theme`() {
        assertEquals("black", RevealJsAttributesSpec.DEFAULT.attributes[RevealJsAttributeKeys.REVEALJS_THEME])
    }

    @Test
    fun `the default attributes spec enables history and slide numbering`() {
        assertEquals("true", RevealJsAttributesSpec.DEFAULT.attributes[RevealJsAttributeKeys.REVEALJS_HISTORY])
        assertEquals("true", RevealJsAttributesSpec.DEFAULT.attributes[RevealJsAttributeKeys.REVEALJS_SLIDENUMBER])
    }

    @Test
    fun `a custom attributes spec overrides the defaults with supplied entries`() {
        val custom = RevealJsAttributesSpec.DEFAULT.withOverrides(
            mapOf(RevealJsAttributeKeys.REVEALJS_THEME to "white"),
        )
        assertEquals("white", custom.attributes[RevealJsAttributeKeys.REVEALJS_THEME])
        // other defaults preserved
        assertEquals("coderay", custom.attributes[RevealJsAttributeKeys.SOURCE_HIGHLIGHTER])
        assertEquals(14, custom.attributes.size)
    }

    @Test
    fun `an attributes spec rejects a blank value`() {
        assertFailsWith<IllegalArgumentException> {
            RevealJsAttributesSpec(mapOf(RevealJsAttributeKeys.REVEALJS_THEME to "   "))
        }
    }

    // ---------------------------------------------------------------------------
    // RevealJsOutputDir
    // ---------------------------------------------------------------------------

    @Test
    fun `an output dir resolves build_dir docs asciidocRevealJs`() {
        val dir = RevealJsOutputDir(File("/tmp/proj/build"))
        assertEquals(File("/tmp/proj/build/docs/asciidocRevealJs"), dir.asFile())
    }

    @Test
    fun `an output dir rejects a blank build dir`() {
        assertFailsWith<IllegalArgumentException> {
            RevealJsOutputDir(File(""))
        }
    }

    // ---------------------------------------------------------------------------
    // SlideSourceDir
    // ---------------------------------------------------------------------------

    @Test
    fun `a slide source dir resolves project_slides_misc`() {
        val dir = SlideSourceDir(File("/tmp/proj"))
        assertEquals(File("/tmp/proj/slides/misc"), dir.asFile())
    }

    @Test
    fun `a slide source dir rejects a blank project dir`() {
        assertFailsWith<IllegalArgumentException> {
            SlideSourceDir(File(""))
        }
    }

    // ---------------------------------------------------------------------------
    // SlideMetadata
    // ---------------------------------------------------------------------------

    @Test
    fun `a slide metadata keeps name and filename`() {
        val meta = SlideMetadata(name = "intro", filename = "intro.html")
        assertEquals("intro", meta.name)
        assertEquals("intro.html", meta.filename)
    }

    @Test
    fun `a slide metadata rejects a blank name`() {
        assertFailsWith<IllegalArgumentException> { SlideMetadata(name = "", filename = "intro.html") }
    }

    @Test
    fun `a slide metadata rejects a blank filename`() {
        assertFailsWith<IllegalArgumentException> { SlideMetadata(name = "intro", filename = "") }
    }

    // ---------------------------------------------------------------------------
    // SlideMetadataScanner — pure adoc scanning
    // ---------------------------------------------------------------------------

    @Test
    fun `the scanner lists adoc files as slide metadata sorted by name`(@TempDir dir: File) {
        dir.resolve("a-deck.adoc").writeText("= a")
        dir.resolve("z-deck.adoc").writeText("= z")
        dir.resolve("ignore.txt").writeText("ignored")
        dir.resolve("notes").mkdirs()

        val metas = SlideMetadataScanner.scan(dir)
        assertEquals(2, metas.size)
        assertEquals("a-deck", metas[0].name)
        assertEquals("a-deck.html", metas[0].filename)
        assertEquals("z-deck", metas[1].name)
        assertEquals("z-deck.html", metas[1].filename)
    }

    @Test
    fun `the scanner returns an empty list when the directory does not exist`(@TempDir parent: File) {
        val missing = parent.resolve("does-not-exist")
        val metas = SlideMetadataScanner.scan(missing)
        assertTrue(metas.isEmpty())
    }

    @Test
    fun `the scanner returns an empty list for a directory with no adoc files`(@TempDir dir: File) {
        dir.resolve("notes.txt").writeText("ignored")
        assertEquals(0, SlideMetadataScanner.scan(dir).size)
    }

    // ---------------------------------------------------------------------------
    // DashboardJsonSerializer
    // ---------------------------------------------------------------------------

    @Test
    fun `the dashboard serializer renders an empty array for no slides`() {
        val json = DashboardJsonSerializer.serialize(emptyList())
        assertEquals("[]", json)
    }

    @Test
    fun `the dashboard serializer renders a single slide as a one-element array`() {
        val json = DashboardJsonSerializer.serialize(
            listOf(SlideMetadata("intro", "intro.html")),
        )
        assertEquals("""[
  {"name": "intro", "filename": "intro.html"}
]""", json)
    }

    @Test
    fun `the dashboard serializer renders multiple slides comma-separated`() {
        val json = DashboardJsonSerializer.serialize(
            listOf(
                SlideMetadata("intro", "intro.html"),
                SlideMetadata("topic", "topic.html"),
            ),
        )
        assertEquals("""[
  {"name": "intro", "filename": "intro.html"},
  {"name": "topic", "filename": "topic.html"}
]""", json)
    }

    @Test
    fun `the dashboard serializer escapes double quotes in slide names`() {
        val json = DashboardJsonSerializer.serialize(
            listOf(SlideMetadata("quote\"deck", "quote-deck.html")),
        )
        // The embedded double quote in the name must be escaped as \"
        assertTrue(json.contains("quote\\\"deck"))
    }

    // ---------------------------------------------------------------------------
    // CleanBuildTarget
    // ---------------------------------------------------------------------------

    @Test
    fun `a clean build target reports the slides json, images dir and html files`(@TempDir build: File) {
        val output = build.resolve("docs").resolve("asciidocRevealJs")
        output.mkdirs()
        output.resolve("slides.json").writeText("[]")
        output.resolve("index.html").writeText("<html/>")
        output.resolve("deck.html").writeText("<html/>")
        output.resolve("talaria.css").writeText("body{}")
        output.resolve("images").mkdirs()
        output.resolve("images").resolve("logo.png").writeText("png")

        val target = CleanBuildTarget(output)
        val report = target.collect()
        assertTrue(report.slidesJsonDeleted)
        assertEquals(2, report.htmlFilesDeleted.size)
        assertTrue(report.imagesDirDeleted)
        assertEquals(4, report.cleanedCount())
    }

    @Test
    fun `a clean build target reports no deletions for a missing output dir`(@TempDir build: File) {
        val target = CleanBuildTarget(build.resolve("docs").resolve("asciidocRevealJs"))
        val report = target.collect()
        assertFalse(report.slidesJsonDeleted)
        assertFalse(report.imagesDirDeleted)
        assertEquals(0, report.cleanedCount())
    }

    @Test
    fun `a clean build target does not delete non-html non-json non-images files`(@TempDir build: File) {
        val output = build.resolve("docs").resolve("asciidocRevealJs")
        output.mkdirs()
        output.resolve("talaria.css").writeText("body{}")
        output.resolve("data.txt").writeText("kept")

        val target = CleanBuildTarget(output)
        val report = target.collect()
        assertEquals(0, report.cleanedCount())
        assertTrue(output.resolve("talaria.css").exists())
        assertTrue(output.resolve("data.txt").exists())
    }
}