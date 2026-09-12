package com.navin.kidsdrawing.gallery.domain

import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.gallery.persistence.AtomicGalleryCatalogStore
import java.util.UUID

sealed interface GalleryPreviewGenerationResult {
    data class Ready(val reference: String) : GalleryPreviewGenerationResult
    data class Failed(val message: String? = null) : GalleryPreviewGenerationResult
}

interface GalleryPreviewService {
    suspend fun generate(entryId: String, document: DrawingDocument): GalleryPreviewGenerationResult
    fun exists(reference: String): Boolean
    suspend fun delete(reference: String)
}

object NoOpGalleryPreviewService : GalleryPreviewService {
    override suspend fun generate(
        entryId: String,
        document: DrawingDocument,
    ): GalleryPreviewGenerationResult = GalleryPreviewGenerationResult.Failed("Preview unavailable.")

    override fun exists(reference: String): Boolean = false

    override suspend fun delete(reference: String) = Unit
}

enum class GalleryPromotionFailureCode {
    EMPTY_ARTWORK,
    DOCUMENT_SAVE_FAILED,
    CATALOG_SAVE_FAILED,
}

sealed interface GalleryPromotionResult {
    data class Saved(
        val record: GalleryArtworkRecord,
        val document: DrawingDocument,
    ) : GalleryPromotionResult

    data class Failed(
        val code: GalleryPromotionFailureCode,
        val message: String,
    ) : GalleryPromotionResult
}

sealed interface GalleryListResult {
    data class Ready(val cards: List<GalleryArtworkCardModel>) : GalleryListResult
    data object Empty : GalleryListResult
    data class Unavailable(val message: String) : GalleryListResult
}

sealed interface GalleryReopenResult {
    data class Ready(
        val record: GalleryArtworkRecord,
        val document: DrawingDocument,
    ) : GalleryReopenResult

    data object EntryMissing : GalleryReopenResult
    data object ArtworkMissing : GalleryReopenResult
    data object ArtworkIncompatible : GalleryReopenResult
    data class CatalogUnavailable(val message: String) : GalleryReopenResult
}

sealed interface GalleryDeleteResult {
    data object ConfirmationRequired : GalleryDeleteResult
    data object EntryMissing : GalleryDeleteResult
    data object Deleted : GalleryDeleteResult
    data object ProtectedWorkingDocument : GalleryDeleteResult
    data class Failed(val message: String) : GalleryDeleteResult
}

/**
 * Local Gallery transaction boundary.
 *
 * DrawingDocument remains artwork truth. The catalog only indexes independently persisted promoted
 * documents. Preview generation runs strictly after both document and catalog truth are durable.
 */
