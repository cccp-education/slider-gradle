package slider.translation

import contracts.i18n.LanguageCatalog
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import slider.DeckContext

/**
 * Infrastructure adapter that implements [LanguageModelAdapter] using a
 * langchain4j [ChatModel].
 *
 * This is the bridge between the pure `slider.translation` domain and
 * the concrete LLM providers (Ollama, Gemini, Mistral, …). The domain
 * layer never references langchain4j — it only sees the port.
 *
 * @param model  the resolved langchain4j chat model for the active provider.
 */
class OllamaLanguageModelAdapter(
    private val model: ChatModel,
) : LanguageModelAdapter {

    override fun translate(
        deckContext: DeckContext,
        adocContent: String,
        sourceLanguage: String,
        targetLanguage: String,
    ): String? {
        val source = LanguageCatalog.findByCode(sourceLanguage)
        val target = LanguageCatalog.findByCode(targetLanguage)
        val sourceName = source?.nativeName ?: sourceLanguage
        val targetName = target?.nativeName ?: targetLanguage

        val systemMsg = SystemMessage.from(
            "You are a professional translator. Translate the following AsciiDoc " +
                "presentation content from $sourceName ($sourceLanguage) " +
                "to $targetName ($targetLanguage). " +
                "Preserve all AsciiDoc syntax, Reveal.js attributes, and structural " +
                "elements (= for titles, == for sections, [ ] for attributes). " +
                "Only translate the human-readable text. " +
                "Return ONLY the translated AsciiDoc content, no commentary."
        )
        val userMsg = UserMessage.from(adocContent)

        return runCatching {
            val response = model.chat(listOf(systemMsg, userMsg))
            val text = response.aiMessage().text()
            if (text.isNullOrBlank()) null else text
        }.getOrNull()
    }
}