package slider.wiring

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository

/**
 * Thin Gradle adapter that translates a [WiringSpec] into concrete Gradle
 * repository declarations, plugin applications, and gem dependency
 * declarations.
 *
 * Replaces the former `SliderManager.Repositories`, `SliderManager.Plugins`,
 * and `SliderManager.Dependencies` nested objects. The pure wiring data
 * lives in [WiringSpec] (no Gradle dependency); this object only knows how
 * to apply that data to a Gradle [Project].
 *
 * Usage:
 * ```
 * GradleWiring.configure(project)             // applies WiringSpec.DEFAULT
 * GradleWiring.configure(project, customSpec) // applies a custom spec (tests)
 * ```
 */
object GradleWiring {

    /**
     * Applies the supplied [spec] to [project]:
     * 1. configures all repositories,
     * 2. applies all plugins,
     * 3. declares all gem dependencies on the `asciidoctorGems` configuration.
     *
     * Defaults to [WiringSpec.DEFAULT] when no spec is provided.
     */
    fun configure(project: Project, spec: WiringSpec = WiringSpec.DEFAULT) {
        configureRepositories(project, spec.repositories)
        applyPlugins(project, spec.plugins)
        configureDependencies(project, spec.gems)
    }

    /**
     * Declares every [repositories] entry on [project] using its
     * [RepositoryHandler], honouring the per-repository [GroupRouting].
     */
    fun configureRepositories(
        project: Project,
        repositories: List<RepositorySpec>,
    ) {
        repositories.forEach { spec ->
            when (spec.kind) {
                RepositoryKind.MAVEN -> project.repositories.maven { repo ->
                    repo.url = project.uri(spec.url!!)
                    applyRouting(repo, spec.routing)
                }
                RepositoryKind.MAVEN_CENTRAL -> project.repositories.mavenCentral { repo ->
                    applyRouting(repo, spec.routing)
                }
                is RepositoryKind.IVY -> project.repositories.ivy { repo ->
                    repo.url = project.uri(spec.url!!)
                    repo.patternLayout { layout ->
                        layout.artifact(spec.kind.artifactPattern)
                    }
                    repo.metadataSources { sources ->
                        spec.kind.metadataSources.forEach { src ->
                            when (src) {
                                IvyMetadataSource.ARTIFACT -> sources.artifact()
                            }
                        }
                    }
                    applyRouting(repo, spec.routing)
                }
            }
        }
    }

    /**
     * Applies every [plugins] entry to [project] via `project.plugins.apply`.
     */
    fun applyPlugins(project: Project, plugins: List<PluginSpec>) {
        plugins.forEach { spec -> project.plugins.apply(spec.id) }
    }

    /**
     * Adds every [gems] entry to the `asciidoctorGems` configuration.
     */
    fun configureDependencies(project: Project, gems: List<GemDependency>) {
        gems.forEach { gem ->
            project.dependencies.add("asciidoctorGems", gem.toCoordinates())
        }
    }

    /**
     * Applies a [GroupRouting] rule to a repository content filter.
     *
     * Works for both [MavenArtifactRepository] and Ivy repositories — both
     * expose a `content { ... }` block with `includeGroup` / `excludeGroup`.
     */
    private fun applyRouting(
        repo: ArtifactRepository,
        routing: GroupRouting,
    ) {
        when (routing) {
            GroupRouting.None -> { /* no filter */ }
            is GroupRouting.IncludeGroup -> repo.content { c -> c.includeGroup(routing.group) }
            is GroupRouting.ExcludeGroup -> repo.content { c -> c.excludeGroup(routing.group) }
        }
    }
}