package slider.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SlideDeploymentRequestTest {

    @TempDir
    lateinit var tempDir: File

    private fun validRequest(
        slidesDir: File = tempDir.resolve("slides").apply { mkdirs() },
        repoDir: File = tempDir.resolve("repo"),
        remoteUrl: String = "https://github.com/org/slides.git",
        branch: String = "main",
        message: String = "deploy slides",
        username: String = "user",
        password: String = "token",
    ) = SlideDeploymentRequest(
        slidesDir = slidesDir,
        repoDir = repoDir,
        remoteUrl = remoteUrl,
        branch = branch,
        commitMessage = message,
        username = username,
        password = password,
    )

    @Test
    fun `should construct with valid arguments`() {
        val req = validRequest()

        assertThat(req.slidesDir).isDirectory
        assertThat(req.repoDir.absolutePath).endsWith("repo")
        assertThat(req.remoteUrl).isEqualTo("https://github.com/org/slides.git")
        assertThat(req.branch).isEqualTo("main")
        assertThat(req.commitMessage).isEqualTo("deploy slides")
        assertThat(req.username).isEqualTo("user")
        assertThat(req.password).isEqualTo("token")
    }

    @Test
    fun `should reject blank remote url`() {
        assertThatCode { validRequest(remoteUrl = "  ") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("remoteUrl")
    }

    @Test
    fun `should reject blank branch`() {
        assertThatCode { validRequest(branch = "") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("branch")
    }

    @Test
    fun `should reject blank commit message`() {
        assertThatCode { validRequest(message = "") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("commitMessage")
    }

    @Test
    fun `should reject blank username`() {
        assertThatCode { validRequest(username = "") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("username")
    }

    @Test
    fun `should reject blank password`() {
        assertThatCode { validRequest(password = "") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("password")
    }

    @Test
    fun `should reject non-existent slides directory`() {
        assertThatCode { validRequest(slidesDir = tempDir.resolve("missing")) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("slidesDir")
    }

    @Test
    fun `should reject slides directory that is a file`() {
        val file = tempDir.resolve("notadir").apply { writeText("x") }
        assertThatCode { validRequest(slidesDir = file) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("slidesDir")
    }

    @Test
    fun `credentials should expose username and password`() {
        val req = validRequest()
        assertThat(req.credentials.username).isEqualTo("user")
        assertThat(req.credentials.password).isEqualTo("token")
    }

    @Test
    fun `GitCredentials should reject blank username`() {
        assertThatCode { GitCredentials(username = "", password = "token") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("username")
    }

    @Test
    fun `GitCredentials should reject blank password`() {
        assertThatCode { GitCredentials(username = "user", password = "") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("password")
    }
}