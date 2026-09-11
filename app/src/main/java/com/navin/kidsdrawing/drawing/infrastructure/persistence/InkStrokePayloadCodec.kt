package com.navin.kidsdrawing.drawing.infrastructure.persistence

import androidx.ink.brush.InputToolType
import androidx.ink.storage.StrokeInputBatchSerialization
import androidx.ink.strokes.MutableStrokeInputBatch
import androidx.ink.strokes.StrokeInput
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import java.io.InputStream
import java.io.OutputStream

/**
 * Stable AndroidX Ink 1.0 production implementation of the app-owned payload boundary.
 *
 * Encoding is background persistence work only. AndroidX Ink types remain inside drawing
 * infrastructure and never become the document/history API.
 */
object InkStrokePayloadCodec : StrokePayloadCodec {
    override fun encode(record: InkStrokeRecord, output: OutputStream) {
        validateOptionalAxes(record.points)
        val batch = MutableStrokeInputBatch()
        record.points.forEach { point ->
            batch.add(
                type = record.tool.toInkToolType(),
                x = point.x,
                y = point.y,
                elapsedTimeMillis = point.elapsedTimeMillis,
                strokeUnitLengthCm = StrokeInput.NO_STROKE_UNIT_LENGTH,
                pressure = point.pressure,
                tiltRadians = point.tiltRadians ?: StrokeInput.NO_TILT,
                orientationRadians = point.orientationRadians ?: StrokeInput.NO_ORIENTATION,
            )
        }
        StrokeInputBatchSerialization.encode(batch, output)
    }

    override fun decode(input: InputStream): DecodedStrokePayload {
        val batch = StrokeInputBatchSerialization.decode(input)
        require(!batch.isEmpty()) { "Persisted Ink stroke payload cannot be empty." }
        require(batch.hasPressure()) {
            "Persisted Art Lab Ink payload must contain pressure for every input sample."
        }

        val points = List(batch.size) { index ->
            val point = batch[index]
            StrokePoint(
                x = point.x,
                y = point.y,
                elapsedTimeMillis = point.elapsedTimeMillis,
                pressure = point.pressure,
                tiltRadians = point.tiltRadians.takeIf { point.hasTilt },
                orientationRadians = point.orientationRadians.takeIf { point.hasOrientation },
            )
        }
        return DecodedStrokePayload(
            tool = batch.getToolType().toPointerTool(),
            points = points,
        )
    }

    private fun validateOptionalAxes(points: List<StrokePoint>) {
        require(points.map { it.tiltRadians != null }.distinct().size <= 1) {
            "A single Ink input batch cannot mix present and absent tilt samples."
        }
        require(points.map { it.orientationRadians != null }.distinct().size <= 1) {
            "A single Ink input batch cannot mix present and absent orientation samples."
        }
    }

    private fun PointerTool.toInkToolType(): InputToolType = when (this) {
        PointerTool.FINGER -> InputToolType.TOUCH
        PointerTool.STYLUS,
        PointerTool.STYLUS_ERASER,
        -> InputToolType.STYLUS
        PointerTool.UNKNOWN -> InputToolType.UNKNOWN
    }

    private fun InputToolType.toPointerTool(): PointerTool = when (this) {
        InputToolType.TOUCH -> PointerTool.FINGER
        InputToolType.STYLUS -> PointerTool.STYLUS
        else -> PointerTool.UNKNOWN
    }
}
