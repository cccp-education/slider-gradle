package slider.wiring

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WiringSpecTest {

    // ---------------------------------------------------------------------------
    // GroupRouting
    // ---------------------------------------------------------------------------

    @Test
    fun `GroupRouting None carries no group name`() {
        assertIs<GroupRouting.None>(GroupRouting.None)
    }

    @Test
    fun `GroupRouting IncludeGroup keeps its name`() {
        assertEquals("rubygems", GroupRouting.IncludeGroup("rubygems").group)
    }

    @Test
    fun `GroupRouting ExcludeGroup keeps its name`() {
        assertEquals("rubygems", GroupRouting.ExcludeGroup("rubygems").group)
    }

    @Test
    fun `GroupRouting IncludeGroup rejects a blank group`() {
        assertFailsWith<IllegalArgumentException> { GroupRouting.IncludeGroup("") }
        assertFailsWith<IllegalArgumentException> { GroupRouting.IncludeGroup("   ") }
    }

    @Test
    fun `GroupRouting ExcludeGroup rejects a blank group`() {
        assertFailsWith<IllegalArgumentException> { GroupRouting.ExcludeGroup("") }
    }

    // ---------------------------------------------------------------------------
    // RepositorySpec
    // ---------------------------------------------------------------------------

    @Test
    fun `a Maven repository spec keeps its url and routing`() {
        val spec = RepositorySpec(
            kind = RepositoryKind.MAVEN,
            url = "https://plugins.gradle.org/m2/",
            routing = GroupRouting.None,
        )
        assertEquals(RepositoryKind.MAVEN, spec.kind)
        assertEquals("https://plugins.gradle.org/m2/", spec.url)
        assertIs<GroupRouting.None>(spec.routing)
    }

    @Test
    fun `a MavenCentral repository spec has no url`() {
        val spec = RepositorySpec(
            kind = RepositoryKind.MAVEN_CENTRAL,
            url = null,
            routing = GroupRouting.ExcludeGroup("rubygems"),
        )
        assertEquals(RepositoryKind.MAVEN_CENTRAL, spec.kind)
        assertNull(spec.url)
        assertIs<GroupRouting.ExcludeGroup>(spec.routing)
    }

    @Test
    fun `an Ivy repository spec keeps its pattern layout and metadata source`() {
        val spec = RepositorySpec(
            kind = RepositoryKind.IVY(
                artifactPattern = "[module]-[revision].gem",
                metadataSources = listOf(IvyMetadataSource.ARTIFACT),
            ),
            url = "https://rubygems.org/gems/",
            routing = GroupRouting.IncludeGroup("rubygems"),
        )
        val ivy = spec.kind as RepositoryKind.IVY
        assertEquals("[module]-[revision].gem", ivy.artifactPattern)
        assertEquals(listOf(IvyMetadataSource.ARTIFACT), ivy.metadataSources)
    }

    @Test
    fun `a Maven repository rejects a blank url`() {
        assertFailsWith<IllegalArgumentException> {
            RepositorySpec(kind = RepositoryKind.MAVEN, url = "", routing = GroupRouting.None)
        }
        assertFailsWith<IllegalArgumentException> {
            RepositorySpec(kind = RepositoryKind.MAVEN, url = "   ", routing = GroupRouting.None)
        }
    }

    @Test
    fun `a MavenCentral repository rejects a non-null url`() {
        assertFailsWith<IllegalArgumentException> {
            RepositorySpec(
                kind = RepositoryKind.MAVEN_CENTRAL,
                url = "https://example.com",
                routing = GroupRouting.None,
            )
        }
    }

    @Test
    fun `a MavenCentral repository accepts a null url`() {
        val spec = RepositorySpec(
            kind = RepositoryKind.MAVEN_CENTRAL,
            url = null,
            routing = GroupRouting.None,
        )
        assertNull(spec.url)
    }

    @Test
    fun `an Ivy repository rejects a blank url`() {
        assertFailsWith<IllegalArgumentException> {
            RepositorySpec(
                kind = RepositoryKind.IVY(
                    artifactPattern = "[module]-[revision].gem",
                    metadataSources = listOf(IvyMetadataSource.ARTIFACT),
                ),
                url = "",
                routing = GroupRouting.None,
            )
        }
    }

    @Test
    fun `an Ivy repository rejects an empty metadata source list`() {
        assertFailsWith<IllegalArgumentException> {
            RepositorySpec(
                kind = RepositoryKind.IVY(
                    artifactPattern = "[module]-[revision].gem",
                    metadataSources = emptyList(),
                ),
                url = "https://rubygems.org/gems/",
                routing = GroupRouting.None,
            )
        }
    }

    // ---------------------------------------------------------------------------
    // PluginSpec
    // ---------------------------------------------------------------------------

    @Test
    fun `a plugin spec keeps its id`() {
        assertEquals(
            "com.github.node-gradle.node",
            PluginSpec("com.github.node-gradle.node").id,
        )
    }

    @Test
    fun `a plugin spec rejects a blank id`() {
        assertFailsWith<IllegalArgumentException> { PluginSpec("") }
        assertFailsWith<IllegalArgumentException> { PluginSpec("   ") }
    }

    // ---------------------------------------------------------------------------
    // GemDependency
    // ---------------------------------------------------------------------------

    @Test
    fun `a gem dependency keeps its coordinates`() {
        val gem = GemDependency(
            group = "rubygems",
            name = "asciidoctor-revealjs",
            version = "5.2.0",
            classifier = "gem",
        )
        assertEquals("rubygems", gem.group)
        assertEquals("asciidoctor-revealjs", gem.name)
        assertEquals("5.2.0", gem.version)
        assertEquals("gem", gem.classifier)
    }

    @Test
    fun `a gem dependency rejects a blank group`() {
        assertFailsWith<IllegalArgumentException> {
            GemDependency(group = "", name = "x", version = "1.0", classifier = "gem")
        }
    }

    @Test
    fun `a gem dependency rejects a blank name`() {
        assertFailsWith<IllegalArgumentException> {
            GemDependency(group = "rubygems", name = "", version = "1.0", classifier = "gem")
        }
    }

    @Test
    fun `a gem dependency rejects a blank version`() {
        assertFailsWith<IllegalArgumentException> {
            GemDependency(group = "rubygems", name = "x", version = "", classifier = "gem")
        }
    }

    @Test
    fun `a gem dependency rejects a blank classifier`() {
        assertFailsWith<IllegalArgumentException> {
            GemDependency(group = "rubygems", name = "x", version = "1.0", classifier = "")
        }
    }

    @Test
    fun `a gem dependency renders its ivy coordinates string`() {
        val gem = GemDependency(
            group = "rubygems",
            name = "asciidoctor-revealjs",
            version = "5.2.0",
            classifier = "gem",
        )
        assertEquals("rubygems:asciidoctor-revealjs:5.2.0@gem", gem.toCoordinates())
    }

    // ---------------------------------------------------------------------------
    // WiringSpec — default aggregates
    // ---------------------------------------------------------------------------

    @Test
    fun `the default wiring spec declares six repositories`() {
        assertEquals(6, WiringSpec.DEFAULT.repositories.size)
    }

    @Test
    fun `the default wiring spec declares three plugins`() {
        assertEquals(3, WiringSpec.DEFAULT.plugins.size)
        assertEquals(
            listOf(
                "com.github.node-gradle.node",
                "org.asciidoctor.jvm.gems.classic",
                "org.asciidoctor.jvm.revealjs.classic",
            ),
            WiringSpec.DEFAULT.plugins.map { it.id },
        )
    }

    @Test
    fun `the default wiring spec declares one gem dependency`() {
        assertEquals(1, WiringSpec.DEFAULT.gems.size)
        val gem = WiringSpec.DEFAULT.gems.first()
        assertEquals("rubygems", gem.group)
        assertEquals("asciidoctor-revealjs", gem.name)
        assertEquals("5.2.0", gem.version)
        assertEquals("gem", gem.classifier)
    }

    @Test
    fun `the default wiring spec routes rubygems repos with include or exclude groups`() {
        val routingGroups = WiringSpec.DEFAULT.repositories
            .mapNotNull { spec ->
                when (val r = spec.routing) {
                    is GroupRouting.IncludeGroup -> "include:${r.group}"
                    is GroupRouting.ExcludeGroup -> "exclude:${r.group}"
                    GroupRouting.None -> null
                }
            }
        assertTrue(routingGroups.contains("include:rubygems"))
        assertTrue(routingGroups.contains("exclude:rubygems"))
    }

    @Test
    fun `the default wiring spec targets the plugins gradle org maven repository first`() {
        val first = WiringSpec.DEFAULT.repositories.first()
        assertEquals(RepositoryKind.MAVEN, first.kind)
        assertEquals("https://plugins.gradle.org/m2/", first.url)
    }

    @Test
    fun `the default wiring spec targets mavenCentral excluding rubygems`() {
        val mavenCentral = WiringSpec.DEFAULT.repositories.first { it.kind == RepositoryKind.MAVEN_CENTRAL }
        assertEquals(GroupRouting.ExcludeGroup("rubygems"), mavenCentral.routing)
    }

    @Test
    fun `the default wiring spec targets the rubygems Ivy repository last`() {
        val last = WiringSpec.DEFAULT.repositories.last()
        assertIs<RepositoryKind.IVY>(last.kind)
        assertEquals("https://rubygems.org/gems/", last.url)
        assertEquals(GroupRouting.IncludeGroup("rubygems"), last.routing)
    }
}