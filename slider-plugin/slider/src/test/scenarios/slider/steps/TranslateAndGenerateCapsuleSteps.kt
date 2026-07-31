package slider.steps

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.capsule.CapsuleTaskNames

class TranslateAndGenerateCapsuleSteps : En {

    private var taskName: String = ""
    private var taskDescription: String = ""
    private var taskGroup: String = ""

    init {

        When("the translateAndGenerateCapsule task name is read") {
            taskName = CapsuleTaskNames.TRANSLATE_AND_GENERATE_CAPSULE
        }

        When("the translateAndGenerateCapsule task description is read") {
            taskDescription = CapsuleTaskNames.TRANSLATE_AND_GENERATE_DESCRIPTION
        }

        When("the translateAndGenerateCapsule task group is read") {
            taskGroup = CapsuleTaskNames.GROUP
        }

        Then("the task name should be {string}") { expected: String ->
            assertThat(taskName).isEqualTo(expected)
        }

        Then("the description should mention {string}") { expected: String ->
            assertThat(taskDescription).contains(expected)
        }

        Then("the task group should be {string}") { expected: String ->
            assertThat(taskGroup).isEqualTo(expected)
        }
    }
}