class GalleryRepository(
    private val documentStore: AtomicDrawingDocumentStore,
    private val catalogStore: AtomicGalleryCatalogStore,
    private val previewService: GalleryPreviewService = NoOpGalleryPreviewService,
    private val protectedWorkingDocumentId: String,
    private val clockMillis: () -> Long = System::currentTimeMillis,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) {
    suspend fun promoteCompletedArtwork(
        workingDocument: DrawingDocument,
        title: String,
        completionKind: GalleryCompletionKind,
    ): GalleryPromotionResult {
        if (workingDocument.activeInkStrokes().isEmpty()) {
            return GalleryPromotionResult.Failed(
                GalleryPromotionFailureCode.EMPTY_ARTWORK,
                "There is no finished child drawing to save to Gallery.",
            )
        }

        val completedAt = clockMillis().coerceAtLeast(0L)
        val entryId = "gallery-${idFactory()}"
        val galleryDocumentId = "gallery-document-${idFactory()}"
        val promoted = workingDocument.copy(documentId = galleryDocumentId)

        try {
            // Atomic ordering invariant: authoritative Gallery document exists before metadata.
            documentStore.save(promoted)
        } catch (failure: Throwable) {
            return GalleryPromotionResult.Failed(
                GalleryPromotionFailureCode.DOCUMENT_SAVE_FAILED,
                failure.message ?: "The artwork could not be saved yet.",
            )
        }

        val baseRecord = GalleryArtworkRecord(
            entryId = entryId,
            documentId = galleryDocumentId,
            title = title,
            source = GalleryArtworkSource.LESSON,
            lessonId = promoted.metadata.lessonId,
            lessonRevision = promoted.metadata.lessonRevision,
            completionKind = completionKind,
            completedAtEpochMillis = completedAt,
        )

        try {
            catalogStore.upsert(baseRecord)
        } catch (failure: Throwable) {
            // There is no Gallery truth without the catalog record. Best-effort cleanup prevents
            // unindexed promoted documents while never touching the protected working artwork.
            runCatching { documentStore.delete(galleryDocumentId) }
            return GalleryPromotionResult.Failed(
                GalleryPromotionFailureCode.CATALOG_SAVE_FAILED,
                failure.message ?: "The Gallery could not update yet.",
            )
        }

        var finalRecord = baseRecord
        when (val preview = runCatching {
            previewService.generate(entryId, promoted)
        }.getOrElse { GalleryPreviewGenerationResult.Failed(it.message) }) {
            is GalleryPreviewGenerationResult.Ready -> {
                val ready = baseRecord.copy(
                    previewStatus = GalleryPreviewStatus.READY,
                    previewReference = preview.reference,
                )
                if (runCatching { catalogStore.upsert(ready) }.isSuccess) finalRecord = ready
            }

            is GalleryPreviewGenerationResult.Failed -> {
                val failed = baseRecord.copy(
                    previewStatus = GalleryPreviewStatus.FAILED,
                    previewReference = null,
                )
                if (runCatching { catalogStore.upsert(failed) }.isSuccess) finalRecord = failed
            }
        }

        return GalleryPromotionResult.Saved(finalRecord, promoted)
    }

    suspend fun listArtwork(): GalleryListResult = when (val loaded = catalogStore.load()) {
        AtomicGalleryCatalogStore.LoadResult.Missing -> GalleryListResult.Empty
        is AtomicGalleryCatalogStore.LoadResult.Corrupt -> GalleryListResult.Unavailable(
            "Your Gallery index needs recovery. Saved artwork files were left untouched.",
        )
        is AtomicGalleryCatalogStore.LoadResult.Loaded -> {
            if (loaded.catalog.records.isEmpty()) return GalleryListResult.Empty
            GalleryListResult.Ready(
                loaded.catalog.records.newestFirst().map { record ->
                    val usablePreview = record.previewReference
                        ?.takeIf { record.previewStatus == GalleryPreviewStatus.READY }
                        ?.takeIf(previewService::exists)
                    GalleryArtworkCardModel(record, usablePreview)
                },
            )
        }
    }

    suspend fun reopen(entryId: String): GalleryReopenResult {
        val record = when (val loaded = catalogStore.load()) {
            AtomicGalleryCatalogStore.LoadResult.Missing -> return GalleryReopenResult.EntryMissing
            is AtomicGalleryCatalogStore.LoadResult.Corrupt -> return GalleryReopenResult.CatalogUnavailable(
                "Your Gallery index could not be read. No artwork was deleted.",
            )
            is AtomicGalleryCatalogStore.LoadResult.Loaded ->
                loaded.catalog.records.firstOrNull { it.entryId == entryId }
                    ?: return GalleryReopenResult.EntryMissing
        }

        val loadedDocument = documentStore.load(record.documentId)
            ?: return GalleryReopenResult.ArtworkMissing
        val document = loadedDocument.document
        if (document.documentId != record.documentId) return GalleryReopenResult.ArtworkIncompatible
        if (record.lessonId != null && (
                document.metadata.lessonId != record.lessonId ||
                    document.metadata.lessonRevision != record.lessonRevision
                )
        ) {
            return GalleryReopenResult.ArtworkIncompatible
        }
        return GalleryReopenResult.Ready(record, document)
    }

    suspend fun delete(entryId: String, confirmed: Boolean): GalleryDeleteResult {
        if (!confirmed) return GalleryDeleteResult.ConfirmationRequired
        val record = when (val loaded = catalogStore.load()) {
            AtomicGalleryCatalogStore.LoadResult.Missing -> return GalleryDeleteResult.EntryMissing
            is AtomicGalleryCatalogStore.LoadResult.Corrupt -> return GalleryDeleteResult.Failed(
                "The Gallery index could not be read, so nothing was deleted.",
            )
            is AtomicGalleryCatalogStore.LoadResult.Loaded ->
                loaded.catalog.records.firstOrNull { it.entryId == entryId }
                    ?: return GalleryDeleteResult.EntryMissing
        }

        if (record.documentId == protectedWorkingDocumentId) {
            return GalleryDeleteResult.ProtectedWorkingDocument
        }

        return try {
            // Delete artwork first. If catalog mutation then fails, the record remains visible as a
            // recoverable missing-artwork state rather than falsely claiming the file was removed.
            documentStore.delete(record.documentId)
            catalogStore.remove(record.entryId)
            record.previewReference?.let { runCatching { previewService.delete(it) } }
            GalleryDeleteResult.Deleted
        } catch (failure: Throwable) {
            GalleryDeleteResult.Failed(failure.message ?: "The artwork could not be deleted safely.")
        }
    }
}
