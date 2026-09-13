package com.navin.kidsdrawing.product.freedraw

import com.navin.kidsdrawing.drawing.domain.DrawingBrushPreset
import com.navin.kidsdrawing.drawing.domain.DrawingTool
import com.navin.kidsdrawing.drawing.domain.DrawingToolSettings
import com.navin.kidsdrawing.product.profile.AgeBand

data class FreeDrawPresentationPolicy(
    val toolColumns: Int,
    val paletteColumns: Int,
    val minimumControlHeightDp: Int,
    val maxControlTrayHeightDp: Int,
    val showToolDescriptions: Boolean,
    val paletteArgb: List<Int>,
)

data class FreeDrawSizeChoice(
    val label: String,
    val width: Float,
)

fun freeDrawPresentationPolicyFor(ageBand: AgeBand): FreeDrawPresentationPolicy = when (ageBand) {
    AgeBand.LITTLE_ARTIST -> FreeDrawPresentationPolicy(
        toolColumns = 2,
        paletteColumns = 4,
        minimumControlHeightDp = 58,
        maxControlTrayHeightDp = 300,
        showToolDescriptions = true,
        paletteArgb = CHILD_SAFE_PALETTE,
    )
    AgeBand.CREATIVE_EXPLORER -> FreeDrawPresentationPolicy(
        toolColumns = 2,
        paletteColumns = 6,
        minimumControlHeightDp = 54,
        maxControlTrayHeightDp = 280,
        showToolDescriptions = true,
        paletteArgb = CHILD_SAFE_PALETTE,
    )
    AgeBand.GROWING_ARTIST -> FreeDrawPresentationPolicy(
        toolColumns = 4,
        paletteColumns = 8,
        minimumControlHeightDp = 48,
        maxControlTrayHeightDp = 240,
        showToolDescriptions = false,
        paletteArgb = CHILD_SAFE_PALETTE,
    )
    AgeBand.YOUNG_ARTIST -> FreeDrawPresentationPolicy(
        toolColumns = 4,
        paletteColumns = 8,
        minimumControlHeightDp = 44,
        maxControlTrayHeightDp = 220,
        showToolDescriptions = false,
        paletteArgb = CHILD_SAFE_PALETTE,
    )
}

fun freeDrawSizeChoices(
    tool: DrawingTool,
    preset: DrawingBrushPreset,
): List<FreeDrawSizeChoice> {
    val center = if (tool == DrawingTool.ERASER) {
        DrawingToolSettings.DEFAULT_ERASER_WIDTH
    } else {
        preset.defaultWidth
    }
    return listOf(
        FreeDrawSizeChoice("Small", (center * 0.65f).coerceAtLeast(DrawingToolSettings.MIN_TOOL_WIDTH)),
        FreeDrawSizeChoice("Medium", center),
        FreeDrawSizeChoice("Large", (center * 1.55f).coerceAtMost(DrawingToolSettings.MAX_TOOL_WIDTH)),
    )
}

val CHILD_SAFE_PALETTE: List<Int> = listOf(
    0xFF242321.toInt(), // charcoal
    0xFFEF6C68.toInt(), // coral
    0xFFF2A93B.toInt(), // warm yellow
    0xFF5C8D63.toInt(), // leaf green
    0xFF3D7CC9.toInt(), // blue
    0xFF6B5AA6.toInt(), // violet
    0xFFB65B8A.toInt(), // berry
    0xFF8A5C3B.toInt(), // warm brown
)
