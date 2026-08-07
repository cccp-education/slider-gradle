package slider.steps

import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat

class PublishTaskDecompositionSteps(private val world: SliderWorld) {

    @Given("a Gradle project with the slider plugin applied")
    fun createSliderProject() {
        world.createGradleProject()
        assertThat(world.projectDir).exists()
    }

    @When("the project tasks are listed")
    fun listProjectTasks() = runBlocking {
        world.executeGradle("tasks", "--quiet")
    }

    @Then("the task {string} should be in the {string} group")
    fun taskShouldBeInGroup(taskName: String, group: String) = runBlocking {
        world.executeGradle("tasks", "--group", group, "--quiet")
        val output = world.buildResult?.output ?: ""
        assertThat(output)
            .describedAs("Task '$taskName' should be in group '$group'")
            .contains(taskName)
    }

    @Then("the task {string} should depend on {string}")
    fun taskShouldDependOn(taskName: String, dependency: String) = runBlocking {
        world.executeGradle("$taskName", "--dry-run", "--quiet")
        val output = world.buildResult?.output ?: ""
        assertThat(output)
            .describedAs("Task '$taskName' should depend on '$dependency'")
            .contains(dependency)
    }
}
