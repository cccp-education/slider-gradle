package slider

import slider.ai.AssistantManager.createChatTasks
import slider.extension.SliderExtensionRegistrar
import slider.prerequisite.SliderPrerequisiteRegistrar.checkJavaVersion
import slider.scaffold.SliderScaffoldRegistrar.scaffoldDeckContextIfAbsent
import slider.scaffold.SliderScaffoldRegistrar.scaffoldSlidesContextIfAbsent
import slider.scaffold.SliderScaffoldRegistrar.scaffoldSlidesIfAbsent
import slider.wiring.SliderWiringRegistrar.applyPlugins
import slider.wiring.SliderWiringRegistrar.configureDependencies
import slider.wiring.SliderWiringRegistrar.configureRepositories
import org.gradle.api.Plugin
import org.gradle.api.Project

class SliderPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            checkJavaVersion()
            scaffoldSlidesIfAbsent(this)
            scaffoldSlidesContextIfAbsent(this)
            scaffoldDeckContextIfAbsent(this)
            configureRepositories(this)
            applyPlugins(this)
            configureDependencies(this)
            SliderExtensionRegistrar.configure(this)
            SliderTasks.registerTasks(this)
            afterEvaluate { createChatTasks() }
        }
    }

    typealias SliderExtension = slider.extension.SliderExtension
}