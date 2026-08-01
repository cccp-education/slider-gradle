package slider.scaffold

import slider.SliderConfig.yamlMapper
import slider.SliderPlugin
import slider.Slides.Slide.DEFAULT_SLIDES_FOLDER
import slider.Slides.Slide.SLIDES_CONTEXT_YML
import slider.Slides.Slide.SLIDES_FOLDER
import org.gradle.api.Project

object SliderScaffoldRegistrar {

    fun scaffoldSlidesIfAbsent(project: Project) {
        with(project) {
            val slidesDir = layout.projectDirectory.asFile.resolve(SLIDES_FOLDER)
            val miscDir = slidesDir.resolve(DEFAULT_SLIDES_FOLDER)

            if (slidesDir.exists() && SlidesScaffolder.isSlidesConfigComplete(miscDir)) return

            val zip = SliderPlugin::class.java
                .classLoader
                .getResourceAsStream("slides.zip")
                ?: error(
                    "slides.zip not found in plugin classpath. " +
                            "Please report this issue at https://github.com/cheroliv/slider-gradle"
                )

            val result = SlidesScaffolder.extractSlidesZip(zip, layout.projectDirectory.asFile)
            when (result) {
                is ScaffoldResult.Created -> {
                    println("✅ slides/ directory initialised from plugin defaults.")
                    println("📁 Edit slides/${DEFAULT_SLIDES_FOLDER}/*-deck.adoc to get started.")
                }
                is ScaffoldResult.Failed -> error("Cannot extract slides.zip: ${result.reason}")
                is ScaffoldResult.Skipped -> { /* no-op */ }
            }
        }
    }

    fun scaffoldSlidesContextIfAbsent(project: Project) {
        with(project) {
            val slidesContext = layout.projectDirectory.asFile.resolve(SLIDES_CONTEXT_YML)

            if (slidesContext.exists()) return

            val default = ScaffoldDefaults.defaultSlidesConfiguration()
            yamlMapper.writeValue(slidesContext, default)

            println("✅ slides-context.yml generated with default values.")
            println("✏️  Edit slides-context.yml with your actual Git repository configuration.")
        }
    }

    fun scaffoldDeckContextIfAbsent(project: Project) {
        with(project) {
            val miscDir = layout.projectDirectory.asFile
                .resolve(SLIDES_FOLDER)
                .resolve(DEFAULT_SLIDES_FOLDER)
            val deckContext = miscDir.resolve("example-deck-context.yml")

            if (deckContext.exists()) return

            miscDir.mkdirs()

            val default = ScaffoldDefaults.defaultDeckContext()
            yamlMapper.writeValue(deckContext, default)

            println("✅ example-deck-context.yml generated in slides/misc/.")
            println("✏️  Edit slides/misc/example-deck-context.yml with your deck details.")
        }
    }
}
