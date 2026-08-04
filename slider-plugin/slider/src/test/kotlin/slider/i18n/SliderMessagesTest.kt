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
}
