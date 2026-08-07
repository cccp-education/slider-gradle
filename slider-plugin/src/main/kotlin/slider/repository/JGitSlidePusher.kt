package slider.repository

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

/**
 * Result of initialising a Git repository and creating the initial commit.
 *
 * The commit SHA is exposed as a plain [String] so that no JGit type
 * leaks through the domain boundary.
 */
sealed class CommitResult {
    data class Success(val commitSha: String) : CommitResult()
    data class Failure(val error: String) : CommitResult()
}

/**
 * Result of pushing the local repository to the configured remote.
 */
sealed class SlidePushResult {
    data object Success : SlidePushResult()
    data class Failure(val error: String) : SlidePushResult()
}

/**
 * JGit adapter — performs the actual Git init/commit/push operations
 * against a [SlideDeploymentRequest].
 *
 * This object is the infrastructure adapter in the DDD hexagonal
 * architecture: the domain layer ([SlideDeployer]) handles file-system
 * preparation, while this adapter handles Git wire operations.
 *
 * All JGit types are contained within this file — the public API
 * returns only domain types ([CommitResult] / [SlidePushResult]).
 */
object JGitSlidePusher {

    private const val REMOTE_ORIGIN: String = "origin"

    fun initAndCommit(req: SlideDeploymentRequest): CommitResult = try {
        val commit = Git.init()
            .setInitialBranch(req.branch)
            .setDirectory(req.repoDir)
            .call()
            .use { git ->
                git.remoteAdd()
                    .setName(REMOTE_ORIGIN)
                    .setUri(URIish(req.remoteUrl))
                    .call()
                git.add().addFilepattern(".").call()
                git.commit().setMessage(req.commitMessage).call()
            }
        CommitResult.Success(commit.name)
    } catch (e: Exception) {
        CommitResult.Failure(e.message ?: "Failed to init and commit")
    }

    fun push(req: SlideDeploymentRequest): SlidePushResult = try {
        Git.open(req.repoDir).use { git ->
            git.push()
                .setRemote(REMOTE_ORIGIN)
                .setForce(true)
                .setCredentialsProvider(
                    UsernamePasswordCredentialsProvider(
                        req.credentials.username,
                        req.credentials.password,
                    )
                )
                .call()
        }
        SlidePushResult.Success
    } catch (e: Exception) {
        SlidePushResult.Failure(e.message ?: "Failed to push to remote")
    }
}