package com.navin.kidsdrawing.gallery.domain

const val CURRENT_GALLERY_CATALOG_SCHEMA_VERSION: Int = 1

enum class GalleryArtworkSource {
    LESSON,
    FREE_DRAW,
}

enum class GalleryCompletionKind {
    DRAWING_ONLY,
    COLORED,
}

enum class GalleryPreviewStatus {
    MISSING,
    READY,
    FAILED,
}

data class GalleryArtworkRecord(
    val entryId: String,
    val documentId: String,
    val title: String,
    val source: GalleryArtworkSource,
    val lessonId: String? = null,
    val lessonRevision: Int? = null,
    val completionKind: GalleryCompletionKind,
    val completedAtEpochMillis: Long,
    val previewStatus: GalleryPreviewStatus = GalleryPreviewStatus.MISSING,
    val previewReference: String? = null,
) {
    init {
        require(entryId.isNotBlank()) { "Gallery entry ID cannot be blank." }
        require(documentId.isNotBlank()) { "Gallery document ID cannot be blank." }
        require(title.isNotBlank()) { "Gallery title cannot be blank." }
        require(completedAtEpochMillis >= 0L) { "Gallery completion time cannot be negative." }
        require((lessonId == null) == (lessonRevision == null)) {
            "Lesson provenance must include both lesson ID and revision or neither."
        }
        lessonRevision?.let { require(it >= 1) { "Lesson revision must be positive." } }
        when (source) {
            GalleryArtworkSource.LESSON -> require(lessonId != null && lessonRevision != null) {
                "Lesson Gallery artwork requires lesson ID and revision provenance."
            }
            GalleryArtworkSource.FREE_DRAW -> require(lessonId == null && lessonRevision == null) {
                "Free Draw Gallery artwork cannot carry lesson provenance."
            }
        }
        when (previewStatus) {
            GalleryPreviewStatus.READY -> require(!previewReference.isNullOrBlank()) {
                "Ready Gallery preview requires a reference."
            }
            GalleryPreviewStatus.MISSING,
            GalleryPreviewStatus.FAILED,
            -> require(previewReference == null) {
                "Non-ready Gallery preview cannot keep a preview reference."
            }
        }
    }
}

data class GalleryCatalog(
    val schemaVersion: Int = CURRENT_GALLERY_CATALOG_SCHEMA_VERSION,
    val records: List<GalleryArtworkRecord> = emptyList(),
) {
    init {
        require(schemaVersion == CURRENT_GALLERY_CATALOG_SCHEMA_VERSION) {
            "Unsupported Gallery catalog schema: $schemaVersion"
        }
        require(records.map { it.entryId }.toSet().size == records.size) {
            "Gallery entry IDs must be unique."
        }
        require(records.map { it.documentId }.toSet().size == records.size) {
            "Gallery document IDs must be unique."
        }
    }

    fun newestFirst(): GalleryCatalog = copy(records = records.newestFirst())
}

fun List<GalleryArtworkRecord>.newestFirst(): List<GalleryArtworkRecord> =
    sortedWith(
        compareByDescending<GalleryArtworkRecord> { it.completedAtEpochMillis }
            .thenByDescending { it.entryId },
    )

data class GalleryArtworkCardModel(
    val record: GalleryArtworkRecord,
    val usablePreviewReference: String?,
) {
    val usesFallbackArtwork: Boolean
        get() = usablePreviewReference == null
}
