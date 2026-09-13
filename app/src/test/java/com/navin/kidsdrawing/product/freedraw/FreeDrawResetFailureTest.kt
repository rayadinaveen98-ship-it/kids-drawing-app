package com.navin.kidsdrawing.product.freedraw

import com.navin.kidsdrawing.drawing.domain.DrawingToolSettings
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.drawing.infrastructure.persistence.DrawingDocumentBinaryCodec
import com.navin.kidsdrawing.drawing.infrastructure.persistence.JvmStrokePayloadCodec
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeDrawResetFailureTest {
    @Test
    fun failedBlankPersistenceAfterGalleryPromotionDoesNotReplaceWorkingTruth() = runBlocking {
        val root = File(
            System.getProperty("java.io.tmpdir"),
            "kids-drawing-free-draw-reset-${UUID.randomUUID()}",
        )
        check(root.mkdirs())
        try {
            var failResetSave = false
            val store = AtomicDrawingDocumentStore(
                rootDirectory = root,
                faultInjector = { stage ->
                    if (failResetSave && stage == AtomicDrawingDocumentStore.SaveStage.TEMP_SYNCED) {
                        throw IOException("injected blank reset failure")
                    }
                },
                documentCodec = DrawingDocumentBinaryCodec(JvmStrokePayloadCodec),
            )
            val settings = MemorySettingsStore()
            val runtime = FreeDrawRuntimeCore(store, settings, clockMillis = { 1_000L })
            runtime.recover()
            runtime.commitChildStroke(stroke())
            val beforeReset = runtime.documentEngine.state.value.document
            assertTrue(beforeReset.activeInkStrokes().isNotEmpty())

            failResetSave = true
            assertTrue(runCatching { runtime.resetAfterGalleryPromotion() }.isFailure)

            assertEquals(beforeReset, runtime.documentEngine.state.value.document)
            failResetSave = false
            val restored = FreeDrawRuntimeCore(store, settings, clockMillis = { 2_000L })
            restored.recover()
            assertEquals(beforeReset.operations, restored.documentEngine.state.value.document.operations)
            assertTrue(restored.hasVisibleArtwork())
        } finally {
            root.deleteRecursively()
        }
    }

    private class MemorySettingsStore : FreeDrawToolSettingsPersistence {
        private var settings: DrawingToolSettings = FreeDrawRuntimeCore.initialFreeDrawSettings()

        override suspend fun load(): DrawingToolSettings = settings

        override suspend fun save(settings: DrawingToolSettings) {
            this.settings = settings
        }
    }

    private fun stroke() = InkStrokeRecord(
        strokeId = "working-stroke",
        brushPresetId = "raw.surface.preset",
        colorArgb = 0xFF242321.toInt(),
        opacity = 1f,
        baseSize = 10f,
        tool = PointerTool.FINGER,
        points = listOf(
            StrokePoint(100f, 100f, 0L, 1f),
            StrokePoint(180f, 180f, 20L, 1f),
        ),
    )
}
