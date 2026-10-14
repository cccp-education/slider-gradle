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
 * CI shard — DDD decomposition (scaffold, config, prerequisite, extension, wiring,
 * revealjs, playwright domains).
 *
 * Exhaustive, disjoint feature shard (see `CucumberShardCoreRunner` for the rationale).
 * Guarded by `slider.ci.SliderCucumberShardCoverageTest`.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/15_scaffold_decomposition.feature")
@SelectClasspathResource("features/16_config_decomposition.feature")
@SelectClasspathResource("features/17_prerequisite_decomposition.feature")
@SelectClasspathResource("features/18_extension_decomposition.feature")
@SelectClasspathResource("features/19_wiring_decomposition.feature")
@SelectClasspathResource("features/20_revealjs_decomposition.feature")
@SelectClasspathResource("features/21_playwright_decomposition.feature")
@ConfigurationParameter(
    key = FEATURES_PROPERTY_NAME,
    value = "src/test/features/15_scaffold_decomposition.feature,src/test/features/16_config_decomposition.feature," +
        "src/test/features/17_prerequisite_decomposition.feature,src/test/features/18_extension_decomposition.feature," +
        "src/test/features/19_wiring_decomposition.feature,src/test/features/20_revealjs_decomposition.feature," +
        "src/test/features/21_playwright_decomposition.feature"
)
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "slider.steps")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty, html:build/reports/cucumber-shard-decomposition.html, json:build/reports/cucumber-shard-decomposition.json"
)
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @wip and not @integration")
class CucumberShardDecompositionRunner
