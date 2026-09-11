package com.navin.kidsdrawing.drawing.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DrawingTool {
    PENCIL,
    ERASER,
}

data class DrawingToolSettings(
    val tool: DrawingTool = DrawingTool.PENCIL,
    val colorArgb: Int = DEFAULT_PENCIL_COLOR_ARGB,
    val width: Float = DEFAULT_PENCIL_WIDTH,
) {
    init {
        require(width.isFinite() && width in MIN_TOOL_WIDTH..MAX_TOOL_WIDTH) {
            "Drawing tool width must be finite and within $MIN_TOOL_WIDTH..$MAX_TOOL_WIDTH."
        }
    }

    companion object {
        const val DEFAULT_PENCIL_COLOR_ARGB: Int = -0xDBDCDF // 0xFF242321
        const val DEFAULT_PENCIL_WIDTH: Float = 10f
        const val DEFAULT_ERASER_WIDTH: Float = 28f
        const val MIN_TOOL_WIDTH: Float = 2f
        const val MAX_TOOL_WIDTH: Float = 80f
    }
}

/**
 * Product-owned tool state. Feature UI observes this engine and sends commands; it never owns the
 * authoritative tool/color/width values used by the low-level drawing surface.
 */
class DrawingToolEngine(
    initialSettings: DrawingToolSettings = DrawingToolSettings(),
) {
    private val _state = MutableStateFlow(initialSettings)
    val state: StateFlow<DrawingToolSettings> = _state.asStateFlow()

    fun selectTool(tool: DrawingTool) {
        val current = _state.value
        val nextWidth = when {
            tool == current.tool -> current.width
            tool == DrawingTool.ERASER && current.width < 16f -> DrawingToolSettings.DEFAULT_ERASER_WIDTH
            tool == DrawingTool.PENCIL && current.width > 40f -> DrawingToolSettings.DEFAULT_PENCIL_WIDTH
            else -> current.width
        }
        _state.value = current.copy(tool = tool, width = nextWidth)
    }

    fun setColor(colorArgb: Int) {
        _state.value = _state.value.copy(colorArgb = colorArgb)
    }

    fun setWidth(width: Float) {
        _state.value = _state.value.copy(
            width = width.coerceIn(DrawingToolSettings.MIN_TOOL_WIDTH, DrawingToolSettings.MAX_TOOL_WIDTH),
        )
    }

    fun replace(settings: DrawingToolSettings) {
        _state.value = settings
    }
}
