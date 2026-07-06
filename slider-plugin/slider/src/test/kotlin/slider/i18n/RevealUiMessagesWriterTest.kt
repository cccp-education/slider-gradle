package slider.i18n

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class RevealUiMessagesWriterTest {

    @TempDir
    lateinit var outputDir: File

    @Test
    fun `writeAll should create one messages_{code} js file per supported language`() {
        RevealUiMessagesWriter.writeAll(outputDir)

        RevealUiMessageCatalog.all().forEach { messages ->
            val file = outputDir.resolve("messages_${messages.languageCode}.js")
            assertThat(file)
                .withFailMessage("messages_${messages.languageCode}.js should exist")
                .exists()
        }
    }

    @Test
    fun `written file should contain RevealI18n messages assignment for the matching code`() {
        RevealUiMessagesWriter.writeAll(outputDir)

        val frFile = outputDir.resolve("messages_fr.js")
        val content = frFile.readText()

        assertThat(content).contains("RevealI18n")
        assertThat(content).contains("fr")
        assertThat(content).contains("Diapositive précédente")
        assertThat(content).contains("Diapositive suivante")
    }

    @Test
    fun `written file for Arabic should embed rtl true flag`() {
        RevealUiMessagesWriter.writeAll(outputDir)

        val arFile = outputDir.resolve("messages_ar.js")
        val content = arFile.readText()

        assertThat(content).contains("ar")
        assertThat(content).contains("rtl")
        assertThat(content).contains("الشريحة السابقة")
    }

    @Test
    fun `written file for English should not embed rtl flag set to true`() {
        RevealUiMessagesWriter.writeAll(outputDir)

        val enFile = outputDir.resolve("messages_en.js")
        val content = enFile.readText()

        assertThat(content).doesNotContain("rtl: true")
    }

    @Test
    fun `written file should expose nav messages as a nested nav object`() {
        RevealUiMessagesWriter.writeAll(outputDir)

        val enFile = outputDir.resolve("messages_en.js")
        val content = enFile.readText()

        assertThat(content).contains("nav")
        assertThat(content).contains("prev")
        assertThat(content).contains("next")
        assertThat(content).contains("up")
        assertThat(content).contains("help")
    }

    @Test
    fun `written file should expose controls messages as a nested controls object`() {
        RevealUiMessagesWriter.writeAll(outputDir)

        val frFile = outputDir.resolve("messages_fr.js")
        val content = frFile.readText()

        assertThat(content).contains("controls")
        assertThat(content).contains("overview")
        assertThat(content).contains("speakerNotes")
        assertThat(content).contains("fullscreen")
    }

    @Test
    fun `writeAll should return the list of written files`() {
        val written = RevealUiMessagesWriter.writeAll(outputDir)

        assertThat(written).hasSize(RevealUiMessageCatalog.all().size)
        written.forEach { file ->
            assertThat(file.name).startsWith("messages_").endsWith(".js")
        }
    }

    @Test
    fun `written files should be valid JavaScript opening with a statement`() {
        RevealUiMessagesWriter.writeAll(outputDir)

        RevealUiMessageCatalog.all().forEach { messages ->
            val file = outputDir.resolve("messages_${messages.languageCode}.js")
            val content = file.readText()
            assertThat(content)
                .withFailMessage("${file.name} should start with RevealI18n assignment")
                .startsWith("RevealI18n")
        }
    }
}