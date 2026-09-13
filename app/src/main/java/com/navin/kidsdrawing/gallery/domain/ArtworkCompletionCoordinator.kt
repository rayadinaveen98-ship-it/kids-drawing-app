package com.navin.kidsdrawing.gallery.domain

import com.navin.kidsdrawing.drawing.domain.DrawingDocument

/** Engine-facing completion port. UI must never implement completion ordering itself. */
interface ArtworkCompletionPort {
    val source: GalleryArtworkSource
    val completionKind: GalleryCompletionKind
    val currentDocument: DrawingDocument

    /** Durable working-artwork/session boundary before semantic completion. */
    suspend fun saveBeforeFinish()

    /** Returns true only when the owning product runtime accepts completion. */
    suspend fun finishSemanticState(): Boolean

    /** Persists the finished semantic snapshot before Gallery promotion begins. */
    suspend fun saveAfterFinish()
}

sealed interface ArtworkCompletionResult {
    data class Saved(
        val record: GalleryArtworkRecord,
        val document: DrawingDocument,
    ) : ArtworkCompletionResult

    data class Failed(val message: String) : ArtworkCompletionResult
}

class ArtworkCompletionCoordinator(
    private val galleryRepository: GalleryRepository,
) {
    suspend fun complete(
        port: ArtworkCompletionPort,
        title: String,
    ): ArtworkCompletionResult {
        return try {
            port.saveBeforeFinish()
            if (!port.finishSemanticState()) {
                return ArtworkCompletionResult.Failed(
                    "This artwork is not ready to finish yet. Your work is still safe.",
                )
            }
            port.saveAfterFinish()
            when (val promoted = galleryRepository.promoteCompletedArtwork(
                workingDocument = port.currentDocument,
                title = title,
                source = port.source,
                completionKind = port.completionKind,
            )) {
                is GalleryPromotionResult.Saved -> ArtworkCompletionResult.Saved(
                    record = promoted.record,
                    document = promoted.document,
                )
                is GalleryPromotionResult.Failed -> ArtworkCompletionResult.Failed(promoted.message)
            }
        } catch (failure: Throwable) {
            ArtworkCompletionResult.Failed(
                failure.message ?: "The artwork could not finish saving yet. Your drawing is still safe.",
            )
        }
    }
}
