package slider.translate

import org.gradle.api.Plugin
import org.gradle.api.Project
import slider.translate.TranslatorManager.registerDisplaySupportedLanguagesTask
import slider.translate.TranslatorManager.registerTranslationTasks

@Deprecated(
    message = "Replaced by slider.translation domain (EPIC SLD-5). Use translateDeck task via education.cccp.slider instead.",
    replaceWith = ReplaceWith("slider.SliderPlugin"),
    level = DeprecationLevel.WARNING,
)
class TranslatorPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit =
        project.run {
            registerDisplaySupportedLanguagesTask()
            registerTranslationTasks()
        }
}
