package com.navin.kidsdrawing.coloring.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ColoringSessionEngineTest {
    @Test
    fun activeSessionOwnsModeToolColorAndWidthSemantics() {
        val engine = ColoringSessionEngine.start(
            childDocumentId = "doc-1",
            lessonId = "cute-cat",
            lessonRevision = 1,
            mode = ColoringSessionMode.COLOR_WITH_ME,
        )

        assertEquals(ColoringSessionMode.COLOR_WITH_ME, engine.state.value.mode)
        assertTrue(engine.selectColor(0xFFEF6C68.toInt()))
        assertTrue(engine.selectTool(ColoringSessionTool.ERASER))
        assertTrue(engine.setBrushWidth(120f))

        val state = engine.state.value
        assertEquals(0xFFEF6C68.toInt(), state.selectedColorArgb)
        assertEquals(ColoringSessionTool.ERASER, state.selectedTool)
        assertEquals(ColoringSessionState.MAX_BRUSH_WIDTH, state.brushWidth)
    }

    @Test
    fun snapshotRestorePreservesActiveSemanticState() {
        val engine = ColoringSessionEngine.start(
            childDocumentId = "doc-2",
            lessonId = "cute-cat",
            lessonRevision = 1,
            mode = ColoringSessionMode.COLOR_MYSELF,
        )
        engine.selectColor(0xFF6AB7D8.toInt())
        engine.setBrushWidth(48f)
        val snapshot = engine.snapshot(5_000L)

        val restored = ColoringSessionEngine.restore(snapshot)

        assertEquals(snapshot.sessionId, restored.state.value.sessionId)
        assertEquals(ColoringSessionMode.COLOR_MYSELF, restored.state.value.mode)
        assertEquals(0xFF6AB7D8.toInt(), restored.state.value.selectedColorArgb)
        assertEquals(48f, restored.state.value.brushWidth)
        assertEquals(ColoringSessionPhase.ACTIVE, restored.state.value.phase)
    }

    @Test
    fun finishedSessionRejectsFurtherPresentationMutations() {
        val engine = ColoringSessionEngine.start(
            childDocumentId = "doc-3",
            lessonId = "cute-cat",
            lessonRevision = 1,
            mode = ColoringSessionMode.COLOR_MYSELF,
        )

        assertTrue(engine.finish())
        assertEquals(ColoringSessionPhase.FINISHED, engine.state.value.phase)
        assertFalse(engine.finish())
        assertFalse(engine.selectTool(ColoringSessionTool.ERASER))
        assertFalse(engine.selectColor(0xFF000000.toInt()))
        assertFalse(engine.setBrushWidth(20f))
    }

    @Test
    fun sessionIdIsDeterministicForDocumentIdentity() {
        assertEquals(
            "coloring-lesson-lab-cute-cat-document",
            ColoringSessionEngine.sessionIdFor("lesson-lab-cute-cat-document"),
        )
    }
}
