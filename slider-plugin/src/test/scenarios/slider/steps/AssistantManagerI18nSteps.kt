package slider.steps

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.i18n.SliderMessages

class AssistantManagerI18nSteps : En {

    private var resolvedMessage: String = ""

    init {

        When("the i18n message key {string} is resolved in English") { key: String ->
            resolvedMessage = SliderMessages.get(key, "en")
        }

        When("the i18n message key {string} is resolved in French") { key: String ->
            resolvedMessage = SliderMessages.get(key, "fr")
        }

        When("the i18n message key {string} is formatted in English with model {string}") { key: String, model: String ->
            resolvedMessage = SliderMessages.format(key, "en", model)
        }

        When("the i18n message key {string} is formatted in French with model {string}") { key: String, model: String ->
            resolvedMessage = SliderMessages.format(key, "fr", model)
        }

        Then("the message should contain {string}") { expected: String ->
            assertThat(resolvedMessage).contains(expected)
        }

        Then("the message should be {string}") { expected: String ->
            assertThat(resolvedMessage).isEqualTo(expected)
        }
    }
}
