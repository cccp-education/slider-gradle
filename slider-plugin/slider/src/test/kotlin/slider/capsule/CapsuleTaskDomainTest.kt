package slider.capsule

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CapsuleTaskDomainTest {

    // ---------------------------------------------------------------------------
    // CapsuleAdocDir — resolves <projectDir>/slides/misc
    // ---------------------------------------------------------------------------

    @Test
    fun `a capsule adoc dir resolves project_slides_misc`(@TempDir projectDir: File) {
        val dir = CapsuleAdocDir(projectDir)
        assertEquals(File(projectDir, "slides/misc"), dir.asFile())
    }

    @Test
    fun `a capsule adoc dir rejects a blank project dir`() {
        assertFailsWith<IllegalArgumentException> { CapsuleAdocDir(File("")) }
    }

    @Test
    fun `a capsule adoc dir lists only adoc files sorted by name`(@TempDir projectDir: File) {
        val miscDir = File(projectDir, "slides/misc").apply { mkdirs() }
        File(miscDir, "deck-b.adoc").writeText("= B")
        File(miscDir, "deck-a.adoc").writeText("= A")
        File(miscDir, "ignored.txt").writeText("skip")

        val adocFiles = CapsuleAdocDir(projectDir).adocFiles()

        assertEquals(listOf("deck-a.adoc", "deck-b.adoc"), adocFiles.map { it.name })
    }

    @Test
    fun `a capsule adoc dir returns empty list when misc is missing`(@TempDir projectDir: File) {
        assertTrue(CapsuleAdocDir(projectDir).adocFiles().isEmpty())
    }

    // ---------------------------------------------------------------------------
    // CapsuleScriptDir — resolves <buildDir>/capsule
    // ---------------------------------------------------------------------------

    @Test
    fun `a capsule script dir resolves build_capsule`(@TempDir buildDir: File) {
        val dir = CapsuleScriptDir(buildDir)
        assertEquals(File(buildDir, "capsule"), dir.asFile())
    }

    @Test
    fun `a capsule script dir rejects a blank build dir`() {
        assertFailsWith<IllegalArgumentException> { CapsuleScriptDir(File("")) }
    }

    @Test
    fun `a capsule script dir ensureCreated creates the directory`(@TempDir buildDir: File) {
        val dir = CapsuleScriptDir(buildDir)
        assertFalse(File(buildDir, "capsule").exists())

        dir.ensureCreated()

        assertTrue(File(buildDir, "capsule").isDirectory())
    }

    @Test
    fun `a capsule script dir ensureCreated is idempotent`(@TempDir buildDir: File) {
        val dir = CapsuleScriptDir(buildDir)
        dir.ensureCreated()
        // Second call must not throw
        dir.ensureCreated()
        assertTrue(File(buildDir, "capsule").isDirectory())
    }

    @Test
    fun `a capsule script dir scriptFile resolves name_without_extension-script_txt`(@TempDir buildDir: File) {
        val dir = CapsuleScriptDir(buildDir)
        val scriptFile = dir.scriptFileFor("kotlin-intro")
        assertEquals(File(buildDir, "capsule/kotlin-intro-script.txt"), scriptFile)
    }

    // ---------------------------------------------------------------------------
    // CapsuleTaskNames — stable task identifiers
    // ---------------------------------------------------------------------------

    @Test
    fun `the capsule generate task name is generateCapsule`() {
        assertEquals("generateCapsule", CapsuleTaskNames.GENERATE_CAPSULE)
    }

    @Test
    fun `the capsule task group is slider`() {
        assertEquals("slider", CapsuleTaskNames.GROUP)
    }

    @Test
    fun `the capsule task description mentions speaker notes and capsule-gradle`() {
        assertTrue(CapsuleTaskNames.DESCRIPTION.contains("speaker notes"))
        assertTrue(CapsuleTaskNames.DESCRIPTION.contains("capsule-gradle"))
    }
}