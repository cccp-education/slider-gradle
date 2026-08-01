package slider.repository

import slider.SliderConfig.localConf
import slider.Slides.RevealJsSlides.TASK_PUBLISH_SLIDES
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/**
 * Thin Gradle adapter that registers the `deploySlides` task previously
 * declared in `SliderManager.Tasks.registerPublishSlidesTask`.
 *
 * Pure deployment logic lives in the `slider.repository` domain
 * ([SlidePublisher], [SlideDeployer], [JGitSlidePusher]); this object
 * only wires them into a Gradle `DefaultTask` declaration.
 */
object PublishTaskRegistrar {

    /**
     * Registers the `deploySlides` task on [project].
     *
     * Reads the YAML configuration via [localConf] (which uses the
     * non-deprecated `findProperty` API) and delegates the actual
     * publish pipeline to [SlidePublisher.publish].
     */
    fun register(project: Project) {
        project.tasks.register<DefaultTask>(TASK_PUBLISH_SLIDES) {
            group = "deploy"
            description = "Deploy generated slides to the configured remote repository."
            dependsOn("asciidoctor")
            doFirst { task ->
                logger.info("Task description :\n\t${task.description}")
            }
            doLast {
                val conf = project.localConf
                val repoDir = project.layout.buildDirectory.get().asFile
                    .resolve(conf.pushSlides!!.to)

                SlidePublisher.publish(
                    project = project,
                    slidesDirPath = {
                        project.layout.buildDirectory.get().asFile
                            .resolve(conf.srcPath!!)
                            .absolutePath
                    },
                    pathTo = { repoDir.absolutePath },
                )
            }
        }
    }
}
