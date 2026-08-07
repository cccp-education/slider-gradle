package slider.scaffold

/**
 * Outcome of a scaffolding operation.
 *
 * Sealed type — the caller branches on the variant:
 * - [Created] — the file/directory was created from defaults
 * - [Skipped] — the target already exists, no-op
 * - [Failed] — the operation could not complete, [reason] explains why
 */
sealed class ScaffoldResult {
    data class Created(val path: String) : ScaffoldResult()
    data class Skipped(val path: String) : ScaffoldResult()
    data class Failed(val reason: String) : ScaffoldResult()
}