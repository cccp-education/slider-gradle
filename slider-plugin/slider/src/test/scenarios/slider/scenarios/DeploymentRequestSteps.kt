package slider.scenarios

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.repository.CopyResult
import slider.repository.RepoDirResult
import slider.repository.SlideDeploymentRequest
import slider.repository.SlideDeployer
import java.io.File
import java.nio.file.Files

class DeploymentRequestSteps : En {

    private lateinit var slidesDir: File
    private lateinit var repoDir: File
    private var remoteUrl: String = ""
    private var branch: String = ""
    private var commitMessage: String = ""
    private var username: String = ""
    private var password: String = ""
    private var request: SlideDeploymentRequest? = null
    private var caught: Throwable? = null
    private var repoResult: RepoDirResult? = null
    private var copyResult: CopyResult? = null

    init {

        Given("a slides directory with {int} file(s)") { count: Int ->
            slidesDir = Files.createTempDirectory("slides").toFile()
            repeat(count) { i ->
                slidesDir.resolve("slide-$i.html").writeText("<html>$i</html>")
            }
        }

        Given("a non-existent slides directory") {
            slidesDir = Files.createTempDirectory("parent").toFile()
                .resolve("missing")
        }

        Given("a repo directory path {string}") { path: String ->
            repoDir = Files.createTempDirectory("deploy-test").toFile()
                .resolve(path)
        }

        Given("a repo directory path {string} with a stale file") { path: String ->
            repoDir = Files.createTempDirectory("deploy-test").toFile()
                .resolve(path)
            repoDir.mkdirs()
            repoDir.resolve("stale.txt").writeText("old")
        }

        Given("a remote url {string}") { url: String ->
            remoteUrl = url
        }

        Given("a branch {string}") { b: String ->
            branch = b
        }

        Given("a commit message {string}") { msg: String ->
            commitMessage = msg
        }

        Given("credentials {string} and {string}") { user: String, pass: String ->
            username = user
            password = pass
        }

        When("the deployment request is created") {
            request = SlideDeploymentRequest(
                slidesDir = slidesDir,
                repoDir = repoDir,
                remoteUrl = remoteUrl,
                branch = branch,
                commitMessage = commitMessage,
                username = username,
                password = password,
            )
        }

        When("the deployment request creation is attempted") {
            try {
                request = SlideDeploymentRequest(
                    slidesDir = slidesDir,
                    repoDir = repoDir,
                    remoteUrl = remoteUrl,
                    branch = branch,
                    commitMessage = commitMessage,
                    username = username,
                    password = password,
                )
            } catch (t: Throwable) {
                caught = t
            }
        }

        Then("the request should be valid") {
            assertThat(request).isNotNull
        }

        Then("the request credentials should have username {string}") { expected: String ->
            assertThat(request?.credentials?.username).isEqualTo(expected)
        }

        Then("the request credentials should have password {string}") { expected: String ->
            assertThat(request?.credentials?.password).isEqualTo(expected)
        }

        Then("the creation should fail with a message containing {string}") { fragment: String ->
            assertThat(caught)
                .withFailMessage("Expected a failure containing '$fragment'")
                .isNotNull()
            assertThat(caught!!.message).contains(fragment)
        }

        When("the repo directory is created") {
            repoResult = SlideDeployer.createRepoDir(repoDir)
        }

        Then("the repo directory should exist and be empty") {
            assertThat(repoResult).isInstanceOf(RepoDirResult.Success::class.java)
            assertThat(repoDir).exists().isDirectory
            assertThat(repoDir.listFiles()).isEmpty()
        }

        When("the slides are copied") {
            val req = request ?: SlideDeploymentRequest(
                slidesDir = slidesDir,
                repoDir = repoDir,
                remoteUrl = remoteUrl.ifBlank { "https://github.com/org/slides.git" },
                branch = branch.ifBlank { "main" },
                commitMessage = commitMessage.ifBlank { "deploy" },
                username = username.ifBlank { "user" },
                password = password.ifBlank { "token" },
            )
            copyResult = SlideDeployer.copySlides(req)
        }

        Then("the repo directory should contain {int} file(s)") { count: Int ->
            assertThat(copyResult).isInstanceOf(CopyResult.Success::class.java)
            val files = repoDir.listFiles { f -> f.isFile } ?: emptyArray()
            assertThat(files).hasSize(count)
        }

        Then("the copy should fail") {
            assertThat(copyResult).isInstanceOf(CopyResult.Failure::class.java)
        }
    }
}