package slider.i18n

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertTrue

class MessagesEnReferenceTest {

    @ParameterizedTest
    @ValueSource(strings = [
        "task.group.build",
        "task.group.generate",
        "task.group.info",
        "task.group.setup",
        "task.group.verify",
        "task.group.deploy",
        "task.group.slider",
        "task.group.translator",
        "task.group.collect",
        "task.group.slider-ai",
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
        "task.translateDeck.translated",
        "task.translateDeck.failed",
        "task.translateDeck.summary",
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
    fun `english reference key is non blank`(key: String) {
        val en = SliderMessages.get(key, "en")
        assertTrue(en.isNotBlank(), "Key '$key' should have a non-blank English reference value")
    }
}