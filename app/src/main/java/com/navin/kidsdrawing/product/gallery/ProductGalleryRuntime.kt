package com.navin.kidsdrawing.product.gallery

import android.content.Context
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.gallery.domain.ArtworkCompletionCoordinator
import com.navin.kidsdrawing.gallery.domain.ArtworkCompletionPort
import com.navin.kidsdrawing.gallery.domain.ArtworkCompletionResult
import com.navin.kidsdrawing.gallery.domain.GalleryCompletionKind
import com.navin.kidsdrawing.gallery.domain.GalleryDeleteResult
import com.navin.kidsdrawing.gallery.domain.GalleryListResult
import com.navin.kidsdrawing.gallery.domain.GalleryReopenResult
import com.navin.kidsdrawing.gallery.domain.GalleryRepository
import com.navin.kidsdrawing.gallery.persistence.AtomicGalleryCatalogStore
import com.navin.kidsdrawing.lesson.lab.LessonLabRuntimeCore
import com.navin.kidsdrawing.lesson.session.FinishForNow
import com.navin.kidsdrawing.lesson.session.LessonCommandResult
import com.navin.kidsdrawing.product.coloring.ProductColoringRuntime
import com.navin.kidsdrawing.product.lesson.ProductLessonRuntime
import java.io.File

/** Production/offline adapter for completion + local Gallery orchestration. */
class ProductGalleryRuntime(
    context: Context,
    private val lessonRuntime: ProductLessonRuntime,
    private val coloringRuntime: ProductColoringRuntime,
) {
    private val appContext = context.applicationContext
    private val previewService = AndroidGalleryPreviewService(appContext)
    private val documentStore = AtomicDrawingDocumentStore(
        File(appContext.filesDir, ProductLessonRuntime.DOCUMENT_DIRECTORY),
    )
    private val catalogStore = AtomicGalleryCatalogStore(
        File(appContext.filesDir, GALLERY_DIRECTORY),
    )
    private val repository = GalleryRepository(
        documentStore = documentStore,
        catalogStore = catalogStore,
        previewService = previewService,
        protectedWorkingDocumentId = LessonLabRuntimeCore.DOCUMENT_ID,
    )
    private val completionCoordinator = ArtworkCompletionCoordinator(repository)

    suspend fun finishDrawingForNow(title: String): ArtworkCompletionResult =
        completionCoordinator.complete(DrawingCompletionPort(), title)

    suspend fun finishColoring(title: String): ArtworkCompletionResult =
        completionCoordinator.complete(ColoringCompletionPort(), title)

    suspend fun listArtwork(): GalleryListResult = repository.listArtwork()

    suspend fun reopen(entryId: String): GalleryReopenResult = repository.reopen(entryId)

    suspend fun delete(entryId: String, confirmed: Boolean): GalleryDeleteResult =
        repository.delete(entryId, confirmed)

    fun previewFile(reference: String): File? = previewService.fileFor(reference)

    private inner class DrawingCompletionPort : ArtworkCompletionPort {
        override val completionKind: GalleryCompletionKind = GalleryCompletionKind.DRAWING_ONLY
        override val currentDocument: DrawingDocument
            get() = lessonRuntime.documentEngine.state.value.document

        override suspend fun saveBeforeFinish() {
            lessonRuntime.saveNow()
        }

        override suspend fun finishSemanticState(): Boolean =
            lessonRuntime.dispatch(FinishForNow) is LessonCommandResult.Accepted

        override suspend fun saveAfterFinish() {
            lessonRuntime.saveNow()
        }
    }

    private inner class ColoringCompletionPort : ArtworkCompletionPort {
        override val completionKind: GalleryCompletionKind = GalleryCompletionKind.COLORED
        override val currentDocument: DrawingDocument
            get() = coloringRuntime.documentEngine.state.value.document

        override suspend fun saveBeforeFinish() {
            // Persists active coloring + current DrawingDocument before the semantic finish signal.
            coloringRuntime.saveNow()
        }

        override suspend fun finishSemanticState(): Boolean = coloringRuntime.finish()

        override suspend fun saveAfterFinish() {
            coloringRuntime.saveNow()
        }
    }

    companion object {
        const val GALLERY_DIRECTORY = "gallery"
    }
}
