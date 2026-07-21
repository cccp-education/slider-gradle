package slider.revealjs

import java.io.File

/**
 * AsciiDoc / Reveal.js attribute keys consumed by the slider plugin when
 * configuring the `asciidoctorRevealJs` task.
 *
 * The keys mirror the constants previously declared in
 * `slider.Slides.RevealJsSlides` but are grouped here so the domain owns its
 * rendering contract.
 */
object RevealJsAttributeKeys {
    const val BUILD_GRADLE = "build-gradle"
    const val ENDPOINT_URL = "endpoint-url"
    const val SOURCE_HIGHLIGHTER = "source-highlighter"
    const val CODERAY_CSS = "coderay-css"
    const val IMAGEDIR = "imagesdir"
    const val TOC = "toc"
    const val ICONS = "icons"
    const val SETANCHORS = "setanchors"
    const val IDPREFIX = "idprefix"
    const val IDSEPARATOR = "idseparator"
    const val DOCINFO = "docinfo"
    const val REVEALJS_THEME = "revealjs_theme"
    const val REVEALJS_CUSTOMCSS = "revealjs_customcss"
    const val REVEALJS_TRANSITION = "revealjs_transition"
    const val REVEALJS_HISTORY = "revealjs_history"
    const val REVEALJS_SLIDENUMBER = "revealjs_slideNumber"
}

/**
 * Value object describing the AsciiDoc + Reveal.js rendering attributes
 * applied to the `asciidoctorRevealJs` task.
 *
 * Pure data — no Gradle dependency. The Gradle adapter [RevealJsTaskRegistrar]
 * turns the [attributes] map into the `attributes(mapOf(...))` call.
 */
data class RevealJsAttributesSpec(
    val attributes: Map<String, String>,
) {
    init {
        // Empty strings are allowed (Asciidoctor uses them to disable a flag),
        // but whitespace-only values are rejected as accidental blanks.
        require(attributes.values.none { it.isNotEmpty() && it.trim().isEmpty() }) {
            "attribute values must not be blank"
        }
    }

    /**
     * Returns a new spec with the supplied [overrides] applied on top of this
     * spec's [attributes]. Useful for a consumer overriding only the theme
     * while keeping the other defaults.
     */
    fun withOverrides(overrides: Map<String, String>): RevealJsAttributesSpec =
        copy(attributes = attributes + overrides)

    companion object {

        /**
         * Default rendering attributes used by [slider.SliderManager]:
         * coderay syntax highlighting, talaria.css custom CSS, black theme,
         * history + slide numbering enabled, left TOC, font icons, slide
         * anchor configuration.
         *
         * The dynamic `build-gradle` and `endpoint-url` attributes are NOT
         * included here — they are injected by the Gradle adapter
         * [RevealJsTaskRegistrar] because they depend on the consumer
         * project's layout.
         */
        val DEFAULT: RevealJsAttributesSpec = RevealJsAttributesSpec(
            attributes = mapOf(
                RevealJsAttributeKeys.SOURCE_HIGHLIGHTER to "coderay",
                RevealJsAttributeKeys.CODERAY_CSS to "style",
                RevealJsAttributeKeys.IMAGEDIR to "./images",
                RevealJsAttributeKeys.TOC to "left",
                RevealJsAttributeKeys.ICONS to "font",
                RevealJsAttributeKeys.SETANCHORS to "",
                RevealJsAttributeKeys.IDPREFIX to "slide-",
                RevealJsAttributeKeys.IDSEPARATOR to "-",
                RevealJsAttributeKeys.DOCINFO to "shared",
                RevealJsAttributeKeys.REVEALJS_THEME to "black",
                RevealJsAttributeKeys.REVEALJS_CUSTOMCSS to "talaria.css",
                RevealJsAttributeKeys.REVEALJS_TRANSITION to "slide",
                RevealJsAttributeKeys.REVEALJS_HISTORY to "true",
                RevealJsAttributeKeys.REVEALJS_SLIDENUMBER to "true",
            ),
        )
    }
}

/**
 * Value object describing the Reveal.js output directory
 * (`<buildDir>/docs/asciidocRevealJs`).
 */
data class RevealJsOutputDir(val buildDir: File) {
    init {
        require(buildDir.path.isNotBlank()) { "buildDir must not be blank" }
    }

    /**
     * Resolves the concrete output directory file.
     */
    fun asFile(): File = buildDir.resolve("docs").resolve("asciidocRevealJs")
}

/**
 * Value object describing the slide source directory
 * (`<projectDir>/slides/misc`).
 */
