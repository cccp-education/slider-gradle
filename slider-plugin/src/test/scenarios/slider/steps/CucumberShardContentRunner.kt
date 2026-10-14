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
 * CI shard — content / visual validation domains (RTL assertions, slide content).
 *
 * These features are PURE DOMAIN assertions on snapshot DTOs — they do NOT launch a
 * real browser. No ffmpeg/Chromium is required by any slider BDD shard.
 *
 * Exhaustive, disjoint feature shard (see `CucumberShardCoreRunner` for the rationale).
 * Guarded by `slider.ci.SliderCucumberShardCoverageTest`.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/10_rtl_visual_validation.feature")
@SelectClasspathResource("features/24_slide_content_validation.feature")
@ConfigurationParameter(
    key = FEATURES_PROPERTY_NAME,
    value = "src/test/features/10_rtl_visual_validation.feature,src/test/features/24_slide_content_validation.feature"
)
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "slider.steps")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty, html:build/reports/cucumber-shard-content.html, json:build/reports/cucumber-shard-content.json"
)
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @wip and not @integration")
class CucumberShardContentRunner
