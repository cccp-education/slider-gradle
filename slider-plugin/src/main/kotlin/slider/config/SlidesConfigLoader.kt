package slider.config

import com.fasterxml.jackson.databind.ObjectMapper
import slider.AiConfiguration
import slider.GitPushConfiguration
import slider.RepositoryConfiguration
import slider.RepositoryCredentials
import slider.SlidesConfiguration
import java.io.File

/**
 * Loads the slides YAML configuration from disk and deserialises it into a
 * [SlidesConfiguration].
 *
 * Pure domain logic — the [ObjectMapper] is injected by the caller so this
 * loader is fully testable without Gradle. On any parsing failure it returns
 * an empty [SlidesConfiguration] to let the build continue with degraded
 * behaviour, matching the historical contract of
 * `SliderManager.Configuration.readSlidesConfigurationFile`.
 *
 * Replaces `SliderManager.Configuration.readSlidesConfigurationFile`.
 */
object SlidesConfigLoader {

    /**
     * Reads and deserialises the configuration file at [configFile] using the
     * supplied [mapper]. Returns an empty [SlidesConfiguration] on any error.
     */
    fun load(configFile: File, mapper: ObjectMapper): SlidesConfiguration = try {
        mapper.readValue(configFile, SlidesConfiguration::class.java)
    } catch (_: Exception) {
        emptyConfiguration()
    }

    /**
     * Reads and deserialises the configuration file located at [configPath]
     * resolved against [baseDir] using the supplied [mapper]. Returns an
     * empty [SlidesConfiguration] on any error.
     */
    fun load(configPath: String, baseDir: String, mapper: ObjectMapper): SlidesConfiguration =
        load(File(resolveConfigFile(configPath, baseDir)), mapper)

    /**
     * The fallback empty configuration used when the YAML cannot be read.
     *
     * Public so callers (and tests) can assert identity with the degraded
     * configuration rather than re-constructing an equivalent instance.
     */
    fun emptyConfiguration(): SlidesConfiguration = SlidesConfiguration(
        srcPath = "",
        pushSlides = GitPushConfiguration(
            from = "",
            to = "",
            repo = RepositoryConfiguration(
                name = "",
                repository = "",
                credentials = RepositoryCredentials(username = "", password = "")
            ),
            branch = "",
            message = ""
        ),
        ai = AiConfiguration()
    )

    private fun resolveConfigFile(configPath: String, baseDir: String): String =
        if (configPath.startsWith(baseDir)) configPath
        else "$baseDir${System.getProperty("file.separator")}$configPath"
}