package slider.i18n

import contracts.i18n.LanguageCatalog
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RevealUiMessageCatalogTest {

    @Test
    fun `catalog should expose one RevealUiMessages per LanguageCatalog entry`() {
        val catalog = RevealUiMessageCatalog.all()

        assertThat(catalog).hasSize(LanguageCatalog.ALL.size)
    }

    @Test
    fun `catalog should cover all 10 ISO codes from LanguageCatalog`() {
        val catalog = RevealUiMessageCatalog.all()

        LanguageCatalog.supportedCodes().forEach { code ->
            assertThat(catalog.map { it.languageCode })
                .withFailMessage("Catalog should contain messages for ISO code '$code'")
                .contains(code)
        }
    }

    @Test
    fun `catalog findByCode should return the matching RevealUiMessages`() {
        val messages = RevealUiMessageCatalog.findByCode("fr")

        assertThat(messages).isNotNull
        assertThat(messages!!.languageCode).isEqualTo("fr")
    }

    @Test
    fun `catalog findByCode should return null for unknown code`() {
        assertThat(RevealUiMessageCatalog.findByCode("xx")).isNull()
    }

    @Test
    fun `catalog French messages should use French tooltips for navigation`() {
        val fr = RevealUiMessageCatalog.findByCode("fr")!!

        assertThat(fr.nav.prev).isEqualTo("Diapositive précédente")
        assertThat(fr.nav.next).isEqualTo("Diapositive suivante")
        assertThat(fr.nav.up).isEqualTo("Diapositive parente")
        assertThat(fr.nav.help).isEqualTo("Aide")
    }

    @Test
    fun `catalog English messages should use English tooltips for navigation`() {
        val en = RevealUiMessageCatalog.findByCode("en")!!

        assertThat(en.nav.prev).isEqualTo("Previous slide")
        assertThat(en.nav.next).isEqualTo("Next slide")
        assertThat(en.nav.up).isEqualTo("Up slide")
        assertThat(en.nav.help).isEqualTo("Help")
    }

    @Test
    fun `catalog Arabic messages should be rtl`() {
        val ar = RevealUiMessageCatalog.findByCode("ar")!!

        assertThat(ar.isRtl).isTrue()
        assertThat(ar.nav.prev).isNotBlank()
    }

    @Test
    fun `catalog Urdu messages should be rtl`() {
        val ur = RevealUiMessageCatalog.findByCode("ur")!!

        assertThat(ur.isRtl).isTrue()
    }

    @Test
    fun `catalog all messages should have non blank nav and controls labels`() {
        RevealUiMessageCatalog.all().forEach { messages ->
            assertThat(messages.nav.prev).withFailMessage("prev blank for ${messages.languageCode}").isNotBlank()
            assertThat(messages.nav.next).withFailMessage("next blank for ${messages.languageCode}").isNotBlank()
            assertThat(messages.nav.up).withFailMessage("up blank for ${messages.languageCode}").isNotBlank()
            assertThat(messages.nav.help).withFailMessage("help blank for ${messages.languageCode}").isNotBlank()
            assertThat(messages.controls.overview)
                .withFailMessage("overview blank for ${messages.languageCode}").isNotBlank()
            assertThat(messages.controls.speakerNotes)
                .withFailMessage("speakerNotes blank for ${messages.languageCode}").isNotBlank()
            assertThat(messages.controls.fullscreen)
                .withFailMessage("fullscreen blank for ${messages.languageCode}").isNotBlank()
        }
    }
}