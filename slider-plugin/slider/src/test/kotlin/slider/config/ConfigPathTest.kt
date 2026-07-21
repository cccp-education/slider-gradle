package slider.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import java.io.File
import java.nio.file.Files

class ConfigPathTest {

    @Test
    fun `rejects a blank path`() {
        assertFailsWith<IllegalArgumentException> { ConfigPath("") }
        assertFailsWith<IllegalArgumentException> { ConfigPath("   ") }
    }

    @Test
    fun `accepts a non-blank path`() {
        assertEquals("managed_config_path", ConfigPath("managed_config_path").value)
    }

    @Test
    fun `resolveAgainst returns the path unchanged when already absolute under base dir`() {
        val base = Files.createTempDirectory("base").toFile().absolutePath
        val abs = "$base/slides-context.yml"
        assertEquals(abs, ConfigPath(abs).resolveAgainst(base))
    }

    @Test
    fun `resolveAgainst concatenates base dir and path when relative`() {
        val base = Files.createTempDirectory("base").toFile().absolutePath
        val sep = File.separator
        assertEquals("$base${sep}slides-context.yml", ConfigPath("slides-context.yml").resolveAgainst(base))
    }
}