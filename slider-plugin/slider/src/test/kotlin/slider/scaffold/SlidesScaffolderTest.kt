package slider.scaffold

import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertIs

class SlidesScaffolderTest {

    @TempDir
    lateinit var projectDir: Path

    private val miscDir get() = projectDir.resolve("slides").resolve("misc").toFile()

    @Test
    fun `isSlidesConfigComplete returns false when misc dir does not exist`() {
        assertFalse(SlidesScaffolder.isSlidesConfigComplete(miscDir))
    }

    @Test
    fun `isSlidesConfigComplete returns false when index html is missing`() {
        miscDir.mkdirs()
        File(miscDir, "intro-deck.adoc").writeText("= Intro")
        assertFalse(SlidesScaffolder.isSlidesConfigComplete(miscDir))
    }

    @Test
    fun `isSlidesConfigComplete returns false when no deck adoc file is present`() {
        miscDir.mkdirs()
        File(miscDir, "index.html").writeText("<html></html>")
        assertFalse(SlidesScaffolder.isSlidesConfigComplete(miscDir))
    }

    @Test
    fun `isSlidesConfigComplete returns true when index html and at least one deck adoc are present`() {
        miscDir.mkdirs()
        File(miscDir, "index.html").writeText("<html></html>")
        File(miscDir, "intro-deck.adoc").writeText("= Intro")
        assertTrue(SlidesScaffolder.isSlidesConfigComplete(miscDir))
    }

    @Test
    fun `isSlidesConfigComplete returns true with multiple deck adoc files`() {
        miscDir.mkdirs()
        File(miscDir, "index.html").writeText("<html></html>")
        File(miscDir, "intro-deck.adoc").writeText("= Intro")
        File(miscDir, "kotlin-coroutines-deck.adoc").writeText("= Coroutines")
        assertTrue(SlidesScaffolder.isSlidesConfigComplete(miscDir))
    }

    @Test
    fun `isSlidesConfigComplete ignores directories ending with deck adoc`() {
        miscDir.mkdirs()
        File(miscDir, "index.html").writeText("<html></html>")
        File(miscDir, "fake-deck.adoc").mkdirs()
        assertFalse(SlidesScaffolder.isSlidesConfigComplete(miscDir))
    }

    @Test
    fun `extractSlidesZip creates all files from a non-empty zip input stream`() {
        val zipBytes = zipOf(
            "slides/misc/index.html" to "<html></html>".toByteArray(),
            "slides/misc/intro-deck.adoc" to "= Intro".toByteArray(),
        )
        val result = SlidesScaffolder.extractSlidesZip(zipBytes.inputStream(), projectDir.toFile())

        assertIs<ScaffoldResult.Created>(result)
        assertTrue(File(projectDir.toFile(), "slides/misc/index.html").exists())
        assertTrue(File(projectDir.toFile(), "slides/misc/intro-deck.adoc").exists())
    }

    @Test
    fun `extractSlidesZip skips directory entries`() {
        val zipBytes = zipOf(
            "slides/" to null,
            "slides/misc/" to null,
            "slides/misc/index.html" to "<html></html>".toByteArray(),
        )
        val result = SlidesScaffolder.extractSlidesZip(zipBytes.inputStream(), projectDir.toFile())

        assertIs<ScaffoldResult.Created>(result)
        assertTrue(File(projectDir.toFile(), "slides/misc/index.html").exists())
        assertFalse(File(projectDir.toFile(), "slides/misc/").isDirectory && File(projectDir.toFile(), "slides/misc/").listFiles()?.isEmpty() ?: true)
    }

    @Test
    fun `extractSlidesZip returns Created when at least one file is extracted`() {
        val zipBytes = zipOf(
            "slides/misc/index.html" to "<html></html>".toByteArray(),
        )
        val result = SlidesScaffolder.extractSlidesZip(zipBytes.inputStream(), projectDir.toFile())

        assertIs<ScaffoldResult.Created>(result)
    }

    private fun zipOf(vararg entries: Pair<String, ByteArray?>): ByteArray {
        val baos = java.io.ByteArrayOutputStream()
        java.util.zip.ZipOutputStream(baos).use { zos ->
            entries.forEach { (name, content) ->
                if (content == null) {
                    zos.putNextEntry(java.util.zip.ZipEntry("$name/"))
                    zos.closeEntry()
                } else {
                    zos.putNextEntry(java.util.zip.ZipEntry(name))
                    zos.write(content)
                    zos.closeEntry()
                }
            }
        }
        return baos.toByteArray()
    }
}