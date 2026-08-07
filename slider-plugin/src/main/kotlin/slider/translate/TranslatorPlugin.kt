package slider.translate

import slider.translate.TranslatorManager.createDisplaySupportedLanguagesTask
import slider.translate.TranslatorManager.createTranslationTasks
import org.gradle.api.Plugin
import org.gradle.api.Project

@Deprecated(
    message = "Replaced by slider.translation domain (EPIC SLD-5). Use translateDeck task via education.cccp.slider instead.",
    replaceWith = ReplaceWith("slider.SliderPlugin"),
    level = DeprecationLevel.WARNING,
)
class TranslatorPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        createDisplaySupportedLanguagesTask()
        createTranslationTasks()
    }
}