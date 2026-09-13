package com.navin.kidsdrawing.product.gallery

import android.content.Context
import com.navin.kidsdrawing.coloring.session.ColoringSessionPhase
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.gallery.domain.ArtworkCompletionCoordinator
import com.navin.kidsdrawing.gallery.domain.ArtworkCompletionPort
import com.navin.kidsdrawing.gallery.domain.ArtworkCompletionResult
import com.navin.kidsdrawing.gallery.domain.GalleryArtworkSource
import com.navin.kidsdrawing.gallery.domain.GalleryCompletionKind
import com.navin.kidsdrawing.gallery.domain.GalleryDeleteResult
import com.navin.kidsdrawing.gallery.domain.GalleryListResult
import com.navin.kidsdrawing.gallery.domain.GalleryReopenResult
import com.navin.kidsdrawing.gallery.domain.GalleryRepository
import com.navin.kidsdrawing.gallery.persistence.AtomicGalleryCatalogStore
import com.navin.kidsdrawing.lesson.session.FinishForNow
import com.navin.kidsdrawing.lesson.session.LessonCommandResult
import com.navin.kidsdrawing.lesson.session.LessonFinishReason
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import com.navin.kidsdrawing.product.coloring.ProductColoringRuntime
import com.navin.kidsdrawing.product.lesson.ProductLessonRuntime
import java.io.File

/** Production/offline adapter for completion + local Gallery orchestration. */
class ProductGalleryRuntime private constructor(
    context: Context,
    private val protectedWorkingDocumentId: String,
    private val lessonRuntime: ProductLessonRuntime?,
    private val coloringRuntime: ProductColoringRuntime?,
) {
    constructor(
        context: Context,
        lessonRuntime: ProductLessonRuntime,
        coloringRuntime: ProductColoringRuntime,
    ) : this(
        context = context.applicationContext,
        protectedWorkingDocumentId = lessonRuntime.runtimeIdentity.documentId,
        lessonRuntime = lessonRuntime,
        coloringRuntime = coloringRuntime,
    )

    /** Gallery access that does not require an active lesson/coloring runtime. */
    constructor(
        context: Context,
        protectedWorkingDocumentId: String,
    ) : this(
        context = context.applicationContext,
        protectedWorkingDocumentId = protectedWorkingDocumentId,
        lessonRuntime = null,
        coloringRuntime = null,
    )

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
        protectedWorkingDocumentId = protectedWorkingDocumentId,
    )
    private val completionCoordinator = ArtworkCompletionCoordinator(repository)

    init {
        require(protectedWorkingDocumentId.isNotBlank()) {
            "Gallery protected working document ID cannot be blank."
        }
    }

    suspend fun finishDrawingForNow(title: String): ArtworkCompletionResult {
        if (lessonRuntime == null) {
            return ArtworkCompletionResult.Failed("No guided drawing is active to finish.")
        }
        return completionCoordinator.complete(DrawingCompletionPort(lessonRuntime), title)
    }

    suspend fun finishColoring(title: String): ArtworkCompletionResult {
        if (coloringRuntime == null) {
            return ArtworkCompletionResult.Failed("No coloring session is active to finish.")
        }
        return completionCoordinator.complete(ColoringCompletionPort(coloringRuntime), title)
    }

    /** Shared completion boundary for non-lesson product runtimes such as Free Draw. */
    suspend fun complete(
        port: ArtworkCompletionPort,
        title: String,
    ): ArtworkCompletionResult = completionCoordinator.complete(port, title)

    suspend fun listArtwork(): GalleryListResult = repository.listArtwork()

    suspend fun reopen(entryId: String): GalleryReopenResult = repository.reopen(entryId)

    suspend fun delete(entryId: String, confirmed: Boolean): GalleryDeleteResult =
        repository.delete(entryId, confirmed)

    fun previewFile(reference: String): File? = previewService.fileFor(reference)

    private class DrawingCompletionPort(
        private val runtime: ProductLessonRuntime,
    ) : ArtworkCompletionPort {
        override val source: GalleryArtworkSource = GalleryArtworkSource.LESSON
        override val completionKind: GalleryCompletionKind = GalleryCompletionKind.DRAWING_ONLY
        override val currentDocument: DrawingDocument
            get() = runtime.documentEngine.state.value.document

        override suspend fun saveBeforeFinish() {
            runtime.saveNow()
        }

        override suspend fun finishSemanticState(): Boolean {
            val current = runtime.sessionState.value
            if (current is LessonSessionState.Finished) {
                return current.reason == LessonFinishReason.FINISHED_FOR_NOW
            }
            return runtime.dispatch(FinishForNow) is LessonCommandResult.Accepted
        }

        override suspend fun saveAfterFinish() {
            runtime.saveNow()
        }
    }

    private class ColoringCompletionPort(
        private val runtime: ProductColoringRuntime,
    ) : ArtworkCompletionPort {
        override val source: GalleryArtworkSource = GalleryArtworkSource.LESSON
        override val completionKind: GalleryCompletionKind = GalleryCompletionKind.COLORED
        override val currentDocument: DrawingDocument
            get() = runtime.documentEngine.state.value.document

        override suspend fun saveBeforeFinish() {
            runtime.saveNow()
        }

        override suspend fun finishSemanticState(): Boolean {
            if (runtime.sessionState.value?.phase == ColoringSessionPhase.FINISHED) return true
            return runtime.finish()
        }

        override suspend fun saveAfterFinish() {
            runtime.saveNow()
        }
    }

    companion object {
        const val GALLERY_DIRECTORY = "gallery"
    }
}
