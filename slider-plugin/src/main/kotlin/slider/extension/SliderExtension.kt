package slider.extension

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * DSL extension for the slider plugin.
 *
 * Usage in build.gradle.kts:
 * ```
 * slider {
 *     configPath = file("slides-context.yml").absolutePath
 * }
 * ```
 *
 * Lives in the `slider.extension` domain so the DSL contract is owned by the
 * domain rather than by the [slider.SliderPlugin] entry point. Gradle injects
 * the [ObjectFactory] via the `@Inject` constructor, as required for extensions
 * created through `project.extensions.create(...)`.
 */
open class SliderExtension @Inject constructor(objects: ObjectFactory) {

    @Suppress("unused")
    val configPath: Property<String> = objects.property(String::class.java)

    @Suppress("unused")
    val language: Property<String> = objects.property(String::class.java)

    @Suppress("unused")
    val supportedLanguages: Property<String> = objects.property(String::class.java)
}