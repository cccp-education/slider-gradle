@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}
// CNV-16 — JitPack mirror for the education.cccp.build.* conventions
// (bypasses Sonatype / Gradle Plugin Portal). The JitPack tag is pinned while
// the canonical plugin declarations may stay on the released version.
pluginManagement.repositories.maven("https://jitpack.io")
pluginManagement.resolutionStrategy.eachPlugin {
    if (requested.id.id.startsWith("education.cccp.build.")) {
        useModule("com.github.cccp-education.conventions-gradle:conventions-plugin:0.0.15")
    }
}


plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
    id("com.gradleup.nmcp.settings").version("1.5.0")
}

val globalProps =
    java.util.Properties().also {
        val globalFile = file(System.getProperty("user.home") + "/.gradle/gradle.properties")
        if (globalFile.exists()) it.load(globalFile.inputStream())
    }

nmcpSettings {
    centralPortal {
        // Empty fallback (not `error`): a CI runner has no credentials in
        // ~/.gradle/gradle.properties — failing here would break EVERY task at
        // configuration time, including `test`/`compileKotlin`. Credentials are
        // only required by the actual publish tasks. Pattern shared with codebase
        // (S-269) and dashboard (S-217).
        username = globalProps.getProperty("ossrhUsername") ?: ""
        password = globalProps.getProperty("ossrhPassword") ?: ""
        publishingType = "AUTOMATIC"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "slider-plugin"
