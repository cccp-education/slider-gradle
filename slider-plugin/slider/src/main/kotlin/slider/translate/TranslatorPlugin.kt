package slider.translate

import slider.translate.TranslatorManager.createDisplaySupportedLanguagesTask
import slider.translate.TranslatorManager.createTranslationTasks
import org.gradle.api.Plugin
import org.gradle.api.Project

class TranslatorPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        createDisplaySupportedLanguagesTask()
        createTranslationTasks()
    }
}