package com.navin.kidsdrawing.drawing.domain

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DrawingEngineMetricsTest {
    @Test
    fun historyCursorDepthAndRedoDepthTrackAuthoritativeTimeline() = runBlocking {
        var id = 0
        val engine = DrawingDocumentEngine(
            initialDocument = DrawingDocumentEngine.newDocument(
                documentId = "metrics-doc",
                nowEpochMillis = 1_000L,
            ),
            clockMillis = { 2_000L + id },
            idFactory = { (++id).toString() },
        )

        assertEquals(0, engine.state.value.historyCursor)
        assertEquals(0, engine.state.value.historyDepth)
        assertEquals(0, engine.state.value.redoDepth)

        engine.commitChildStroke(stroke("one"))
        engine.commitChildStroke(stroke("two"))
        assertEquals(2, engine.state.value.historyCursor)
        assertEquals(2, engine.state.value.historyDepth)
        assertEquals(0, engine.state.value.redoDepth)

        engine.undo()
        assertEquals(1, engine.state.value.historyCursor)
        assertEquals(2, engine.state.value.historyDepth)
        assertEquals(1, engine.state.value.redoDepth)

        engine.redo()
        assertEquals(2, engine.state.value.historyCursor)
        assertEquals(2, engine.state.value.historyDepth)
        assertEquals(0, engine.state.value.redoDepth)
    }

    private fun stroke(id: String) = InkStrokeRecord(
        strokeId = id,
        brushPresetId = "pencil.standard",
        colorArgb = 0xFF242321.toInt(),
        opacity = 1f,
        baseSize = 10f,
        tool = PointerTool.FINGER,
        points = listOf(
            StrokePoint(0f, 0f, 0L, 1f),
            StrokePoint(10f, 10f, 16L, 1f),
        ),
    )
}
