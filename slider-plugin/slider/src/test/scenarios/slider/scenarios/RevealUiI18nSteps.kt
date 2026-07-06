package slider.scenarios

import contracts.i18n.LanguageCatalog
import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.i18n.RevealUiMessageCatalog
import slider.i18n.RevealUiMessages
import slider.i18n.RevealUiMessagesWriter
import java.io.File
import java.nio.file.Files

class RevealUiI18nSteps : En {

    private var catalog: List<RevealUiMessages> = emptyList()
    private var queriedMessages: RevealUiMessages? = null
    private var outputDir: File = Files.createTempDirectory("reveal-ui-i18n-cucumber").toFile()
    private var writtenFiles: List<File> = emptyList()

    init {

        When("the Reveal UI message catalog is queried") {
            catalog = RevealUiMessageCatalog.all()
        }

        Then("it should contain one entry per LanguageCatalog language") {
            assertThat(catalog).hasSize(LanguageCatalog.ALL.size)
        }

        Then("it should cover all LanguageCatalog supported codes") {
            LanguageCatalog.supportedCodes().forEach { code ->
                assertThat(catalog.map { it.languageCode })
                    .withFailMessage("Catalog should cover ISO code '$code'")
                    .contains(code)
            }
        }

        When("the Reveal UI messages for {string} are queried") { code: String ->
            queriedMessages = RevealUiMessageCatalog.findByCode(code)
            assertThat(queriedMessages)
                .withFailMessage("No Reveal UI messages found for code '$code'")
                .isNotNull()
        }

        Then("the RTL flag should be true") {
            assertThat(queriedMessages?.isRtl)
                .withFailMessage("Expected RTL flag to be true for ${queriedMessages?.languageCode}")
                .isTrue()
        }

        Then("the RTL flag should be false") {
            assertThat(queriedMessages?.isRtl)
                .withFailMessage("Expected RTL flag to be false for ${queriedMessages?.languageCode}")
                .isFalse()
        }

        When("the Reveal UI messages writer writes all messages to the output directory") {
            writtenFiles = RevealUiMessagesWriter.writeAll(outputDir)
        }

        Then("one messages js file should exist for each supported language") {
            LanguageCatalog.supportedCodes().forEach { code ->
                val file = outputDir.resolve("messages_$code.js")
                assertThat(file)
                    .withFailMessage("messages_$code.js should exist in output directory")
                    .exists()
            }
        }

        Then("the messages js file for {string} should contain {string}") { code: String, expected: String ->
            val file = outputDir.resolve("messages_$code.js")
            assertThat(file.readText())
                .withFailMessage("messages_$code.js should contain '$expected'")
                .contains(expected)
        }

        Then("the messages js file for {string} should not contain {string}") { code: String, unexpected: String ->
            val file = outputDir.resolve("messages_$code.js")
            assertThat(file.readText())
                .withFailMessage("messages_$code.js should not contain '$unexpected'")
                .doesNotContain(unexpected)
        }

        Then("each written file should start with {string}") { prefix: String ->
            writtenFiles.forEach { file ->
                assertThat(file.readText())
                    .withFailMessage("${file.name} should start with '$prefix'")
                    .startsWith(prefix)
            }
        }
    }
}