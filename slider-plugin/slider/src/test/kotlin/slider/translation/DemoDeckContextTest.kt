package slider.translation

import contracts.i18n.LanguageCatalog
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import slider.AuthorContext
import slider.DeckContext
import slider.SliderConfig.yamlMapper
import java.io.File

/**
 * Dogfooding unit test — validates the 3 demo deck-context.yml files
 * (fr/en/ar) that ship with the repo so [TranslateDeckTask] can translate
 * them into all 10 supported languages.
 *
 * Deck contexts live in `<repo-root>/slides/misc/capsule-feed-demo-{fr,en,ar}-deck-context.yml`
 * and reference their sibling `.adoc` files via [DeckContext.outputFile].
 */
class DemoDeckContextTest {

    @Test
    fun `fr demo deck-context should parse with valid languageCode and matching adoc`() {
        val ctx = loadDemoContext("fr")

        assertThat(ctx.languageCode).isEqualTo("fr")
        assertThat(ctx.subject).isNotBlank()
        ctx.requireValidLanguage()

        val adoc = adocFor(ctx, "fr")
        assertThat(adoc.exists())
            .withFailMessage("FR demo adoc not found: ${adoc.absolutePath}")
            .isTrue()
    }

    @Test
    fun `en demo deck-context should parse with valid languageCode and matching adoc`() {
        val ctx = loadDemoContext("en")

        assertThat(ctx.languageCode).isEqualTo("en")
        assertThat(ctx.subject).isNotBlank()
        ctx.requireValidLanguage()

        val adoc = adocFor(ctx, "en")
        assertThat(adoc.exists())
            .withFailMessage("EN demo adoc not found: ${adoc.absolutePath}")
            .isTrue()
    }

    @Test
    fun `ar demo deck-context should parse with valid languageCode and matching adoc`() {
        val ctx = loadDemoContext("ar")

        assertThat(ctx.languageCode).isEqualTo("ar")
        assertThat(ctx.subject).isNotBlank()
        ctx.requireValidLanguage()

        val adoc = adocFor(ctx, "ar")
        assertThat(adoc.exists())
            .withFailMessage("AR demo adoc not found: ${adoc.absolutePath}")
            .isTrue()
    }

    @Test
    fun `all 3 demo deck-contexts should have languageCode in LanguageCatalog supported codes`() {
        listOf("fr", "en", "ar").forEach { code ->
            val ctx = loadDemoContext(code)
            assertThat(ctx.languageCode)
                .`as`("Demo deck-context $code languageCode")
                .isIn(LanguageCatalog.supportedCodes())
        }
    }

    @Test
    fun `all 3 demo deck-contexts should have non-blank subject and author`() {
        listOf("fr", "en", "ar").forEach { code ->
            val ctx = loadDemoContext(code)
            assertThat(ctx.subject).`as`("subject $code").isNotBlank()
            assertThat(ctx.author.name).`as`("author name $code").isNotBlank()
            assertThat(ctx.duration).`as`("duration $code").isPositive()
        }
    }

    @Test
    fun `all 3 demo deck-contexts should have outputFile pointing to existing adoc`() {
        listOf("fr", "en", "ar").forEach { code ->
            val ctx = loadDemoContext(code)
            val adoc = adocFor(ctx, code)
            assertThat(adoc.exists())
                .withFailMessage("outputFile '${ctx.outputFile}' for $code does not resolve to existing adoc")
                .isTrue()
        }
    }

    private fun loadDemoContext(code: String): DeckContext {
        val file = demoContextFile(code)
        assertThat(file.exists())
            .withFailMessage("Demo deck-context file not found: ${file.absolutePath}")
            .isTrue()
        return yamlMapper.readValue(file, DeckContext::class.java)
    }

    private fun adocFor(ctx: DeckContext, code: String): File {
        val parent = demoContextFile(code).parentFile
        return File(parent, ctx.outputFile)
    }

    private fun demoContextFile(code: String): File {
        val repoRoot = resolveRepoRoot()
        return File(repoRoot, "slides/misc/capsule-feed-demo-$code-deck-context.yml")
            .takeIf { it.exists() }
            ?: File(repoRoot, "foundry/public/slider-gradle/slides/misc/capsule-feed-demo-$code-deck-context.yml")
    }

    private fun resolveRepoRoot(): File {
        var dir = File(".").absoluteFile
        while (dir != null && dir.parentFile != null) {
            if (File(dir, "slides/misc/capsule-feed-demo-fr-deck.adoc").exists()) return dir
            if (File(dir, "foundry/public/slider-gradle/slides/misc/capsule-feed-demo-fr-deck.adoc").exists()) return dir
            dir = dir.parentFile
        }
        return File(".").absoluteFile
    }
}