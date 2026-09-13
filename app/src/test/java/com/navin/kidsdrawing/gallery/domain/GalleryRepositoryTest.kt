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
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GalleryRepositoryTest {
    @Test
    fun lessonPromotionPreservesEditableOperationsAndProvenanceUnderDistinctIdentity() = withRoots { documentRoot, catalogRoot ->
        runBlocking {
            val ids = ArrayDeque(listOf("entry-1", "document-1"))
            val repository = repository(documentRoot, catalogRoot, idFactory = { ids.removeFirst() })
            val working = lessonWorkingDocument(includeColor = true)

            val result = repository.promoteCompletedArtwork(
                workingDocument = working,
                title = "Cute Cat",
                source = GalleryArtworkSource.LESSON,
                completionKind = GalleryCompletionKind.COLORED,
            ) as GalleryPromotionResult.Saved

            assertNotEquals(working.documentId, result.document.documentId)
            assertEquals("gallery-document-document-1", result.document.documentId)
            assertEquals(working.operations, result.document.operations)
            assertEquals(working.metadata, result.document.metadata)
            assertEquals(working.logicalSize, result.document.logicalSize)
            assertEquals(working.backgroundRole, result.document.backgroundRole)
            assertEquals(GalleryArtworkSource.LESSON, result.record.source)

            val reopened = repository.reopen(result.record.entryId) as GalleryReopenResult.Ready
            assertEquals(result.record.documentId, reopened.document.documentId)
            assertEquals(working.operations, reopened.document.operations)
            assertEquals("cute-cat", reopened.document.metadata.lessonId)
        }
    }

    @Test
    fun freeDrawPromotionHasExplicitSourceAndNoLessonProvenance() = withRoots { documentRoot, catalogRoot ->
        runBlocking {
            val repository = repository(documentRoot, catalogRoot)
            val working = freeDrawWorkingDocument()

            val result = repository.promoteCompletedArtwork(
                workingDocument = working,
                title = "My Drawing",
                source = GalleryArtworkSource.FREE_DRAW,
                completionKind = GalleryCompletionKind.DRAWING_ONLY,
            ) as GalleryPromotionResult.Saved

            assertEquals(GalleryArtworkSource.FREE_DRAW, result.record.source)
            assertNull(result.record.lessonId)
            assertNull(result.record.lessonRevision)
            assertNull(result.document.metadata.lessonId)
            assertNull(result.document.metadata.lessonRevision)
            assertNotEquals(working.documentId, result.document.documentId)

            val reopened = repository.reopen(result.record.entryId) as GalleryReopenResult.Ready
            assertEquals(GalleryArtworkSource.FREE_DRAW, reopened.record.source)
            assertEquals(working.operations, reopened.document.operations)
        }
    }

    @Test
    fun sourceAndDocumentProvenanceMustAgreeBeforePromotion() = withRoots { documentRoot, catalogRoot ->
        runBlocking {
            val repository = repository(documentRoot, catalogRoot)

            val lessonAsFreeDraw = repository.promoteCompletedArtwork(
                workingDocument = lessonWorkingDocument(),
                title = "Wrong source",
                source = GalleryArtworkSource.FREE_DRAW,
                completionKind = GalleryCompletionKind.DRAWING_ONLY,
            ) as GalleryPromotionResult.Failed
            assertEquals(GalleryPromotionFailureCode.INVALID_PROVENANCE, lessonAsFreeDraw.code)

            val freeDrawAsLesson = repository.promoteCompletedArtwork(
                workingDocument = freeDrawWorkingDocument(),
                title = "Wrong source",
                source = GalleryArtworkSource.LESSON,
                completionKind = GalleryCompletionKind.DRAWING_ONLY,
            ) as GalleryPromotionResult.Failed
            assertEquals(GalleryPromotionFailureCode.INVALID_PROVENANCE, freeDrawAsLesson.code)
        }
    }

    @Test
    fun failedPromotedDocumentSaveCreatesNoGalleryRecord() = withRoots { documentRoot, catalogRoot ->
        runBlocking {
            val failingDocumentStore = documentStore(documentRoot) { stage ->
                if (stage == AtomicDrawingDocumentStore.SaveStage.TEMP_SYNCED) {
                    throw IOException("injected Gallery document save failure")
                }
            }
            val catalogStore = AtomicGalleryCatalogStore(catalogRoot)
            val repository = GalleryRepository(
                documentStore = failingDocumentStore,
                catalogStore = catalogStore,
                protectedWorkingDocumentId = WORKING_ID,
                clockMillis = { 7_000L },
                idFactory = { UUID.randomUUID().toString() },
            )

            val result = repository.promoteCompletedArtwork(
                workingDocument = lessonWorkingDocument(),
                title = "Cute Cat",
                source = GalleryArtworkSource.LESSON,
                completionKind = GalleryCompletionKind.DRAWING_ONLY,
            )

            assertTrue(result is GalleryPromotionResult.Failed)
            assertEquals(
                GalleryPromotionFailureCode.DOCUMENT_SAVE_FAILED,
                (result as GalleryPromotionResult.Failed).code,
            )
            assertEquals(AtomicGalleryCatalogStore.LoadResult.Missing, catalogStore.load())
        }
    }

    @Test
    fun previewFailureLeavesSavedArtworkVisibleWithFallback() = withRoots { documentRoot, catalogRoot ->
        runBlocking {
            val preview = FakePreviewService(failGeneration = true)
            val repository = repository(documentRoot, catalogRoot, preview = preview)

            val saved = repository.promoteCompletedArtwork(
                workingDocument = lessonWorkingDocument(),
                title = "Cute Cat",
                source = GalleryArtworkSource.LESSON,
                completionKind = GalleryCompletionKind.DRAWING_ONLY,
            ) as GalleryPromotionResult.Saved
            assertEquals(GalleryPreviewStatus.FAILED, saved.record.previewStatus)

            val list = repository.listArtwork() as GalleryListResult.Ready
            assertEquals(1, list.cards.size)
            assertTrue(list.cards.single().usesFallbackArtwork)
            assertEquals(saved.record.documentId, list.cards.single().record.documentId)
        }
    }

    @Test
    fun missingDerivedPreviewFallsBackButReopenStillUsesAuthoritativeDocument() = withRoots { documentRoot, catalogRoot ->
        runBlocking {
            val preview = FakePreviewService(failGeneration = false, fileExists = false)
            val repository = repository(documentRoot, catalogRoot, preview = preview)
            val saved = repository.promoteCompletedArtwork(
                workingDocument = lessonWorkingDocument(includeColor = true),
                title = "Cute Cat",
                source = GalleryArtworkSource.LESSON,
                completionKind = GalleryCompletionKind.COLORED,
            ) as GalleryPromotionResult.Saved
            assertEquals(GalleryPreviewStatus.READY, saved.record.previewStatus)

            val list = repository.listArtwork() as GalleryListResult.Ready
            assertTrue(list.cards.single().usesFallbackArtwork)
            val reopened = repository.reopen(saved.record.entryId)
            assertTrue(reopened is GalleryReopenResult.Ready)
            reopened as GalleryReopenResult.Ready
            assertEquals(saved.record.documentId, reopened.document.documentId)
            assertTrue(reopened.document.hasColoringOperations())
        }
    }

    @Test
    fun missingAuthoritativeDocumentIsTypedFailureNeverPreviewReconstruction() = withRoots { documentRoot, catalogRoot ->
        runBlocking {
            val preview = FakePreviewService()
            val documentStore = documentStore(documentRoot)
            val catalogStore = AtomicGalleryCatalogStore(catalogRoot)
            val repository = GalleryRepository(
                documentStore = documentStore,
                catalogStore = catalogStore,
                previewService = preview,
                protectedWorkingDocumentId = WORKING_ID,
                clockMillis = { 8_000L },
                idFactory = sequentialIds(),
            )
            val saved = repository.promoteCompletedArtwork(
                workingDocument = lessonWorkingDocument(),
                title = "Cute Cat",
                source = GalleryArtworkSource.LESSON,
                completionKind = GalleryCompletionKind.DRAWING_ONLY,
            ) as GalleryPromotionResult.Saved
            documentStore.delete(saved.record.documentId)

            assertEquals(GalleryReopenResult.ArtworkMissing, repository.reopen(saved.record.entryId))
        }
    }

    @Test
    fun deleteRequiresConfirmationAndCannotTargetProtectedWorkingDocument() = withRoots { documentRoot, catalogRoot ->
        runBlocking {
            val documentStore = documentStore(documentRoot)
            val catalogStore = AtomicGalleryCatalogStore(catalogRoot)
            val repository = GalleryRepository(
                documentStore = documentStore,
                catalogStore = catalogStore,
                protectedWorkingDocumentId = WORKING_ID,
                clockMillis = { 9_000L },
                idFactory = sequentialIds(),
            )
            val saved = repository.promoteCompletedArtwork(
                workingDocument = lessonWorkingDocument(),
                title = "Cute Cat",
                source = GalleryArtworkSource.LESSON,
                completionKind = GalleryCompletionKind.DRAWING_ONLY,
            ) as GalleryPromotionResult.Saved

            assertEquals(
                GalleryDeleteResult.ConfirmationRequired,
                repository.delete(saved.record.entryId, confirmed = false),
            )
            assertTrue(repository.reopen(saved.record.entryId) is GalleryReopenResult.Ready)

            assertEquals(GalleryDeleteResult.Deleted, repository.delete(saved.record.entryId, confirmed = true))
            assertEquals(GalleryReopenResult.EntryMissing, repository.reopen(saved.record.entryId))

            documentStore.save(lessonWorkingDocument())
            catalogStore.upsert(
                GalleryArtworkRecord(
                    entryId = "bad-working-reference",
                    documentId = WORKING_ID,
                    title = "Working",
                    source = GalleryArtworkSource.LESSON,
                    lessonId = "cute-cat",
                    lessonRevision = 1,
                    completionKind = GalleryCompletionKind.DRAWING_ONLY,
                    completedAtEpochMillis = 10_000L,
                ),
            )
            assertEquals(
                GalleryDeleteResult.ProtectedWorkingDocument,
                repository.delete("bad-working-reference", confirmed = true),
            )
            assertTrue(documentStore.hasRecoverableDocument(WORKING_ID))
        }
    }

    private fun repository(
        documentRoot: File,
        catalogRoot: File,
        preview: GalleryPreviewService = FakePreviewService(),
        idFactory: () -> String = sequentialIds(),
    ) = GalleryRepository(
        documentStore = documentStore(documentRoot),
        catalogStore = AtomicGalleryCatalogStore(catalogRoot),
        previewService = preview,
        protectedWorkingDocumentId = WORKING_ID,
        clockMillis = { 6_000L },
        idFactory = idFactory,
    )

    private fun documentStore(
        root: File,
        faultInjector: (AtomicDrawingDocumentStore.SaveStage) -> Unit = {},
    ) = AtomicDrawingDocumentStore(
        rootDirectory = root,
        faultInjector = faultInjector,
        documentCodec = DrawingDocumentBinaryCodec(JvmStrokePayloadCodec),
    )

    private fun lessonWorkingDocument(includeColor: Boolean = false): DrawingDocument =
        workingDocument(
            metadata = DrawingDocumentMetadata(
                lessonId = "cute-cat",
                lessonRevision = 1,
            ),
            includeColor = includeColor,
        )

    private fun freeDrawWorkingDocument(): DrawingDocument =
        workingDocument(metadata = DrawingDocumentMetadata(), includeColor = false)

    private fun workingDocument(
        metadata: DrawingDocumentMetadata,
        includeColor: Boolean,
    ): DrawingDocument {
        val base = DrawingDocumentEngine.newDocument(
            documentId = WORKING_ID,
            nowEpochMillis = 1_000L,
            metadata = metadata,
        )
        val line = DocumentOperation.AddInkStroke(
            operationId = "line-op",
            createdAtEpochMillis = 2_000L,
            stroke = stroke("line", 0xFF242321.toInt()),
        )
        val operations = if (includeColor) {
            listOf(
                line,
                DocumentOperation.AddColorStroke(
                    operationId = "color-op",
                    createdAtEpochMillis = 3_000L,
                    stroke = stroke("color", 0xFFEF6C68.toInt()),
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
            StrokePoint(100f, 100f, 0L, 1f),
            StrokePoint(180f, 180f, 20L, 1f),
        ),
    )

    private fun sequentialIds(): () -> String {
        var value = 0
        return { "id-${++value}" }
    }

    private class FakePreviewService(
        private val failGeneration: Boolean = false,
        private val fileExists: Boolean = true,
    ) : GalleryPreviewService {
        val deleted = mutableListOf<String>()

        override suspend fun generate(
            entryId: String,
            document: DrawingDocument,
        ): GalleryPreviewGenerationResult = if (failGeneration) {
            GalleryPreviewGenerationResult.Failed("injected preview failure")
        } else {
            GalleryPreviewGenerationResult.Ready("$entryId.png")
        }

        override fun exists(reference: String): Boolean = fileExists

        override suspend fun delete(reference: String) {
            deleted += reference
        }
    }

    private fun withRoots(block: (File, File) -> Unit) {
        val base = File(
            System.getProperty("java.io.tmpdir"),
            "kids-drawing-gallery-repository-${UUID.randomUUID()}",
        )
        val documentRoot = File(base, "documents")
        val catalogRoot = File(base, "catalog")
        check(documentRoot.mkdirs())
        check(catalogRoot.mkdirs())
        try {
            block(documentRoot, catalogRoot)
        } finally {
            base.deleteRecursively()
        }
    }

    private companion object {
        const val WORKING_ID = "lesson-lab-cute-cat-document"
    }
}
