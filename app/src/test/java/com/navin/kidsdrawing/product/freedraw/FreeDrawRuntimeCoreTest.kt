package com.navin.kidsdrawing.product.freedraw

import com.navin.kidsdrawing.drawing.domain.DrawingBrushPreset
import com.navin.kidsdrawing.drawing.domain.DrawingTool
import com.navin.kidsdrawing.drawing.domain.DrawingToolSettings
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.drawing.infrastructure.persistence.DrawingDocumentBinaryCodec
import com.navin.kidsdrawing.drawing.infrastructure.persistence.JvmStrokePayloadCodec
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeDrawRuntimeCoreTest {
    @Test
    fun firstRecoveryCreatesStableBlankFreeDrawDocument() = withRuntime { runtime, store, _ ->
        runBlocking {
            val outcome = runtime.recover()

            assertEquals(FreeDrawRecoveryOutcome.BlankCreated, outcome)
            val document = runtime.documentEngine.state.value.document
            assertEquals(FreeDrawRuntimeCore.WORKING_DOCUMENT_ID, document.documentId)
            assertNull(document.metadata.lessonId)
            assertNull(document.metadata.lessonRevision)
            assertTrue(document.operations.isEmpty())
            assertTrue(store.hasRecoverableDocument(FreeDrawRuntimeCore.WORKING_DOCUMENT_ID))
        }
    }

    @Test
    fun editsAndToolChoiceRecoverFromDurableStores() = withRuntime { runtime, store, settings ->
        runBlocking {
            runtime.recover()
            runtime.selectBrush(DrawingBrushPreset.CRAYON)
            runtime.setColor(0xFFEF6C68.toInt())
            runtime.setWidth(20f)
            runtime.commitChildStroke(stroke("raw"))
            runtime.selectEraser()
            runtime.commitEraseMask(mask("erase"))

            val firstDocument = runtime.documentEngine.state.value.document
            assertEquals(2, firstDocument.operations.size)
            val persistedStroke = firstDocument.activeInkStrokes().single()
            assertEquals("crayon.standard", persistedStroke.brushPresetId)
            assertEquals(0.62f, persistedStroke.opacity, 0f)
            assertEquals(0xFFEF6C68.toInt(), persistedStroke.colorArgb)
            assertEquals(20f, persistedStroke.baseSize, 0f)

            val restored = FreeDrawRuntimeCore(store, settings, clockMillis = { 9_000L })
            val outcome = restored.recover()
            assertTrue(outcome is FreeDrawRecoveryOutcome.Restored)
            assertEquals(firstDocument.operations, restored.documentEngine.state.value.document.operations)
            assertEquals(DrawingTool.ERASER, restored.toolEngine.state.value.tool)
            assertEquals(DrawingBrushPreset.CRAYON, restored.toolEngine.state.value.brushPreset)
            assertEquals(0xFFEF6C68.toInt(), restored.toolEngine.state.value.colorArgb)
            assertEquals(20f, restored.toolEngine.state.value.width, 0f)
        }
    }

    @Test
    fun undoRedoPersistEditableOperationHistory() = withRuntime { runtime, store, settings ->
        runBlocking {
            runtime.recover()
            runtime.commitChildStroke(stroke("one"))
            runtime.commitChildStroke(stroke("two"))
            assertEquals(2, runtime.documentEngine.state.value.document.operations.size)

            assertTrue(runtime.undo())
            assertEquals(1, runtime.documentEngine.state.value.document.operations.size)
            assertTrue(runtime.redo())
            assertEquals(2, runtime.documentEngine.state.value.document.operations.size)

            val restored = FreeDrawRuntimeCore(store, settings)
            restored.recover()
            assertEquals(2, restored.documentEngine.state.value.document.operations.size)
            assertEquals(2, restored.documentEngine.state.value.document.activeInkStrokes().size)
        }
    }

    @Test
    fun clearRequiresExplicitConfirmationAndRemainsUndoable() = withRuntime { runtime, _, _ ->
        runBlocking {
            runtime.recover()
            runtime.commitChildStroke(stroke("one"))
            val before = runtime.documentEngine.state.value.document.operations

            assertEquals(FreeDrawClearResult.ConfirmationRequired, runtime.clear(confirmed = false))
            assertEquals(before, runtime.documentEngine.state.value.document.operations)
            assertTrue(runtime.hasVisibleArtwork())

            assertEquals(FreeDrawClearResult.Cleared, runtime.clear(confirmed = true))
            assertFalse(runtime.hasVisibleArtwork())
            assertTrue(runtime.documentEngine.state.value.canUndo)
            assertTrue(runtime.undo())
            assertTrue(runtime.hasVisibleArtwork())
        }
    }

    @Test
    fun resetAfterGalleryPromotionCreatesFreshWorkingCanvasButKeepsToolPreference() = withRuntime { runtime, store, settings ->
        runBlocking {
            runtime.recover()
            runtime.selectBrush(DrawingBrushPreset.MARKER)
            runtime.setColor(0xFF1565C0.toInt())
            runtime.commitChildStroke(stroke("finished"))

            runtime.resetAfterGalleryPromotion()

            val blank = runtime.documentEngine.state.value.document
            assertEquals(FreeDrawRuntimeCore.WORKING_DOCUMENT_ID, blank.documentId)
            assertTrue(blank.operations.isEmpty())
            assertFalse(runtime.hasVisibleArtwork())

            val restored = FreeDrawRuntimeCore(store, settings)
            restored.recover()
            assertTrue(restored.documentEngine.state.value.document.operations.isEmpty())
            assertEquals(DrawingBrushPreset.MARKER, restored.toolEngine.state.value.brushPreset)
            assertEquals(0xFF1565C0.toInt(), restored.toolEngine.state.value.colorArgb)
        }
    }

    private fun withRuntime(
        block: (FreeDrawRuntimeCore, AtomicDrawingDocumentStore, MemorySettingsStore) -> Unit,
    ) {
        val root = File(
            System.getProperty("java.io.tmpdir"),
            "kids-drawing-free-draw-${UUID.randomUUID()}",
        )
        check(root.mkdirs())
        try {
            val store = AtomicDrawingDocumentStore(
                rootDirectory = root,
                documentCodec = DrawingDocumentBinaryCodec(JvmStrokePayloadCodec),
            )
            val settings = MemorySettingsStore()
            val runtime = FreeDrawRuntimeCore(store, settings, clockMillis = { 1_000L })
            block(runtime, store, settings)
        } finally {
            root.deleteRecursively()
        }
    }

    private class MemorySettingsStore(
        var settings: DrawingToolSettings = FreeDrawRuntimeCore.initialFreeDrawSettings(),
    ) : FreeDrawToolSettingsPersistence {
        override suspend fun load(): DrawingToolSettings = settings

        override suspend fun save(settings: DrawingToolSettings) {
            this.settings = settings
        }
    }

    private fun stroke(id: String) = InkStrokeRecord(
        strokeId = id,
        brushPresetId = "raw.surface.preset",
        colorArgb = 0xFF000000.toInt(),
        opacity = 1f,
        baseSize = 3f,
        tool = PointerTool.FINGER,
        points = listOf(
            StrokePoint(100f, 100f, 0L, 1f),
            StrokePoint(180f, 180f, 20L, 1f),
        ),
    )

    private fun mask(id: String) = EraseMaskRecord(
        maskId = id,
        baseSize = 28f,
        points = listOf(
            StrokePoint(120f, 120f, 0L, 1f),
            StrokePoint(150f, 150f, 16L, 1f),
        ),
    )
}
