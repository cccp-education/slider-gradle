package slider.i18n

import contracts.i18n.LanguageCatalog

/**
 * Immutable UI messages for a Reveal.js presentation in a given language.
 *
 * Covers navigation controls (prev/next/up/help tooltips) and control bar
 * buttons (overview, speaker notes, fullscreen). Each [RevealUiMessages]
 * is bound to a single ISO 639-1 language code and exposes the [isRtl]
 * flag derived from [LanguageCatalog].
 *
 * Used by [RevealUiMessageCatalog] to provide a message bundle for each
 * of the 10 supported languages, then written to `messages_{code}.js`
 * by [RevealUiMessagesWriter] for the Reveal.js i18n plugin.
 */
data class RevealUiMessages(
    val languageCode: String,
    val nav: RevealUiNavMessages,
    val controls: RevealUiControlsMessages,
) {
    /** RTL flag derived from [LanguageCatalog] — true for Arabic and Urdu. */
    val isRtl: Boolean
        get() = LanguageCatalog.findByCode(languageCode)?.rtl == true
}

/** Navigation tooltips shown by Reveal.js on hover (prev/next/up/help). */
data class RevealUiNavMessages(
    val prev: String,
    val next: String,
    val up: String,
    val help: String,
)

/** Control bar button labels (overview, speaker notes, fullscreen). */
data class RevealUiControlsMessages(
    val overview: String,
    val speakerNotes: String,
    val fullscreen: String,
)