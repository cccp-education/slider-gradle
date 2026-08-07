package slider.repository

import java.io.File

/**
 * Credentials for authenticating against the remote Git repository.
 *
 * Both fields must be non-blank — empty credentials would cause JGit to
 * fail at push time with an opaque 401, so we fail fast at construction.
 */
data class GitCredentials(
    val username: String,
    val password: String,
) {
    init {
        require(username.isNotBlank()) { "GitCredentials.username must not be blank" }
        require(password.isNotBlank()) { "GitCredentials.password must not be blank" }
    }
}

/**
 * Immutable description of a single slide-deployment operation.
 *
 * This value object is the DDD boundary between the Gradle plugin layer
 * (which knows about `Project`, `layout.buildDirectory`, YAML config) and
 * the pure domain layer that performs the actual Git push.
 *
 * Every field is validated at construction so that downstream code
 * (the `SlideDeployer`) can assume the request is well-formed.
 *
 * @param slidesDir     directory containing the generated slides HTML,
 *                      must exist and be a directory.
 * @param repoDir       destination directory for the temporary Git repo,
 *                      need not exist yet (will be created by the deployer).
 * @param remoteUrl     remote repository URL, must be non-blank.
 * @param branch        target branch name, must be non-blank.
 * @param commitMessage commit message, must be non-blank.
 * @param username      Git credentials username, must be non-blank.
 * @param password      Git credentials password/token, must be non-blank.
 */
data class SlideDeploymentRequest(
    val slidesDir: File,
    val repoDir: File,
    val remoteUrl: String,
    val branch: String,
    val commitMessage: String,
    val username: String,
    val password: String,
) {
    val credentials: GitCredentials = GitCredentials(username, password)

    init {
        require(slidesDir.exists() && slidesDir.isDirectory) {
            "SlideDeploymentRequest.slidesDir must be an existing directory: ${slidesDir.absolutePath}"
        }
        require(remoteUrl.isNotBlank()) {
            "SlideDeploymentRequest.remoteUrl must not be blank"
        }
        require(branch.isNotBlank()) {
            "SlideDeploymentRequest.branch must not be blank"
        }
        require(commitMessage.isNotBlank()) {
            "SlideDeploymentRequest.commitMessage must not be blank"
        }
    }
}