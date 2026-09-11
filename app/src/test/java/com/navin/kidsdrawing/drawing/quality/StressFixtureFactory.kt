package com.navin.kidsdrawing.drawing.quality

import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint

/** Deterministic workload factory matching docs/18_DRAWING_PERFORMANCE_GATES.md. */
internal object StressFixtureFactory {
    const val W2_OPERATION_COUNT = 2_000
    const val W2_SAMPLES_PER_STROKE = 125
    const val W2_TOTAL_SAMPLES = W2_OPERATION_COUNT * W2_SAMPLES_PER_STROKE
    const val W3_OPERATION_COUNT = 5_000

    fun w2Document(): DrawingDocument = document(
        documentId = "quality-w2-heavy",
        operationCount = W2_OPERATION_COUNT,
        samplesPerStroke = W2_SAMPLES_PER_STROKE,
    )

    fun w3Document(): DrawingDocument = document(
        documentId = "quality-w3-stress",
        operationCount = W3_OPERATION_COUNT,
        samplesPerStroke = 8,
    )

    fun representativePersistenceDocument(operationCount: Int = 250): DrawingDocument = document(
        documentId = "quality-persistence",
        operationCount = operationCount,
        samplesPerStroke = 16,
    )

    fun document(
        documentId: String,
        operationCount: Int,
        samplesPerStroke: Int,
    ): DrawingDocument {
        require(operationCount > 0)
        require(samplesPerStroke > 0)
        val createdAt = 1_000L
        val base = DrawingDocumentEngine.newDocument(
            documentId = documentId,
            nowEpochMillis = createdAt,
        )
        return base.copy(
            modifiedAtEpochMillis = createdAt + operationCount,
            operations = List(operationCount) { index ->
                DocumentOperation.AddInkStroke(
                    operationId = "quality-op-$index",
                    createdAtEpochMillis = createdAt + index + 1L,
                    stroke = stroke(index, samplesPerStroke),
                )
            },
        )
    }

    fun stroke(index: Int, sampleCount: Int): InkStrokeRecord {
        val color = COLORS[index % COLORS.size]
        val width = 4f + (index % 29)
        val tool = if (index % 7 == 0) PointerTool.STYLUS else PointerTool.FINGER
        return InkStrokeRecord(
            strokeId = "quality-stroke-$index",
            brushPresetId = if (index % 3 == 0) "pencil.standard" else "marker.standard",
            colorArgb = color,
            opacity = 1f,
            baseSize = width,
            tool = tool,
            points = List(sampleCount) { sample ->
                val x = ((index * 17 + sample * 7) % 1000).toFloat()
                val y = ((index * 31 + sample * 11) % 1000).toFloat()
                StrokePoint(
                    x = x,
                    y = y,
                    elapsedTimeMillis = sample * 8L,
                    pressure = 0.25f + ((sample % 70) / 100f),
                    tiltRadians = if (tool == PointerTool.STYLUS && sample % 3 == 0) 0.2f else null,
                    orientationRadians = if (tool == PointerTool.STYLUS && sample % 5 == 0) 0.7f else null,
                )
            },
        )
    }

    private val COLORS = intArrayOf(
        0xFF242321.toInt(),
        0xFF315C9A.toInt(),
        0xFFB9473C.toInt(),
        0xFF4F7C52.toInt(),
        0xFF8E5D9F.toInt(),
    )
}
