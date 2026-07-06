package slider.i18n

import contracts.i18n.LanguageCatalog
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RevealRtlResolverTest {

    @Test
    fun `resolveRtl should return true for Arabic`() {
        assertThat(RevealRtlResolver.resolveRtl("ar")).isTrue()
    }

    @Test
    fun `resolveRtl should return true for Urdu`() {
        assertThat(RevealRtlResolver.resolveRtl("ur")).isTrue()
    }

    @Test
    fun `resolveRtl should return false for French`() {
        assertThat(RevealRtlResolver.resolveRtl("fr")).isFalse()
    }

    @Test
    fun `resolveRtl should return false for English`() {
        assertThat(RevealRtlResolver.resolveRtl("en")).isFalse()
    }

    @Test
    fun `resolveRtl should return false for every LTR LanguageCatalog entry`() {
        LanguageCatalog.ALL.filterNot { it.rtl }.forEach { lang ->
            assertThat(RevealRtlResolver.resolveRtl(lang.code))
                .withFailMessage("resolveRtl('${lang.code}') should be false for LTR language")
                .isFalse()
        }
    }

    @Test
    fun `resolveRtl should return true for every RTL LanguageCatalog entry`() {
        LanguageCatalog.ALL.filter { it.rtl }.forEach { lang ->
            assertThat(RevealRtlResolver.resolveRtl(lang.code))
                .withFailMessage("resolveRtl('${lang.code}') should be true for RTL language")
                .isTrue()
        }
    }

    @Test
    fun `resolveRtl should return false for unknown language code`() {
        assertThat(RevealRtlResolver.resolveRtl("xx")).isFalse()
    }

    @Test
    fun `rtlLanguages should contain exactly Arabic and Urdu`() {
        val rtl = RevealRtlResolver.rtlLanguages()

        assertThat(rtl).containsExactlyInAnyOrder("ar", "ur")
    }

    @Test
    fun `rtlLanguages size should match LanguageCatalog RTL count`() {
        val expected = LanguageCatalog.ALL.count { it.rtl }

        assertThat(RevealRtlResolver.rtlLanguages()).hasSize(expected)
    }
}