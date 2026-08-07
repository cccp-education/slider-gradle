package slider.steps

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.i18n.SliderMessages
import java.util.MissingResourceException

class SliderMessagesResolutionSteps : En {

    private var resolvedMessage: String = ""
    private var resolutionError: Throwable? = null

    init {

        Given("a slider message resolver") { /* no-op, SliderMessages is a singleton */ }

        When("the i18n message key {string} is resolved in language {string}") { key: String, lang: String ->
            resolutionError = null
            try {
                resolvedMessage = SliderMessages.get(key, lang)
            } catch (e: MissingResourceException) {
                resolutionError = e
            }
        }

        When("the i18n message key {string} is formatted in language {string} with args {string}") { key: String, lang: String, argsRaw: String ->
            val args = argsRaw.split("|").map { it.trim() }.toTypedArray()
            resolvedMessage = SliderMessages.format(key, lang, *args)
        }

        Then("the resolved message should contain {string}") { expected: String ->
            assertThat(resolvedMessage).contains(expected)
        }

        Then("the message should not be blank") {
            assertThat(resolvedMessage).isNotBlank()
        }

        Then("the resolution should fail with a missing resource error") {
            assertThat(resolutionError)
                .describedAs("Expected a MissingResourceException")
                .isInstanceOf(MissingResourceException::class.java)
        }

        Then("the message should be the English fallback for {string}") { key: String ->
            val englishFallback = SliderMessages.get(key, "en")
            assertThat(resolvedMessage).isEqualTo(englishFallback)
        }
    }
}