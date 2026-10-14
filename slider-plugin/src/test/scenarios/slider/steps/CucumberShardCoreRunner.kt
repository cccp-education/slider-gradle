package slider.steps

import io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME
import io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME
import io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME
import io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite

/**
 * CI shard — core deck generation (minimal config, propose context, reveal transition,
 * auto-animate).
 *
 * One of the exhaustive, disjoint feature shards that replace the single monolithic
 * `CucumberTestRunner` invocation in CI (`full-tests.yml`), so the BDD suite runs on
 * several machines in parallel instead of one job. Each shard keeps the one-JVM
 * efficiency of the aggregate runner (no per-feature bootstrap).
 *
 * Exhaustiveness/disjointness is guarded by `slider.ci.SliderCucumberShardCoverageTest`.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/1_minimal_configuration.feature")
@SelectClasspathResource("features/2_propose_deck_context.feature")
@SelectClasspathResource("features/3_revealjs_transition.feature")
@SelectClasspathResource("features/4_auto_animate.feature")
@ConfigurationParameter(
    key = FEATURES_PROPERTY_NAME,
    value = "src/test/features/1_minimal_configuration.feature,src/test/features/2_propose_deck_context.feature," +
        "src/test/features/3_revealjs_transition.feature,src/test/features/4_auto_animate.feature"
)
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "slider.steps")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty, html:build/reports/cucumber-shard-core.html, json:build/reports/cucumber-shard-core.json"
)
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @wip and not @integration")
class CucumberShardCoreRunner
