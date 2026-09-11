package com.navin.kidsdrawing.drawing.domain

enum class StrokeAuthorRole {
    CHILD,
    TEACHER_GENERATED,
}

enum class BackgroundRole {
    PAPER,
}

data class DrawingDocumentMetadata(
    val lessonId: String? = null,
    val lessonRevision: Int? = null,
)

data class EraseMaskRecord(
    val maskId: String,
    val baseSize: Float,
    val points: List<StrokePoint>,
) {
    init {
        require(maskId.isNotBlank()) { "maskId cannot be blank." }
        require(baseSize.isFinite() && baseSize > 0f) { "baseSize must be finite and positive." }
        require(points.isNotEmpty()) { "An erase mask must contain at least one point." }
    }
}

sealed interface DocumentOperation {
    val operationId: String
    val createdAtEpochMillis: Long

    data class AddInkStroke(
        override val operationId: String,
        override val createdAtEpochMillis: Long,
        val stroke: InkStrokeRecord,
    ) : DocumentOperation

    data class AddEraseMask(
        override val operationId: String,
        override val createdAtEpochMillis: Long,
        val mask: EraseMaskRecord,
    ) : DocumentOperation

    data class ClearDocument(
        override val operationId: String,
        override val createdAtEpochMillis: Long,
    ) : DocumentOperation
}

data class DrawingDocument(
    val documentSchemaVersion: Int = CURRENT_DOCUMENT_SCHEMA_VERSION,
    val documentId: String,
    val logicalSize: DocumentSize,
    val createdAtEpochMillis: Long,
    val modifiedAtEpochMillis: Long,
    val backgroundRole: BackgroundRole = BackgroundRole.PAPER,
    val metadata: DrawingDocumentMetadata = DrawingDocumentMetadata(),
    val operations: List<DocumentOperation> = emptyList(),
) {
    init {
        require(documentSchemaVersion > 0) { "documentSchemaVersion must be positive." }
        require(documentId.isNotBlank()) { "documentId cannot be blank." }
        require(modifiedAtEpochMillis >= createdAtEpochMillis) {
            "modifiedAtEpochMillis cannot precede createdAtEpochMillis."
        }
        require(operations.map { it.operationId }.toSet().size == operations.size) {
            "Document operation IDs must be unique."
        }
    }

    /** Operations that currently contribute to the visible child document after the last Clear. */
    fun activeOperations(): List<DocumentOperation> {
        val lastClearIndex = operations.indexOfLast { it is DocumentOperation.ClearDocument }
        return if (lastClearIndex < 0) operations else operations.drop(lastClearIndex + 1)
    }

    fun activeInkStrokes(): List<InkStrokeRecord> = activeOperations()
        .filterIsInstance<DocumentOperation.AddInkStroke>()
        .map { it.stroke }
}

const val CURRENT_DOCUMENT_SCHEMA_VERSION: Int = 1
