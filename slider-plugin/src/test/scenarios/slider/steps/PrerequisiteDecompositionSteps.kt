package slider.steps

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import slider.prerequisite.JavaVersionGuard

class PrerequisiteDecompositionSteps : En {

    private var guardError: Throwable? = null
    private var guardPassed: Boolean = false

    init {

        When("the guard is checked against major version {int}") { major: Int ->
            try {
                JavaVersionGuard.requireJava23(major)
                guardPassed = true
                guardError = null
            } catch (e: Throwable) {
                guardPassed = false
                guardError = e
            }
        }

        When("the guard is checked against major version string {string}") { majorString: String ->
            try {
                JavaVersionGuard.requireJava23FromMajor(majorString)
                guardPassed = true
                guardError = null
            } catch (e: Throwable) {
                guardPassed = false
                guardError = e
            }
        }

        Then("the guard should pass") {
            assertThat(guardError).isNull()
            assertThat(guardPassed).isTrue()
        }

        Then("the guard should fail with message {string}") { expected: String ->
            assertThat(guardPassed).isFalse()
            assertThat(guardError).isNotNull()
            assertThat(guardError!!.message).isEqualTo(expected)
        }

        Then("the guard should fail with a version parse error") {
            assertThat(guardPassed).isFalse()
            assertThat(guardError).isInstanceOf(IllegalArgumentException::class.java)
        }
    }
}