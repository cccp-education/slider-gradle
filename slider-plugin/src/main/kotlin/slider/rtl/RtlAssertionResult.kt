package slider.rtl

/**
 * Result of evaluating [RtlSlideAssertion] against a [SlideRenderData].
 *
 * [passed] is true only when [failures] is empty. Each [RtlAssertionFailure]
 * carries a [RtlAssertionCode] and a human-readable message that includes
 * the code name for traceability.
 */
data class RtlAssertionResult(
    val passed: Boolean,
    val failures: List<RtlAssertionFailure>,
) {
    fun failureCodes(): List<RtlAssertionCode> = failures.map { it.code }

    companion object {
        fun success(): RtlAssertionResult = RtlAssertionResult(true, emptyList())

        fun of(failures: List<RtlAssertionFailure>): RtlAssertionResult =
            RtlAssertionResult(failures.isEmpty(), failures)
    }
}

data class RtlAssertionFailure(
    val code: RtlAssertionCode,
    val message: String,
)