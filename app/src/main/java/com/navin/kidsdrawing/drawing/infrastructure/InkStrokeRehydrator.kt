package com.navin.kidsdrawing.drawing.infrastructure

import androidx.ink.brush.Brush
import androidx.ink.brush.InputToolType
import androidx.ink.brush.StockBrushes
import androidx.ink.strokes.MutableStrokeInputBatch
import androidx.ink.strokes.Stroke
import androidx.ink.strokes.StrokeInput
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import kotlin.math.roundToInt

/** Rebuilds an AndroidX Ink [Stroke] from the app-owned persisted stroke record. */
internal object InkStrokeRehydrator {
    fun rehydrate(record: InkStrokeRecord): Stroke {
        val inputs = MutableStrokeInputBatch()
        record.points.forEach { point ->
            inputs.add(
                type = record.tool.toInkInputTool(),
                x = point.x,
                y = point.y,
                elapsedTimeMillis = point.elapsedTimeMillis,
                strokeUnitLengthCm = StrokeInput.NO_STROKE_UNIT_LENGTH,
                pressure = point.pressure,
                tiltRadians = point.tiltRadians ?: StrokeInput.NO_TILT,
                orientationRadians = point.orientationRadians ?: StrokeInput.NO_ORIENTATION,
            )
        }
        return Stroke(
            brush = brushFor(record),
            inputs = inputs,
        )
    }

    private fun brushFor(record: InkStrokeRecord): Brush {
        val family = when (record.brushPresetId) {
            "pencil.standard" -> StockBrushes.pressurePen(StockBrushes.PressurePenVersion.V1)
            "crayon.standard",
            "marker.standard",
            -> StockBrushes.marker(StockBrushes.MarkerVersion.V1)
            else -> when (record.tool) {
                PointerTool.STYLUS,
                PointerTool.STYLUS_ERASER,
                -> StockBrushes.pressurePen(StockBrushes.PressurePenVersion.V1)
                PointerTool.FINGER,
                PointerTool.UNKNOWN,
                -> StockBrushes.marker(StockBrushes.MarkerVersion.V1)
            }
        }
        return Brush.createWithColorIntArgb(
            family = family,
            colorIntArgb = applyOpacity(record.colorArgb, record.opacity),
            size = record.baseSize,
            epsilon = BRUSH_EPSILON,
        )
    }

    private fun PointerTool.toInkInputTool(): InputToolType = when (this) {
        PointerTool.FINGER -> InputToolType.TOUCH
        PointerTool.STYLUS,
        PointerTool.STYLUS_ERASER,
        -> InputToolType.STYLUS
        PointerTool.UNKNOWN -> InputToolType.UNKNOWN
    }

    private fun applyOpacity(colorArgb: Int, opacity: Float): Int {
        val sourceAlpha = (colorArgb ushr 24) and 0xFF
        val effectiveAlpha = (sourceAlpha * opacity).roundToInt().coerceIn(0, 255)
        return (colorArgb and 0x00FFFFFF) or (effectiveAlpha shl 24)
    }

    private const val BRUSH_EPSILON = 0.5f
}
