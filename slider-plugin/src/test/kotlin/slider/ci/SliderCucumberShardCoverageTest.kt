package slider.ci

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

/**
 * CI shard coverage guard.
 *
 * The monolithic `CucumberTestRunner` is split in `full-tests.yml` into exhaustive,
 * disjoint feature shards, each run on its own CI machine. This guard kills the
 * "test fantôme" risk (S-269): a feature that belongs to no shard (or to two) would
 * silently stop being executed.
 *
 * It asserts, from the repository sources (no Gradle model, pure file parsing):
 * 1. the shard runners form a **disjoint** partition whose union is **every**
 *    feature file under `src/test/features/`, and each shard preserves the aggregate
 *    tag filter (`not @wip and not @integration`);
 * 2. every shard runner is registered as a Gradle task **and** invoked by the CI
 *    workflow (`full-tests.yml`);
 * 3. the functional shards form an exhaustive, disjoint partition of the
 *    `functionalTest` classes.
 */
class SliderCucumberShardCoverageTest {

    private val repoRoot: File = run {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        while (dir.parentFile != null && !dir.resolve(".github/workflows/full-tests.yml").isFile) {
            dir = dir.parentFile
        }
        check(dir.resolve(".github/workflows/full-tests.yml").isFile) {
            "full-tests.yml not found above ${System.getProperty("user.dir")}"
        }
        dir
    }

    private val scenarioDir = repoRoot.resolve("slider-plugin/src/test/scenarios/slider/steps")
    private val featuresDir = repoRoot.resolve("slider-plugin/src/test/features")
    private val buildScript = repoRoot.resolve("slider-plugin/build.gradle.kts")
    private val workflow = repoRoot.resolve(".github/workflows/full-tests.yml")

    private fun allFeatures(): Set<String> =
        featuresDir.listFiles { f -> f.isFile && f.name.endsWith(".feature") }!!
            .map { it.name }
            .toSet()

    private fun shardRunners(): List<File> =
        scenarioDir.listFiles { f -> f.isFile && f.name.startsWith("CucumberShard") && f.name.endsWith("Runner.kt") }!!
            .sortedBy { it.name }

    private fun runnerFeatures(runner: File): List<String> {
        val text = runner.readText()
        // A shard may select the whole `features` directory (tag-partitioned) — then it
        // spans every feature.
        if (Regex("""@SelectClasspathResource\("features"\)""").containsMatchIn(text)) {
            return allFeatures().toList()
        }
        return Regex("""@SelectClasspathResource\("features/([^"]+\.feature)"\)""")
            .findAll(text)
            .map { it.groupValues[1] }
            .toList()
    }

    private fun runnerTags(runner: File): String =
        Regex("""FILTER_TAGS_PROPERTY_NAME,\s*value = "([^"]*)"""")
            .find(runner.readText())?.groupValues?.get(1) ?: ""

    @Test
    fun `the shards cover every feature, and a feature is split only by tag filters`() {
        val runners = shardRunners()
        assertThat(runners)
            .describedAs("CI shard runners must exist and be at least 5")
            .hasSizeGreaterThanOrEqualTo(5)

        val perRunner = runners.associate { it.name to runnerFeatures(it) }
        perRunner.forEach { (name, features) ->
            assertThat(features)
                .describedAs("%s must select at least one feature", name)
                .isNotEmpty()
        }

        // Every shard keeps the aggregate exclusion filter, so the sharded union runs
        // exactly what `cucumberTest` used to run (no silent inclusion/exclusion drift).
        runners.forEach { r ->
            val tags = runnerTags(r)
            assertThat(tags)
                .describedAs("%s must exclude @wip and @integration (aggregate contract)", r.name)
                .contains("not @wip")
                .contains("not @integration")
        }

        val byFeature = mutableMapOf<String, MutableList<File>>()
        runners.forEach { r -> runnerFeatures(r).forEach { byFeature.getOrPut(it) { mutableListOf() }.add(r) } }

        assertThat(byFeature.keys)
            .describedAs("the shards must cover every feature file")
            .containsExactlyInAnyOrderElementsOf(allFeatures())

        // A feature may appear in several shards only if each such shard restricts it
        // by a tag filter — never a silent duplicate run.
        byFeature.filterValues { it.size > 1 }.forEach { (feature, rs) ->
            rs.forEach { r ->
                assertThat(runnerTags(r))
                    .describedAs("%s overlaps on %s and must carry a non-blank tag filter", r.name, feature)
                    .isNotBlank()
            }
        }
    }

    @Test
    fun `every shard runner is a Gradle task and is run by the CI workflow`() {
        val runnerClasses = shardRunners().map { it.nameWithoutExtension }
        val build = buildScript.readText()
        val ci = workflow.readText()

        runnerClasses.forEach { runner ->
            assertThat(build)
                .describedAs("%s must be registered as a CucumberTaskSpec", runner)
                .contains(runner)
        }

        val buildShardTasks = Regex("""name = "(cucumberTestShard[A-Za-z0-9]+)"""")
            .findAll(build).map { it.groupValues[1] }.toSet()
        val ciShardTasks = Regex("""cucumberTestShard[A-Za-z0-9]+""")
            .findAll(ci).map { it.value }.toSet()

        assertThat(buildShardTasks)
            .describedAs("shard CucumberTaskSpec tasks")
            .hasSizeGreaterThanOrEqualTo(5)
        assertThat(ciShardTasks)
            .describedAs("full-tests.yml must run every shard task")
            .containsExactlyInAnyOrderElementsOf(buildShardTasks)
    }

    @Test
    fun `the functional shards form an exhaustive and disjoint partition of the functional tests`() {
        val functionalDir = repoRoot.resolve("slider-plugin/src/functionalTest/kotlin")
        val all = functionalDir.walkTopDown()
            .filter { it.isFile && it.name.endsWith("FunctionalTest.kt") }
            .map { file ->
                val pkg = Regex("""(?m)^package\s+([A-Za-z0-9_.]+)""").find(file.readText())!!.groupValues[1]
                "$pkg.${file.nameWithoutExtension}"
            }
            .toSet()

        val sharded = Regex("""--tests\s+(slider\.[A-Za-z0-9_.]+)""")
            .findAll(workflow.readText())
            .map { it.groupValues[1] }
            .toList()

        assertThat(all).describedAs("functionalTest classes found").isNotEmpty()
        val duplicates = sharded.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        assertThat(duplicates)
            .describedAs("a functional test must belong to exactly one shard")
            .isEmpty()
        assertThat(sharded.toSet())
            .describedAs("the functional shards must cover every functional test")
            .containsExactlyInAnyOrderElementsOf(all)
    }

    @Test
    fun `the unit job runs the plain test task`() {
        // The unit job must run `test` (which excludes the shard runners and the
        // environment-gated Playwright visual tests) — a missing unit job would let
        // the whole unit suite silently drop out of CI.
        val ci = workflow.readText()
        assertThat(ci)
            .describedAs("full-tests.yml must run the unit `test` task")
            .contains("./gradlew test")
    }
}
