package slider.capsule

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import slider.i18n.SliderMessages

/**
 * Thin Gradle adapter that registers the `generateCapsule` task previously
 * declared in `SliderManager.Tasks.registerAsciidocCapsuleTask`.
 *
 * Pure path data lives in the `slider.capsule` domain value objects
 * ([CapsuleAdocDir], [CapsuleScriptDir]); the parsing and serialization are
 * delegated to [AsciidocSpeakerNoteParser] and [CapsuleScriptWriter]. This
 * object only wires them into a Gradle `DefaultTask` declaration.
 *
 * Behavioural contract preserved bit-for-bit:
 * - task name `generateCapsule`, group `slider`
 * - `outputs.upToDateWhen { false }` so the task always re-runs
 * - for each `.adoc` file in `<projectDir>/slides/misc`:
 *   - parse speaker notes via [AsciidocSpeakerNoteParser.parse]
 *   - when the script is empty (no speaker notes), log a skip warning and
 *     continue to the next deck
 *   - otherwise write `<buildDir>/capsule/<deckName>-script.txt` via
 *     [CapsuleScriptWriter.write] and log the slide count
 */
object CapsuleTaskRegistrar {

    /**
     * Registers the `generateCapsule` task on [project].
     */
    fun register(project: Project) {
        val adocDir = CapsuleAdocDir(project.projectDir)
        val scriptDir = CapsuleScriptDir(project.layout.buildDirectory.get().asFile)
        val lang = SliderMessages.resolveLanguage(project)

        project.tasks.register<DefaultTask>(CapsuleTaskNames.GENERATE_CAPSULE) {
            group = CapsuleTaskNames.GROUP
            description = SliderMessages.get("task.generateCapsule.description", lang)
            outputs.upToDateWhen { false }

            doLast {
                adocDir.adocFiles().forEach { adoc ->
                    val script = AsciidocSpeakerNoteParser.parse(
                        adocContent = adoc.readText(),
                        deckName = adoc.nameWithoutExtension,
                    )
                    if (script.isEmpty) {
                        logger.lifecycle(
                            SliderMessages.format("task.generateCapsule.skipped", lang, adoc.name),
                        )
                        return@forEach
                    }
                    val scriptFile = scriptDir.scriptFileFor(adoc.nameWithoutExtension)
                    scriptFile.writeText(CapsuleScriptWriter.write(script))
                    logger.lifecycle(
                        SliderMessages.format("task.generateCapsule.written", lang, scriptFile.name, script.segments.size),
                    )
                }
            }
        }
    }

    /**
     * Registers the `translateAndGenerateCapsule` composite task on [project].
     *
     * This task depends on both `translateDeck` and `generateCapsule`, wiring
     * the translation pipeline (SLD-5) to the capsule feed pipeline (SLD-2.5)
     * so that a single Gradle invocation produces capsule scripts for all
     * translated decks.
     */
    fun registerTranslateAndGenerateCapsule(project: Project) {
        val lang = SliderMessages.resolveLanguage(project)
        project.tasks.register<DefaultTask>(CapsuleTaskNames.TRANSLATE_AND_GENERATE_CAPSULE) {
            group = CapsuleTaskNames.GROUP
            description = SliderMessages.get("task.translateAndGenerateCapsule.description", lang)
            dependsOn(CapsuleTaskNames.GENERATE_CAPSULE)
            dependsOn("translateDeck")
        }
    }
}