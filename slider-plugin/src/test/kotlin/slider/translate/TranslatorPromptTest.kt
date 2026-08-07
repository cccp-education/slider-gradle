package slider.translate

import contracts.i18n.LanguageCatalog
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TranslatorPromptTest {

    private val promptManager = TranslatorManager.PromptManager

    @Test
    fun `prompt should contain the source text to translate`() {
        val pair = "fr" to "en"
        val prompt = promptManager.run { pair.getTranslatePromptMessage("Bonjour le monde") }

        assertThat(prompt).contains("Bonjour le monde")
    }

    @Test
    fun `prompt should mention target language native name for Arabic`() {
        val pair = "fr" to "ar"
        val prompt = promptManager.run { pair.getTranslatePromptMessage("Bonjour") }
        val arabicNativeName = LanguageCatalog.findByCode("ar")?.nativeName

        assertThat(arabicNativeName).isNotNull()
        assertThat(prompt)
            .withFailMessage("Prompt should mention Arabic native name '$arabicNativeName'")
            .contains(arabicNativeName!!)
    }

    @Test
    fun `prompt should mention target language native name for Chinese`() {
        val pair = "en" to "zh"
        val prompt = promptManager.run { pair.getTranslatePromptMessage("Hello") }
        val chineseNativeName = LanguageCatalog.findByCode("zh")?.nativeName

        assertThat(chineseNativeName).isNotNull()
        assertThat(prompt).contains(chineseNativeName!!)
    }

    @Test
    fun `prompt should mention source language native name`() {
        val pair = "fr" to "en"
        val prompt = promptManager.run { pair.getTranslatePromptMessage("Bonjour") }
        val frenchNativeName = LanguageCatalog.findByCode("fr")?.nativeName

        assertThat(frenchNativeName).isNotNull()
        assertThat(prompt).contains(frenchNativeName!!)
    }

    @Test
    fun `prompt should contain both source and target native names for fr to ur`() {
        val pair = "fr" to "ur"
        val prompt = promptManager.run { pair.getTranslatePromptMessage("Bonjour") }
        val frenchNative = LanguageCatalog.findByCode("fr")?.nativeName
        val urduNative = LanguageCatalog.findByCode("ur")?.nativeName

        assertThat(prompt).contains(frenchNative!!).contains(urduNative!!)
    }

    @Test
    fun `prompt should contain target ISO code for clarity`() {
        val pair = "fr" to "hi"
        val prompt = promptManager.run { pair.getTranslatePromptMessage("Bonjour") }

        assertThat(prompt).contains("hi")
    }
}