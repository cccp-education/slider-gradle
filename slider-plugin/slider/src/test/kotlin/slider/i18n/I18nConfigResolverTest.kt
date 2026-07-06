package slider.i18n

import contracts.i18n.I18nConfig
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class I18nConfigResolverTest {

    @Test
    fun `cascade layer 1 CLI -Planguage wins over gradle properties DSL YAML and default`() {
        val resolved = I18nConfigResolver.resolve(
            cliProps = mapOf("language" to "en"),
            gradleProperties = mapOf("language" to "es"),
            dslLanguage = "ar",
            dslSupportedLanguages = listOf("fr", "en"),
            yamlLanguageCode = "zh",
            defaultLanguage = "fr",
        )
        assertThat(resolved.activeLanguage).isEqualTo("en")
    }

    @Test
    fun `cascade layer 2 gradle properties wins over DSL YAML and default`() {
        val resolved = I18nConfigResolver.resolve(
            cliProps = emptyMap(),
            gradleProperties = mapOf("language" to "es"),
            dslLanguage = "ar",
            dslSupportedLanguages = listOf("fr", "en"),
            yamlLanguageCode = "zh",
            defaultLanguage = "fr",
        )
        assertThat(resolved.activeLanguage).isEqualTo("es")
    }

    @Test
    fun `cascade layer 3 DSL wins over YAML and default`() {
        val resolved = I18nConfigResolver.resolve(
            cliProps = emptyMap(),
            gradleProperties = emptyMap(),
            dslLanguage = "ar",
            dslSupportedLanguages = listOf("fr", "en", "ar"),
            yamlLanguageCode = "zh",
            defaultLanguage = "fr",
        )
        assertThat(resolved.activeLanguage).isEqualTo("ar")
    }

    @Test
    fun `cascade layer 4 YAML languageCode wins over default`() {
        val resolved = I18nConfigResolver.resolve(
            cliProps = emptyMap(),
            gradleProperties = emptyMap(),
            dslLanguage = "",
            dslSupportedLanguages = emptyList(),
            yamlLanguageCode = "zh",
            defaultLanguage = "fr",
        )
        assertThat(resolved.activeLanguage).isEqualTo("zh")
    }

    @Test
    fun `cascade layer 5 default fr when nothing is set`() {
        val resolved = I18nConfigResolver.resolve(
            cliProps = emptyMap(),
            gradleProperties = emptyMap(),
            dslLanguage = "",
            dslSupportedLanguages = emptyList(),
            yamlLanguageCode = null,
            defaultLanguage = "fr",
        )
        assertThat(resolved.activeLanguage).isEqualTo("fr")
    }

    @Test
    fun `resolved I18nConfig should validate successfully for supported code`() {
        val resolved = I18nConfigResolver.resolve(
            cliProps = mapOf("language" to "en"),
            gradleProperties = emptyMap(),
            dslLanguage = "",
            dslSupportedLanguages = listOf("fr", "en"),
            yamlLanguageCode = null,
            defaultLanguage = "fr",
        )
        val result = resolved.validate()
        assertThat(result).isInstanceOf(contracts.i18n.I18nValidationResult.Valid::class.java)
    }

    @Test
    fun `resolved I18nConfig supportedLanguages should fall back to activeLanguage singleton when empty`() {
        val resolved = I18nConfigResolver.resolve(
            cliProps = mapOf("language" to "en"),
            gradleProperties = emptyMap(),
            dslLanguage = "",
            dslSupportedLanguages = emptyList(),
            yamlLanguageCode = null,
            defaultLanguage = "fr",
        )
        assertThat(resolved.supportedLanguages).containsExactly("en")
    }

    @Test
    fun `invalid CLI language code should fall back to default`() {
        val resolved = I18nConfigResolver.resolve(
            cliProps = mapOf("language" to "xx"),
            gradleProperties = emptyMap(),
            dslLanguage = "",
            dslSupportedLanguages = emptyList(),
            yamlLanguageCode = null,
            defaultLanguage = "fr",
        )
        assertThat(resolved.activeLanguage).isEqualTo("fr")
    }

    @Test
    fun `fallback language should always be default`() {
        val resolved = I18nConfigResolver.resolve(
            cliProps = mapOf("language" to "en"),
            gradleProperties = emptyMap(),
            dslLanguage = "",
            dslSupportedLanguages = listOf("fr", "en"),
            yamlLanguageCode = null,
            defaultLanguage = "fr",
        )
        assertThat(resolved.fallbackLanguage).isEqualTo("fr")
    }
}