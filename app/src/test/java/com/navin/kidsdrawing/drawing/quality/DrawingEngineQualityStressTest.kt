package com.navin.kidsdrawing.drawing.quality

import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawingEngineQualityStressTest {
    @Test
    fun w1NormalDocumentMatchesFiveHundredOperationContract() {
        val document = ArtLabQualityWorkloadFactory.w1()

        assertEquals(ArtLabQualityWorkloadFactory.W1_OPERATION_COUNT, document.operations.size)
        assertEquals(450, document.activeInkStrokes().size)
        assertEquals(document.operations.size, document.operations.map { it.operationId }.toSet().size)
    }

    @Test
    fun w2HeavyDocumentMatchesContractAndPreservesStableOrdering() {
        val document = StressFixtureFactory.w2Document()
        val strokes = document.operations.filterIsInstance<DocumentOperation.AddInkStroke>()

        assertEquals(StressFixtureFactory.W2_OPERATION_COUNT, document.operations.size)
        assertEquals(StressFixtureFactory.W2_OPERATION_COUNT, strokes.size)
        assertEquals(
            StressFixtureFactory.W2_TOTAL_SAMPLES,
            strokes.sumOf { it.stroke.points.size },
        )
        assertEquals("quality-op-0", document.operations.first().operationId)
        assertEquals("quality-op-1999", document.operations.last().operationId)
        assertEquals(document.operations.size, document.operations.map { it.operationId }.toSet().size)
    }

    @Test
    fun w3StressDocumentReachesFiveThousandOperationsWithoutInvalidState() {
        val document = StressFixtureFactory.w3Document()

        assertEquals(StressFixtureFactory.W3_OPERATION_COUNT, document.operations.size)
        assertEquals(StressFixtureFactory.W3_OPERATION_COUNT, document.activeInkStrokes().size)
        assertEquals(document.operations.size, document.operations.map { it.operationId }.toSet().size)
        assertTrue(document.activeInkStrokes().all { stroke ->
            stroke.points.zipWithNext().all { (a, b) -> b.elapsedTimeMillis >= a.elapsedTimeMillis }
        })
    }

    @Test
    fun w2HundredActionUndoRedoLoopRestoresExactTailOrdering() = runBlocking {
        var now = 50_000L
        val original = StressFixtureFactory.w2Document()
        val originalIds = original.operations.map { it.operationId }
        val engine = DrawingDocumentEngine(
            initialDocument = original,
            clockMillis = { ++now },
        )

        repeat(100) { assertTrue(engine.undo()) }
        assertEquals(StressFixtureFactory.W2_OPERATION_COUNT - 100, engine.state.value.historyCursor)
        assertEquals(100, engine.state.value.redoDepth)
        assertTrue(engine.state.value.canRedo)

        repeat(100) { assertTrue(engine.redo()) }
        assertEquals(originalIds, engine.state.value.document.operations.map { it.operationId })
        assertEquals(StressFixtureFactory.W2_OPERATION_COUNT, engine.state.value.historyCursor)
        assertEquals(0, engine.state.value.redoDepth)
        assertFalse(engine.state.value.canRedo)
    }

    @Test
    fun repeatedClearUndoDoesNotLoseLargeDocumentHistory() = runBlocking {
        var now = 75_000L
        var ids = 0
        val original = StressFixtureFactory.document(
            documentId = "quality-clear-stress",
            operationCount = 1_000,
            samplesPerStroke = 4,
        )
        val engine = DrawingDocumentEngine(
            initialDocument = original,
            clockMillis = { ++now },
            idFactory = { "quality-generated-${++ids}" },
        )

        repeat(50) {
            engine.clear()
            assertTrue(engine.state.value.document.activeInkStrokes().isEmpty())
            assertTrue(engine.undo())
            assertEquals(1_000, engine.state.value.document.activeInkStrokes().size)
            assertTrue(engine.state.value.canRedo)
            // The next fresh Clear intentionally invalidates this redo branch.
        }

        assertEquals(1_000, engine.state.value.document.activeInkStrokes().size)
        assertEquals(original.operations.map { it.operationId }, engine.state.value.document.operations.map { it.operationId })
    }
}
