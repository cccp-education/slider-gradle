package slider.i18n

import contracts.i18n.LanguageCatalog

/**
 * Resolves the Right-To-Left flag for a Reveal.js deck from its ISO 639-1
 * language code.
 *
 * Reveal.js supports RTL via the `rtl` config option (mapped by
 * asciidoctor-revealjs to `revealjs_rightToLeft`). Only Arabic (ar) and
 * Urdu (ur) are RTL among the 10 supported languages. Unknown codes
 * default to LTR (false).
 *
 * Consumed by [slider.SliderManager.Tasks] to wire
 * `RevealJSOptions.setRightToLeft` from the resolved deck language.
 */
object RevealRtlResolver {

    /** Returns true when [languageCode] is a RTL language (Arabic, Urdu), false otherwise. */
    fun resolveRtl(languageCode: String): Boolean =
        LanguageCatalog.findByCode(languageCode)?.rtl == true

    /** Returns the set of ISO codes that require RTL layout. */
    fun rtlLanguages(): Set<String> = LanguageCatalog.ALL
        .filter { it.rtl }
        .map { it.code }
        .toSet()
}