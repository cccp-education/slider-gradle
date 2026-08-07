package slider.steps

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.extension.RevealJsPin

class ExtensionDecompositionSteps : En {

    private var pin: RevealJsPin? = null
    private var constructionError: Throwable? = null

    init {

        When("the default Reveal.js pin is built") {
            pin = RevealJsPin()
            constructionError = null
        }

        When("a Reveal.js pin is built with a blank version") {
            try { RevealJsPin(version = "") } catch (e: Throwable) { constructionError = e }
        }

        When("a Reveal.js pin is built with a blank tag") {
            try { RevealJsPin(tag = "   ") } catch (e: Throwable) { constructionError = e }
        }

        When("a Reveal.js pin is built with a blank organisation") {
            try { RevealJsPin(organisation = "") } catch (e: Throwable) { constructionError = e }
        }

        When("a Reveal.js pin is built with a blank repository") {
            try { RevealJsPin(repository = "") } catch (e: Throwable) { constructionError = e }
        }

        When("a Reveal.js pin is built with version {string} organisation {string} repository {string} tag {string}") {
            version: String, organisation: String, repository: String, tag: String ->
            pin = RevealJsPin(
                version = version,
                organisation = organisation,
                repository = repository,
                tag = tag,
            )
            constructionError = null
        }

        Then("the pin tag should be {string}") { expected: String ->
            assertThat(pin?.tag).isEqualTo(expected)
        }

        Then("the pin organisation should be {string}") { expected: String ->
            assertThat(pin?.organisation).isEqualTo(expected)
        }

        Then("the pin repository should be {string}") { expected: String ->
            assertThat(pin?.repository).isEqualTo(expected)
        }

        Then("the pin version should be {string}") { expected: String ->
            assertThat(pin?.version).isEqualTo(expected)
        }

        Then("the pin construction should fail with a validation error") {
            assertThat(constructionError).isNotNull()
            assertThat(constructionError).isInstanceOf(IllegalArgumentException::class.java)
        }
    }
}