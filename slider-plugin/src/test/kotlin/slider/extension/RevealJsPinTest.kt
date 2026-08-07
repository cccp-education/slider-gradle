package slider.extension

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RevealJsPinTest {

    @Test
    fun `the default pin targets revealjs 5_2_1`() {
        val pin = RevealJsPin()
        assertEquals("5.2.1", pin.tag)
    }

    @Test
    fun `the default pin points to the hakimel organisation`() {
        assertEquals("hakimel", RevealJsPin().organisation)
    }

    @Test
    fun `the default pin points to the reveal dot js repository`() {
        assertEquals("reveal.js", RevealJsPin().repository)
    }

    @Test
    fun `the default pin sets the revealjs version to 5_2_0`() {
        assertEquals("5.2.0", RevealJsPin().version)
    }

    @Test
    fun `a blank version is rejected`() {
        assertFailsWith<IllegalArgumentException> { RevealJsPin(version = "") }
        assertFailsWith<IllegalArgumentException> { RevealJsPin(version = "   ") }
    }

    @Test
    fun `a blank tag is rejected`() {
        assertFailsWith<IllegalArgumentException> { RevealJsPin(tag = "") }
    }

    @Test
    fun `a blank organisation is rejected`() {
        assertFailsWith<IllegalArgumentException> { RevealJsPin(organisation = "") }
    }

    @Test
    fun `a blank repository is rejected`() {
        assertFailsWith<IllegalArgumentException> { RevealJsPin(repository = "") }
    }

    @Test
    fun `a custom pin preserves all supplied values`() {
        val pin = RevealJsPin(
            version = "6.0.0",
            organisation = "custom-org",
            repository = "custom-repo",
            tag = "6.0.0",
        )
        assertEquals("6.0.0", pin.version)
        assertEquals("custom-org", pin.organisation)
        assertEquals("custom-repo", pin.repository)
        assertEquals("6.0.0", pin.tag)
    }
}