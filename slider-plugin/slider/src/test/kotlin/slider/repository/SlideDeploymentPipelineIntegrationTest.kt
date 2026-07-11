package slider.repository

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Integration tests for the full slide-deployment domain pipeline:
 *   [SlideDeployer] (file-system) → [JGitSlidePusher] (Git wire)
 *
 * Uses a local bare Git repository as the "remote" — no network access.
 */
class SlideDeploymentPipelineIntegrationTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var slidesDir: File
    private lateinit var repoDir: File
    private lateinit var bareRemote: File

    private fun setupBareRemote(): File {
        bareRemote = tempDir.resolve("bare-remote.git")
        Git.init().setBare(true).setDirectory(bareRemote).call().close()
        return bareRemote
    }

    private fun setupSlidesDir(fileCount: Int): File {
        slidesDir = tempDir.resolve("slides").apply { mkdirs() }
        repeat(fileCount) { i ->
            slidesDir.resolve("slide-$i.html").writeText("<html><body>Slide $i</body></html>")
        }
        return slidesDir
    }

    private fun setupRepoDir(): File {
        repoDir = tempDir.resolve("repo").apply { mkdirs() }
        return repoDir
    }

    private fun buildRequest(
        remoteUrl: String = "file://${bareRemote.absolutePath}",
        branch: String = "main",
        commitMessage: String = "integration test deploy",
    ): SlideDeploymentRequest = SlideDeploymentRequest(
        slidesDir = slidesDir,
        repoDir = repoDir,
        remoteUrl = remoteUrl,
        branch = branch,
        commitMessage = commitMessage,
        username = "user",
        password = "token",
    )

    @Test
    fun `pipeline should init commit and push to a local bare remote`() {
        setupBareRemote()
        setupSlidesDir(2)
        setupRepoDir()
        val request = buildRequest(branch = "gh-pages")

        SlideDeployer.createRepoDir(repoDir)
        SlideDeployer.copySlides(request)

        val commitResult = JGitSlidePusher.initAndCommit(request)
        assertThat(commitResult).isInstanceOf(CommitResult.Success::class.java)

        val pushResult = JGitSlidePusher.push(request)
        assertThat(pushResult).isInstanceOf(SlidePushResult.Success::class.java)

        val remoteRepo = FileRepositoryBuilder().setGitDir(bareRemote).build()
        assertThat(remoteRepo.allRefs).isNotEmpty
        assertThat(remoteRepo.resolve("refs/heads/gh-pages")).isNotNull()
        remoteRepo.close()
    }

    @Test
    fun `pipeline should push the correct commit message`() {
        setupBareRemote()
        setupSlidesDir(1)
        setupRepoDir()
        val request = buildRequest(commitMessage = "custom message from integration test")

        SlideDeployer.createRepoDir(repoDir)
        SlideDeployer.copySlides(request)
        JGitSlidePusher.initAndCommit(request)
        JGitSlidePusher.push(request)

        val remoteRepo = FileRepositoryBuilder().setGitDir(bareRemote).build()
        val git = Git.wrap(remoteRepo)
        val commits = git.log().call().toList()
        assertThat(commits).hasSize(1)
        assertThat(commits[0].fullMessage).isEqualTo("custom message from integration test")
        git.close()
        remoteRepo.close()
    }

    @Test
    fun `pipeline should fail gracefully when remote is unreachable`() {
        setupSlidesDir(1)
        setupRepoDir()
        val request = buildRequest(remoteUrl = "file:///nonexistent/path/to/repo.git")

        SlideDeployer.createRepoDir(repoDir)
        SlideDeployer.copySlides(request)
        JGitSlidePusher.initAndCommit(request)

        val pushResult = JGitSlidePusher.push(request)
        assertThat(pushResult).isInstanceOf(SlidePushResult.Failure::class.java)
    }
}