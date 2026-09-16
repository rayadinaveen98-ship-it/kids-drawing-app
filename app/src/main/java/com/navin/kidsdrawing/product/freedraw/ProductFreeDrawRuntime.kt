package com.navin.kidsdrawing.product.freedraw

import android.content.Context
import com.navin.kidsdrawing.drawing.domain.DrawingBrushPreset
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.gallery.domain.ArtworkCompletionPort
import com.navin.kidsdrawing.gallery.domain.ArtworkCompletionResult
import com.navin.kidsdrawing.gallery.domain.GalleryArtworkSource
import com.navin.kidsdrawing.gallery.domain.GalleryCompletionKind
import com.navin.kidsdrawing.product.gallery.ProductGalleryRuntime
import com.navin.kidsdrawing.product.lesson.ProductLessonRuntime
import com.navin.kidsdrawing.product.quality.ProductTimingEvidence
import com.navin.kidsdrawing.product.quality.ProductTimingMetric
import java.io.File

sealed interface FreeDrawFinishResult {
    data class Saved(
        val completion: ArtworkCompletionResult.Saved,
        val workingCanvasReset: Boolean,
    ) : FreeDrawFinishResult

    data class Failed(val message: String) : FreeDrawFinishResult
}

/** Android product adapter around [FreeDrawRuntimeCore]. */
class ProductFreeDrawRuntime(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val core = FreeDrawRuntimeCore(
        documentStore = AtomicDrawingDocumentStore(
            File(appContext.filesDir, ProductLessonRuntime.DOCUMENT_DIRECTORY),
        ),
        toolSettingsStore = AndroidFreeDrawToolSettingsStore(appContext),
    )

    val galleryRuntime = ProductGalleryRuntime(
        context = appContext,
        protectedWorkingDocumentId = FreeDrawRuntimeCore.WORKING_DOCUMENT_ID,
    )

    val documentEngine get() = core.documentEngine
    val toolEngine get() = core.toolEngine

    suspend fun recover(): FreeDrawRecoveryOutcome =
        ProductTimingEvidence.measure(ProductTimingMetric.FREE_DRAW_RECOVERY) {
            core.recover()
        }

    suspend fun commitChildStroke(stroke: InkStrokeRecord) = core.commitChildStroke(stroke)

    suspend fun commitEraseMask(mask: EraseMaskRecord) = core.commitEraseMask(mask)

    suspend fun undo(): Boolean = core.undo()

    suspend fun redo(): Boolean = core.redo()

    suspend fun clear(confirmed: Boolean): FreeDrawClearResult = core.clear(confirmed)

    suspend fun selectBrush(preset: DrawingBrushPreset) = core.selectBrush(preset)

    suspend fun selectEraser() = core.selectEraser()

    suspend fun setColor(colorArgb: Int) = core.setColor(colorArgb)

    suspend fun setWidth(width: Float) = core.setWidth(width)

    suspend fun saveNow() = core.saveNow()

    suspend fun onBackground() = core.onBackground()

    fun hasVisibleArtwork(): Boolean = core.hasVisibleArtwork()

    suspend fun finishToGallery(title: String): FreeDrawFinishResult {
        val completion = galleryRuntime.complete(FreeDrawCompletionPort(), title)
        return when (completion) {
            is ArtworkCompletionResult.Failed -> FreeDrawFinishResult.Failed(completion.message)
            is ArtworkCompletionResult.Saved -> {
                val reset = runCatching { core.resetAfterGalleryPromotion() }.isSuccess
                FreeDrawFinishResult.Saved(
                    completion = completion,
                    workingCanvasReset = reset,
                )
            }
        }
    }

    private inner class FreeDrawCompletionPort : ArtworkCompletionPort {
        override val source: GalleryArtworkSource = GalleryArtworkSource.FREE_DRAW
        override val completionKind: GalleryCompletionKind = GalleryCompletionKind.DRAWING_ONLY
        override val currentDocument
            get() = core.documentEngine.state.value.document

        override suspend fun saveBeforeFinish() {
            core.saveNow()
        }

        override suspend fun finishSemanticState(): Boolean = core.hasVisibleArtwork()

        override suspend fun saveAfterFinish() {
            core.saveNow()
        }
    }

    companion object {
        const val WORKING_DOCUMENT_ID: String = FreeDrawRuntimeCore.WORKING_DOCUMENT_ID
    }
}
