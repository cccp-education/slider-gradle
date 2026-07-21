package slider.prerequisite

/**
 * Guards the build against unsupported Java versions.
 *
 * The slider plugin requires Java 23+ due to asciidoctor-gradle OUT_OF_PROCESS
 * behaviour and Gradle 9 compatibility requirements.
 *
 * Pure domain logic — no Gradle, no JVM introspection. The caller is responsible
 * for providing the current major version (e.g. via
 * `org.gradle.api.JavaVersion.current().majorVersion`).
 */
object JavaVersionGuard {

    /** Minimum supported Java major version. */
    private const val MINIMUM_MAJOR: Int = 23

    /**
     * Fails fast with a clear message if the supplied [currentMajor] is below 23.
     *
     * @throws IllegalArgumentException when [currentMajor] is less than 23.
     */
    fun requireJava23(currentMajor: Int) {
        require(currentMajor >= MINIMUM_MAJOR) {
            "education.cccp.slider requires Java 23+. Current: Java $currentMajor"
        }
    }

    /**
     * Parses [majorVersionString] as an integer and delegates to [requireJava23].
     *
     * Useful when the version comes from `JavaVersion.current().majorVersion`
     * (a String) without leaking Gradle types into the domain.
     *
     * @throws IllegalArgumentException when the string is blank, non-numeric,
     *   or denotes a Java version below 23.
     */
    fun requireJava23FromMajor(majorVersionString: String) {
        require(majorVersionString.isNotBlank()) {
            "education.cccp.slider requires Java 23+. Current: Java <unknown>"
        }
        val parsed = majorVersionString.trim().toIntOrNull()
            ?: throw IllegalArgumentException(
                "education.cccp.slider requires Java 23+. Current: Java $majorVersionString",
            )
        requireJava23(parsed)
    }
}