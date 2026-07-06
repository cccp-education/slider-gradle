package slider.scenarios

import contracts.i18n.I18nValidationResult
import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.i18n.I18nConfigResolver

class I18nFallbackSteps : En {

    private var resolvedActiveLanguage: String = ""
    private var resolvedFallbackLanguage: String = ""
    private var resolvedSupportedLanguages: List<String> = emptyList()
    private var validationResult: I18nValidationResult? = null

    init {

        When("the i18n config is resolved with default language {string}") { default: String ->
            val resolved = I18nConfigResolver.resolve(
                cliProps = emptyMap(),
                gradleProperties = emptyMap(),
                dslLanguage = "",
                dslSupportedLanguages = emptyList(),
                yamlLanguageCode = null,
                defaultLanguage = default,
            )
            resolvedFallbackLanguage = resolved.fallbackLanguage
            resolvedActiveLanguage = resolved.activeLanguage
        }

        When("the i18n config is resolved with CLI language {string} and default {string}") { cli: String, default: String ->
            val resolved = I18nConfigResolver.resolve(
                cliProps = mapOf("language" to cli),
                gradleProperties = emptyMap(),
                dslLanguage = "",
                dslSupportedLanguages = emptyList(),
                yamlLanguageCode = null,
                defaultLanguage = default,
            )
            resolvedActiveLanguage = resolved.activeLanguage
            resolvedFallbackLanguage = resolved.fallbackLanguage
            validationResult = resolved.validate()
        }

        When("the i18n config is resolved with empty CLI language and gradle properties language {string}") { gradleProp: String ->
            val resolved = I18nConfigResolver.resolve(
                cliProps = emptyMap(),
                gradleProperties = mapOf("language" to gradleProp),
                dslLanguage = "",
                dslSupportedLanguages = emptyList(),
                yamlLanguageCode = null,
                defaultLanguage = "fr",
            )
            resolvedActiveLanguage = resolved.activeLanguage
        }

        When("the i18n config is resolved with CLI language {string} and empty supported languages") { cli: String ->
            val resolved = I18nConfigResolver.resolve(
                cliProps = mapOf("language" to cli),
                gradleProperties = emptyMap(),
                dslLanguage = "",
                dslSupportedLanguages = emptyList(),
                yamlLanguageCode = null,
                defaultLanguage = "fr",
            )
            resolvedSupportedLanguages = resolved.supportedLanguages
        }

        Then("the fallback language should be {string}") { expected: String ->
            assertThat(resolvedFallbackLanguage).isEqualTo(expected)
        }

        Then("the active language should fall back to {string}") { expected: String ->
            assertThat(resolvedActiveLanguage).isEqualTo(expected)
        }

        Then("the active language should be {string}") { expected: String ->
            assertThat(resolvedActiveLanguage).isEqualTo(expected)
        }

        Then("the supported languages should contain only {string}") { expected: String ->
            assertThat(resolvedSupportedLanguages).containsExactly(expected)
        }

        Then("the config should validate successfully") {
            assertThat(validationResult).isInstanceOf(I18nValidationResult.Valid::class.java)
        }
    }
}