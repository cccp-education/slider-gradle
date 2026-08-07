package slider

import slider.config.SlidesConfigLoader
import slider.config.YamlMapperFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.Project
import java.io.File

object SliderConfig {
    const val CONFIG_PATH_KEY = "managed_config_path"

    val Project.localConf: SlidesConfiguration
        get() = SlidesConfigLoader.load(
            configPath = findProperty(CONFIG_PATH_KEY)?.toString().orEmpty(),
            baseDir = rootDir.absolutePath,
            mapper = yamlMapper,
        )

    val yamlMapper: ObjectMapper
        get() = YamlMapperFactory.create()

    fun readSlidesConfigurationFile(
        configPath: () -> String
    ): SlidesConfiguration = SlidesConfigLoader.load(
        configFile = File(configPath()),
        mapper = yamlMapper,
    )
}
