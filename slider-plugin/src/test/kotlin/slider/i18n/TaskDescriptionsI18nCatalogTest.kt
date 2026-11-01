package slider.i18n

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * XB-FP-6 (S-322) — pure-domain replacement for the 20 TestKit invocations of
 * `TaskDescriptionsI18nFunctionalTest`.
 *
 * Task descriptions come from the `i18n/Messages_*.properties` bundles read by
 * [SliderMessages]; the functional test proved it by running `gradle tasks --all`
 * once per language (each run a full TestKit build). The catalog is pure, so the
 * exhaustive matrix (24 task keys × 10 languages) is asserted here, and the
 * functional suite keeps a single smoke build.
 */
class TaskDescriptionsI18nCatalogTest {
    private val taskKeys =
        listOf(
            "asciidoctor",
            "asciidoctorRevealJs",
            "cleanBuild",
            "deploySlides",
            "generateDashboard",
            "generateDeck",
            "generateDeckPipeline",
            "generateRevealUiMessages",
            "helloGemini",
            "helloGeminiStreaming",
            "helloHuggingFace",
            "helloHuggingFaceStreaming",
            "helloMistral",
            "helloMistralStreaming",
            "helloOllama",
            "helloOllamaStreaming",
            "installPlaywright",
            "proposeDeckContext",
            "reindexRag",
            "reportFunctionalTests",
            "reportTests",
            "serveSlides",
            "translateDeck",
            "visualTest",
        ).map { "task.$it.description" }

    @ParameterizedTest
    @ValueSource(strings = ["en", "fr", "zh", "hi", "es", "ar", "bn", "pt", "ru", "ur"])
    fun `every task has a non-blank localized description`(language: String) {
        taskKeys.forEach { key ->
            val description = SliderMessages.get(key, language)
            assertThat(description)
                .describedAs("%s must be non-blank and translated in '%s'", key, language)
                .isNotBlank()
                .isNotEqualTo(key)
        }
    }

    @Test
    fun `french descriptions are localized, not english fallbacks`() {
        taskKeys.forEach { key ->
            assertThat(SliderMessages.get(key, "fr"))
                .describedAs("%s must be translated in French, not the English fallback", key)
                .isNotEqualTo(SliderMessages.get(key, "en"))
        }
    }

    @Test
    fun `english descriptions match the published messages`() {
        assertThat(SliderMessages.get("task.cleanBuild.description", "en"))
            .isEqualTo("Delete generated presentation artifacts from the build directory.")
        assertThat(SliderMessages.get("task.asciidoctorRevealJs.description", "en"))
            .isEqualTo("Compile AsciiDoc sources into a Reveal.js HTML presentation.")
        assertThat(SliderMessages.get("task.serveSlides.description", "en"))
            .startsWith("Serve slides using the serve package")
        assertThat(SliderMessages.get("task.visualTest.description", "en"))
            .startsWith("Run Playwright visual snapshot tests")
        assertThat(SliderMessages.get("task.translateDeck.description", "en"))
            .startsWith("Translate a deck")
    }

    @Test
    fun `french descriptions match the published messages`() {
        assertThat(SliderMessages.get("task.cleanBuild.description", "fr"))
            .startsWith("Supprime les artefacts de présentation")
        assertThat(SliderMessages.get("task.asciidoctorRevealJs.description", "fr"))
            .startsWith("Compile les sources AsciiDoc en présentation HTML Reveal.js")
        assertThat(SliderMessages.get("task.serveSlides.description", "fr"))
            .startsWith("Sert les diapositives")
        assertThat(SliderMessages.get("task.visualTest.description", "fr"))
            .startsWith("Exécute les tests de snapshot visuel Playwright")
        assertThat(SliderMessages.get("task.translateDeck.description", "fr"))
            .startsWith("Traduit un deck")
    }
}
