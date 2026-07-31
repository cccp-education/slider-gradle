package slider.translation

import contracts.i18n.TranslationRequest
import contracts.i18n.TranslationResult
import contracts.i18n.TranslationService
import slider.DeckContext

class TranslationServiceAdapter(
    private val modelAdapter: LanguageModelAdapter,
    private val deckContext: DeckContext,
) : TranslationService {

    override fun translate(request: TranslationRequest): TranslationResult {
        val translated = modelAdapter.translate(
            deckContext = deckContext,
            adocContent = request.sourceText,
            sourceLanguage = request.sourceLanguage,
            targetLanguage = request.targetLanguage,
        )
        return if (translated.isNullOrBlank()) {
            TranslationResult.Failure("LLM returned empty or null response")
        } else {
            TranslationResult.Success(translated)
        }
    }
}
