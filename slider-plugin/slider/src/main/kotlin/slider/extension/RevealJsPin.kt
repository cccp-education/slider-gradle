package slider.extension

/**
 * Value object describing the Reveal.js dependency pin applied by the slider
 * plugin.
 *
 * The plugin pins both the asciidoctor-revealjs gem version (used at build
 * time) and the reveal.js HTML template tag (fetched from GitHub at packaging
 * time). Both come from the `org.asciidoctor.gradle.jvm.slides.RevealJSExtension`
 * configuration but, as a domain value, this object is pure — it carries no
 * Gradle types and can be unit-tested in isolation.
 *
 * Defaults match the pin in force since SLD-1 (reveal.js 5.2.1).
 */
data class RevealJsPin(
    val version: String = DEFAULT_VERSION,
    val organisation: String = DEFAULT_ORGANISATION,
    val repository: String = DEFAULT_REPOSITORY,
    val tag: String = DEFAULT_TAG,
) {

    init {
        require(version.isNotBlank()) { "RevealJsPin.version must not be blank" }
        require(organisation.isNotBlank()) { "RevealJsPin.organisation must not be blank" }
        require(repository.isNotBlank()) { "RevealJsPin.repository must not be blank" }
        require(tag.isNotBlank()) { "RevealJsPin.tag must not be blank" }
    }

    private companion object {
        const val DEFAULT_VERSION: String = "5.2.0"
        const val DEFAULT_ORGANISATION: String = "hakimel"
        const val DEFAULT_REPOSITORY: String = "reveal.js"
        const val DEFAULT_TAG: String = "5.2.1"
    }
}