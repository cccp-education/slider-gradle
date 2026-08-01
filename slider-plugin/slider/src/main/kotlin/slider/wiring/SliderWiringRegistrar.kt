package slider.wiring

import org.gradle.api.Project

object SliderWiringRegistrar {

    fun configureRepositories(project: Project) =
        GradleWiring.configureRepositories(
            project = project,
            repositories = WiringSpec.DEFAULT.repositories,
        )

    fun applyPlugins(project: Project) =
        GradleWiring.applyPlugins(
            project = project,
            plugins = WiringSpec.DEFAULT.plugins,
        )

    fun configureDependencies(project: Project) =
        GradleWiring.configureDependencies(
            project = project,
            gems = WiringSpec.DEFAULT.gems,
        )
}
