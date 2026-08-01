package slider.repository

import slider.SliderConfig.localConf
import org.gradle.api.Project
import java.io.File

/**
 * Thin Gradle adapter that orchestrates the full slide-publish pipeline.
 *
 * Reads the YAML configuration via [localConf], builds a
 * [SlideDeploymentRequest], and delegates file-system preparation to
 * [SlideDeployer] and Git wire operations to [JGitSlidePusher].
 *
 * This object replaces the former [slider.SliderManager.Git] nested object.
 */
object SlidePublisher {

    /**
     * Full publish pipeline:
     * - Creates a clean temporary repo directory at [pathTo]
     * - Copies slides from [slidesDirPath] into it via [SlideDeployer]
     * - Commits and pushes if the copy succeeds
     * - Cleans up both the repo dir and the source slides dir on success
     */
    fun publish(
        project: Project,
        slidesDirPath: () -> String,
        pathTo: () -> String,
    ) {
        val conf = project.localConf
        val slidesDir = File(slidesDirPath())
        val repoDir = File(pathTo())
        val pushConf = conf.pushSlides ?: return
        val request = SlideDeploymentRequest(
            slidesDir = slidesDir,
            repoDir = repoDir,
            remoteUrl = pushConf.repo.repository,
            branch = pushConf.branch,
            commitMessage = pushConf.message,
            username = pushConf.repo.credentials.username,
            password = pushConf.repo.credentials.password,
        )

        val repoResult = SlideDeployer.createRepoDir(repoDir)
        if (repoResult is RepoDirResult.Failure) {
            project.logger.error("Cannot create repo dir: ${repoResult.error}")
            return
        }

        val copyResult = SlideDeployer.copySlides(request)
        if (copyResult is CopyResult.Failure) {
            project.logger.error("Cannot copy slides: ${copyResult.error}")
            SlideDeployer.cleanupRepoDir(request)
            return
        }

        val commitResult = JGitSlidePusher.initAndCommit(request)
        if (commitResult is CommitResult.Failure) {
            project.logger.error("Cannot init and commit: ${commitResult.error}")
            SlideDeployer.cleanupRepoDir(request)
            return
        }

        val pushResult = JGitSlidePusher.push(request)
        if (pushResult is SlidePushResult.Failure) {
            project.logger.error("Cannot push slides: ${pushResult.error}")
            SlideDeployer.cleanupRepoDir(request)
            return
        }

        SlideDeployer.cleanupRepoDir(request)
        SlideDeployer.cleanupSlidesDir(request)
    }
}
