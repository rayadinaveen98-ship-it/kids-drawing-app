package com.navin.kidsdrawing.drawing.quality

import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint

/** Internal Art Lab workloads matching docs/18_DRAWING_PERFORMANCE_GATES.md. */
object ArtLabQualityWorkloadFactory {
    const val W2_OPERATION_COUNT = 2_000
    const val W2_SAMPLES_PER_OPERATION = 125
    const val W3_OPERATION_COUNT = 5_000
    const val W3_SAMPLES_PER_OPERATION = 16

    fun w2(): DrawingDocument = document(
        documentId = "quality-lab-w2",
        operationCount = W2_OPERATION_COUNT,
        samplesPerOperation = W2_SAMPLES_PER_OPERATION,
    )

    fun w3(): DrawingDocument = document(
        documentId = "quality-lab-w3",
        operationCount = W3_OPERATION_COUNT,
        samplesPerOperation = W3_SAMPLES_PER_OPERATION,
    )

    private fun document(
        documentId: String,
        operationCount: Int,
        samplesPerOperation: Int,
    ): DrawingDocument {
        val base = DrawingDocumentEngine.newDocument(
            documentId = documentId,
            nowEpochMillis = 1_000L,
        )
        return base.copy(
            modifiedAtEpochMillis = 1_000L + operationCount,
            operations = List(operationCount) { index ->
                if (index % 10 == 9) eraseOperation(index, samplesPerOperation) else inkOperation(index, samplesPerOperation)
            },
        )
    }

    private fun inkOperation(index: Int, sampleCount: Int): DocumentOperation.AddInkStroke {
        val stylus = index % 7 == 0
        val tool = if (stylus) PointerTool.STYLUS else PointerTool.FINGER
        return DocumentOperation.AddInkStroke(
            operationId = "quality-ink-op-$index",
            createdAtEpochMillis = 1_001L + index,
            stroke = InkStrokeRecord(
                strokeId = "quality-ink-$index",
                brushPresetId = if (index % 3 == 0) "pencil.standard" else "marker.standard",
                colorArgb = COLORS[index % COLORS.size],
                opacity = 1f,
                baseSize = 4f + (index % 29),
                tool = tool,
                points = points(index, sampleCount, stylus = stylus),
            ),
        )
    }

    private fun eraseOperation(index: Int, sampleCount: Int): DocumentOperation.AddEraseMask =
        DocumentOperation.AddEraseMask(
            operationId = "quality-erase-op-$index",
            createdAtEpochMillis = 1_001L + index,
            mask = EraseMaskRecord(
                maskId = "quality-mask-$index",
                baseSize = 12f + (index % 40),
                points = points(index, sampleCount, stylus = false),
            ),
        )

    private fun points(index: Int, count: Int, stylus: Boolean): List<StrokePoint> =
        List(count) { sample ->
            StrokePoint(
                x = ((index * 17 + sample * 7) % 1000).toFloat(),
                y = ((index * 31 + sample * 11) % 1000).toFloat(),
                elapsedTimeMillis = sample * 8L,
                pressure = 0.25f + ((sample % 70) / 100f),
                tiltRadians = if (stylus) 0.2f + (sample % 5) * 0.01f else null,
                orientationRadians = if (stylus) 0.7f + (sample % 7) * 0.01f else null,
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
