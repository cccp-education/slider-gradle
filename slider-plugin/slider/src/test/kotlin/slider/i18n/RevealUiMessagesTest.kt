package slider.i18n

import contracts.i18n.LanguageCatalog
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RevealUiMessagesTest {

    @Test
    fun `RevealUiMessages should expose the language ISO code`() {
        val messages = RevealUiMessages(
            languageCode = "fr",
            nav = RevealUiNavMessages(
                prev = "Diapositive précédente",
                next = "Diapositive suivante",
                up = "Diapositive parente",
                help = "Aide",
            ),
            controls = RevealUiControlsMessages(
                overview = "Vue d'ensemble",
                speakerNotes = "Notes du présentateur",
                fullscreen = "Plein écran",
            ),
        )

        assertThat(messages.languageCode).isEqualTo("fr")
    }

    @Test
    fun `RevealUiMessages for Arabic should expose the ISO code and rtl flag`() {
        val messages = RevealUiMessages(
            languageCode = "ar",
            nav = RevealUiNavMessages(
                prev = "الشريحة السابقة",
                next = "الشريحة التالية",
                up = "الشريحة الأصل",
                help = "مساعدة",
            ),
            controls = RevealUiControlsMessages(
                overview = "نظرة عامة",
                speakerNotes = "ملاحظات المتحدث",
                fullscreen = "ملء الشاشة",
            ),
        )

        assertThat(messages.languageCode).isEqualTo("ar")
        assertThat(messages.isRtl).isTrue()
    }

    @Test
    fun `RevealUiMessages for French should not be rtl`() {
        val messages = RevealUiMessages(
            languageCode = "fr",
            nav = RevealUiNavMessages(
                prev = "Diapositive précédente",
                next = "Diapositive suivante",
                up = "Diapositive parente",
                help = "Aide",
            ),
            controls = RevealUiControlsMessages(
                overview = "Vue d'ensemble",
                speakerNotes = "Notes du présentateur",
                fullscreen = "Plein écran",
            ),
        )

        assertThat(messages.isRtl).isFalse()
    }

    @Test
    fun `RevealUiMessages isRtl should derive from LanguageCatalog`() {
        LanguageCatalog.ALL.forEach { lang ->
            val messages = RevealUiMessages(
                languageCode = lang.code,
                nav = RevealUiNavMessages(
                    prev = "prev",
                    next = "next",
                    up = "up",
                    help = "help",
                ),
                controls = RevealUiControlsMessages(
                    overview = "overview",
                    speakerNotes = "speaker notes",
                    fullscreen = "fullscreen",
                ),
            )

            assertThat(messages.isRtl)
                .withFailMessage("rtl flag for '${lang.code}' should match LanguageCatalog")
                .isEqualTo(lang.rtl)
        }
    }
}