data class SlideSourceDir(val projectDir: File) {
    init {
        require(projectDir.path.isNotBlank()) { "projectDir must not be blank" }
    }

    /**
     * Resolves the concrete source directory file.
     */
    fun asFile(): File = projectDir.resolve("slides").resolve("misc")

    /**
     * Resolves the images/ subdirectory under the source dir.
     */
    fun imagesDir(): File = asFile().resolve("images")
}

/**
 * Value object describing a single slide presentation metadata entry
 * (name + generated HTML filename).
 */
data class SlideMetadata(val name: String, val filename: String) {
    init {
        require(name.isNotBlank()) { "name must not be blank" }
        require(filename.isNotBlank()) { "filename must not be blank" }
    }
}

/**
 * Domain service that scans a directory for `.adoc` files and returns the
 * corresponding [SlideMetadata] list, sorted by name.
 *
 * Pure — no Gradle dependency. Returns an empty list when the directory does
 * not exist or contains no `.adoc` files.
 */
object SlideMetadataScanner {

    /**
     * Scans [dir] and returns the sorted slide metadata list.
     */
    fun scan(dir: File): List<SlideMetadata> {
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        return dir.listFiles { file ->
            file.isFile && file.extension == "adoc"
        }
            ?.sortedBy { it.name }
            ?.map { file ->
                SlideMetadata(
                    name = file.nameWithoutExtension,
                    filename = "${file.nameWithoutExtension}.html",
                )
            }
            ?: emptyList()
    }
}

/**
 * Domain service that serialises a list of [SlideMetadata] into the
 * `slides.json` dashboard payload.
 *
 * Pure — no Gradle dependency, no external JSON library. Renders a compact
 * JSON array with one object per slide.
 */
object DashboardJsonSerializer {

    /**
     * Serialises [slides] into the `slides.json` payload.
     */
    fun serialize(slides: List<SlideMetadata>): String {
        if (slides.isEmpty()) return "[]"
        return buildString {
            appendLine("[")
            slides.forEachIndexed { index, slide ->
                append("  {")
                append("\"name\": \"")
                append(escape(slide.name))
                append("\", ")
                append("\"filename\": \"")
                append(escape(slide.filename))
                append("\"")
                append("}")
                if (index < slides.size - 1) append(",")
                appendLine()
            }
            append("]")
        }
    }

    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"")
}

/**
 * Value object describing the target output directory to clean before a
 * fresh `asciidoctorRevealJs` run.
 *
 * Reports the deletions performed via [CleanReport] so the Gradle adapter can
 * log a concise summary.
 */
data class CleanBuildTarget(val outputDir: File) {

    /**
     * Collects and deletes the artifacts under [outputDir]:
     * - `slides.json` file
     * - `images/` directory (recursive)
     * - all top-level `.html` files
     *
     * Other files (CSS, data) are preserved. Returns a [CleanReport]
     * describing what was deleted. Safe to call when [outputDir] does not
     * exist — returns an all-false report.
     */
    fun collect(): CleanReport {
        if (!outputDir.exists() || !outputDir.isDirectory) {
            return CleanReport(
                slidesJsonDeleted = false,
                imagesDirDeleted = false,
                htmlFilesDeleted = emptyList(),
            )
        }
        val slidesJsonDeleted = outputDir.resolve("slides.json").let { f ->
            if (f.exists()) { f.delete() } else false
        }
        val imagesDirDeleted = outputDir.resolve("images").let { d ->
            if (d.exists() && d.isDirectory) { d.deleteRecursively() } else false
        }
        val htmlFiles = outputDir.listFiles { f ->
            f.isFile && f.name.endsWith(".html")
        }?.filter { f -> f.delete() } ?: emptyList()
        return CleanReport(
            slidesJsonDeleted = slidesJsonDeleted,
            imagesDirDeleted = imagesDirDeleted,
            htmlFilesDeleted = htmlFiles.map { it.name },
        )
    }
}

/**
 * Report of the deletions performed by [CleanBuildTarget.collect].
 */
data class CleanReport(
    val slidesJsonDeleted: Boolean,
    val imagesDirDeleted: Boolean,
    val htmlFilesDeleted: List<String>,
) {

    /**
     * Total number of artifacts deleted (slides.json + images dir + html files).
     */
    fun cleanedCount(): Int =
        (if (slidesJsonDeleted) 1 else 0) +
            (if (imagesDirDeleted) 1 else 0) +
            htmlFilesDeleted.size
}