package slider.playwright

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PlaywrightDomainTest {

    // ---------------------------------------------------------------------------
    // PlaywrightDir
    // ---------------------------------------------------------------------------

    @Test
    fun `a playwright dir resolves project_src_test_playwright`(@TempDir projectDir: File) {
        val dir = PlaywrightDir(projectDir)
        assertEquals(File(projectDir, "src/test/playwright"), dir.asFile())
    }

    @Test
    fun `a playwright dir rejects a blank project dir`() {
        assertFailsWith<IllegalArgumentException> { PlaywrightDir(File("")) }
    }

    @Test
    fun `a playwright dir config path resolves playwright_config_ts`(@TempDir projectDir: File) {
        val dir = PlaywrightDir(projectDir)
        assertEquals(
            File(projectDir, "src/test/playwright/playwright.config.ts").absolutePath,
            dir.configPath(),
        )
    }

    // ---------------------------------------------------------------------------
    // ServeCommand
    // ---------------------------------------------------------------------------

    @Test
    fun `a serve command keeps its package name and target dir`() {
        val cmd = ServeCommand(
            packageName = "@bundle-serve@0.1.0",
            servedDir = File("/tmp/proj/build/docs/asciidocRevealJs"),
        )
        assertEquals("@bundle-serve@0.1.0", cmd.packageName)
        assertEquals(File("/tmp/proj/build/docs/asciidocRevealJs"), cmd.servedDir)
    }

    @Test
    fun `a serve command rejects a blank package name`() {
        assertFailsWith<IllegalArgumentException> {
            ServeCommand(packageName = "", servedDir = File("/tmp/out"))
        }
    }

    @Test
    fun `a serve command rejects a blank served dir`() {
        assertFailsWith<IllegalArgumentException> {
            ServeCommand(packageName = "serve", servedDir = File(""))
        }
    }

    @Test
    fun `a serve command renders its npx args as a single-element list`() {
        val cmd = ServeCommand("@serve@1.0", File("/tmp/out"))
        assertEquals(listOf("/tmp/out"), cmd.npxArgs())
    }

    // ---------------------------------------------------------------------------
    // InstallCommand
    // ---------------------------------------------------------------------------

    @Test
    fun `the default install command targets the chromium browser`() {
        val cmd = InstallCommand.DEFAULT
        assertEquals("playwright", cmd.binary)
        assertEquals(listOf("install", "chromium"), cmd.npxArgs())
    }

    @Test
    fun `a custom install command keeps its binary and browser`() {
        val cmd = InstallCommand(binary = "playwright", browsers = listOf("firefox", "webkit"))
        assertEquals(listOf("install", "firefox", "webkit"), cmd.npxArgs())
    }

    @Test
    fun `an install command rejects a blank binary`() {
        assertFailsWith<IllegalArgumentException> {
            InstallCommand(binary = "", browsers = listOf("chromium"))
        }
    }

    @Test
    fun `an install command rejects an empty browser list`() {
        assertFailsWith<IllegalArgumentException> {
            InstallCommand(binary = "playwright", browsers = emptyList())
        }
    }

    // ---------------------------------------------------------------------------
    // VisualTestCommand
    // ---------------------------------------------------------------------------

    @Test
    fun `a visual test command keeps its binary and config path`() {
        val cmd = VisualTestCommand(
            binary = "playwright",
            configPath = "/tmp/proj/src/test/playwright/playwright.config.ts",
        )
        assertEquals("playwright", cmd.binary)
        assertEquals(
            "/tmp/proj/src/test/playwright/playwright.config.ts",
            cmd.configPath,
        )
    }

    @Test
    fun `a visual test command rejects a blank binary`() {
        assertFailsWith<IllegalArgumentException> {
            VisualTestCommand(binary = "", configPath = "/tmp/cfg")
        }
    }

    @Test
    fun `a visual test command rejects a blank config path`() {
        assertFailsWith<IllegalArgumentException> {
            VisualTestCommand(binary = "playwright", configPath = "")
        }
    }

    @Test
    fun `a visual test command renders its npx args as test --config path`() {
        val cmd = VisualTestCommand("playwright", "/tmp/cfg.ts")
        assertEquals(listOf("test", "--config", "/tmp/cfg.ts"), cmd.npxArgs())
    }

    // ---------------------------------------------------------------------------
    // Task names
    // ---------------------------------------------------------------------------

    @Test
    fun `the task names are stable`() {
        assertEquals("serveSlides", PlaywrightTaskNames.SERVE_SLIDES)
        assertEquals("visualTest", PlaywrightTaskNames.VISUAL_TEST)
        assertEquals("installPlaywright", PlaywrightTaskNames.INSTALL_PLAYWRIGHT)
    }
}