package slider.wiring

/**
 * Routing rule applied to a repository: either no restriction, or a
 * `rubygems`-style group include/exclude filter.
 *
 * Sealed hierarchy keeps the routing cases exhaustive and type-safe so the
 * Gradle adapter (see [GradleWiring]) can pattern-match without instanceof.
 */
sealed interface GroupRouting {

    /**
     * No include/exclude filter — repository accepts every group.
     */
    object None : GroupRouting

    /**
     * Include only the supplied [group] in the repository content filter.
     */
    data class IncludeGroup(val group: String) : GroupRouting {
        init {
            require(group.isNotBlank()) { "group must not be blank" }
        }
    }

    /**
     * Exclude the supplied [group] from the repository content filter.
     */
    data class ExcludeGroup(val group: String) : GroupRouting {
        init {
            require(group.isNotBlank()) { "group must not be blank" }
        }
    }
}

/**
 * Metadata sources supported by an Ivy repository declaration.
 *
 * Mirrors the Gradle `metadataSources` DSL — only `artifact()` is used by
 * the slider plugin (rubygems .gem resolution), but the enum is kept
 * extensible for future Ivy repositories.
 */
enum class IvyMetadataSource {
    ARTIFACT,
}

/**
 * Kind of repository to declare.
 *
 * - [MAVEN]          → a Maven repository at a fixed [RepositorySpec.url]
 * - [MAVEN_CENTRAL]  → the Maven Central repository (no url, Gradle built-in)
 * - [IVY]            → an Ivy repository with a custom artifact pattern
 *                     and metadata sources
 */
sealed interface RepositoryKind {

    /**
     * A Maven repository hosted at a fixed URL.
     */
    object MAVEN : RepositoryKind

    /**
     * The Maven Central repository (Gradle built-in, no URL needed).
     */
    object MAVEN_CENTRAL : RepositoryKind

    /**
     * An Ivy repository with a custom artifact pattern and metadata sources.
     *
     * @property artifactPattern  Ivy pattern such as `[module]-[revision].gem`
     * @property metadataSources   non-empty list of metadata sources to query
     */
    data class IVY(
        val artifactPattern: String,
        val metadataSources: List<IvyMetadataSource>,
    ) : RepositoryKind {
        init {
            require(metadataSources.isNotEmpty()) { "metadataSources must not be empty" }
        }
    }
}

/**
 * Value object describing a single repository declaration.
 *
 * Pure data — no Gradle dependency. The Gradle adapter [GradleWiring] turns
 * each [RepositorySpec] into a concrete `repositories { ... }` block.
 *
 * @property kind     kind of repository (Maven, MavenCentral, Ivy)
 * @property url      URL for Maven/Ivy repositories, `null` for MavenCentral
 * @property routing  group include/exclude filter applied to the repository
 */
data class RepositorySpec(
    val kind: RepositoryKind,
    val url: String?,
    val routing: GroupRouting,
) {
    init {
        when (kind) {
            RepositoryKind.MAVEN -> require(!url.isNullOrBlank()) {
                "Maven repository requires a non-blank url"
            }
            RepositoryKind.MAVEN_CENTRAL -> require(url == null) {
                "MavenCentral repository must not declare a url"
            }
            is RepositoryKind.IVY -> require(!url.isNullOrBlank()) {
                "Ivy repository requires a non-blank url"
            }
        }
    }
}

/**
 * Value object describing a single Gradle plugin to apply.
 *
 * @property id  Gradle plugin id, e.g. `org.asciidoctor.jvm.gems.classic`
 */
data class PluginSpec(val id: String) {
    init {
        require(id.isNotBlank()) { "plugin id must not be blank" }
    }
}

/**
 * Value object describing a Ruby gem dependency declared on the
 * `asciidoctorGems` configuration.
 *
 * The `@gem` classifier is mandatory for Ivy-based gem resolution.
 *
 * @property group      group id (e.g. `rubygems`)
 * @property name       artifact name (e.g. `asciidoctor-revealjs`)
 * @property version    semantic version (e.g. `5.2.0`)
 * @property classifier artifact classifier (e.g. `gem`)
 */
