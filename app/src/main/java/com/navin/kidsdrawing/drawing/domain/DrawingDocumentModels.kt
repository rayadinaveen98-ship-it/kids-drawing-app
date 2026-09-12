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

    /** Protected child drawing / line-art ink. */
    data class AddInkStroke(
        override val operationId: String,
        override val createdAtEpochMillis: Long,
        val stroke: InkStrokeRecord,
    ) : DocumentOperation

    /** Drawing-stage erase. This retains legacy semantics for schema-1 documents. */
    data class AddEraseMask(
        override val operationId: String,
        override val createdAtEpochMillis: Long,
        val mask: EraseMaskRecord,
    ) : DocumentOperation

    /** Child-authored coloring ink composited below protected line art. */
    data class AddColorStroke(
        override val operationId: String,
        override val createdAtEpochMillis: Long,
        val stroke: InkStrokeRecord,
    ) : DocumentOperation

    /** Coloring-only erase. It may clear color projection pixels but never line-art pixels. */
    data class AddColorEraseMask(
        override val operationId: String,
        override val createdAtEpochMillis: Long,
        val mask: EraseMaskRecord,
    ) : DocumentOperation

    data class ClearDocument(
        override val operationId: String,
        override val createdAtEpochMillis: Long,
    ) : DocumentOperation
}

fun DocumentOperation.isColoringOperation(): Boolean =
    this is DocumentOperation.AddColorStroke || this is DocumentOperation.AddColorEraseMask

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
        require(documentSchemaVersion in 1..CURRENT_DOCUMENT_SCHEMA_VERSION) {
            "Unsupported document schema version: $documentSchemaVersion"
        }
        require(documentId.isNotBlank()) { "documentId cannot be blank." }
        require(modifiedAtEpochMillis >= createdAtEpochMillis) {
            "modifiedAtEpochMillis cannot precede createdAtEpochMillis."
        }
        require(operations.map { it.operationId }.toSet().size == operations.size) {
            "Document operation IDs must be unique."
        }
        if (documentSchemaVersion < COLORING_DOCUMENT_SCHEMA_VERSION) {
            require(operations.none(DocumentOperation::isColoringOperation)) {
                "Coloring operations require drawing document schema $COLORING_DOCUMENT_SCHEMA_VERSION+."
            }
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

    fun activeColorStrokes(): List<InkStrokeRecord> = activeOperations()
        .filterIsInstance<DocumentOperation.AddColorStroke>()
        .map { it.stroke }

    fun hasColoringOperations(): Boolean = activeOperations().any(DocumentOperation::isColoringOperation)
}

const val COLORING_DOCUMENT_SCHEMA_VERSION: Int = 2
const val CURRENT_DOCUMENT_SCHEMA_VERSION: Int = COLORING_DOCUMENT_SCHEMA_VERSION
