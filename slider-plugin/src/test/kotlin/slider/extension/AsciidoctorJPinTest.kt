package slider.extension

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AsciidoctorJPinTest {

    @Test
    fun `the default pin targets AsciidoctorJ 3_0_1`() {
        assertEquals("3.0.1", AsciidoctorJPin().version)
    }

    @Test
    fun `a blank version is rejected`() {
        assertFailsWith<IllegalArgumentException> { AsciidoctorJPin(version = "") }
        assertFailsWith<IllegalArgumentException> { AsciidoctorJPin(version = "   ") }
    }

    @Test
    fun `a custom pin preserves the supplied version`() {
        assertEquals("9.9.9", AsciidoctorJPin(version = "9.9.9").version)
    }
}
