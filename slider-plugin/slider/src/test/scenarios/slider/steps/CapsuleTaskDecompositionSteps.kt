package slider.steps

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.capsule.CapsuleAdocDir
import slider.capsule.CapsuleScriptDir
import slider.capsule.CapsuleTaskNames
import java.io.File
import java.nio.file.Files

class CapsuleTaskDecompositionSteps : En {

    private var adocDir: CapsuleAdocDir? = null
    private var adocDirError: Throwable? = null
    private var adocFiles: List<File> = emptyList()

    private var scriptDir: CapsuleScriptDir? = null
    private var scriptDirError: Throwable? = null
    private var ensuredScriptDir: File? = null

    init {

        // -------------------------------------------------------------------------
        // CapsuleAdocDir
        // -------------------------------------------------------------------------

        When("a capsule adoc dir is built from project dir {string}") { projectDir: String ->
            adocDir = CapsuleAdocDir(File(projectDir))
            adocDirError = null
        }

        When("a capsule adoc dir is built with a blank project dir") {
            try {
                CapsuleAdocDir(File(""))
            } catch (e: Throwable) {
                adocDirError = e
            }
        }

        When("a capsule adoc dir is built with adoc files {string} and {string} plus a {string}") {
            adoc1: String, adoc2: String, ignored: String ->
            val projectDir = Files.createTempDirectory("slider-capsule-adoc").toFile()
            val miscDir = File(projectDir, "slides/misc").apply { mkdirs() }
            File(miscDir, adoc1).writeText("= A")
            File(miscDir, adoc2).writeText("= B")
            File(miscDir, ignored).writeText("skip")
            adocDir = CapsuleAdocDir(projectDir)
            adocDirError = null
        }

        When("a capsule adoc dir is built from a project dir without a slides_misc directory") {
            val projectDir = Files.createTempDirectory("slider-capsule-no-misc").toFile()
            adocDir = CapsuleAdocDir(projectDir)
            adocDirError = null
        }

        Then("the capsule adoc dir path should be {string}") { expected: String ->
            assertThat(adocDir?.asFile()?.path).isEqualTo(expected)
        }

        Then("the capsule adoc dir construction should fail with a validation error") {
            assertThat(adocDirError)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        Then("the capsule adoc dir should list files {string}") { expected: String ->
            val expectedList = expected.split(",").filter { it.isNotBlank() }
            adocFiles = adocDir?.adocFiles().orEmpty()
            assertThat(adocFiles.map { it.name }).isEqualTo(expectedList)
        }

        Then("the capsule adoc dir should list no files") {
            adocFiles = adocDir?.adocFiles().orEmpty()
            assertThat(adocFiles).isEmpty()
        }

        // -------------------------------------------------------------------------
        // CapsuleScriptDir
        // -------------------------------------------------------------------------

        When("a capsule script dir is built from build dir {string}") { buildDir: String ->
            scriptDir = CapsuleScriptDir(File(buildDir))
            scriptDirError = null
        }

        When("a capsule script dir is built with a blank build dir") {
            try {
                CapsuleScriptDir(File(""))
            } catch (e: Throwable) {
                scriptDirError = e
            }
        }

        When("a capsule script dir is ensured created from a fresh build dir") {
            val freshBuildDir = Files.createTempDirectory("slider-capsule-script").toFile()
            scriptDir = CapsuleScriptDir(freshBuildDir)
            ensuredScriptDir = scriptDir?.ensureCreated()
            scriptDirError = null
        }

        Then("the capsule script dir path should be {string}") { expected: String ->
            assertThat(scriptDir?.asFile()?.path).isEqualTo(expected)
        }

        Then("the capsule script dir construction should fail with a validation error") {
            assertThat(scriptDirError)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        Then("the capsule script directory should exist") {
            assertThat(ensuredScriptDir).isNotNull()
            assertThat(ensuredScriptDir?.isDirectory).isTrue()
        }

        Then("the capsule script file for deck {string} should be {string}") {
            deckName: String, expected: String ->
            assertThat(scriptDir?.scriptFileFor(deckName)?.path).isEqualTo(expected)
        }

        // -------------------------------------------------------------------------
        // CapsuleTaskNames
        // -------------------------------------------------------------------------

        When("the capsule task names are read") {
            // No-op — constants read in the Then steps
        }

        Then("the generate capsule task name should be {string}") { expected: String ->
            assertThat(CapsuleTaskNames.GENERATE_CAPSULE).isEqualTo(expected)
        }

        Then("the capsule task group should be {string}") { expected: String ->
            assertThat(CapsuleTaskNames.GROUP).isEqualTo(expected)
        }

        Then("the capsule task description should mention {string}") { expected: String ->
            assertThat(CapsuleTaskNames.DESCRIPTION).contains(expected)
        }
    }
}