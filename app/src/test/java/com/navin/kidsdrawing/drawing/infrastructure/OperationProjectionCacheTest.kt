package com.navin.kidsdrawing.drawing.infrastructure

import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import org.junit.Assert.assertEquals
import org.junit.Test

class OperationProjectionCacheTest {
    @Test
    fun undoingClearRestoresCachedPreClearOperationsWithoutReprojection() {
        var projectionCalls = 0
        val cache = OperationProjectionCache<String> { operation ->
            projectionCalls++
            operation.operationId
        }
        val base = DrawingDocumentEngine.newDocument(
            documentId = "projection-doc",
            nowEpochMillis = 1_000L,
        )
        val a = inkOperation("a", 1_001L)
        val b = inkOperation("b", 1_002L)
        val clear = DocumentOperation.ClearDocument("clear", 1_003L)

        val cleared = base.copy(
            modifiedAtEpochMillis = 1_003L,
            operations = listOf(a, b, clear),
        )
        assertEquals(emptyList<String>(), cache.project(cleared))
        assertEquals(2, projectionCalls)

        val afterUndoClear = cleared.copy(
            modifiedAtEpochMillis = 1_004L,
            operations = listOf(a, b),
        )
        assertEquals(listOf("a", "b"), cache.project(afterUndoClear))
        assertEquals(2, projectionCalls)
    }

    @Test
    fun droppingLastOperationDoesNotReprojectUnchangedHistoryAndRedoReprojectsOnlyTail() {
        var projectionCalls = 0
        val cache = OperationProjectionCache<String> { operation ->
            projectionCalls++
            operation.operationId
        }
        val base = DrawingDocumentEngine.newDocument(
            documentId = "projection-doc",
            nowEpochMillis = 2_000L,
        )
        val a = inkOperation("a", 2_001L)
        val b = inkOperation("b", 2_002L)

        val full = base.copy(modifiedAtEpochMillis = 2_002L, operations = listOf(a, b))
        assertEquals(listOf("a", "b"), cache.project(full))
        assertEquals(2, projectionCalls)

        val undone = full.copy(modifiedAtEpochMillis = 2_003L, operations = listOf(a))
        assertEquals(listOf("a"), cache.project(undone))
        assertEquals(2, projectionCalls)

        assertEquals(listOf("a", "b"), cache.project(full))
        assertEquals(3, projectionCalls)
    }

    @Test
    fun changingDocumentIdInvalidatesProjectionCache() {
        var projectionCalls = 0
        val cache = OperationProjectionCache<String> { operation ->
            projectionCalls++
            operation.operationId
        }
        val operation = inkOperation("same-id", 3_001L)
        val first = DrawingDocumentEngine.newDocument("doc-one", 3_000L).copy(
            modifiedAtEpochMillis = 3_001L,
            operations = listOf(operation),
        )
        val second = DrawingDocumentEngine.newDocument("doc-two", 4_000L).copy(
            modifiedAtEpochMillis = 4_001L,
            operations = listOf(operation.copy(createdAtEpochMillis = 4_001L)),
        )

        cache.project(first)
        cache.project(second)

        assertEquals(2, projectionCalls)
    }

    private fun inkOperation(id: String, createdAt: Long) = DocumentOperation.AddInkStroke(
        operationId = id,
        createdAtEpochMillis = createdAt,
        stroke = InkStrokeRecord(
            strokeId = "stroke-$id",
            brushPresetId = "pencil.standard",
            colorArgb = 0xFF242321.toInt(),
            opacity = 1f,
            baseSize = 8f,
            tool = PointerTool.FINGER,
            points = listOf(
                StrokePoint(10f, 10f, 0L, 1f),
                StrokePoint(20f, 20f, 16L, 1f),
            ),
        ),
    )
}
