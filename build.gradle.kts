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

