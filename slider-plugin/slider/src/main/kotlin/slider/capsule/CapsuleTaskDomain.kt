package slider.capsule

import java.io.File

/**
 * Value object describing the AsciiDoc deck source directory scanned by the
 * `generateCapsule` task (`<projectDir>/slides/misc`).
 *
 * Pure data — no Gradle dependency. The Gradle adapter [CapsuleTaskRegistrar]
 * uses this value object to resolve the directory and list the `.adoc` files
 * without re-hardcoding the path.
 */
data class CapsuleAdocDir(val projectDir: File) {
    init {
        require(projectDir.path.isNotBlank()) { "projectDir must not be blank" }
    }

    /**
     * Resolves the concrete `<projectDir>/slides/misc` directory file.
     */
    fun asFile(): File = projectDir.resolve("slides").resolve("misc")

    /**
     * Lists the `.adoc` files directly contained in this directory, sorted by
     * name. Returns an empty list when the directory does not exist or holds
     * no `.adoc` file. Subdirectories are not traversed.
     */
    fun adocFiles(): List<File> {
        val dir = asFile()
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        return dir.listFiles { file -> file.isFile && file.extension == "adoc" }
            ?.sortedBy { it.name }
            ?: emptyList()
    }
}

/**
 * Value object describing the capsule script output directory
 * (`<buildDir>/capsule`).
 *
 * Pure data — no Gradle dependency. The Gradle adapter [CapsuleTaskRegistrar]
 * calls [ensureCreated] before writing each script file resolved by
 * [scriptFileFor].
 */
data class CapsuleScriptDir(val buildDir: File) {
    init {
        require(buildDir.path.isNotBlank()) { "buildDir must not be blank" }
    }

    /**
     * Resolves the concrete `<buildDir>/capsule` directory file.
     */
    fun asFile(): File = buildDir.resolve("capsule")

    /**
     * Creates the capsule directory if it does not exist yet, idempotently.
     */
    fun ensureCreated(): File = asFile().apply { mkdirs() }

    /**
     * Resolves the script output file for the given [deckName] (without
     * extension): `<buildDir>/capsule/<deckName>-script.txt`.
     */
    fun scriptFileFor(deckName: String): File =
        ensureCreated().resolve("$deckName-script.txt")
}

/**
 * Stable task identifiers and metadata owned by the `slider.capsule` domain.
 *
 * Kept here so the Gradle adapter [CapsuleTaskRegistrar] no longer re-declares
 * the task name, group and description inline.
 */
object CapsuleTaskNames {
    const val GENERATE_CAPSULE = "generateCapsule"
    const val GROUP = "slider"
    const val DESCRIPTION =
        "Extract speaker notes from AsciiDoc decks and generate a capsule script (consumed by capsule-gradle)."
}