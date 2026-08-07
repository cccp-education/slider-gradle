package slider.i18n

import java.io.File

/**
 * Writes [RevealUiMessages] as `messages_{code}.js` files consumable by
 * the Reveal.js i18n plugin.
 *
 * Each file declares a `RevealI18n.messages["{code}"] = { ... }` assignment
 * exposing the navigation and control labels for that language. The RTL
 * flag is only emitted when `true` (Arabic, Urdu) so LTR languages stay
 * implicit and the output stays minimal.
 *
 * The generated files follow the Reveal.js i18n plugin convention:
 * they are loaded as `<script>` tags in the deck HTML and consumed by
 * the `lang` and `i18n` Reveal.js config options.
 */
object RevealUiMessagesWriter {

    /** Writes one `messages_{code}.js` file per [RevealUiMessageCatalog] entry into [outputDir]. */
    fun writeAll(outputDir: File): List<File> =
        RevealUiMessageCatalog.all().map { messages -> write(messages, outputDir) }

    /** Writes a single `messages_{code}.js` file for [messages] into [outputDir]. */
    fun write(messages: RevealUiMessages, outputDir: File): File {
        outputDir.mkdirs()
        val file = outputDir.resolve("messages_${messages.languageCode}.js")
        file.writeText(render(messages))
        return file
    }

    private fun render(messages: RevealUiMessages): String = buildString {
        append("RevealI18n.messages[\"${messages.languageCode}\"] = {")
        append("\n  nav: {")
        append("\n    prev: ").append(jsString(messages.nav.prev)).append(",")
        append("\n    next: ").append(jsString(messages.nav.next)).append(",")
        append("\n    up: ").append(jsString(messages.nav.up)).append(",")
        append("\n    help: ").append(jsString(messages.nav.help))
        append("\n  },")
        append("\n  controls: {")
        append("\n    overview: ").append(jsString(messages.controls.overview)).append(",")
        append("\n    speakerNotes: ").append(jsString(messages.controls.speakerNotes)).append(",")
        append("\n    fullscreen: ").append(jsString(messages.controls.fullscreen))
        append("\n  }")
        if (messages.isRtl) append(",\n  rtl: true")
        append("\n};\n")
    }

    /** Quotes a string for a JavaScript string literal with basic escaping. */
    private fun jsString(value: String): String =
        "\"" + value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n") + "\""
}