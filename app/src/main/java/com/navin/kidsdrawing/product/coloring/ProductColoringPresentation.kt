package com.navin.kidsdrawing.product.coloring

import com.navin.kidsdrawing.coloring.regions.GuidedColoringProgress
import com.navin.kidsdrawing.coloring.session.ColoringSessionMode

data class ColoringCompanionPresentation(
    val eyebrow: String,
    val instruction: String,
    val stepLabel: String? = null,
)

/** Child-facing coloring copy derived only from the session mode and authored coloring progress. */
object ProductColoringPresentationPolicy {
    fun from(
        mode: ColoringSessionMode,
        progress: GuidedColoringProgress?,
        preparedFillAvailable: Boolean,
    ): ColoringCompanionPresentation = when (mode) {
        ColoringSessionMode.COLOR_MYSELF -> ColoringCompanionPresentation(
            eyebrow = "YOUR COLORS",
            instruction = if (preparedFillAvailable) {
                "Pick a color and use Fill for prepared areas, or Brush to color freely. Your drawing lines stay safe."
            } else {
                "Your colors, your way. The drawing lines stay safe while you experiment."
            },
        )

        ColoringSessionMode.COLOR_WITH_ME -> guided(progress)
    }

    private fun guided(progress: GuidedColoringProgress?): ColoringCompanionPresentation {
        if (progress == null || progress.totalSteps == 0) {
            return ColoringCompanionPresentation(
                eyebrow = "COLOR WITH ME",
                instruction = "We’ll add color one part at a time. Take your time.",
            )
        }
        if (progress.isComplete) {
            return ColoringCompanionPresentation(
                eyebrow = "COLORING COMPLETE",
                instruction = "All coloring steps are done. Add any finishing touches you want, then save your artwork.",
                stepLabel = "${progress.totalSteps} of ${progress.totalSteps}",
            )
        }

        val index = progress.currentStepIndex ?: 0
        val step = progress.currentStep
        val label = step?.id?.toChildLabel()
        val instruction = if (step?.regionIds?.isNotEmpty() == true) {
            "Choose a color, select Fill, then tap inside this part."
        } else {
            "Use Brush to add color to this part. Your lines can be loose and playful."
        }
        return ColoringCompanionPresentation(
            eyebrow = "COLOR WITH ME",
            instruction = instruction,
            stepLabel = buildString {
                append("Step ${index + 1} of ${progress.totalSteps}")
                if (!label.isNullOrBlank()) append(" · $label")
            },
        )
    }
}

internal fun String.toChildLabel(): String = split('_', '-', '.')
    .filter(String::isNotBlank)
    .joinToString(" ") { token -> token.replaceFirstChar { it.uppercaseChar() } }
