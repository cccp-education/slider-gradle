package slider.i18n

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertNotEquals

class MessagesZhTranslationTest {

    @ParameterizedTest
    @ValueSource(strings = [
        "task.cleanBuild.description",
        "task.cleanBuild.cleaned",
        "task.asciidoctorRevealJs.description",
        "task.generateDashboard.description",
        "task.generateDashboard.success",
        "task.generateDashboard.generated",
        "task.generateDashboard.presentations",
        "task.serveSlides.description",
        "task.serveSlides.serving",
        "task.installPlaywright.description",
        "task.visualTest.description",
        "task.deploySlides.description",
        "task.translateDeck.description",
        "task.translateDeck.translating",
        "task.translateDeck.provider",
        "task.translateDeck.targets",
        "task.translateDeck.noTargets",
        "task.generateRevealUiMessages.description",
        "task.generateRevealUiMessages.written",
        "task.reportTests.description",
        "task.reportFunctionalTests.description",
        "task.asciidoctor.description",
        "translate.error.deckContextNotFound",
        "translate.error.adocNotFound",
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
    fun `chinese translation differs from english for key`(key: String) {
        val en = SliderMessages.get(key, "en")
        val zh = SliderMessages.get(key, "zh")
        assertNotEquals(en, zh, "Key '$key' should be translated to Chinese, not a copy of English")
    }
}