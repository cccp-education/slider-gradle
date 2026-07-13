package slider.translation

import slider.DeckContext

/**
 * Port (DDD hexagonal interface) for the LLM that performs the actual
 * deck translation.
 *
 * The domain layer depends on this abstraction, never on a concrete
 * langchain4j or Ollama client. This keeps the domain pure and testable
 * with a simple stub.
 *
 * Implementations live in the infrastructure adapter layer
 * (e.g. an Ollama-backed adapter in `slider.ai`).
 */
interface LanguageModelAdapter {

    /**
     * Translates the given AsciiDoc content from [sourceLanguage] to
     * [targetLanguage].
     *
     * @param deckContext      the source deck metadata (subject, audience,
     *                         slides hints) — provides context to the LLM
     *                         for a more accurate translation.
     * @param adocContent      the AsciiDoc source to translate.
     * @param sourceLanguage   ISO 639-1 source code.
     * @param targetLanguage   ISO 639-1 target code.
     * @return the translated AsciiDoc content, or null if the LLM returned
     *         an empty/invalid response.
     */
    fun translate(
        deckContext: DeckContext,
        adocContent: String,
        sourceLanguage: String,
        targetLanguage: String,
    ): String?
}