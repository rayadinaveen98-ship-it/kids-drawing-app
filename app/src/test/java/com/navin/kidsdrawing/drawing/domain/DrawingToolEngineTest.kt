package com.navin.kidsdrawing.drawing.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DrawingToolEngineTest {
    @Test
    fun uiCommandsUpdateOneAuthoritativeToolState() {
        val engine = DrawingToolEngine()

        engine.selectTool(DrawingTool.ERASER)
        assertEquals(DrawingTool.ERASER, engine.state.value.tool)
        assertEquals(DrawingToolSettings.DEFAULT_ERASER_WIDTH, engine.state.value.width, 0f)

        engine.setColor(0xFF1565C0.toInt())
        engine.setWidth(34f)
        assertEquals(0xFF1565C0.toInt(), engine.state.value.colorArgb)
        assertEquals(34f, engine.state.value.width, 0f)

        engine.selectTool(DrawingTool.PENCIL)
        assertEquals(DrawingTool.PENCIL, engine.state.value.tool)
        assertEquals(34f, engine.state.value.width, 0f)
    }

    @Test
    fun widthCommandsAreClampedToTechnicalBounds() {
        val engine = DrawingToolEngine()

        engine.setWidth(-100f)
        assertEquals(DrawingToolSettings.MIN_TOOL_WIDTH, engine.state.value.width, 0f)

        engine.setWidth(500f)
        assertEquals(DrawingToolSettings.MAX_TOOL_WIDTH, engine.state.value.width, 0f)
    }

    @Test
    fun settingsRejectNonFiniteOrOutOfRangeWidths() {
        assertThrows(IllegalArgumentException::class.java) {
            DrawingToolSettings(width = Float.NaN)
        }
        assertThrows(IllegalArgumentException::class.java) {
            DrawingToolSettings(width = 1000f)
        }
    }
}
