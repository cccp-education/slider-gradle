package slider.scaffold

import slider.AiConfiguration
import slider.DeckContext
import slider.GitPushConfiguration
import slider.NotesConfiguration
import slider.PageNotesStyle
import slider.RevealJsContext
import slider.RepositoryConfiguration
import slider.RepositoryCredentials
import slider.SlideHint
import slider.SlidesConfiguration
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Domain service for slides scaffolding.
 *
 * Pure logic — no Gradle, no Jackson, no logging. The Gradle adapter
 * (`SliderManager.Scaffold`) wires this to a `Project` and handles
 * YAML serialisation via the shared `yamlMapper`.
 *
 * Responsibilities:
 * - Detect whether a slides/ configuration is complete ([isSlidesConfigComplete])
 * - Extract a bundled slides.zip into a target directory ([extractSlidesZip])
 */
object SlidesScaffolder {

    private const val INDEX_HTML = "index.html"
    private const val DECK_ADOC_SUFFIX = "-deck.adoc"

    /**
     * A complete slides configuration requires:
     * - `index.html`         — dashboard entry point in `slides/misc/`
     * - at least one `*-deck.adoc` source file in `slides/misc/`
     *
     * Decks are discovered by scanning `*.adoc` files directly, following the
     * `<slug>_<lang>-deck.adoc` convention.
     */
    fun isSlidesConfigComplete(miscDir: File): Boolean {
        if (!miscDir.resolve(INDEX_HTML).exists()) return false
        val decks = miscDir.listFiles { f ->
            f.isFile && f.name.endsWith(DECK_ADOC_SUFFIX)
        }
        return decks?.isNotEmpty() ?: false
    }

    /**
     * Extracts every non-directory entry of the given `slides.zip` stream into
     * [targetDir]. Parent directories are created on demand. Returns
     * [ScaffoldResult.Created] on success, [ScaffoldResult.Failed] if the
     * stream is empty or unreadable.
     */
    fun extractSlidesZip(zipStream: InputStream, targetDir: File): ScaffoldResult {
        return try {
            var extracted = 0
            ZipInputStream(zipStream).use { zis ->
                generateSequence { zis.nextEntry }
                    .filterNot { it.isDirectory }
                    .forEach { entry ->
                        val target = targetDir.resolve(entry.name)
                        target.parentFile.mkdirs()
                        target.outputStream().use { out -> zis.copyTo(out) }
                        zis.closeEntry()
                        extracted++
                    }
            }
            if (extracted == 0) ScaffoldResult.Failed("slides.zip contains no files")
            else ScaffoldResult.Created(targetDir.absolutePath)
        } catch (e: Exception) {
            ScaffoldResult.Failed(e.message ?: "Cannot extract slides.zip")
        }
    }
}