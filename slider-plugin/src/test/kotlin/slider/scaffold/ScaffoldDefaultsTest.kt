package slider.scaffold

import slider.AiConfiguration
import slider.AuthorContext
import slider.DeckContext
import slider.GitPushConfiguration
import slider.NotesConfiguration
import slider.PageNotesStyle
import slider.RevealJsContext
import slider.RepositoryConfiguration
import slider.RepositoryCredentials
import slider.SlideHint
import slider.SlidesConfiguration
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import arrow.integrations.jackson.module.registerArrowModule
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertIs

class ScaffoldDefaultsTest {

    private val yamlMapper: ObjectMapper = YAMLMapper()
        .let { ObjectMapper(YAMLFactory()) }
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerKotlinModule()
        .registerArrowModule()

    @TempDir
    lateinit var tmpDir: Path

    @Test
    fun `defaultSlidesConfiguration builds a SlidesConfiguration with placeholder values`() {
        val conf = ScaffoldDefaults.defaultSlidesConfiguration()

        assertEquals("docs/asciidocRevealJs", conf.srcPath)
        assertEquals("build/docs/asciidocRevealJs", conf.pushSlides?.from)
        assertEquals("build/slides-repo", conf.pushSlides?.to)
        assertEquals("main", conf.pushSlides?.branch)
        assertEquals("deploy slides", conf.pushSlides?.message)
        assertEquals("slides", conf.pushSlides?.repo?.name)
        assertEquals("https://github.com/your-org/your-slides-repo.git", conf.pushSlides?.repo?.repository)
        assertEquals("your-username", conf.pushSlides?.repo?.credentials?.username)
        assertEquals("your-token", conf.pushSlides?.repo?.credentials?.password)
        assertEquals(listOf("your-gemini-api-key"), conf.ai?.gemini)
        assertEquals(listOf("your-mistral-api-key"), conf.ai?.mistral)
        assertEquals(listOf("your-huggingface-api-key"), conf.ai?.huggingface)
    }

    @Test
    fun `defaultDeckContext builds a DeckContext with placeholder values and three slides`() {
        val ctx = ScaffoldDefaults.defaultDeckContext()

        assertEquals("Your presentation subject", ctx.subject)
        assertEquals("Your target audience", ctx.audience)
        assertEquals(45, ctx.duration)
        assertEquals("fr", ctx.languageCode)
        assertEquals("example-deck.adoc", ctx.outputFile)
        assertEquals("Your Name", ctx.author.name)
        assertEquals("your.email@example.com", ctx.author.email)
        assertEquals("sky", ctx.revealjs.theme)
        assertEquals("c/t", ctx.revealjs.slideNumber)
        assertEquals(1408, ctx.revealjs.width)
        assertEquals(792, ctx.revealjs.height)
        assertTrue(ctx.revealjs.controls)
        assertEquals("edges", ctx.revealjs.controlsLayout)
        assertTrue(ctx.revealjs.history)
        assertTrue(ctx.revealjs.fragmentInURL)
        assertTrue(ctx.notes.speakerNotes)
        assertTrue(ctx.notes.pageNotes)
        assertEquals(PageNotesStyle.DETAILED, ctx.notes.pageNotesStyle)
        assertEquals(3, ctx.slides.size)
        assertEquals("Agenda", ctx.slides[0].title)
        assertEquals("First Topic", ctx.slides[1].title)
        assertEquals("Summary and Next Steps", ctx.slides[2].title)
    }

    @Test
    fun `defaultDeckContext serialises to a valid YAML file`() {
        val ctx = ScaffoldDefaults.defaultDeckContext()
        val target = File(tmpDir.toFile(), "example-deck-context.yml")

        yamlMapper.writeValue(target, ctx)

        assertTrue(target.exists())
        val content = target.readText()
        assertTrue(content.startsWith("---"))
        assertTrue(content.contains("subject:"))
        assertTrue(content.contains("audience:"))
        assertTrue(content.contains("duration: 45"))
        assertTrue(content.contains("languageCode:"))
        assertTrue(content.contains("outputFile:"))
        assertTrue(content.contains("author:"))
        assertTrue(content.contains("name:"))
        assertTrue(content.contains("email:"))
        assertTrue(content.contains("revealjs:"))
        assertTrue(content.contains("notes:"))
        assertTrue(content.contains("slides:"))
        assertTrue(content.contains("Agenda"))
        assertTrue(content.contains("First Topic"))
        assertTrue(content.contains("Summary and Next Steps"))
    }
}