// ── buildscript resolutionStrategy ────────────────────────────────────────────────
// Gradle 9.5.1 pinne annotations:13.0 (Kotlin embedded) en strictly.
// koog-agents 0.8.0 → koog-utils/koog-http-client-core/koog-prompt-llm →
// annotations:26.0.2-1. Codebase-plugin exclut koog-agents mais les sous-modules
// koog transitifs contournent l'exclusion. Solution : forcer annotations:26.0.2-1.
buildscript {
    repositories { mavenLocal(); mavenCentral() }
    configurations.all { resolutionStrategy { force("org.jetbrains:annotations:26.0.2-1") } }
}

import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.plugin.compatibility.compatibility

plugins {
    `java-library`
    alias(libs.plugins.publish)
    alias(libs.plugins.codebase)
    id("education.cccp.build.gradle-plugin") version "0.0.2"
    id("education.cccp.build.publishing") version "0.0.2"
    id("education.cccp.build.functional-test") version "0.0.2"
    id("education.cccp.build.cucumber") version "0.0.2"
    id("education.cccp.build.logback-exclusion") version "0.0.2"
}

group = "education.cccp"
version = "0.0.9"

repositories {
    mavenCentral()
    gradlePluginPortal()
    listOf(
        "https://repo.gradle.org/gradle/libs-releases/",
        "https://plugins.gradle.org/m2/",
//        "https://maven.xillio.com/artifactory/libs-release/",
        "https://mvnrepository.com/repos/springio-plugins-release",
        "https://archiva-repository.apache.org/archiva/repository/public/"
    ).forEach(::maven)
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(libs.bundles.asciidoctor)
    implementation(libs.node.gradle)

    api(libs.bundles.slider)
    api(libs.bundles.jgit)
    api(libs.commons.io)

    // N0 contracts — i18n (internationalisation deck, EPIC SLD-3)
    implementation(libs.i18n.contracts)

    // Coroutines - IMPORTANT for the asynchronous tests
    testImplementation(libs.bundles.coroutines)

    testImplementation(libs.slf4j)
    testRuntimeOnly(libs.logback)

    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.junit.jupiter)

    // Cucumber dependencies (runner + steps)
    testImplementation(libs.bundles.cucumber)

    // Playwright E2E tests
    testImplementation(libs.playwright)

    // functionalTest additional deps (convention fournit gradleTestKit + junit + assertj)
    functionalTestConventions {
        additionalDependencies = listOf(
            "org.slf4j:slf4j-api:2.0.17",
            "ch.qos.logback:logback-classic:1.5.26",
            libs.mockito.kotlin.get().toString(),
            libs.mockito.junit.jupiter.get().toString()
        )
    }
}

// Cucumber features/steps dirs (step definitions in src/test/scenarios)
cucumberConventions {
    featuresDir = "src/test/features"
    scenariosDir = "src/test/scenarios"
}

// Exclude Cucumber step definitions from the unit `test` task.
// The convention plugin excludes `*.scenarios.*` by default, but this
// project uses the `slider.steps` package, so we add a matching filter.
tasks.named<Test>("test") {
    filter {
        excludeTestsMatching("slider.steps.**")
    }
}

tasks.withType<Test>().configureEach {
    // Permet de masquer l'avertissement relatif au chargement dynamique d'agents
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    testLogging {
        exceptionFormat = FULL
    }
    failOnNoDiscoveredTests = false
}

gradlePlugin {
    plugins {
        vcsUrl = "https://github.com/cheroliv/slider-gradle.git"
        website = "https://cheroliv.com"
        create("slider") {
            id = libs.plugins.slider.get().pluginId
            implementationClass = "slider.SliderPlugin"
            displayName = "Slider Plugin"
            description = "Gradle plugin for slider generation."
            listOf(
                "revealjs",
                "slide-generator",
                "slide",
                "jgit",
                "asciidoc",
                "langchain4j",
                "ollama",
                "mistal-ai",
                "huggingface",
                "gemini",
                "kotlin-DSL"
            ).run(tags::set)

            @Suppress("UnstableApiUsage")
            compatibility {
                features {
                    // asciidoctorRevealJs runs OUT_OF_PROCESS via JRuby — not compatible
                    // with Configuration Cache. Will be revisited when asciidoctor-gradle
                    // stabilises beyond 5.0.0-alpha.1.
                    configurationCache = false
                }
            }
        }
        create("slider-translator") {
            id = "education.cccp.slider.translator"
            implementationClass = "slider.translate.TranslatorPlugin"
            displayName = "Slider Translator Plugin"
            description = "Gradle plugin for slide translation across 10 languages."
            tags.set(listOf("translation", "i18n", "llm", "ollama", "kotlin-DSL"))
        }
    }
}

publishingConventions {
    publicationType = "PLUGIN"
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("Slider Gradle Plugin")
            description.set("Gradle plugin for slider generation.")
        }
    }
    repositories {
        mavenCentral()
    }
}