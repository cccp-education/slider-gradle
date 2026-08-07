package slider.steps

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import slider.wiring.GroupRouting
import slider.wiring.PluginSpec
import slider.wiring.RepositoryKind
import slider.wiring.RepositorySpec
import slider.wiring.GemDependency
import slider.wiring.WiringSpec
import java.net.URI

class WiringDecompositionSteps : En {

    private var routing: GroupRouting? = null
    private var routingError: Throwable? = null
    private var repository: RepositorySpec? = null
    private var repositoryError: Throwable? = null
    private var plugin: PluginSpec? = null
    private var pluginError: Throwable? = null
    private var gem: GemDependency? = null
    private var gemError: Throwable? = null
    private var spec: WiringSpec? = null

    init {

        // -------------------------------------------------------------------------
        // GroupRouting
        // -------------------------------------------------------------------------

        When("an include group routing is built for {string}") { group: String ->
            routing = GroupRouting.IncludeGroup(group)
            routingError = null
        }

        When("an exclude group routing is built for {string}") { group: String ->
            routing = GroupRouting.ExcludeGroup(group)
            routingError = null
        }

        When("an include group routing is built with a blank group") {
            try {
                GroupRouting.IncludeGroup("")
            } catch (e: Throwable) {
                routingError = e
            }
        }

        When("an exclude group routing is built with a blank group") {
            try {
                GroupRouting.ExcludeGroup("   ")
            } catch (e: Throwable) {
                routingError = e
            }
        }

        Then("the routing group should be {string}") { expected: String ->
            val actual = when (val r = routing) {
                is GroupRouting.IncludeGroup -> r.group
                is GroupRouting.ExcludeGroup -> r.group
                GroupRouting.None -> null
                null -> null
            }
            assertThat(actual).isEqualTo(expected)
        }

        Then("the routing construction should fail with a validation error") {
            assertThat(routingError)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        // -------------------------------------------------------------------------
        // RepositorySpec
        // -------------------------------------------------------------------------

        When("a Maven repository spec is built with url {string} and no routing") { url: String ->
            repository = RepositorySpec(
                kind = RepositoryKind.MAVEN,
                url = url,
                routing = GroupRouting.None,
            )
            repositoryError = null
        }

        When("a Maven repository spec is built with a blank url") {
            try {
                RepositorySpec(kind = RepositoryKind.MAVEN, url = "", routing = GroupRouting.None)
            } catch (e: Throwable) {
                repositoryError = e
            }
        }

        When("a MavenCentral repository spec is built excluding {string}") { group: String ->
            repository = RepositorySpec(
                kind = RepositoryKind.MAVEN_CENTRAL,
                url = null,
                routing = GroupRouting.ExcludeGroup(group),
            )
            repositoryError = null
        }

        When("a MavenCentral repository spec is built with url {string}") { url: String ->
            try {
                RepositorySpec(
                    kind = RepositoryKind.MAVEN_CENTRAL,
                    url = url,
                    routing = GroupRouting.None,
                )
            } catch (e: Throwable) {
                repositoryError = e
            }
        }

        Then("the repository url should be {string}") { expected: String ->
            assertThat(repository?.url).isEqualTo(expected)
        }

        Then("the repository url should be null") {
            assertThat(repository?.url).isNull()
        }

        Then("the repository kind should be {string}") { expected: String ->
            val actual = when (repository?.kind) {
                RepositoryKind.MAVEN -> "MAVEN"
                RepositoryKind.MAVEN_CENTRAL -> "MAVEN_CENTRAL"
                is RepositoryKind.IVY -> "IVY"
                null -> null
            }
            assertThat(actual).isEqualTo(expected)
        }

        Then("the repository construction should fail with a validation error") {
            assertThat(repositoryError)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        // -------------------------------------------------------------------------
        // PluginSpec
        // -------------------------------------------------------------------------

        When("a plugin spec is built with id {string}") { id: String ->
            plugin = PluginSpec(id)
            pluginError = null
        }

        When("a plugin spec is built with a blank id") {
            try {
                PluginSpec("   ")
            } catch (e: Throwable) {
                pluginError = e
            }
        }

        Then("the plugin id should be {string}") { expected: String ->
            assertThat(plugin?.id).isEqualTo(expected)
        }

        Then("the plugin construction should fail with a validation error") {
            assertThat(pluginError)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        // -------------------------------------------------------------------------
        // GemDependency
        // -------------------------------------------------------------------------

        When("a gem dependency is built with group {string} name {string} version {string} classifier {string}") {
            group: String, name: String, version: String, classifier: String ->
            gem = GemDependency(
                group = group,
                name = name,
                version = version,
                classifier = classifier,
            )
            gemError = null
        }

        When("a gem dependency is built with a blank name") {
            try {
                GemDependency(
                    group = "rubygems",
                    name = "",
                    version = "1.0",
                    classifier = "gem",
                )
            } catch (e: Throwable) {
                gemError = e
            }
        }

        Then("the gem coordinates should be {string}") { expected: String ->
            assertThat(gem?.toCoordinates()).isEqualTo(expected)
        }

        Then("the gem construction should fail with a validation error") {
            assertThat(gemError)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        // -------------------------------------------------------------------------
        // WiringSpec DEFAULT
        // -------------------------------------------------------------------------

        When("the default wiring spec is built") {
            spec = WiringSpec.DEFAULT
        }

        Then("the spec should declare {int} repositories") { count: Int ->
            assertThat(spec?.repositories).hasSize(count)
        }

        Then("the spec should declare {int} plugins") { count: Int ->
            assertThat(spec?.plugins).hasSize(count)
        }

        Then("the spec should declare {int} gem") { count: Int ->
            assertThat(spec?.gems).hasSize(count)
        }

        Then("the first plugin id should be {string}") { expected: String ->
            assertThat(spec?.plugins?.first()?.id).isEqualTo(expected)
        }

        Then("the spec should include the {string} group on at least one repository") {
            group: String ->
            val hasInclude = spec?.repositories.orEmpty().any { repo ->
                repo.routing is GroupRouting.IncludeGroup &&
                    (repo.routing as GroupRouting.IncludeGroup).group == group
            }
            assertThat(hasInclude).isTrue()
        }

        Then("the spec should exclude the {string} group on at least one repository") {
            group: String ->
            val hasExclude = spec?.repositories.orEmpty().any { repo ->
                repo.routing is GroupRouting.ExcludeGroup &&
                    (repo.routing as GroupRouting.ExcludeGroup).group == group
            }
            assertThat(hasExclude).isTrue()
        }

        Then("the last repository should be an Ivy repository") {
            assertThat(spec?.repositories?.last()?.kind)
                .isInstanceOf(RepositoryKind.IVY::class.java)
        }

        Then("the last repository url should be {string}") { expected: String ->
            assertThat(spec?.repositories?.last()?.url).isEqualTo(expected)
        }
    }
}