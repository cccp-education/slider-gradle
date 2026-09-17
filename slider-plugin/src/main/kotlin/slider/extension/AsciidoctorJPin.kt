package slider.extension

/**
 * Value object describing the AsciidoctorJ dependency pin applied by the
 * slider plugin.
 *
 * The plugin must pin the AsciidoctorJ version to the one carried by its own
 * runtime classpath. Without it, the `asciidoctorRevealJs` OUT_OF_PROCESS
 * worker resolves the asciidoctor-gradle-plugin default (`asciidoctorj:3.0.0`,
 * bundling `jruby-complete:9.4.8.0` with `jar-dependencies 0.4.1`) while the
 * plugin classpath brings a newer AsciidoctorJ (`asciidoctorj:3.0.1`,
 * `jruby-stdlib:9.4.14.0` with `jar-dependencies 0.5.4`). Both JRuby runtimes
 * land on the same worker classpath and JRuby activates `jar-dependencies`
 * 0.4.1 before failing to activate 0.5.4 — the gems preparation then aborts
 * (`Gem::LoadError`) and the gem jar stays empty (pre-existing S-023/S-034).
 *
 * Pinning the extension to a single AsciidoctorJ version keeps one JRuby
 * runtime on the worker, so `asciidoctorGemsPrepare` succeeds and
 * `asciidoctorRevealJs` finds `asciidoctor-revealjs`.
 *
 * As a domain value, this object is pure — it carries no Gradle types and can
 * be unit-tested in isolation.
 */
data class AsciidoctorJPin(
    val version: String = DEFAULT_VERSION,
) {

    init {
        require(version.isNotBlank()) { "AsciidoctorJPin.version must not be blank" }
    }

    private companion object {
        const val DEFAULT_VERSION: String = "3.0.1"
    }
}
