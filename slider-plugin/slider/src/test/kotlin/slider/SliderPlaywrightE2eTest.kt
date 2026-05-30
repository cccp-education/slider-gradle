package slider

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

/**
 * E2E test Playwright-jvm pour le pipeline slider-gradle.
 *
 * 5 assertions :
 * - generateSlides BUILD SUCCESSFUL
 * - index.html contient reveal.js (.reveal .slides)
 * - Titre, slide visible, h2 visible
 * - Bounding box slide dans le viewport
 * - Screenshot > 10 KB (page non-vide)
 *
 * Le HTML généré est servi via HTTP local (npx serve)
 * pour permettre la résolution des dépendances CDN externes
 * (reveal.js, bootstrap, jquery) que file:// bloque.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SliderPlaywrightE2eTest {

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

    /**
     * Démarre `npx serve` sur [port] en servant [serveRoot] et attend que le serveur
     * soit prêt (HTTP 200 sur la racine). Retourne le [Process] démarré.
     *
     * @throws RuntimeException si le serveur ne répond pas dans les 15 secondes.
     */
    private fun startHttpServer(serveRoot: Path, port: Int = 4333): Process {
        val process = ProcessBuilder(
            "npx", "serve", serveRoot.toAbsolutePath().toString(),
            "--listen", port.toString(),
            "--no-clipboard",      // désactive la copie auto dans le presse-papier
            "--no-port-switching"  // ne change pas de port si occupé
        )
            .directory(serveRoot.toFile())
            .redirectErrorStream(true)
            .start()

        // Attend que le serveur réponde (max 15s)
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
                // pas encore prêt
            }
            Thread.sleep(500)
        }
        process.destroyForcibly()
        throw RuntimeException("npx serve did not respond within 15s on port $port")
    }

    @Test
    fun `generateSlides then Playwright assert reveal js content`(@TempDir tempDir: Path) {
        // ── 1. Préparer le projet Gradle consommateur ──────────────────
        val settings = tempDir.resolve("settings.gradle.kts")
        settings.writeText("""
            pluginManagement {
                repositories { mavenLocal(); gradlePluginPortal(); mavenCentral() }
            }
            rootProject.name = "slider-e2e-test"
        """.trimIndent())

        val build = tempDir.resolve("build.gradle.kts")
        build.writeText("""
            plugins {
                id("org.asciidoctor.jvm.revealjs.classic") version "5.0.0-alpha.1"
                id("education.cccp.slider") version "0.0.6"
            }
            slider { configPath = file("slides-context.yml").absolutePath }
        """.trimIndent())

        val configYml = tempDir.resolve("slides-context.yml")
        configYml.writeText("""
            presentation:
              title: "E2E Test Playwright"
              subtitle: "Slider Gradle Plugin"
              author:
                name: "CCCP Education"
                email: "cccp.education@gmail.com"
            revealjs:
              theme: "black"
              transition: "slide"
              slideNumber: true
              history: true
            slides:
              - src: "intro.adoc"
                title: "Introduction"
        """.trimIndent())

        val slidesDir = tempDir.resolve("slides").resolve("misc")
        slidesDir.createDirectories()
        val introAdoc = slidesDir.resolve("intro.adoc")
        introAdoc.writeText("""
            = E2E Test Playwright
            :revealjs_theme: black
            :revealjs_transition: slide
            :toc: auto

            == Playwright E2E

            This slide was generated by the slider Gradle plugin
            and validated by a Playwright-jvm end-to-end test.

            === Features Tested

            * Reveal.js rendering
            * Playwright screenshot capture
            * CI readiness
        """.trimIndent())

        // ── 2. Exécuter generateSlides via mavenLocal ─────────────────
        // withPluginClasspath() n'inclut pas les dépendances transitives
        // (asciidoctor-gradle) → on utilise mavenLocal pour résoudre le
        // plugin slider publié (avec toutes ses dépendances).
        val result = GradleRunner.create()
            .withProjectDir(tempDir.toFile())
            .withArguments("asciidoctorRevealJs", "--stacktrace")
            .forwardOutput()
            .build()

        assertThat(result.task(":asciidoctorRevealJs")?.outcome)
            .`as`("asciidoctorRevealJs should succeed")
            .isEqualTo(org.gradle.testkit.runner.TaskOutcome.SUCCESS)

        // ── 3. Vérifier le HTML généré ──
        // Asciidoctor-gradle produit les slides reveal.js dans build/docs/asciidocRevealJs/
        // (pas dans slides/misc/ qui contient les sources .adoc + le template index.html)
        val slideOutputDir = tempDir.resolve("build/docs/asciidocRevealJs")
        val slideHtml = slideOutputDir.resolve("intro.html")
        assertThat(slideHtml.toFile().exists())
            .`as`("intro.html should exist in $slideOutputDir").isTrue()
        assertThat(slideHtml.toFile().length()).`as`("intro.html should not be empty").isGreaterThan(100)

        // ── 4. Démarrer HTTP serveur local ─────────────────────
        // file:// ne peut pas résoudre les CDN externes (reveal.js, bootstrap, jquery).
        // On sert le tempDir via npx serve pour que Playwright charge
        // correctement toutes les dépendances.
        val httpPort = 4333
        val httpServer = startHttpServer(tempDir, httpPort)

        var context: BrowserContext? = null
        try {
            // ── 5. Playwright — ouvrir le slide reveal.js via HTTP ─────────────
            // intro.html dans build/docs/asciidocRevealJs (output d'asciidoctor)
            val httpUrl = "http://localhost:$httpPort/build/docs/asciidocRevealJs/intro.html"
            context = browser.newContext()
            val page = context.newPage()
            page.navigate(httpUrl)
            page.waitForSelector(".reveal .slides")

            val screenshotPath = tempDir.resolve("build/e2e-screenshot.png")
            Files.createDirectories(screenshotPath.parent)
            page.screenshot(
                com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(false)
            )

            // ── 6. Assertions Playwright — UX/UI ────────────────────────────
            PlaywrightAssertions.assertThat(page).hasTitle("E2E Test Playwright")

            // Reveal.js génère 4+ <section> dans .reveal .slides (title + stack parent + nested slides).
            // .first() cible la première slide visible (title slide) pour éviter le strict mode violation.
            val firstSlide = page.locator(".reveal .slides section").first()
            PlaywrightAssertions.assertThat(firstSlide).isVisible()

            val h2 = page.locator(".reveal .slides section section h2").first()
            PlaywrightAssertions.assertThat(h2).isVisible()

            // UX/UI : tout le contenu de la slide est visible dans le viewport
            val slideBox = firstSlide.boundingBox()
            assertThat(slideBox).`as`("Slide bounding box should exist").isNotNull()
            val viewport = page.viewportSize()
            assertThat(viewport).`as`("Viewport should be set").isNotNull()

            assertThat(slideBox!!.x).`as`("Slide left edge should be >= 0").isGreaterThanOrEqualTo(0.0)
            assertThat(slideBox.y).`as`("Slide top edge should be >= 0").isGreaterThanOrEqualTo(0.0)
            assertThat(slideBox.x + slideBox.width)
                .`as`("Slide right edge should fit within viewport width (${viewport!!.width})")
                .isLessThanOrEqualTo(viewport.width.toDouble())
            assertThat(slideBox.y + slideBox.height)
                .`as`("Slide bottom edge should fit within viewport height (${viewport.height})")
                .isLessThanOrEqualTo(viewport.height.toDouble())

            // Screenshot visible proof
            assertThat(screenshotPath.toFile().length())
                .`as`("Screenshot should be non-trivial (> 10 KB)")
                .isGreaterThan(10_000)

        } finally {
            context?.close()
            httpServer.destroyForcibly()
            httpServer.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
        }
    }
}
