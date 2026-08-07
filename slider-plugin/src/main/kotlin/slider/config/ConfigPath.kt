package slider.config

/**
 * Value object representing the path to the slides YAML configuration file.
 *
 * Wraps the raw string path declared in the slider DSL (via the
 * `managed_config_path` project property) and validates that it is usable.
 *
 * Pure value — no Gradle, no filesystem access.
 */
@JvmInline
value class ConfigPath(val value: String) {

    init {
        require(value.isNotBlank()) { "ConfigPath must not be blank" }
    }

    /** Resolves this path against the given base directory. */
    fun resolveAgainst(baseDir: String): String =
        if (value.startsWith(baseDir)) value else "$baseDir${System.getProperty("file.separator")}$value"
}