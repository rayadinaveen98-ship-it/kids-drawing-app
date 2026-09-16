package com.navin.kidsdrawing.product.lesson

/**
 * Pure product boundary for post-drawing actions.
 *
 * Coloring is a lesson-authored capability. A child must never see a coloring action and discover
 * only after tapping it that the selected lesson did not author coloring content.
 */
data class PostDrawingCapabilityPolicy(
    val showColoringChoices: Boolean,
    val guidanceCopy: String,
)

fun postDrawingCapabilityPolicy(coloringAvailable: Boolean): PostDrawingCapabilityPolicy =
    if (coloringAvailable) {
        PostDrawingCapabilityPolicy(
            showColoringChoices = true,
            guidanceCopy = "Choose what happens next: add color now, or save this drawing for later.",
        )
    } else {
        PostDrawingCapabilityPolicy(
            showColoringChoices = false,
            guidanceCopy = "Your drawing is ready. You can save it and come back to the studio.",
        )
    }
