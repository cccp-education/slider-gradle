package slider.i18n

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.MissingResourceException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SliderMessagesTest {

    @ParameterizedTest
    @ValueSource(strings = [
        "task.group.collect",
        "task.group.slider-ai",
        "task.reindexRag.description",
        "task.proposeDeckContext.description",
        "task.generateDeck.description",
        "task.helloOllama.description",
        "task.helloOllamaStreaming.description",
        "task.helloGemini.description",
        "task.helloGeminiStreaming.description",
        "task.helloMistral.description",
        "task.helloMistralStreaming.description",
        "task.helloHuggingFace.description",
        "task.helloHuggingFaceStreaming.description",
    ])
    fun `all new i18n keys resolve in English`(key: String) {
        val value = SliderMessages.get(key, "en")
        assertNotNull(value)
        assertTrue(value.isNotBlank(), "Key '$key' has blank English value")
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "task.group.collect",
        "task.group.slider-ai",
        "task.reindexRag.description",
        "task.proposeDeckContext.description",
        "task.generateDeck.description",
        "task.helloOllama.description",
        "task.helloOllamaStreaming.description",
        "task.helloGemini.description",
        "task.helloGeminiStreaming.description",
        "task.helloMistral.description",
        "task.helloMistralStreaming.description",
        "task.helloHuggingFace.description",
        "task.helloHuggingFaceStreaming.description",
    ])
    fun `all new i18n keys resolve in French`(key: String) {
        val value = SliderMessages.get(key, "fr")
        assertNotNull(value)
        assertTrue(value.isNotBlank(), "Key '$key' has blank French value")
    }

    @Test
    fun `format smoke test description with model name`() {
        val result = SliderMessages.format("task.helloOllama.description", "en", "gpt-oss:120b-cloud")
        assertTrue(result.contains("gpt-oss:120b-cloud"))
        assertTrue(result.contains("smoke test"))
    }

    @Test
    fun `missing key throws MissingResourceException`() {
        assertFailsWith<MissingResourceException> {
            SliderMessages.get("task.nonexistent.key", "en")
        }
    }

    @Test
    fun `fallback to English for unsupported language`() {
        val en = SliderMessages.get("task.reindexRag.description", "en")
        val fallback = SliderMessages.get("task.reindexRag.description", "xx")
        assertEquals(en, fallback)
    }

    @ParameterizedTest
    @ValueSource(strings = ["en", "fr", "zh", "hi", "es", "ar", "bn", "pt", "ru", "ur"])
    fun `resolve task description in all supported languages`(code: String) {
        val value = SliderMessages.get("task.reindexRag.description", code)
        assertNotNull(value)
        assertTrue(value.isNotBlank(), "task.reindexRag.description should not be blank for '$code'")
    }

    @ParameterizedTest
    @ValueSource(strings = ["en", "fr", "zh", "hi", "es", "ar", "bn", "pt", "ru", "ur"])
    fun `format task description with model name in all supported languages`(code: String) {
        val result = SliderMessages.format("task.helloOllama.description", code, "gpt-oss:120b-cloud")
        assertTrue(result.contains("gpt-oss:120b-cloud"), "Formatted message should contain model name for '$code'")
    }

    @Test
    fun `format multi-arg message with all placeholders filled`() {
        val result = SliderMessages.format("task.cleanBuild.cleaned", "en", 3, "/tmp/build", 5, 2, 1)
        assertTrue(result.contains("3"))
        assertTrue(result.contains("/tmp/build"))
        assertTrue(result.contains("5"))
        assertTrue(result.contains("2"))
        assertTrue(result.contains("1"))
    }

    @Test
    fun `format translateDeck translating message with subject and counts`() {
        val result = SliderMessages.format("task.translateDeck.translating", "fr", "Kotlin Coroutines", "fr", 9)
        assertTrue(result.contains("Kotlin Coroutines"))
        assertTrue(result.contains("fr"))
        assertTrue(result.contains("9"))
    }

    @Test
    fun `forLanguage returns non-null bundle for all supported languages`() {
        for (code in listOf("en", "fr", "zh", "hi", "es", "ar", "bn", "pt", "ru", "ur")) {
            val bundle = SliderMessages.forLanguage(code)
            assertNotNull(bundle, "Bundle should not be null for '$code'")
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["task.group.build", "task.group.generate", "task.group.slider-ai"])
    fun `group keys resolve to non-blank values in English`(key: String) {
        val value = SliderMessages.get(key, "en")
        assertTrue(value.isNotBlank(), "Group key '$key' should not be blank")
    }
}
