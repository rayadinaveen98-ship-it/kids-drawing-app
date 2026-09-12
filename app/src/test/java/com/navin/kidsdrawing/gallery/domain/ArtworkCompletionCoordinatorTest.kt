package com.navin.kidsdrawing.gallery.domain

import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentMetadata
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.drawing.infrastructure.persistence.DrawingDocumentBinaryCodec
import com.navin.kidsdrawing.drawing.infrastructure.persistence.JvmStrokePayloadCodec
import com.navin.kidsdrawing.gallery.persistence.AtomicGalleryCatalogStore
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtworkCompletionCoordinatorTest {
    @Test
    fun drawingOnlyCompletionPersistsSemanticBoundaryBeforeGalleryPromotion() = withRuntime { coordinator, catalogStore ->
        runBlocking {
            val events = mutableListOf<String>()
            val port = FakePort(
                completionKind = GalleryCompletionKind.DRAWING_ONLY,
                currentDocument = document(includeColor = false),
                events = events,
            )

            val result = coordinator.complete(port, "Cute Cat")

            assertTrue(result is ArtworkCompletionResult.Saved)
            result as ArtworkCompletionResult.Saved
            assertEquals(GalleryCompletionKind.DRAWING_ONLY, result.record.completionKind)
            assertEquals(listOf("save-before", "finish-semantic", "save-after"), events)
            val catalog = catalogStore.load() as AtomicGalleryCatalogStore.LoadResult.Loaded
            assertEquals(result.record.entryId, catalog.catalog.records.single().entryId)
        }
    }

    @Test
    fun coloredCompletionPromotesPersistedColorOperations() = withRuntime { coordinator, _ ->
        runBlocking {
            val port = FakePort(
                completionKind = GalleryCompletionKind.COLORED,
                currentDocument = document(includeColor = true),
            )

            val result = coordinator.complete(port, "Cute Cat") as ArtworkCompletionResult.Saved

            assertEquals(GalleryCompletionKind.COLORED, result.record.completionKind)
            assertTrue(result.document.hasColoringOperations())
            assertEquals(2, result.document.operations.size)
        }
    }

    @Test
    fun rejectedSemanticCompletionCreatesNoGalleryTruth() = withRuntime { coordinator, catalogStore ->
        runBlocking {
            val port = FakePort(
                completionKind = GalleryCompletionKind.DRAWING_ONLY,
                currentDocument = document(),
                finishAccepted = false,
            )

            val result = coordinator.complete(port, "Cute Cat")

            assertTrue(result is ArtworkCompletionResult.Failed)
            assertEquals(AtomicGalleryCatalogStore.LoadResult.Missing, catalogStore.load())
        }
    }

    @Test
    fun saveAndLeaveBoundaryAloneNeverCreatesCompletedGalleryEntry() = withRuntime { _, catalogStore ->
        runBlocking {
            val port = FakePort(
                completionKind = GalleryCompletionKind.COLORED,
                currentDocument = document(includeColor = true),
            )

            // This mirrors Save & leave: active work is persisted, but no completion coordinator is invoked.
            port.saveBeforeFinish()

            assertEquals(AtomicGalleryCatalogStore.LoadResult.Missing, catalogStore.load())
            assertEquals(listOf("save-before"), port.events)
        }
    }

    private fun withRuntime(
        block: (ArtworkCompletionCoordinator, AtomicGalleryCatalogStore) -> Unit,
    ) {
        val base = File(
            System.getProperty("java.io.tmpdir"),
            "kids-drawing-completion-${UUID.randomUUID()}",
        )
        val documents = File(base, "documents")
        val catalog = File(base, "catalog")
        check(documents.mkdirs())
        check(catalog.mkdirs())
        try {
            val documentStore = AtomicDrawingDocumentStore(
                rootDirectory = documents,
                documentCodec = DrawingDocumentBinaryCodec(JvmStrokePayloadCodec),
            )
            val catalogStore = AtomicGalleryCatalogStore(catalog)
            val repository = GalleryRepository(
                documentStore = documentStore,
                catalogStore = catalogStore,
                protectedWorkingDocumentId = WORKING_ID,
                clockMillis = { 10_000L },
                idFactory = sequentialIds(),
            )
            block(ArtworkCompletionCoordinator(repository), catalogStore)
        } finally {
            base.deleteRecursively()
        }
    }

    private class FakePort(
        override val completionKind: GalleryCompletionKind,
        override val currentDocument: DrawingDocument,
        private val finishAccepted: Boolean = true,
        val events: MutableList<String> = mutableListOf(),
    ) : ArtworkCompletionPort {
        override suspend fun saveBeforeFinish() {
            events += "save-before"
        }

        override suspend fun finishSemanticState(): Boolean {
            events += "finish-semantic"
            return finishAccepted
        }

        override suspend fun saveAfterFinish() {
            events += "save-after"
        }
    }

    private fun document(includeColor: Boolean = false): DrawingDocument {
        val base = DrawingDocumentEngine.newDocument(
            documentId = WORKING_ID,
            nowEpochMillis = 1_000L,
            metadata = DrawingDocumentMetadata("cute-cat", 1),
        )
        val line = DocumentOperation.AddInkStroke(
            operationId = "line-op",
            createdAtEpochMillis = 2_000L,
            stroke = stroke("line", 0xFF252422.toInt()),
        )
        val operations = if (includeColor) {
            listOf(
                line,
                DocumentOperation.AddColorStroke(
                    operationId = "color-op",
                    createdAtEpochMillis = 3_000L,
                    stroke = stroke("color", 0xFFF08B7A.toInt()),
                ),
            )
        } else {
            listOf(line)
        }
        return base.copy(modifiedAtEpochMillis = 4_000L, operations = operations)
    }

    private fun stroke(id: String, color: Int) = InkStrokeRecord(
        strokeId = id,
        brushPresetId = "marker.standard",
        colorArgb = color,
        opacity = 1f,
        baseSize = 18f,
        tool = PointerTool.FINGER,
        points = listOf(
            StrokePoint(120f, 120f, 0L, 1f),
            StrokePoint(200f, 200f, 16L, 1f),
        ),
    )

    private fun sequentialIds(): () -> String {
        var value = 0
        return { "completion-${++value}" }
    }

    private companion object {
        const val WORKING_ID = "lesson-lab-cute-cat-document"
    }
}
