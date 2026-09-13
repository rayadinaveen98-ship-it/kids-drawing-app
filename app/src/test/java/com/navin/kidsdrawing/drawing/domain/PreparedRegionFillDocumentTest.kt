package com.navin.kidsdrawing.drawing.domain

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreparedRegionFillDocumentTest {
    @Test
    fun repeatedFillIsReversibleAndColoringUndoCannotCrossLineArt() = runBlocking {
        var now = 10L
        val engine = DrawingDocumentEngine(
            initialDocument = DrawingDocumentEngine.newDocument(nowEpochMillis = 1L),
            clockMillis = { ++now },
            idFactory = { "id-$now" },
        )
        engine.commitChildStroke(childStroke("outline"))
        engine.commitColorRegionFill(fill("roof", 0xFFFF0000.toInt()))
        engine.commitColorRegionFill(fill("roof", 0xFF0000FF.toInt()))

        assertEquals(3, engine.state.value.document.documentSchemaVersion)
        assertEquals(0xFF0000FF.toInt(), engine.state.value.document.activeColorRegionFills().single().colorArgb)
        assertTrue(engine.state.value.canUndoColoring)

        assertTrue(engine.undoColoring())
        assertEquals(0xFFFF0000.toInt(), engine.state.value.document.activeColorRegionFills().single().colorArgb)
        assertTrue(engine.undoColoring())
        assertTrue(engine.state.value.document.activeColorRegionFills().isEmpty())

        // The protected line-art stroke is now the final operation; coloring undo must stop here.
        assertFalse(engine.undoColoring())
        assertEquals(1, engine.state.value.document.activeInkStrokes().size)

        assertTrue(engine.redoColoring())
        assertEquals(0xFFFF0000.toInt(), engine.state.value.document.activeColorRegionFills().single().colorArgb)
    }

    @Test
    fun schemaTwoCannotContainPreparedFill() {
        val error = runCatching {
            DrawingDocument(
                documentSchemaVersion = 2,
                documentId = "old",
                logicalSize = DocumentSize(1000f, 1000f),
                createdAtEpochMillis = 1L,
                modifiedAtEpochMillis = 2L,
                operations = listOf(
                    DocumentOperation.AddColorRegionFill(
                        operationId = "fill",
                        createdAtEpochMillis = 2L,
                        fill = fill("roof", 0xFFFF0000.toInt()),
                    ),
                ),
            )
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }

    private fun fill(regionId: String, color: Int) = ColorRegionFillRecord(
        regionId = regionId,
        colorArgb = color,
        points = listOf(
            ColorRegionPoint(100f, 100f),
            ColorRegionPoint(400f, 100f),
            ColorRegionPoint(250f, 300f),
        ),
    )

    private fun childStroke(id: String) = InkStrokeRecord(
        strokeId = id,
        brushPresetId = "pencil.standard",
        colorArgb = 0xFF111111.toInt(),
        opacity = 1f,
        baseSize = 6f,
        tool = PointerTool.FINGER,
        points = listOf(
            StrokePoint(100f, 100f, 0L, 1f),
            StrokePoint(200f, 200f, 16L, 1f),
        ),
        authorRole = StrokeAuthorRole.CHILD,
    )
}
