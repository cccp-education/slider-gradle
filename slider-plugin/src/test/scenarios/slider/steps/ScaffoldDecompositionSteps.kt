package slider.steps

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.DeckContext
import slider.SlidesConfiguration
import slider.scaffold.ScaffoldDefaults
import slider.scaffold.ScaffoldResult
import slider.scaffold.SlidesScaffolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ScaffoldDecompositionSteps : En {

    private lateinit var miscDir: File
    private lateinit var projectDir: File
    private var completeness: Boolean? = null
    private var extractionResult: ScaffoldResult? = null
    private var slidesConfig: SlidesConfiguration? = null
    private var deckContext: DeckContext? = null
    private var zipBytes: ByteArray = ByteArray(0)

    private fun newTempDir(prefix: String): File =
        Files.createTempDirectory(prefix).toFile()

    init {

        // ---- isSlidesConfigComplete ------------------------------------------------

        Given("an empty slides misc directory") {
            miscDir = newTempDir("slider-misc-empty").also { it.mkdirs() }
        }

        Given("a slides misc directory with a deck file {string} but no index html") { deckName: String ->
            miscDir = newTempDir("slider-misc-no-index").also { it.mkdirs() }
            File(miscDir, deckName).writeText("= Deck")
        }

        Given("a slides misc directory with index html but no deck adoc file") {
            miscDir = newTempDir("slider-misc-no-deck").also { it.mkdirs() }
            File(miscDir, "index.html").writeText("<html></html>")
        }

        Given("a slides misc directory with index html and a deck file {string}") { deckName: String ->
            miscDir = newTempDir("slider-misc-complete").also { it.mkdirs() }
            File(miscDir, "index.html").writeText("<html></html>")
            File(miscDir, deckName).writeText("= Deck")
        }

        Given("a slides misc directory with index html and {int} deck files") { count: Int ->
            miscDir = newTempDir("slider-misc-multi").also { it.mkdirs() }
            File(miscDir, "index.html").writeText("<html></html>")
            repeat(count) { i -> File(miscDir, "deck-$i-deck.adoc").writeText("= Deck $i") }
        }

        Given("a slides misc directory with index html and a directory named {string}") { dirName: String ->
            miscDir = newTempDir("slider-misc-dir").also { it.mkdirs() }
            File(miscDir, "index.html").writeText("<html></html>")
            File(miscDir, dirName).mkdirs()
        }

        When("the slides configuration completeness is checked") {
            completeness = SlidesScaffolder.isSlidesConfigComplete(miscDir)
        }

        Then("the configuration should be incomplete") {
            assertThat(completeness).isFalse()
        }

        Then("the configuration should be complete") {
            assertThat(completeness).isTrue()
        }

        // ---- extractSlidesZip ------------------------------------------------------

        Given("a slides zip containing {string} and {string}") { file1: String, file2: String ->
            zipBytes = zipOf(file1 to "<html></html>".toByteArray(), file2 to "= Deck".toByteArray())
        }

        Given("a slides zip containing directory entries and the file {string}") { fileName: String ->
            zipBytes = zipOf(
                "slides/" to null,
                "slides/misc/" to null,
                fileName to "<html></html>".toByteArray(),
            )
        }

        Given("an empty slides zip") {
            zipBytes = ByteArray(0)
        }

        When("the zip is extracted into a target project directory") {
            projectDir = newTempDir("slider-zip-target").also { it.mkdirs() }
            val stream: InputStream = if (zipBytes.isEmpty()) InputStream.nullInputStream() else zipBytes.inputStream()
            extractionResult = SlidesScaffolder.extractSlidesZip(stream, projectDir)
        }

        Then("the extraction result should be Created") {
            assertThat(extractionResult).isInstanceOf(ScaffoldResult.Created::class.java)
        }

        Then("the extraction result should be Failed") {
            assertThat(extractionResult).isInstanceOf(ScaffoldResult.Failed::class.java)
        }

        Then("the target directory should contain {string}") { relativePath: String ->
            assertThat(File(projectDir, relativePath)).exists()
        }

        // ---- ScaffoldDefaults ------------------------------------------------------

        When("the default SlidesConfiguration is built") {
            slidesConfig = ScaffoldDefaults.defaultSlidesConfiguration()
        }

        Then("the source path should be {string}") { expected: String ->
            assertThat(slidesConfig?.srcPath).isEqualTo(expected)
        }

        Then("the push from should be {string}") { expected: String ->
            assertThat(slidesConfig?.pushSlides?.from).isEqualTo(expected)
        }

        Then("the push to should be {string}") { expected: String ->
            assertThat(slidesConfig?.pushSlides?.to).isEqualTo(expected)
        }

        Then("the push branch should be {string}") { expected: String ->
            assertThat(slidesConfig?.pushSlides?.branch).isEqualTo(expected)
        }

        Then("the push message should be {string}") { expected: String ->
            assertThat(slidesConfig?.pushSlides?.message).isEqualTo(expected)
        }

        Then("the repo name should be {string}") { expected: String ->
            assertThat(slidesConfig?.pushSlides?.repo?.name).isEqualTo(expected)
        }

        Then("the repo url should be {string}") { expected: String ->
            assertThat(slidesConfig?.pushSlides?.repo?.repository).isEqualTo(expected)
        }

        Then("the credentials username should be {string}") { expected: String ->
            assertThat(slidesConfig?.pushSlides?.repo?.credentials?.username).isEqualTo(expected)
        }

        Then("the credentials password should be {string}") { expected: String ->
            assertThat(slidesConfig?.pushSlides?.repo?.credentials?.password).isEqualTo(expected)
        }

        Then("the gemini key list should contain {string}") { expected: String ->
            assertThat(slidesConfig?.ai?.gemini).contains(expected)
        }

        Then("the mistral key list should contain {string}") { expected: String ->
            assertThat(slidesConfig?.ai?.mistral).contains(expected)
        }

        Then("the huggingface key list should contain {string}") { expected: String ->
            assertThat(slidesConfig?.ai?.huggingface).contains(expected)
        }

        When("the default DeckContext is built") {
            deckContext = ScaffoldDefaults.defaultDeckContext()
        }

        Then("the subject should be {string}") { expected: String ->
            assertThat(deckContext?.subject).isEqualTo(expected)
        }

        Then("the audience should be {string}") { expected: String ->
            assertThat(deckContext?.audience).isEqualTo(expected)
        }

        Then("the duration should be {int}") { expected: Int ->
            assertThat(deckContext?.duration).isEqualTo(expected)
        }

        Then("the language code should be {string}") { expected: String ->
            assertThat(deckContext?.languageCode).isEqualTo(expected)
        }

        Then("the output file should be {string}") { expected: String ->
            assertThat(deckContext?.outputFile).isEqualTo(expected)
        }

        Then("the author name should be {string}") { expected: String ->
            assertThat(deckContext?.author?.name).isEqualTo(expected)
        }

        Then("the author email should be {string}") { expected: String ->
            assertThat(deckContext?.author?.email).isEqualTo(expected)
        }

        Then("the revealjs theme should be {string}") { expected: String ->
            assertThat(deckContext?.revealjs?.theme).isEqualTo(expected)
        }

        Then("the revealjs slide number should be {string}") { expected: String ->
            assertThat(deckContext?.revealjs?.slideNumber).isEqualTo(expected)
        }

        Then("the revealjs width should be {int}") { expected: Int ->
            assertThat(deckContext?.revealjs?.width).isEqualTo(expected)
        }

        Then("the revealjs height should be {int}") { expected: Int ->
            assertThat(deckContext?.revealjs?.height).isEqualTo(expected)
        }

        Then("the revealjs controls should be enabled") {
            assertThat(deckContext?.revealjs?.controls).isTrue()
        }

        Then("the revealjs controls layout should be {string}") { expected: String ->
            assertThat(deckContext?.revealjs?.controlsLayout).isEqualTo(expected)
        }

        Then("the revealjs history should be enabled") {
            assertThat(deckContext?.revealjs?.history).isTrue()
        }

        Then("the revealjs fragment in URL should be enabled") {
            assertThat(deckContext?.revealjs?.fragmentInURL).isTrue()
        }

        Then("the speaker notes should be enabled") {
            assertThat(deckContext?.notes?.speakerNotes).isTrue()
        }

        Then("the page notes should be enabled") {
            assertThat(deckContext?.notes?.pageNotes).isTrue()
        }

        Then("the page notes style should be DETAILED") {
            assertThat(deckContext?.notes?.pageNotesStyle?.name).isEqualTo("DETAILED")
        }

        Then("the slides count should be {int}") { expected: Int ->
            assertThat(deckContext?.slides?.size).isEqualTo(expected)
        }

        Then("the slide {int} title should be {string}") { index: Int, expected: String ->
            assertThat(deckContext?.slides?.get(index - 1)?.title).isEqualTo(expected)
        }
    }

    private fun zipOf(vararg entries: Pair<String, ByteArray?>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            entries.forEach { (name, content) ->
                if (content == null) {
                    zos.putNextEntry(ZipEntry("$name/"))
                    zos.closeEntry()
                } else {
                    zos.putNextEntry(ZipEntry(name))
                    zos.write(content)
                    zos.closeEntry()
                }
            }
        }
        return baos.toByteArray()
    }
}