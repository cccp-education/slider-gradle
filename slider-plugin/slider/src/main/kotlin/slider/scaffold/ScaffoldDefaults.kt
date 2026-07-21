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

/**
 * Default factory for the files scaffolded on first plugin application.
 *
 * Two distinct defaults are provided:
 * - [defaultSlidesConfiguration] — `slides-context.yml` (Git push + AI providers)
 * - [defaultDeckContext] — `example-deck-context.yml` (deck template with 3 slides)
 *
 * Both are pure functions returning typed models; serialisation to YAML is
 * the responsibility of the Gradle adapter.
 */
object ScaffoldDefaults {

    fun defaultSlidesConfiguration(): SlidesConfiguration = SlidesConfiguration(
        srcPath = "docs/asciidocRevealJs",
        pushSlides = GitPushConfiguration(
            from = "build/docs/asciidocRevealJs",
            to = "build/slides-repo",
            branch = "main",
            message = "deploy slides",
            repo = RepositoryConfiguration(
                name = "slides",
                repository = "https://github.com/your-org/your-slides-repo.git",
                credentials = RepositoryCredentials(
                    username = "your-username",
                    password = "your-token"
                )
            )
        ),
        ai = AiConfiguration(
            gemini = listOf("your-gemini-api-key"),
            mistral = listOf("your-mistral-api-key"),
            huggingface = listOf("your-huggingface-api-key"),
        )
    )

    fun defaultDeckContext(): DeckContext = DeckContext(
        subject = "Your presentation subject",
        audience = "Your target audience",
        duration = 45,
        languageCode = "fr",
        outputFile = "example-deck.adoc",
        author = AuthorContext(
            name = "Your Name",
            email = "your.email@example.com"
        ),
        revealjs = RevealJsContext(
            theme = "sky",
            slideNumber = "c/t",
            width = 1408,
            height = 792,
            controls = true,
            controlsLayout = "edges",
            history = true,
            fragmentInURL = true,
        ),
        notes = NotesConfiguration(
            speakerNotes = true,
            pageNotes = true,
            pageNotesStyle = PageNotesStyle.DETAILED,
        ),
        slides = listOf(
            SlideHint(
                title = "Agenda",
                speakerHint = "Introduce the plan in 2 minutes, ask what the audience already knows.",
                pageNotesHint = "List prerequisite knowledge and suggested readings."
            ),
            SlideHint(
                title = "First Topic",
                speakerHint = "Emphasise the most common misconception.",
                pageNotesHint = "Add a hands-on exercise and a reference link."
            ),
            SlideHint(
                title = "Summary and Next Steps",
                speakerHint = "Open the floor: what was new? what is still unclear?",
                pageNotesHint = "Include a 5-question formative assessment."
            ),
        )
    )
}