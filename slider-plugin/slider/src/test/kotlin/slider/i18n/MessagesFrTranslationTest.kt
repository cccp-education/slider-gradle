package slider.i18n

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertNotEquals

class MessagesFrTranslationTest {

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
        "task.generateCapsule.description",
        "task.generateCapsule.skipped",
        "task.generateCapsule.written",
        "task.translateAndGenerateCapsule.description",
        "task.translateDeck.description",
        "task.translateDeck.translating",
        "task.translateDeck.provider",
        "task.translateDeck.targets",
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
    fun `french translation differs from english for key`(key: String) {
        val en = SliderMessages.get(key, "en")
        val fr = SliderMessages.get(key, "fr")
        assertNotEquals(en, fr, "Key '$key' has identical EN and FR translations")
    }
}
