package com.navin.kidsdrawing.product.freedraw

import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.drawing.infrastructure.persistence.DrawingDocumentBinaryCodec
import com.navin.kidsdrawing.drawing.infrastructure.persistence.JvmStrokePayloadCodec
import com.navin.kidsdrawing.gallery.domain.GalleryArtworkSource
import com.navin.kidsdrawing.gallery.domain.GalleryCompletionKind
import com.navin.kidsdrawing.gallery.domain.GalleryDeleteResult
import com.navin.kidsdrawing.gallery.domain.GalleryPromotionResult
import com.navin.kidsdrawing.gallery.domain.GalleryRepository
import com.navin.kidsdrawing.gallery.persistence.AtomicGalleryCatalogStore
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeDrawGalleryContractTest {
    @Test
    fun deletingPromotedFreeDrawCopyNeverDeletesWorkingCanvas() {
        val base = File(
            System.getProperty("java.io.tmpdir"),
            "kids-drawing-free-draw-gallery-${UUID.randomUUID()}",
        )
        val documentRoot = File(base, "documents")
        val catalogRoot = File(base, "catalog")
        check(documentRoot.mkdirs())
        check(catalogRoot.mkdirs())
        try {
            runBlocking {
                val documentStore = AtomicDrawingDocumentStore(
                    rootDirectory = documentRoot,
                    documentCodec = DrawingDocumentBinaryCodec(JvmStrokePayloadCodec),
                )
                val catalogStore = AtomicGalleryCatalogStore(catalogRoot)
                val repository = GalleryRepository(
                    documentStore = documentStore,
                    catalogStore = catalogStore,
                    protectedWorkingDocumentId = FreeDrawRuntimeCore.WORKING_DOCUMENT_ID,
                    clockMillis = { 5_000L },
                    idFactory = sequentialIds(),
                )
                val working = workingDocument()
                documentStore.save(working)

                val saved = repository.promoteCompletedArtwork(
                    workingDocument = working,
                    title = "My Free Drawing",
                    source = GalleryArtworkSource.FREE_DRAW,
                    completionKind = GalleryCompletionKind.DRAWING_ONLY,
                ) as GalleryPromotionResult.Saved

                assertTrue(documentStore.hasRecoverableDocument(FreeDrawRuntimeCore.WORKING_DOCUMENT_ID))
                assertTrue(documentStore.hasRecoverableDocument(saved.document.documentId))

                assertEquals(
                    GalleryDeleteResult.Deleted,
                    repository.delete(saved.record.entryId, confirmed = true),
                )

                assertTrue(documentStore.hasRecoverableDocument(FreeDrawRuntimeCore.WORKING_DOCUMENT_ID))
                assertTrue(documentStore.load(FreeDrawRuntimeCore.WORKING_DOCUMENT_ID) != null)
            }
        } finally {
            base.deleteRecursively()
        }
    }

    private fun workingDocument(): DrawingDocument {
        val base = DrawingDocumentEngine.newDocument(
            documentId = FreeDrawRuntimeCore.WORKING_DOCUMENT_ID,
            nowEpochMillis = 1_000L,
        )
        return base.copy(
            modifiedAtEpochMillis = 2_000L,
            operations = listOf(
                DocumentOperation.AddInkStroke(
                    operationId = "free-line-op",
                    createdAtEpochMillis = 2_000L,
                    stroke = InkStrokeRecord(
                        strokeId = "free-line",
                        brushPresetId = "marker.standard",
                        colorArgb = 0xFF3D7CC9.toInt(),
                        opacity = 1f,
                        baseSize = 24f,
                        tool = PointerTool.FINGER,
                        points = listOf(
                            StrokePoint(100f, 100f, 0L, 1f),
                            StrokePoint(220f, 220f, 20L, 1f),
                        ),
                    ),
                ),
            ),
        )
    }

    private fun sequentialIds(): () -> String {
        var value = 0
        return { "free-gallery-${++value}" }
    }
}
