package slider.scenarios

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.i18n.RevealRtlResolver

class RtlLayoutSteps : En {

    private var deckLanguage: String = ""
    private var rtlEnabled: Boolean = false
    private var rtlLanguages: Set<String> = emptySet()

    init {

        When("the deck language is {string}") { language: String ->
            deckLanguage = language
            rtlEnabled = RevealRtlResolver.resolveRtl(deckLanguage)
        }

        Then("the RTL layout should be enabled") {
            assertThat(rtlEnabled)
                .withFailMessage("RTL layout should be enabled for '$deckLanguage'")
                .isTrue()
        }

        Then("the RTL layout should be disabled") {
            assertThat(rtlEnabled)
                .withFailMessage("RTL layout should be disabled for '$deckLanguage'")
                .isFalse()
        }

        When("the RTL languages are listed") {
            rtlLanguages = RevealRtlResolver.rtlLanguages()
        }

        Then("they should be exactly {string} and {string}") { first: String, second: String ->
            assertThat(rtlLanguages).containsExactlyInAnyOrder(first, second)
        }
    }
}