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
 * CI shard — repository domain (deployment request, git push adapter, publish task).
 *
 * Exhaustive, disjoint feature shard (see `CucumberShardCoreRunner` for the rationale).
 * Guarded by `slider.ci.SliderCucumberShardCoverageTest`.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/11_deployment_request.feature")
@SelectClasspathResource("features/12_git_push_adapter.feature")
@SelectClasspathResource("features/20_publish_task_decomposition.feature")
@ConfigurationParameter(
    key = FEATURES_PROPERTY_NAME,
    value = "src/test/features/11_deployment_request.feature,src/test/features/12_git_push_adapter.feature," +
        "src/test/features/20_publish_task_decomposition.feature"
)
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "slider.steps")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty, html:build/reports/cucumber-shard-repository.html, json:build/reports/cucumber-shard-repository.json"
)
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @wip and not @integration")
class CucumberShardRepositoryRunner
