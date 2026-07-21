package slider.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SlidesConfigLoaderTest {

    @TempDir
    lateinit var projectDir: Path

    private val mapper get() = YamlMapperFactory.create()

    @Test
    fun `load returns a populated SlidesConfiguration for a valid YAML file`() {
        val configFile = File(projectDir.toFile(), "slides-context.yml").apply {
            writeText(
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
                ai:
                  gemini:
                    - "key1"
                """.trimIndent()
            )
        }

        val config = SlidesConfigLoader.load(configFile, mapper)

        assertEquals("docs/asciidocRevealJs", config.srcPath)
        assertEquals("build/slides-repo", config.pushSlides?.to)
        assertEquals("main", config.pushSlides?.branch)
        assertEquals("slides", config.pushSlides?.repo?.name)
        assertEquals("https://github.com/org/repo.git", config.pushSlides?.repo?.repository)
        assertEquals("user", config.pushSlides?.repo?.credentials?.username)
        assertEquals("token", config.pushSlides?.repo?.credentials?.password)
        assertTrue(config.ai?.gemini?.contains("key1") ?: false)
    }

    @Test
    fun `load returns an empty configuration when the file does not exist`() {
        val missing = File(projectDir.toFile(), "missing.yml")
        val config = SlidesConfigLoader.load(missing, mapper)

        assertIsEmptyFallback(config)
    }

    @Test
    fun `load returns an empty configuration when the YAML is malformed`() {
        val malformed = File(projectDir.toFile(), "bad.yml").apply {
            writeText(":::not yaml:::")
        }
        val config = SlidesConfigLoader.load(malformed, mapper)

        assertIsEmptyFallback(config)
    }

    @Test
    fun `load by path and base dir resolves a relative path and reads the file`() {
        File(projectDir.toFile(), "slides-context.yml").writeText(
            """
            srcPath: "out"
            """.trimIndent()
        )

        val config = SlidesConfigLoader.load("slides-context.yml", projectDir.toFile().absolutePath, mapper)

        assertEquals("out", config.srcPath)
    }

    @Test
    fun `emptyConfiguration returns an instance with blank placeholder values`() {
        val empty = SlidesConfigLoader.emptyConfiguration()

        assertEquals("", empty.srcPath)
        assertEquals("", empty.pushSlides?.from)
        assertEquals("", empty.pushSlides?.to)
        assertEquals("", empty.pushSlides?.branch)
        assertEquals("", empty.pushSlides?.message)
        assertEquals("", empty.pushSlides?.repo?.name)
        assertEquals("", empty.pushSlides?.repo?.repository)
        assertEquals("", empty.pushSlides?.repo?.credentials?.username)
        assertEquals("", empty.pushSlides?.repo?.credentials?.password)
        assertNotNull(empty.ai)
    }

    @Test
    fun `emptyConfiguration returns a fresh instance on each call`() {
        val a = SlidesConfigLoader.emptyConfiguration()
        val b = SlidesConfigLoader.emptyConfiguration()
        assertTrue(a !== b)
    }

    private fun assertIsEmptyFallback(config: slider.SlidesConfiguration) {
        assertEquals("", config.srcPath)
        assertEquals("", config.pushSlides?.to)
        assertEquals("", config.pushSlides?.repo?.name)
        assertNull(config.srcPath?.takeIf { it.isNotBlank() })
    }
}