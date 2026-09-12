package com.navin.kidsdrawing.drawing.domain

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawingDocumentEngineTest {
    @Test
    fun longUndoRedoSequencePreservesOperationOrder() = runBlocking {
        val fixture = fixture()
        val engine = fixture.engine

        repeat(500) { index ->
            engine.commitChildStroke(stroke(index))
        }

        assertEquals(500, engine.state.value.document.operations.size)
        assertTrue(engine.state.value.canUndo)
        assertFalse(engine.state.value.canRedo)

        repeat(500) {
            assertTrue(engine.undo())
        }
        assertEquals(0, engine.state.value.document.operations.size)
        assertFalse(engine.state.value.canUndo)
        assertTrue(engine.state.value.canRedo)
        assertFalse(engine.undo())

        repeat(500) {
            assertTrue(engine.redo())
        }

        val restored = engine.state.value.document.operations
            .filterIsInstance<DocumentOperation.AddInkStroke>()
            .map { it.stroke.strokeId }
        assertEquals((0 until 500).map { "stroke-$it" }, restored)
        assertTrue(engine.state.value.canUndo)
        assertFalse(engine.state.value.canRedo)
        assertFalse(engine.redo())
    }

    @Test
    fun clearIsUndoableAndRedoableWithoutCanvasSnapshots() = runBlocking {
        val engine = fixture().engine
        engine.commitChildStroke(stroke(1))
        engine.commitChildStroke(stroke(2))

        engine.clear()
        assertTrue(engine.state.value.document.activeInkStrokes().isEmpty())
        assertTrue(engine.state.value.document.operations.last() is DocumentOperation.ClearDocument)

        assertTrue(engine.undo())
        assertEquals(listOf("stroke-1", "stroke-2"), engine.state.value.document.activeInkStrokes().map { it.strokeId })

        assertTrue(engine.redo())
        assertTrue(engine.state.value.document.activeInkStrokes().isEmpty())
    }

    @Test
    fun newEditAfterUndoInvalidatesRedoBranch() = runBlocking {
        val engine = fixture().engine
        engine.commitChildStroke(stroke(1))
        engine.commitChildStroke(stroke(2))
        engine.commitChildStroke(stroke(3))

        assertTrue(engine.undo())
        assertTrue(engine.undo())
        assertTrue(engine.state.value.canRedo)

        engine.commitChildStroke(stroke(99))

        assertFalse(engine.state.value.canRedo)
        assertFalse(engine.redo())
        assertEquals(
            listOf("stroke-1", "stroke-99"),
            engine.state.value.document.activeInkStrokes().map { it.strokeId },
        )
    }

    @Test
    fun replaceDocumentMakesRecoveredTimelineAuthoritativeAndClearsRedo() = runBlocking {
        val engine = fixture().engine
        engine.commitChildStroke(stroke(1))
        engine.commitChildStroke(stroke(2))
        assertTrue(engine.undo())
        assertTrue(engine.state.value.canRedo)

        val recovered = DrawingDocumentEngine.newDocument(
            documentId = "recovered-document",
            nowEpochMillis = 5_000L,
        ).copy(
            modifiedAtEpochMillis = 5_100L,
            operations = listOf(
                DocumentOperation.AddInkStroke(
                    operationId = "recovered-op",
                    createdAtEpochMillis = 5_050L,
                    stroke = stroke(42),
                ),
            ),
        )

        engine.replaceDocument(recovered)

        assertEquals("recovered-document", engine.state.value.document.documentId)
        assertEquals(listOf("stroke-42"), engine.state.value.document.activeInkStrokes().map { it.strokeId })
        assertTrue(engine.state.value.canUndo)
        assertFalse(engine.state.value.canRedo)
        assertFalse(engine.redo())
    }

    @Test
    fun teacherGeneratedStrokeCannotEnterChildHistory() = runBlocking {
        val engine = fixture().engine
        val teacherStroke = stroke(7).copy(authorRole = StrokeAuthorRole.TEACHER_GENERATED)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { engine.commitChildStroke(teacherStroke) }
        }
        assertTrue(engine.state.value.document.operations.isEmpty())
    }

    @Test
    fun teacherGeneratedStrokeCannotEnterColorHistory() = runBlocking {
        val engine = fixture().engine
        val teacherStroke = stroke(7).copy(authorRole = StrokeAuthorRole.TEACHER_GENERATED)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { engine.commitColorStroke(teacherStroke) }
        }
        assertTrue(engine.state.value.document.operations.isEmpty())
    }

    @Test
    fun eraseMaskParticipatesInOperationHistoryWithoutBitmapState() = runBlocking {
        val engine = fixture().engine
        engine.commitChildStroke(stroke(1))
        val mask = eraseMask("mask-1")

        engine.commitEraseMask(mask)
        assertTrue(engine.state.value.document.operations.last() is DocumentOperation.AddEraseMask)
        assertTrue(engine.undo())
        assertTrue(engine.state.value.document.operations.single() is DocumentOperation.AddInkStroke)
        assertTrue(engine.redo())
        assertTrue(engine.state.value.document.operations.last() is DocumentOperation.AddEraseMask)
    }

    @Test
    fun coloringHistoryStopsAtProtectedLineArtBoundaryAndRedoRestoresOnlyColor() = runBlocking {
        val engine = fixture().engine
        engine.commitChildStroke(stroke(1))
        engine.commitColorStroke(stroke(2).copy(colorArgb = 0xFFFFD54F.toInt()))
        engine.commitColorEraseMask(eraseMask("color-mask"))

        assertEquals(CURRENT_DOCUMENT_SCHEMA_VERSION, engine.state.value.document.documentSchemaVersion)
        assertTrue(engine.state.value.canUndoColoring)
        assertTrue(engine.undoColoring())
        assertTrue(engine.state.value.document.operations.last() is DocumentOperation.AddColorStroke)
        assertTrue(engine.undoColoring())

        // The protected drawing stroke remains the last operation and coloring Undo must stop.
        assertTrue(engine.state.value.document.operations.single() is DocumentOperation.AddInkStroke)
        assertFalse(engine.state.value.canUndoColoring)
        assertFalse(engine.undoColoring())
        assertEquals(listOf("stroke-1"), engine.state.value.document.activeInkStrokes().map { it.strokeId })

        assertTrue(engine.state.value.canRedoColoring)
        assertTrue(engine.redoColoring())
        assertTrue(engine.redoColoring())
        assertFalse(engine.redoColoring())
        assertEquals(listOf("stroke-2"), engine.state.value.document.activeColorStrokes().map { it.strokeId })
        assertTrue(engine.state.value.document.operations.last() is DocumentOperation.AddColorEraseMask)
    }

    @Test
    fun newColorEditAfterColorUndoInvalidatesColorRedoWithoutChangingLineArt() = runBlocking {
        val engine = fixture().engine
        engine.commitChildStroke(stroke(1))
        engine.commitColorStroke(stroke(2))
        engine.commitColorStroke(stroke(3))
        assertTrue(engine.undoColoring())
        assertTrue(engine.state.value.canRedoColoring)

        engine.commitColorStroke(stroke(4))

        assertFalse(engine.state.value.canRedoColoring)
        assertFalse(engine.redoColoring())
        assertEquals(listOf("stroke-1"), engine.state.value.document.activeInkStrokes().map { it.strokeId })
        assertEquals(
            listOf("stroke-2", "stroke-4"),
            engine.state.value.document.activeColorStrokes().map { it.strokeId },
        )
    }

    @Test
    fun schemaOneDocumentMigratesOnlyWhenNewMutationOccurs() = runBlocking {
        val legacy = DrawingDocumentEngine.newDocument(
            documentId = "legacy-document",
            nowEpochMillis = 1_000L,
        ).copy(documentSchemaVersion = 1)
        val engine = DrawingDocumentEngine(legacy, clockMillis = { 2_000L }, idFactory = { "id" })

        assertEquals(1, engine.state.value.document.documentSchemaVersion)
        engine.commitColorStroke(stroke(5))

        assertEquals(CURRENT_DOCUMENT_SCHEMA_VERSION, engine.state.value.document.documentSchemaVersion)
        assertTrue(engine.state.value.document.operations.single() is DocumentOperation.AddColorStroke)
    }

    @Test
    fun emptyHistoryTransitionsAreNoOps() = runBlocking {
        val engine = fixture().engine
        assertFalse(engine.undo())
        assertFalse(engine.redo())
        assertFalse(engine.undoColoring())
        assertFalse(engine.redoColoring())
        assertFalse(engine.state.value.canUndo)
        assertFalse(engine.state.value.canRedo)
    }

    private fun fixture(): Fixture {
        var now = 1_000L
        var id = 0
        val document = DrawingDocumentEngine.newDocument(
            documentId = "document-test",
            nowEpochMillis = now,
        )
        return Fixture(
            engine = DrawingDocumentEngine(
                initialDocument = document,
                clockMillis = { ++now },
                idFactory = { (++id).toString() },
            ),
        )
    }

    private fun stroke(index: Int): InkStrokeRecord = InkStrokeRecord(
        strokeId = "stroke-$index",
        brushPresetId = "pencil.standard",
        colorArgb = 0xFF242321.toInt(),
        opacity = 1f,
        baseSize = 10f,
        tool = PointerTool.FINGER,
        points = listOf(
            point(index.toFloat(), index.toFloat(), 0L),
            point(index + 1f, index + 1f, 16L),
        ),
    )

    private fun eraseMask(id: String): EraseMaskRecord = EraseMaskRecord(
        maskId = id,
        baseSize = 24f,
        points = listOf(
            point(1f, 1f, 0L),
            point(2f, 2f, 16L),
        ),
    )

    private fun point(x: Float, y: Float, time: Long): StrokePoint = StrokePoint(
        x = x,
        y = y,
        elapsedTimeMillis = time,
        pressure = 1f,
    )

    private data class Fixture(
        val engine: DrawingDocumentEngine,
    )
}
