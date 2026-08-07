package slider.i18n

import contracts.i18n.I18nConfig
import contracts.i18n.LanguageCatalog

object I18nConfigResolver {

    fun resolve(
        cliProps: Map<String, String>,
        gradleProperties: Map<String, String>,
        dslLanguage: String,
        dslSupportedLanguages: List<String>,
        yamlLanguageCode: String?,
        defaultLanguage: String = "fr",
    ): I18nConfig {
        val active = resolveActiveLanguage(cliProps, gradleProperties, dslLanguage, yamlLanguageCode, defaultLanguage)
        val supported = resolveSupportedLanguages(cliProps, gradleProperties, dslSupportedLanguages, active)
        return I18nConfig(
            activeLanguage = active,
            supportedLanguages = supported,
            fallbackLanguage = defaultLanguage,
        )
    }

    private fun resolveActiveLanguage(
        cliProps: Map<String, String>,
        gradleProperties: Map<String, String>,
        dslLanguage: String,
        yamlLanguageCode: String?,
        defaultLanguage: String,
    ): String {
        val supported = LanguageCatalog.supportedCodes()
        val candidates = listOf(
            cliProps["language"],
            gradleProperties["language"],
            dslLanguage.takeIf { it.isNotBlank() },
            yamlLanguageCode,
        )
        return candidates.firstOrNull { c -> c != null && c in supported } ?: defaultLanguage
    }

    private fun resolveSupportedLanguages(
        cliProps: Map<String, String>,
        gradleProperties: Map<String, String>,
        dslSupportedLanguages: List<String>,
        active: String,
    ): List<String> {
        val supported = LanguageCatalog.supportedCodes()
        val cliList = cliProps["supportedLanguages"]
            ?.split(",")
            ?.map { it.trim().lowercase() }
            ?.filter { it in supported }
        if (!cliList.isNullOrEmpty()) return cliList
        val propsList = gradleProperties["supportedLanguages"]
            ?.split(",")
            ?.map { it.trim().lowercase() }
            ?.filter { it in supported }
        if (!propsList.isNullOrEmpty()) return propsList
        if (dslSupportedLanguages.isNotEmpty()) {
            return dslSupportedLanguages.map { it.lowercase() }.filter { it in supported }
        }
        return listOf(active)
    }
}