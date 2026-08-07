package slider.repository

import java.io.File

/**
 * Result of creating the temporary repository directory.
 */
sealed class RepoDirResult {
    data object Success : RepoDirResult()
    data class Failure(val error: String) : RepoDirResult()
}

/**
 * Result of copying slide files into the repository directory.
 */
sealed class CopyResult {
    data object Success : CopyResult()
    data class Failure(val error: String) : CopyResult()
}

/**
 * Pure-domain slide deployer — performs file-system operations required
 * before the JGit push, without any dependency on the Gradle `Project` API.
 *
 * This object is intentionally side-effect-free regarding Git: it only
 * prepares the directory layout. The actual Git init/commit/push is
 * delegated to [JGitSlidePusher] (adapter) keeping the domain testable
 * without a real Git remote.
 */
object SlideDeployer {

    /**
     * Creates a clean, empty directory at [repoDir].
     *
     * If a file or directory already exists at that path, it is removed
     * first. This mirrors the previous `FileOps.createRepoDir` behaviour
     * but returns a typed result instead of throwing.
     */
    fun createRepoDir(repoDir: File): RepoDirResult = try {
        if (repoDir.exists() && !repoDir.isDirectory) {
            if (!repoDir.delete()) {
                return RepoDirResult.Failure("Cannot delete file at repo dir path: ${repoDir.absolutePath}")
            }
        }
        if (repoDir.exists() && repoDir.isDirectory) {
            if (!repoDir.deleteRecursively()) {
                return RepoDirResult.Failure("Cannot delete existing repo dir: ${repoDir.absolutePath}")
            }
        }
        if (repoDir.exists()) {
            return RepoDirResult.Failure("Repo dir should not already exist: ${repoDir.absolutePath}")
        }
        if (!repoDir.mkdir()) {
            return RepoDirResult.Failure("Cannot create repo dir: ${repoDir.absolutePath}")
        }
        RepoDirResult.Success
    } catch (e: Exception) {
        RepoDirResult.Failure(e.message ?: "An error occurred while creating the repo dir")
    }

    /**
     * Copies all files from [req.slidesDir] into [req.repoDir] recursively.
     *
     * Returns [CopyResult.Failure] if the slides directory is empty —
     * deploying nothing would create an empty commit, which is almost
     * certainly a misconfiguration.
     */
    fun copySlides(req: SlideDeploymentRequest): CopyResult = try {
        val sources = req.slidesDir.listFiles() ?: emptyArray()
        if (sources.isEmpty()) {
            return CopyResult.Failure("Slides directory is empty: ${req.slidesDir.absolutePath}")
        }
        val copied = req.slidesDir.copyRecursively(req.repoDir, overwrite = true)
        if (!copied) {
            return CopyResult.Failure("Unable to copy slides directory to repo dir")
        }
        CopyResult.Success
    } catch (e: Exception) {
        CopyResult.Failure(e.message ?: "An error occurred during file copy")
    }

    /**
     * Cleans up the temporary repository directory after a push
     * (whether the push succeeded or failed).
     */
    fun cleanupRepoDir(req: SlideDeploymentRequest) {
        req.repoDir.deleteRecursively()
    }

    /**
     * Cleans up the source slides directory after a successful deployment.
     */
    fun cleanupSlidesDir(req: SlideDeploymentRequest) {
        req.slidesDir.deleteRecursively()
    }
}