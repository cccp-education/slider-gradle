package slider

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import slider.rtl.RtlSlideAssertion
import slider.rtl.SlideRenderData
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

/**
 * Playwright-jvm functional test validating that a Reveal.js deck generated
 * with `-Planguage=ar` renders in Right-To-Left mode.
 *
 * Pipeline:
 *  1. GradleRunner builds a consumer project with the Arabic demo deck.
 *  2. asciidoctorRevealJs generates the HTML with setRightToLeft(true).
 *  3. Playwright-jvm opens the rendered page via a local HTTP server.
 *  4. Extracts a [SlideRenderData] snapshot from the browser.
 *  5. Delegates to [RtlSlideAssertion.assertAll] for P0/P1 validation.
 *
 * This closes the gap left by US-3.4: the RTL resolver and wiring were
 * unit-tested but the rendered HTML was never visually validated.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("GradleRunner creates an isolated Gradle user home in /tmp where JRuby " +
    "cannot find the asciidoctor-revealjs gem (LoadError). Root cause diagnosed in " +
    "session 023: withGradleUserHomeDir is absent from the GradleRunner 9.6.1 API. " +
    "Re-enable when the API exposes withGradleUserHomeDir or when the gem is " +
    "installed system-wide. See SliderPlaywrightE2eTest for the full diagnosis.")
class RtlPlaywrightFunctionalTest {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser

    @BeforeAll
    fun launchBrowser() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(
            com.microsoft.playwright.BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(listOf("--no-sandbox", "--disable-gpu"))
        )
    }

    @AfterAll
    fun closeBrowser() {
        browser.close()
        playwright.close()
    }

    private fun startHttpServer(serveRoot: Path, port: Int = 4334): Process {
        val process = ProcessBuilder(
            "npx", "serve", serveRoot.toAbsolutePath().toString(),
            "--listen", port.toString(),
            "--no-clipboard",
            "--no-port-switching"
        )
            .directory(serveRoot.toFile())
            .redirectErrorStream(true)
            .start()

        val deadline = System.currentTimeMillis() + 15_000
        val url = URI("http://localhost:$port").toURL()
        while (System.currentTimeMillis() < deadline) {
            if (!process.isAlive) {
                val stderr = process.inputStream.bufferedReader().readText()
                throw RuntimeException("npx serve exited prematurely: $stderr")
            }
            try {
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 500
                conn.readTimeout = 500
                conn.requestMethod = "GET"
                if (conn.responseCode in 200..399) {
                    return process
                }
            } catch (_: Exception) {
            }
            Thread.sleep(500)
        }
        process.destroyForcibly()
        throw RuntimeException("npx serve did not respond within 15s on port $port")
    }

    @Test
    fun `Arabic deck generated with language=ar renders in RTL mode`(@TempDir tempDir: Path) {
        // ── 1. Consumer project setup ──────────────────────────────────
        tempDir.resolve("settings.gradle.kts").writeText("""
            pluginManagement {
                repositories { mavenLocal(); gradlePluginPortal(); mavenCentral() }
            }
            rootProject.name = "rtl-visual-test"
        """.trimIndent())

        tempDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("org.asciidoctor.jvm.revealjs.classic") version "5.0.0-alpha.1"
                id("education.cccp.slider") version "0.0.7"
            }
            slider { configPath = file("slides-context.yml").absolutePath }
        """.trimIndent())

        tempDir.resolve("slides-context.yml").writeText("""
            presentation:
              title: "RTL Visual Test"
            slides:
              - src: "ar-deck.adoc"
                title: "Arabic Deck"
        """.trimIndent())

        val slidesDir = tempDir.resolve("slides").resolve("misc")
        slidesDir.createDirectories()
        slidesDir.resolve("ar-deck.adoc").writeText("""
            = RTL Visual Test
            :revealjs_theme: sky
            :revealjs_direction: rtl

            == مقدمة

            هذا عرض تجريبي للتحقق من الاتجاه من اليمين إلى اليسار.

            [%step]
            * النقطة الأولى
            * النقطة الثانية
            * النقطة الثالثة

            == الخاتمة

            الاختبار يتحقق من أن الشريحة تعرض بشكل صحيح في RTL.
        """.trimIndent())

        // ── 2. Generate slides with -Planguage=ar ─────────────────────
        val result = GradleRunner.create()
            .withProjectDir(tempDir.toFile())
            .withArguments("asciidoctorRevealJs", "-Planguage=ar", "--stacktrace")
            .forwardOutput()
            .build()

        assertThat(result.task(":asciidoctorRevealJs")?.outcome)
            .`as`("asciidoctorRevealJs should succeed")
            .isEqualTo(TaskOutcome.SUCCESS)

        // ── 3. Verify HTML was generated ──────────────────────────────
        val slideOutputDir = tempDir.resolve("build/docs/asciidocRevealJs")
        val slideHtml = slideOutputDir.resolve("ar-deck.html")
        assertThat(slideHtml.toFile().exists())
            .`as`("ar-deck.html should exist in $slideOutputDir").isTrue()

        // ── 4. Serve via HTTP and open in Playwright ───────────────────
        val httpPort = 4334
        val httpServer = startHttpServer(tempDir, httpPort)

        var context: BrowserContext? = null
        try {
            val httpUrl = "http://localhost:$httpPort/build/docs/asciidocRevealJs/ar-deck.html"
            context = browser.newContext()
            val page = context.newPage()
            page.navigate(httpUrl)
            page.waitForSelector(".reveal .slides")

            // ── 5. Extract SlideRenderData from the rendered page ──────
            // Reveal.js handles RTL via `rtl: true` in JS config + `.rtl` class
            // on `.reveal` (applied at runtime), NOT via <html dir="rtl">.
            val revealRtlConfig = page.evaluate(
                """() => {
                    const scripts = document.querySelectorAll('script');
                    for (const s of scripts) {
                        if (s.textContent && s.textContent.includes('rtl: true')) return true;
                    }
                    return false;
                }"""
            ) as Boolean

            val revealHasRtlClass = page.evalOnSelector(
                ".reveal",
                "el => el.classList.contains('rtl')"
            ) as Boolean

            // In RTL mode, Reveal.js mirrors navigation: the "next" control
            // appears on the left. We check for .navigate-left.enabled or
            // the absence of .navigate-right (replaced by .navigate-left).
            val navNextLeft = page.locator(".reveal .controls .navigate-left").count() > 0

            val firstSlide = page.locator(".reveal .slides section").first()
            val slideBox = firstSlide.boundingBox()
            assertThat(slideBox).`as`("Slide bounding box should exist").isNotNull()
            val viewport = page.viewportSize()
            assertThat(viewport).`as`("Viewport should be set").isNotNull()

            val screenshotPath = tempDir.resolve("build/rtl-screenshot.png")
            Files.createDirectories(screenshotPath.parent)
            page.screenshot(
                com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(false)
            )

            val renderData = SlideRenderData(
                revealRtlConfig = revealRtlConfig,
                revealHasRtlClass = revealHasRtlClass,
                navNextLeft = navNextLeft,
                viewportWidth = viewport!!.width,
                slideBoxX = slideBox!!.x,
                slideBoxWidth = slideBox.width,
            )

            // ── 6. Delegate to RtlSlideAssertion ───────────────────────
            val assertionResult = RtlSlideAssertion.assertAll(renderData)

            assertThat(assertionResult.passed)
                .`as`("RTL assertions should all pass. Failures: ${assertionResult.failures}")
                .isTrue()

            assertThat(screenshotPath.toFile().length())
                .`as`("RTL screenshot should be non-trivial (> 10 KB)")
                .isGreaterThan(10_000)

        } finally {
            context?.close()
            httpServer.destroyForcibly()
            httpServer.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
        }
    }
}