package slider.repository

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class JGitSlidePusherTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var bareRemote: File

    private fun createBareRemote(): File {
        bareRemote = tempDir.resolve("remote.git")
        Git.init().setBare(true).setDirectory(bareRemote).call().close()
        return bareRemote
    }

    private fun createRequest(
        remoteUrl: String = "file://${createBareRemote().absolutePath}",
        branch: String = "main",
        commitMessage: String = "deploy slides",
        slidesDir: File = tempDir.resolve("slides").apply { mkdirs() },
        repoDir: File = tempDir.resolve("repo").apply { mkdirs() },
    ): SlideDeploymentRequest {
        if (slidesDir.listFiles()?.isEmpty() != false) {
            slidesDir.resolve("index.html").writeText("<html><body>Slide 1</body></html>")
        }
        if (repoDir.listFiles()?.isEmpty() == true) {
            slidesDir.copyRecursively(repoDir, overwrite = true)
        }
        return SlideDeploymentRequest(
            slidesDir = slidesDir,
            repoDir = repoDir,
            remoteUrl = remoteUrl,
            branch = branch,
            commitMessage = commitMessage,
            username = "user",
            password = "token",
        )
    }

    @Test
    fun `initAndCommit should create git repo and return Success with commit SHA`() {
        val req = createRequest()

        val result = JGitSlidePusher.initAndCommit(req)

        assertThat(result).isInstanceOf(CommitResult.Success::class.java)
        val success = result as CommitResult.Success
        assertThat(success.commitSha).isNotBlank()
        assertThat(req.repoDir.resolve(".git")).exists().isDirectory
    }

    @Test
    fun `initAndCommit should stage all files and create commit with configured message`() {
        val req = createRequest()

        JGitSlidePusher.initAndCommit(req)

        val git = Git.open(req.repoDir)
        val commits = git.log().call().toList()
        assertThat(commits).hasSize(1)
        assertThat(commits[0].fullMessage).isEqualTo("deploy slides")
        git.close()
    }

    @Test
    fun `initAndCommit should set the configured branch name`() {
        val req = createRequest(branch = "gh-pages")

        JGitSlidePusher.initAndCommit(req)

        val repo = FileRepositoryBuilder()
            .setGitDir(req.repoDir.resolve(".git"))
            .build()
        assertThat(repo.branch).isEqualTo("gh-pages")
        repo.close()
    }

    @Test
    fun `initAndCommit should add the remote origin URL`() {
        val req = createRequest()

        JGitSlidePusher.initAndCommit(req)

        val repo = FileRepositoryBuilder()
            .setGitDir(req.repoDir.resolve(".git"))
            .build()
        val remoteConfig = repo.config.getString("remote", "origin", "url")
        assertThat(remoteConfig).isEqualTo(req.remoteUrl)
        repo.close()
    }

    @Test
    fun `push should push commits to the remote repository`() {
        val req = createRequest()
        JGitSlidePusher.initAndCommit(req)

        val result = JGitSlidePusher.push(req)

        assertThat(result).isInstanceOf(SlidePushResult.Success::class.java)
        val remoteRepo = FileRepositoryBuilder().setGitDir(bareRemote).build()
        assertThat(remoteRepo.allRefs).isNotEmpty
        remoteRepo.close()
    }

    @Test
    fun `push should return Failure when remote URL is unreachable`() {
        val req = createRequest(remoteUrl = "file:///nonexistent/path/to/repo.git")
        JGitSlidePusher.initAndCommit(req)

        val result = JGitSlidePusher.push(req)

        assertThat(result).isInstanceOf(SlidePushResult.Failure::class.java)
        assertThat((result as SlidePushResult.Failure).error).isNotBlank()
    }
}