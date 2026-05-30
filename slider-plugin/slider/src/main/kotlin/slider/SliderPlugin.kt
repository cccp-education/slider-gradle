package slider

import slider.SliderManager.Dependencies.configureDependencies
import slider.SliderManager.Extensions.configureExtensions
import slider.SliderManager.Plugins.applyPlugins
import slider.SliderManager.Prerequisites.checkJavaVersion
import slider.SliderManager.Repositories.configureRepositories
import slider.SliderManager.Scaffold.scaffoldDeckContextIfAbsent
import slider.SliderManager.Scaffold.scaffoldSlidesContextIfAbsent
import slider.SliderManager.Scaffold.scaffoldSlidesIfAbsent
import slider.SliderManager.Tasks.registerTasks
import slider.ai.AssistantManager.createChatTasks
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject


/**
 * Main entry point for the Slider Gradle plugin.
 *
 * Orchestrates the full plugin setup by delegating each responsibility
 * to a focused nested object inside [SliderManager]:
 * - [SliderManager.Prerequisites] — Java version guard
 * - [SliderManager.Scaffold]      — first-use scaffolding
 * - [SliderManager.Repositories]  — Maven/Ivy repository configuration
 * - [SliderManager.Plugins]       — external plugin application
 * - [SliderManager.Dependencies]  — Ruby gem dependency declaration
 * - [SliderManager.Extensions]    — DSL extension + RevealJS configuration
 * - [SliderManager.Tasks]         — task registration
 *
 * All business logic lives in [SliderManager]. This class is intentionally
 * kept as a thin orchestrator so it remains readable and easy to test.
 */
class SliderPlugin : Plugin<Project> {

    /** Applies the plugin by delegating each setup phase to [SliderManager]. */
    override fun apply(project: Project) {
        with(project) {
            checkJavaVersion()
            scaffoldSlidesIfAbsent()
            scaffoldSlidesContextIfAbsent()
            scaffoldDeckContextIfAbsent()
            configureRepositories()
            applyPlugins()
            configureDependencies()
            configureExtensions()
            registerTasks()
            // AI applied after extension has been created and configured
            // Creating tasks for each model
            afterEvaluate { createChatTasks() }
        }
    }

    /**
     * DSL extension for the slider plugin.
     *
     * Usage in build.gradle.kts:
     * ```
     * slider {
     *     configPath = file("slides-context.yml").absolutePath
     * }
     * ```
     */
    open class SliderExtension @Inject constructor(objects: ObjectFactory) {
        @Suppress("unused")
        val configPath: Property<String> = objects.property(String::class.java)
    }
}