package com.navin.kidsdrawing.drawing.domain

import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class DrawingEngineState(
    val document: DrawingDocument,
    val canUndo: Boolean,
    val canRedo: Boolean,
)

/**
 * Authoritative operation/history owner for the editable child document.
 *
 * Mutations are serialized behind one engine-owned Mutex. Undo/redo moves operations between the
 * document timeline and a redo stack; it never stores full-canvas bitmap snapshots.
 */
class DrawingDocumentEngine(
    initialDocument: DrawingDocument,
    private val clockMillis: () -> Long = System::currentTimeMillis,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) {
    private val mutationMutex = Mutex()
    private val redoStack = ArrayDeque<DocumentOperation>()
    private val _state = MutableStateFlow(
        DrawingEngineState(
            document = initialDocument,
            canUndo = initialDocument.operations.isNotEmpty(),
            canRedo = false,
        ),
    )

    val state: StateFlow<DrawingEngineState> = _state.asStateFlow()

    suspend fun commitChildStroke(stroke: InkStrokeRecord): DocumentOperation.AddInkStroke =
        mutationMutex.withLock {
            require(stroke.authorRole == StrokeAuthorRole.CHILD) {
                "Teacher-generated strokes cannot enter child document history."
            }
            val operation = DocumentOperation.AddInkStroke(
                operationId = nextId("ink"),
                createdAtEpochMillis = clockMillis(),
                stroke = stroke,
            )
            appendNewOperation(operation)
            operation
        }

    suspend fun commitEraseMask(mask: EraseMaskRecord): DocumentOperation.AddEraseMask =
        mutationMutex.withLock {
            val operation = DocumentOperation.AddEraseMask(
                operationId = nextId("erase"),
                createdAtEpochMillis = clockMillis(),
                mask = mask,
            )
            appendNewOperation(operation)
            operation
        }

    suspend fun clear(): DocumentOperation.ClearDocument = mutationMutex.withLock {
        val operation = DocumentOperation.ClearDocument(
            operationId = nextId("clear"),
            createdAtEpochMillis = clockMillis(),
        )
        appendNewOperation(operation)
        operation
    }

    /**
     * Replaces the current editable document with a decoded/recovered document.
     *
     * This is intentionally an engine mutation rather than a StateFlow assignment so a reload
     * cannot race with a live edit. A persisted document is authoritative on restore; any redo
     * branch belonging to the previous in-memory timeline must be discarded.
     */
    suspend fun replaceDocument(document: DrawingDocument) = mutationMutex.withLock {
        redoStack.clear()
        publishDocument(document)
    }

    suspend fun undo(): Boolean = mutationMutex.withLock {
        val current = _state.value.document
        val last = current.operations.lastOrNull() ?: return@withLock false

        redoStack.addLast(last)
        publishDocument(
            current.copy(
                modifiedAtEpochMillis = clockMillis(),
                operations = current.operations.dropLast(1),
            ),
        )
        true
    }

    suspend fun redo(): Boolean = mutationMutex.withLock {
        val operation = redoStack.removeLastOrNull() ?: return@withLock false
        val current = _state.value.document
        publishDocument(
            current.copy(
                modifiedAtEpochMillis = clockMillis(),
                operations = current.operations + operation,
            ),
        )
        true
    }

    private fun appendNewOperation(operation: DocumentOperation) {
        val current = _state.value.document
        redoStack.clear()
        publishDocument(
            current.copy(
                modifiedAtEpochMillis = clockMillis(),
                operations = current.operations + operation,
            ),
        )
    }

    private fun publishDocument(document: DrawingDocument) {
        _state.value = DrawingEngineState(
            document = document,
            canUndo = document.operations.isNotEmpty(),
            canRedo = redoStack.isNotEmpty(),
        )
    }

    private fun nextId(prefix: String): String = "$prefix-${idFactory()}"

    companion object {
        fun newDocument(
            logicalSize: DocumentSize = DocumentSize(1000f, 1000f),
            documentId: String = UUID.randomUUID().toString(),
            nowEpochMillis: Long = System.currentTimeMillis(),
            metadata: DrawingDocumentMetadata = DrawingDocumentMetadata(),
        ): DrawingDocument = DrawingDocument(
            documentId = documentId,
            logicalSize = logicalSize,
            createdAtEpochMillis = nowEpochMillis,
            modifiedAtEpochMillis = nowEpochMillis,
            metadata = metadata,
        )
    }
}
