package slider.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SlideDeployerTest {

    @TempDir
    lateinit var tempDir: File

    private fun newRequest(
        slidesDir: File = tempDir.resolve("slides").apply { mkdirs() },
        repoDir: File = tempDir.resolve("repo"),
    ): SlideDeploymentRequest = SlideDeploymentRequest(
        slidesDir = slidesDir,
        repoDir = repoDir,
        remoteUrl = "https://github.com/org/slides.git",
        branch = "main",
        commitMessage = "deploy slides",
        username = "user",
        password = "token",
    )

    @Test
    fun `createRepoDir should create an empty directory when none exists`() {
        val req = newRequest()
        val result = SlideDeployer.createRepoDir(req.repoDir)

        assertThat(result).isInstanceOf(RepoDirResult.Success::class.java)
        assertThat(req.repoDir).exists().isDirectory
        assertThat(req.repoDir.listFiles()).isEmpty()
    }

    @Test
    fun `createRepoDir should remove existing directory and recreate`() {
        val existing = tempDir.resolve("repo").apply { mkdirs() }
        existing.resolve("stale.txt").writeText("old")

        val req = newRequest(repoDir = existing)
        val result = SlideDeployer.createRepoDir(req.repoDir)

        assertThat(result).isInstanceOf(RepoDirResult.Success::class.java)
        assertThat(req.repoDir).exists().isDirectory
        assertThat(req.repoDir.listFiles()).isEmpty()
    }

    @Test
    fun `createRepoDir should remove existing file at path and recreate as directory`() {
        val file = tempDir.resolve("repo").apply { writeText("x") }

        val req = newRequest(repoDir = file)
        val result = SlideDeployer.createRepoDir(req.repoDir)

        assertThat(result).isInstanceOf(RepoDirResult.Success::class.java)
        assertThat(req.repoDir).exists().isDirectory
    }

    @Test
    fun `copySlides should copy all files from slidesDir to repoDir`() {
        val slidesDir = tempDir.resolve("slides").apply { mkdirs() }
        slidesDir.resolve("index.html").writeText("<html/>")
        slidesDir.resolve("slide1.html").writeText("<html/>")
        val req = newRequest(slidesDir = slidesDir)
        SlideDeployer.createRepoDir(req.repoDir)

        val result = SlideDeployer.copySlides(req)

        assertThat(result).isInstanceOf(CopyResult.Success::class.java)
        assertThat(req.repoDir.resolve("index.html")).exists()
        assertThat(req.repoDir.resolve("slide1.html")).exists()
    }

    @Test
    fun `copySlides should copy nested directories recursively`() {
        val slidesDir = tempDir.resolve("slides").apply { mkdirs() }
        slidesDir.resolve("images").mkdirs()
        slidesDir.resolve("images").resolve("logo.png").writeText("png")
        val req = newRequest(slidesDir = slidesDir)
        SlideDeployer.createRepoDir(req.repoDir)

        val result = SlideDeployer.copySlides(req)

        assertThat(result).isInstanceOf(CopyResult.Success::class.java)
        assertThat(req.repoDir.resolve("images").resolve("logo.png")).exists()
    }

    @Test
    fun `copySlides should return Failure when slidesDir is empty`() {
        val slidesDir = tempDir.resolve("empty").apply { mkdirs() }
        val req = newRequest(slidesDir = slidesDir)
        SlideDeployer.createRepoDir(req.repoDir)

        val result = SlideDeployer.copySlides(req)

        assertThat(result).isInstanceOf(CopyResult.Failure::class.java)
    }
}