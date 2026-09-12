package com.navin.kidsdrawing.drawing.infrastructure

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommittedRasterCacheInstrumentationTest {
    @Test
    fun inkEraseUndoRedoAndClearPreserveVisibleRasterSemantics() {
        val size = DocumentSize(100f, 100f)
        val cache = CommittedRasterCache(size)
        val ink = inkOperation("ink-1", 1_001L)
        val erase = eraseOperation("erase-1", 1_002L)
        val clear = DocumentOperation.ClearDocument("clear-1", 1_003L)

        cache.reconcile(DOCUMENT_ID, listOf(ink))
        assertTrue("Ink should make the center pixel visible", centerAlpha(cache) > 0)

        cache.reconcile(DOCUMENT_ID, listOf(ink, erase))
        assertEquals("Erase mask must clear committed ink", 0, centerAlpha(cache))

        cache.reconcile(DOCUMENT_ID, listOf(ink))
        assertTrue("Undo erase must restore ink from checkpoint", centerAlpha(cache) > 0)

        cache.reconcile(DOCUMENT_ID, listOf(ink, erase))
        assertEquals("Redo erase must clear the same pixel", 0, centerAlpha(cache))

        cache.reconcile(DOCUMENT_ID, listOf(ink, clear))
        assertEquals("ClearDocument must clear the flattened projection", 0, centerAlpha(cache))

        cache.reconcile(DOCUMENT_ID, listOf(ink))
        assertTrue("Undo ClearDocument must restore prior committed artwork", centerAlpha(cache) > 0)
    }

    @Test
    fun checkpointAndProjectedStrokeCachesRemainBounded() {
        val size = DocumentSize(100f, 100f)
        val cache = CommittedRasterCache(size)
        val operations = List(160) { index ->
            inkOperation("ink-$index", 2_000L + index, y = 10f + (index % 80))
        }

        cache.reconcile(DOCUMENT_ID, operations)

        assertTrue(cache.checkpointCount() <= 8)
        assertTrue(cache.projectedOperationCount() <= 56)
        assertTrue(
            "100x100 ARGB base plus eight checkpoints must stay under the fixed cache ceiling",
            cache.estimatedRasterBytes() <= 9L * 100L * 100L * 4L,
        )
    }

    private fun centerAlpha(cache: CommittedRasterCache): Int =
        Color.alpha(cache.bitmap().getPixel(50, 50))

    private fun inkOperation(
        id: String,
        createdAt: Long,
        y: Float = 50f,
    ): DocumentOperation.AddInkStroke = DocumentOperation.AddInkStroke(
        operationId = id,
        createdAtEpochMillis = createdAt,
        stroke = InkStrokeRecord(
            strokeId = "stroke-$id",
            brushPresetId = "marker.standard",
            colorArgb = Color.BLACK,
            opacity = 1f,
            baseSize = 18f,
            tool = PointerTool.FINGER,
            points = listOf(
                StrokePoint(10f, y, 0L, 1f),
                StrokePoint(90f, y, 16L, 1f),
            ),
        ),
    )

    private fun eraseOperation(id: String, createdAt: Long): DocumentOperation.AddEraseMask =
        DocumentOperation.AddEraseMask(
            operationId = id,
            createdAtEpochMillis = createdAt,
            mask = EraseMaskRecord(
                maskId = "mask-$id",
                baseSize = 30f,
                points = listOf(
                    StrokePoint(10f, 50f, 0L, 1f),
                    StrokePoint(90f, 50f, 16L, 1f),
                ),
            ),
        )

    private companion object {
        const val DOCUMENT_ID = "raster-cache-test"
    }
}
