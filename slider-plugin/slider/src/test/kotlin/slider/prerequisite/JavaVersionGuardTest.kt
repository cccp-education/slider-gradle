package slider.prerequisite

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JavaVersionGuardTest {

    @Test
    fun `accepts a Java version strictly above the minimum`() {
        assertEquals(Unit, JavaVersionGuard.requireJava23(24))
        assertEquals(Unit, JavaVersionGuard.requireJava23(25))
    }

    @Test
    fun `accepts a Java version exactly at the minimum`() {
        assertEquals(Unit, JavaVersionGuard.requireJava23(23))
    }

    @Test
    fun `rejects a Java version below the minimum with a clear message`() {
        val ex = assertFailsWith<IllegalArgumentException> { JavaVersionGuard.requireJava23(22) }
        assertEquals(
            "education.cccp.slider requires Java 23+. Current: Java 22",
            ex.message,
        )
    }

    @Test
    fun `rejects a very old Java version with the current value in the message`() {
        val ex = assertFailsWith<IllegalArgumentException> { JavaVersionGuard.requireJava23(17) }
        assertEquals(
            "education.cccp.slider requires Java 23+. Current: Java 17",
            ex.message,
        )
    }

    @Test
    fun `requireJava23FromMajor parses a numeric major version string and accepts it when valid`() {
        assertEquals(Unit, JavaVersionGuard.requireJava23FromMajor("24"))
    }

    @Test
    fun `requireJava23FromMajor rejects a non-numeric major version string`() {
        assertFailsWith<IllegalArgumentException> { JavaVersionGuard.requireJava23FromMajor("foo") }
    }

    @Test
    fun `requireJava23FromMajor rejects a numeric major version below the minimum`() {
        val ex = assertFailsWith<IllegalArgumentException> { JavaVersionGuard.requireJava23FromMajor("21") }
        assertEquals(
            "education.cccp.slider requires Java 23+. Current: Java 21",
            ex.message,
        )
    }

    @Test
    fun `requireJava23FromMajor rejects a blank major version string`() {
        assertFailsWith<IllegalArgumentException> { JavaVersionGuard.requireJava23FromMajor("") }
        assertFailsWith<IllegalArgumentException> { JavaVersionGuard.requireJava23FromMajor("   ") }
    }
}