data class GemDependency(
    val group: String,
    val name: String,
    val version: String,
    val classifier: String,
) {
    init {
        require(group.isNotBlank()) { "group must not be blank" }
        require(name.isNotBlank()) { "name must not be blank" }
        require(version.isNotBlank()) { "version must not be blank" }
        require(classifier.isNotBlank()) { "classifier must not be blank" }
    }

    /**
     * Renders the Ivy-style coordinates string consumed by Gradle's
     * `dependencies.add("asciidoctorGems", <coords>)` API.
     */
    fun toCoordinates(): String = "$group:$name:$version@$classifier"
}

/**
 * Aggregate wiring specification consumed by [GradleWiring].
 *
 * Centralises the slider plugin's repository, plugin, and gem dependency
 * declarations as pure data so the Gradle adapter can stay a thin
 * translation layer. [DEFAULT] reproduces the exact wiring previously
 * hard-coded in `SliderManager.Repositories`, `SliderManager.Plugins`, and
 * `SliderManager.Dependencies`.
 */
data class WiringSpec(
    val repositories: List<RepositorySpec>,
    val plugins: List<PluginSpec>,
    val gems: List<GemDependency>,
) {
    companion object {

        /**
         * Default wiring used by [slider.SliderManager]:
         *
         * - 6 repositories: plugins.gradle.org, repo.gradle.org/libs-releases
         *   (rubygems excluded), jcenter-backup mirror (rubygems only),
         *   xillio mirror (rubygems only), mavenCentral (rubygems excluded),
         *   rubygems Ivy layout.
         * - 3 plugins: node-gradle, asciidoctor gems classic, asciidoctor
         *   revealjs classic.
         * - 1 gem: `rubygems:asciidoctor-revealjs:5.2.0@gem`.
         */
        val DEFAULT: WiringSpec = WiringSpec(
            repositories = listOf(
                RepositorySpec(
                    kind = RepositoryKind.MAVEN,
                    url = "https://plugins.gradle.org/m2/",
                    routing = GroupRouting.None,
                ),
                RepositorySpec(
                    kind = RepositoryKind.MAVEN,
                    url = "https://repo.gradle.org/gradle/libs-releases/",
                    routing = GroupRouting.ExcludeGroup("rubygems"),
                ),
                RepositorySpec(
                    kind = RepositoryKind.MAVEN,
                    url = "https://repo.gradle.org/ui/native/jcenter-backup/",
                    routing = GroupRouting.IncludeGroup("rubygems"),
                ),
                RepositorySpec(
                    kind = RepositoryKind.MAVEN,
                    url = "https://maven.xillio.com/artifactory/libs-release/",
                    routing = GroupRouting.IncludeGroup("rubygems"),
                ),
                RepositorySpec(
                    kind = RepositoryKind.MAVEN_CENTRAL,
                    url = null,
                    routing = GroupRouting.ExcludeGroup("rubygems"),
                ),
                RepositorySpec(
                    kind = RepositoryKind.IVY(
                        artifactPattern = "[module]-[revision].gem",
                        metadataSources = listOf(IvyMetadataSource.ARTIFACT),
                    ),
                    url = "https://rubygems.org/gems/",
                    routing = GroupRouting.IncludeGroup("rubygems"),
                ),
            ),
            plugins = listOf(
                PluginSpec("com.github.node-gradle.node"),
                PluginSpec("org.asciidoctor.jvm.gems.classic"),
                PluginSpec("org.asciidoctor.jvm.revealjs.classic"),
            ),
            gems = listOf(
                GemDependency(
                    group = "rubygems",
                    name = "asciidoctor-revealjs",
                    version = "5.2.0",
                    classifier = "gem",
                ),
            ),
        )
    }
}