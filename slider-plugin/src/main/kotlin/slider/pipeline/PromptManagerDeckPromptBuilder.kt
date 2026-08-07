package slider.pipeline

import slider.ai.AssistantManager.PromptManager

/**
 * Production adapter for [DeckPromptBuilder] — bridges the pipeline port to
 * the existing [PromptManager] (system + user prompts of `proposeDeckContext`
 * and `generateDeck`).
 *
 * The pipeline port is intentionally narrow ([buildProposePrompt] /
 * [buildGeneratePrompt] return a single prompt string), while [PromptManager]
 * exposes structured system + user messages. This adapter concatenates them
 * into the flat prompt the LLM consumes — keeping the koog graph free from
 * langchain4j message types and preserving the existing prompt engineering.
 */
class PromptManagerDeckPromptBuilder : DeckPromptBuilder {

    override fun buildProposePrompt(state: DeckState): String = buildString {
        appendLine(PromptManager.contextSystemPrompt)
        appendLine()
        appendLine(
            PromptManager.contextUserMessage(
                subject = state.subject,
                language = state.language,
                authorName = state.authorName,
                authorEmail = state.authorEmail,
                ragContext = state.ragContext,
            ),
        )
    }

    override fun buildGeneratePrompt(state: DeckState): String = buildString {
        appendLine(PromptManager.deckSystemPrompt)
        appendLine()
        appendLine(
            PromptManager.deckUserMessage(
                ctx = slider.SliderConfig.yamlMapper.readValue(
                    state.deckContextJson,
                    slider.DeckContext::class.java,
                ),
                ragContext = state.ragContext,
            ),
        )
    }
}