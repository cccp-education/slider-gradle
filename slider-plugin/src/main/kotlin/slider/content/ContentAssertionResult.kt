package slider.content

/**
 * Result of evaluating [ContentSlideAssertion] against a [SlideContent] and
 * a [SlideLayout]. Sealed hierarchy: either [Passed] (all assertions green)
 * or [Failed] with a list of [ContentAssertionFailure].
 *
 * Pure value object — no Gradle, no Playwright, no I/O. Pattern aligned on
 * [slider.rtl.RtlAssertionResult] but sealed for exhaustive `when` matching.
 */
sealed interface ContentAssertionResult {

    /**
     * All assertions passed.
     */
    data object Passed : ContentAssertionResult

    /**
     * One or more assertions failed.
     *
     * @param failures non-empty list of [ContentAssertionFailure].
     */
    data class Failed(val failures: List<ContentAssertionFailure>) : ContentAssertionResult {

        init {
            require(failures.isNotEmpty()) { "ContentAssertionResult.Failed requires at least one failure" }
        }

        /** Codes of all failures, in order. */
        fun failureCodes(): List<ContentAssertionCode> = failures.map { it.code }
    }
}

/**
 * A single content assertion failure.
 *
 * @param code     the [ContentAssertionCode] that failed.
 * @param message  human-readable message including the code name.
 * @param slideRef the title of the slide the failure applies to.
 */
data class ContentAssertionFailure(
    val code: ContentAssertionCode,
    val message: String,
    val slideRef: String,
)