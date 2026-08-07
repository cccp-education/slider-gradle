package slider.config

import arrow.integrations.jackson.module.registerArrowModule
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/**
 * Factory for the shared Jackson `ObjectMapper` used to serialise and
 * deserialise the slider YAML configuration files (slides-context.yml,
 * deck-context.yml).
 *
 * Configured for YAML, Kotlin and Arrow support — the single source of truth
 * for slider YAML mapping. Replaces the former `SliderManager.Configuration
 * .yamlMapper` extension property.
 */
object YamlMapperFactory {

    /**
     * Builds a fresh `ObjectMapper` configured for YAML, Kotlin and Arrow.
     *
     * The mapper is built each call rather than cached as a shared singleton
     * because Jackson `ObjectMapper` is thread-safe after configuration and
     * callers tend to hold on to the instance for the lifetime of the build.
     * The factory centralises the configuration so every call site uses the
     * same serialisation policy.
     */
    fun create(): ObjectMapper = YAMLFactory()
        .let(::ObjectMapper)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerKotlinModule()
        .registerArrowModule()
}