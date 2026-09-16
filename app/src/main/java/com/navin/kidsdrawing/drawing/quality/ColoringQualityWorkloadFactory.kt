package com.navin.kidsdrawing.drawing.quality

import com.navin.kidsdrawing.drawing.domain.ColorRegionFillRecord
import com.navin.kidsdrawing.drawing.domain.ColorRegionPoint
import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint

/** Deterministic coloring-heavy workloads for P6.5 measurement and regression evidence. */
object ColoringQualityWorkloadFactory {
    const val C1_OPERATION_COUNT = 500
    const val C2_OPERATION_COUNT = 2_000
    const val C3_OPERATION_COUNT = 5_000

    fun c1(): DrawingDocument = document(
        documentId = "quality-color-c1",
        operationCount = C1_OPERATION_COUNT,
        lineArtOperationCount = 80,
        samplesPerStroke = 24,
    )

    fun c2(): DrawingDocument = document(
        documentId = "quality-color-c2",
        operationCount = C2_OPERATION_COUNT,
        lineArtOperationCount = 200,
        samplesPerStroke = 96,
    )

    fun c3(): DrawingDocument = document(
        documentId = "quality-color-c3",
        operationCount = C3_OPERATION_COUNT,
        lineArtOperationCount = 400,
        samplesPerStroke = 20,
    )

    private fun document(
        documentId: String,
        operationCount: Int,
        lineArtOperationCount: Int,
        samplesPerStroke: Int,
    ): DrawingDocument {
        require(lineArtOperationCount in 1 until operationCount)
        val base = DrawingDocumentEngine.newDocument(
            documentId = documentId,
            nowEpochMillis = 10_000L,
        )
        val operations = ArrayList<DocumentOperation>(operationCount)
        repeat(operationCount) { index ->
            operations += if (index < lineArtOperationCount) {
                lineStroke(index, samplesPerStroke)
            } else {
                val colorIndex = index - lineArtOperationCount
                when {
                    colorIndex % 25 == 23 -> colorErase(index, samplesPerStroke)
                    colorIndex % 25 == 24 -> colorFill(index)
                    else -> colorStroke(index, samplesPerStroke)
                }
            }
        }
        return base.copy(
            modifiedAtEpochMillis = 10_000L + operationCount,
            operations = operations,
        )
    }

    private fun lineStroke(index: Int, sampleCount: Int): DocumentOperation.AddInkStroke =
        DocumentOperation.AddInkStroke(
            operationId = "color-quality-line-op-$index",
            createdAtEpochMillis = 10_001L + index,
            stroke = stroke(
                id = "color-quality-line-$index",
                index = index,
                sampleCount = sampleCount,
                colorArgb = 0xFF242321.toInt(),
                baseSize = 5f + (index % 5),
            ),
        )

    private fun colorStroke(index: Int, sampleCount: Int): DocumentOperation.AddColorStroke =
        DocumentOperation.AddColorStroke(
            operationId = "color-quality-color-op-$index",
            createdAtEpochMillis = 10_001L + index,
            stroke = stroke(
                id = "color-quality-color-$index",
                index = index,
                sampleCount = sampleCount,
                colorArgb = COLORS[index % COLORS.size],
                baseSize = 20f + (index % 28),
            ),
        )

    private fun colorErase(index: Int, sampleCount: Int): DocumentOperation.AddColorEraseMask =
        DocumentOperation.AddColorEraseMask(
            operationId = "color-quality-erase-op-$index",
            createdAtEpochMillis = 10_001L + index,
            mask = EraseMaskRecord(
                maskId = "color-quality-erase-$index",
                baseSize = 28f + (index % 22),
                points = points(index, sampleCount),
            ),
        )

    private fun colorFill(index: Int): DocumentOperation.AddColorRegionFill {
        val cell = index % 10
        val x = 80f + cell * 75f
        val y = 80f + ((index / 10) % 10) * 75f
        return DocumentOperation.AddColorRegionFill(
            operationId = "color-quality-fill-op-$index",
            createdAtEpochMillis = 10_001L + index,
            fill = ColorRegionFillRecord(
                regionId = "quality-region-${index % 40}",
                colorArgb = COLORS[index % COLORS.size],
                points = listOf(
                    ColorRegionPoint(x, y),
                    ColorRegionPoint(x + 55f, y),
                    ColorRegionPoint(x + 55f, y + 55f),
                    ColorRegionPoint(x, y + 55f),
                ),
            ),
        )
    }

    private fun stroke(
        id: String,
        index: Int,
        sampleCount: Int,
        colorArgb: Int,
        baseSize: Float,
    ): InkStrokeRecord = InkStrokeRecord(
        strokeId = id,
        brushPresetId = "pencil.standard",
        colorArgb = colorArgb,
        opacity = 1f,
        baseSize = baseSize,
        tool = PointerTool.FINGER,
        points = points(index, sampleCount),
    )

    private fun points(index: Int, count: Int): List<StrokePoint> = List(count) { sample ->
        StrokePoint(
            x = ((index * 29 + sample * 11) % 940 + 30).toFloat(),
            y = ((index * 41 + sample * 13) % 940 + 30).toFloat(),
            elapsedTimeMillis = sample * 8L,
            pressure = 0.55f,
        )
    }

    private val COLORS = intArrayOf(
        0xFFE47C68.toInt(),
        0xFFE9A94A.toInt(),
        0xFFF4D35E.toInt(),
        0xFF78A86B.toInt(),
        0xFF6C9CB8.toInt(),
        0xFF9A83B8.toInt(),
    )
}
