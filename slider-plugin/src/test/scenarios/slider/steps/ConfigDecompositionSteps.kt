package slider.steps

import com.fasterxml.jackson.databind.SerializationFeature
import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.SlidesConfiguration
import slider.config.ConfigPath
import slider.config.SlidesConfigLoader
import slider.config.YamlMapperFactory
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFailsWith

class ConfigDecompositionSteps : En {

    private lateinit var baseDir: File
    private var configPathString: String? = null
    private var resolvedPath: String? = null
    private var loadedConfig: SlidesConfiguration? = null
    private var constructionError: Throwable? = null
    private var yamlContent: String? = null
    private var parsedName: String? = null
    private var parsedVersion: String? = null
    private var parsedPersonName: String? = null
    private var parsedPersonAge: Int? = null
    private var writeDatesAsTimetampsEnabled: Boolean? = null

    private fun newTempDir(prefix: String): File =
        Files.createTempDirectory(prefix).toFile()

    init {

        // ---- ConfigPath ----------------------------------------------------------------

        Given("a blank config path string") {
            configPathString = "   "
        }

        Given("the config path string {string}") { path: String ->
            configPathString = path
        }

        When("the ConfigPath is constructed") {
            try {
                resolvedPath = ConfigPath(requireNotNull(configPathString)).value
            } catch (e: Throwable) {
                constructionError = e
            }
        }

        Then("the construction should fail with a validation error") {
            assertThat(constructionError).isInstanceOf(IllegalArgumentException::class.java)
        }

        Then("the ConfigPath value should be {string}") { expected: String ->
            assertThat(resolvedPath).isEqualTo(expected)
        }

        Given("the base directory {string}") { base: String ->
            baseDir = File(base)
            baseDir.mkdirs()
        }

        When("the path is resolved against the base directory") {
            val path = requireNotNull(configPathString)
            val base = baseDir.absolutePath
            resolvedPath = ConfigPath(path).resolveAgainst(base)
        }

        Then("the resolved path should end with {string}") { expected: String ->
            assertThat(resolvedPath).endsWith(expected)
        }

        Then("the resolved path should be {string}") { expected: String ->
            assertThat(resolvedPath).isEqualTo(expected)
        }

        // ---- SlidesConfigLoader --------------------------------------------------------

        Given("a slides-context.yml file with a valid configuration") {
            baseDir = newTempDir("slider-config-valid")
            File(baseDir, "slides-context.yml").writeText(
                """
                srcPath: "docs/asciidocRevealJs"
                pushSlides:
                  from: "build/docs/asciidocRevealJs"
                  to: "build/slides-repo"
                  repo:
                    name: "slides"
                    repository: "https://github.com/org/repo.git"
                    credentials:
                      username: "user"
                      password: "token"
                  branch: "main"
                  message: "deploy slides"
                """.trimIndent()
            )
        }

        Given("no slides-context.yml file exists") {
            baseDir = newTempDir("slider-config-missing")
        }

        Given("a slides-context.yml file with malformed YAML content") {
            baseDir = newTempDir("slider-config-malformed")
            File(baseDir, "slides-context.yml").writeText(":::not yaml:::")
        }

        When("the configuration is loaded") {
            val configFile = File(baseDir, "slides-context.yml")
            loadedConfig = SlidesConfigLoader.load(configFile, YamlMapperFactory.create())
        }

        Then("the config source path should be {string}") { expected: String ->
            assertThat(loadedConfig?.srcPath).isEqualTo(expected)
        }

        Then("the config push to should be {string}") { expected: String ->
            assertThat(loadedConfig?.pushSlides?.to).isEqualTo(expected)
        }

        Then("the config repo name should be {string}") { expected: String ->
            assertThat(loadedConfig?.pushSlides?.repo?.name).isEqualTo(expected)
        }

        Then("the fallback configuration should be returned") {
            val empty = SlidesConfigLoader.emptyConfiguration()
            assertThat(loadedConfig?.srcPath).isEqualTo(empty.srcPath)
            assertThat(loadedConfig?.pushSlides?.repo?.name).isEqualTo(empty.pushSlides?.repo?.name)
        }

        Then("the config source path should be empty") {
            assertThat(loadedConfig?.srcPath).isEmpty()
        }

        Given("a slides-context.yml file in the base directory with srcPath {string}") { srcPath: String ->
            baseDir = newTempDir("slider-config-by-path")
            File(baseDir, "slides-context.yml").writeText(
                """
                srcPath: "$srcPath"
                """.trimIndent()
            )
        }

        When("the configuration is loaded by path and base dir") {
            loadedConfig = SlidesConfigLoader.load(
                configPath = "slides-context.yml",
                baseDir = baseDir.absolutePath,
                mapper = YamlMapperFactory.create(),
            )
        }

        // ---- YamlMapperFactory ---------------------------------------------------------

        Given("a YAML mapping with name {string} and version {string}") { name: String, version: String ->
            yamlContent = "name: $name\nversion: $version\n"
        }

        When("the mapping is read by the YAML mapper") {
            val mapper = YamlMapperFactory.create()
            val mapType = mapper.typeFactory
                .constructMapType(Map::class.java, String::class.java, String::class.java)
            @Suppress("UNCHECKED_CAST")
            val parsed = mapper.readValue(requireNotNull(yamlContent), mapType) as Map<String, String>
            parsedName = parsed["name"]
            parsedVersion = parsed["version"]
        }

        Then("the parsed name should be {string}") { expected: String ->
            assertThat(parsedName).isEqualTo(expected)
        }

        Then("the parsed version should be {string}") { expected: String ->
            assertThat(parsedVersion).isEqualTo(expected)
        }

        Given("a YAML mapping with name {string} and age {int}") { name: String, age: Int ->
            yamlContent = "name: $name\nage: $age\n"
        }

        When("the mapping is read by the YAML mapper as a Person") {
            val mapper = YamlMapperFactory.create()
            val person = mapper.readValue(requireNotNull(yamlContent), Person::class.java)
            parsedPersonName = person.name
            parsedPersonAge = person.age
        }

        Then("the person name should be {string}") { expected: String ->
            assertThat(parsedPersonName).isEqualTo(expected)
        }

        Then("the person age should be {int}") { expected: Int ->
            assertThat(parsedPersonAge).isEqualTo(expected)
        }

        When("the YAML mapper is created") {
            val mapper = YamlMapperFactory.create()
            writeDatesAsTimetampsEnabled = mapper.serializationConfig
                .isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        Then("WRITE_DATES_AS_TIMESTAMPS should be disabled") {
            assertThat(writeDatesAsTimetampsEnabled).isFalse()
        }
    }

    data class Person(val name: String = "", val age: Int = 0)
}