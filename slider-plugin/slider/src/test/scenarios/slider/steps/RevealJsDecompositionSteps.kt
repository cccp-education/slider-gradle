package slider.steps

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.revealjs.CleanBuildTarget
import slider.revealjs.DashboardJsonSerializer
import slider.revealjs.RevealJsAttributesSpec
import slider.revealjs.RevealJsAttributeKeys
import slider.revealjs.RevealJsOutputDir
import slider.revealjs.SlideMetadata
import slider.revealjs.SlideMetadataScanner
import slider.revealjs.SlideSourceDir
import java.io.File
import java.nio.file.Files

class RevealJsDecompositionSteps : En {

    private var attributesSpec: RevealJsAttributesSpec? = null
    private var specError: Throwable? = null
    private var outputDir: RevealJsOutputDir? = null
    private var sourceDir: SlideSourceDir? = null
    private var metadata: SlideMetadata? = null
    private var metadataError: Throwable? = null
    private var json: String? = null
    private var cleanedCount: Int? = null
    private var slidesJsonDeleted: Boolean? = null

    init {

        // -------------------------------------------------------------------------
        // RevealJsAttributesSpec
        // -------------------------------------------------------------------------

        When("the default attributes spec is built") {
            attributesSpec = RevealJsAttributesSpec.DEFAULT
            specError = null
        }

        When("a custom attributes spec overrides the default with {string} set to {string}") {
            key: String, value: String ->
            attributesSpec = RevealJsAttributesSpec.DEFAULT.withOverrides(mapOf(key to value))
            specError = null
        }

        When("an attributes spec is built with {string} set to {string}") {
            key: String, value: String ->
            try {
                attributesSpec = RevealJsAttributesSpec(mapOf(key to value))
            } catch (e: Throwable) {
                specError = e
            }
        }

        Then("the spec should declare {int} attributes") { count: Int ->
            assertThat(attributesSpec?.attributes).hasSize(count)
        }

        Then("the attribute {string} should be {string}") { key: String, expected: String ->
            assertThat(attributesSpec?.attributes?.get(key)).isEqualTo(expected)
        }

        Then("the spec construction should fail with a validation error") {
            assertThat(specError)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        // -------------------------------------------------------------------------
        // RevealJsOutputDir
        // -------------------------------------------------------------------------

        When("a revealjs output dir is built from build dir {string}") { buildDir: String ->
            outputDir = RevealJsOutputDir(File(buildDir))
        }

        Then("the output dir path should be {string}") { expected: String ->
            assertThat(outputDir?.asFile()?.path).isEqualTo(expected)
        }

        // -------------------------------------------------------------------------
        // SlideSourceDir
        // -------------------------------------------------------------------------

        When("a slide source dir is built from project dir {string}") { projectDir: String ->
            sourceDir = SlideSourceDir(File(projectDir))
        }

        Then("the source dir path should be {string}") { expected: String ->
            assertThat(sourceDir?.asFile()?.path).isEqualTo(expected)
        }

        // -------------------------------------------------------------------------
        // SlideMetadata
        // -------------------------------------------------------------------------

        When("a slide metadata is built with name {string} filename {string}") {
            name: String, filename: String ->
            metadata = SlideMetadata(name = name, filename = filename)
            metadataError = null
        }

        When("a slide metadata is built with a blank name") {
            try {
                SlideMetadata(name = "", filename = "intro.html")
            } catch (e: Throwable) {
                metadataError = e
            }
        }

        Then("the slide name should be {string}") { expected: String ->
            assertThat(metadata?.name).isEqualTo(expected)
        }

        Then("the slide filename should be {string}") { expected: String ->
            assertThat(metadata?.filename).isEqualTo(expected)
        }

        Then("the slide construction should fail with a validation error") {
            assertThat(metadataError)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        // -------------------------------------------------------------------------
        // SlideMetadataScanner
        // -------------------------------------------------------------------------

        When("the scanner scans a missing directory") {
            val missing = Files.createTempDirectory("scanner-missing").toFile()
                .resolve("does-not-exist")
            val metas = SlideMetadataScanner.scan(missing)
            metadata = null
            cleanedCount = metas.size
        }

        Then("the scanner should return an empty list") {
            assertThat(cleanedCount).isEqualTo(0)
        }

        // -------------------------------------------------------------------------
        // DashboardJsonSerializer
        // -------------------------------------------------------------------------

        When("the dashboard serializer serialises no slides") {
            json = DashboardJsonSerializer.serialize(emptyList())
        }

        When("the dashboard serializer serialises a slide named {string}") { name: String ->
            json = DashboardJsonSerializer.serialize(
                listOf(SlideMetadata(name = name, filename = "$name.html")),
            )
        }

        Then("the json payload should be {string}") { expected: String ->
            assertThat(json).isEqualTo(expected)
        }

        Then("the json payload should contain {string}") { expected: String ->
            assertThat(json).contains(expected)
        }

        // -------------------------------------------------------------------------
        // CleanBuildTarget
        // -------------------------------------------------------------------------

        When("a clean build target collects a missing output dir") {
            val missing = Files.createTempDirectory("clean-missing").toFile()
                .resolve("missing-output")
            val report = CleanBuildTarget(missing).collect()
            cleanedCount = report.cleanedCount()
            slidesJsonDeleted = report.slidesJsonDeleted
        }

        Then("the cleaned count should be {int}") { expected: Int ->
            assertThat(cleanedCount).isEqualTo(expected)
        }

        Then("the slides json should not be reported as deleted") {
            assertThat(slidesJsonDeleted).isFalse()
        }
    }
}