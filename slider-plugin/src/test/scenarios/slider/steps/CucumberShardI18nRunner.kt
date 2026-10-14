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
 * CI shard — i18n (translator, reveal UI, RTL layout, fallback, assistant manager,
 * slider messages).
 *
 * Exhaustive, disjoint feature shard (see `CucumberShardCoreRunner` for the rationale).
 * Guarded by `slider.ci.SliderCucumberShardCoverageTest`.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/5_translator_i18n.feature")
@SelectClasspathResource("features/6_reveal_ui_i18n.feature")
@SelectClasspathResource("features/7_rtl_layout.feature")
@SelectClasspathResource("features/8_i18n_fallback.feature")
@SelectClasspathResource("features/9_assistant_manager_i18n.feature")
@SelectClasspathResource("features/25_slider_messages_resolution.feature")
@ConfigurationParameter(
    key = FEATURES_PROPERTY_NAME,
    value = "src/test/features/5_translator_i18n.feature,src/test/features/6_reveal_ui_i18n.feature," +
        "src/test/features/7_rtl_layout.feature,src/test/features/8_i18n_fallback.feature," +
        "src/test/features/9_assistant_manager_i18n.feature,src/test/features/25_slider_messages_resolution.feature"
)
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "slider.steps")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty, html:build/reports/cucumber-shard-i18n.html, json:build/reports/cucumber-shard-i18n.json"
)
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @wip and not @integration")
class CucumberShardI18nRunner
