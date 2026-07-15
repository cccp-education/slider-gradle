plugins {
    alias(libs.plugins.slider)
    alias(libs.plugins.readme)
}

repositories {
    mavenLocal()
    mavenCentral()
}

slider {
    configPath = "slides-context.yml"
        .run(::file)
        .absolutePath
}

// ── Playwright visual test fixtures ──────────────────────────────────────────
// Pre-generates the two Reveal.js HTML decks consumed by the Playwright-jvm
// tests in slider-plugin/slider. The root project uses the real Gradle home
// (JRuby gem present), avoiding the isolated-Jruby issue diagnosed in S-023.
//
// Fixtures:
//   build/playwright-fixtures/ltr/capsule-feed-demo-en-deck.html  (LTR)
//   build/playwright-fixtures/rtl/capsule-feed-demo-ar-deck.html  (RTL)
//
// The RTL fixture requires setRightToLeft(true) which is activated by
// -Planguage=ar at the asciidoctorRevealJs task level. Since that flag is
// global (all decks), we run two passes and copy the relevant HTML into
// separate fixture directories.
//
// Run: ./gradlew generatePlaywrightFixtures
tasks.register<Copy>("copyPlaywrightLtrFixture") {
    group = "verification"
    description = "Copy LTR Reveal.js HTML fixture and assets for Playwright tests."
    dependsOn("asciidoctorRevealJs")
    from(layout.buildDirectory.dir("docs/asciidocRevealJs"))
    into(layout.buildDirectory.dir("playwright-fixtures/ltr"))
    mustRunAfter("asciidoctorRevealJs")
}

tasks.register<Exec>("generateRtlFixture") {
    group = "verification"
    description = "Run asciidoctorRevealJs with -Planguage=ar and copy the RTL fixture + assets."
    // Must run AFTER the LTR fixture is copied, because this task re-runs
    // asciidoctorRevealJs with -Planguage=ar which overwrites the shared
    // build/docs/asciidocRevealJs output with RTL-rendered HTML.
    mustRunAfter("copyPlaywrightLtrFixture")
    commandLine(
        "./gradlew", "asciidoctorRevealJs", "-Planguage=ar",
        "--no-daemon"
    )
    val rtlFixtureDir = layout.buildDirectory.dir("playwright-fixtures/rtl").get().asFile
    doLast {
        val sourceDir = layout.buildDirectory.dir("docs/asciidocRevealJs").get().asFile
        if (!sourceDir.exists()) error("asciidocRevealJs output not found: $sourceDir")
        rtlFixtureDir.deleteRecursively()
        sourceDir.copyRecursively(rtlFixtureDir, overwrite = true)
    }
    outputs.dir(rtlFixtureDir)
}

tasks.register("generatePlaywrightFixtures") {
    group = "verification"
    description = "Generate LTR + RTL Reveal.js HTML fixtures for Playwright visual tests."
    dependsOn("copyPlaywrightLtrFixture", "generateRtlFixture")
}

