package slider.translate

import contracts.i18n.LanguageCatalog
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TranslatorManagerLanguageTest {

    @Test
    fun `supportedLanguages should contain all 10 LanguageCatalog ISO codes`() {
        val supported = TranslatorManager.supportedLanguages

        LanguageCatalog.supportedCodes().forEach { code ->
            assertThat(supported)
                .withFailMessage("supportedLanguages should contain ISO code '$code'")
                .contains(code)
        }
    }

    @Test
    fun `supportedLanguages should not contain legacy display names`() {
        val supported = TranslatorManager.supportedLanguages

        assertThat(supported).doesNotContain("French", "English")
    }

    @Test
    fun `supportedLanguages size should match LanguageCatalog ALL size`() {
        assertThat(TranslatorManager.supportedLanguages).hasSize(LanguageCatalog.ALL.size)
    }

    @Test
    fun `translationTasks should exclude identity permutations`() {
        val tasks = TranslatorManager.run { supportedLanguages.translationTasks() }

        tasks.forEach { entry ->
            val from = entry.second.first
            val to = entry.second.second
            assertThat(from)
                .withFailMessage("Task '${entry.first}' should not translate a language to itself")
                .isNotEqualTo(to)
        }
    }

    @Test
    fun `translationTasks should produce N times N minus 1 permutations for 10 languages`() {
        val tasks = TranslatorManager.run { supportedLanguages.translationTasks() }
        val n = LanguageCatalog.ALL.size

        assertThat(tasks).hasSize(n * (n - 1))
    }
}