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
    val historyCursor: Int,
    val historyDepth: Int,
    val redoDepth: Int,
    val canUndoColoring: Boolean = false,
    val canRedoColoring: Boolean = false,
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
    private val _state = MutableStateFlow(stateFor(initialDocument))

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

    /** Commit child coloring without weakening the protected line-art operation role. */
    suspend fun commitColorStroke(stroke: InkStrokeRecord): DocumentOperation.AddColorStroke =
        mutationMutex.withLock {
            require(stroke.authorRole == StrokeAuthorRole.CHILD) {
                "Teacher-generated strokes cannot enter child coloring history."
            }
            val operation = DocumentOperation.AddColorStroke(
                operationId = nextId("color"),
                createdAtEpochMillis = clockMillis(),
                stroke = stroke,
            )
            appendNewOperation(operation)
            operation
        }

    /** Commit an erase mask that is structurally restricted to the coloring projection. */
    suspend fun commitColorEraseMask(mask: EraseMaskRecord): DocumentOperation.AddColorEraseMask =
        mutationMutex.withLock {
            val operation = DocumentOperation.AddColorEraseMask(
                operationId = nextId("color-erase"),
                createdAtEpochMillis = clockMillis(),
                mask = mask,
            )
            appendNewOperation(operation)
            operation
        }

    /** Commit a validated prepared-region fill as editable coloring history. */
    suspend fun commitColorRegionFill(
        fill: ColorRegionFillRecord,
    ): DocumentOperation.AddColorRegionFill = mutationMutex.withLock {
        val operation = DocumentOperation.AddColorRegionFill(
            operationId = nextId("color-fill"),
            createdAtEpochMillis = clockMillis(),
            fill = fill,
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
        undoLastOperation()
    }

    suspend fun redo(): Boolean = mutationMutex.withLock {
        redoLastOperation()
    }

    /**
     * Coloring UI may never cross into protected line-art history. This returns false at that
     * boundary instead of delegating a global Undo that could remove drawing operations.
     */
    suspend fun undoColoring(): Boolean = mutationMutex.withLock {
        val last = _state.value.document.operations.lastOrNull()
        if (last?.isColoringOperation() != true) return@withLock false
        undoLastOperation()
    }

    /** Redo only a coloring operation previously removed through coloring history. */
    suspend fun redoColoring(): Boolean = mutationMutex.withLock {
        val redo = redoStack.lastOrNull()
        if (redo?.isColoringOperation() != true) return@withLock false
        redoLastOperation()
    }

    private fun undoLastOperation(): Boolean {
        val current = _state.value.document
        val last = current.operations.lastOrNull() ?: return false

        redoStack.addLast(last)
        publishDocument(
            current.copy(
                documentSchemaVersion = CURRENT_DOCUMENT_SCHEMA_VERSION,
                modifiedAtEpochMillis = clockMillis(),
                operations = current.operations.dropLast(1),
            ),
        )
        return true
    }

    private fun redoLastOperation(): Boolean {
        val operation = redoStack.removeLastOrNull() ?: return false
        val current = _state.value.document
        publishDocument(
            current.copy(
                documentSchemaVersion = CURRENT_DOCUMENT_SCHEMA_VERSION,
                modifiedAtEpochMillis = clockMillis(),
                operations = current.operations + operation,
            ),
        )
        return true
    }

    private fun appendNewOperation(operation: DocumentOperation) {
        val current = _state.value.document
        redoStack.clear()
        publishDocument(
            current.copy(
                documentSchemaVersion = CURRENT_DOCUMENT_SCHEMA_VERSION,
                modifiedAtEpochMillis = clockMillis(),
                operations = current.operations + operation,
            ),
        )
    }

    private fun publishDocument(document: DrawingDocument) {
        _state.value = stateFor(document)
    }

    private fun stateFor(document: DrawingDocument): DrawingEngineState {
        val cursor = document.operations.size
        return DrawingEngineState(
            document = document,
            canUndo = cursor > 0,
            canRedo = redoStack.isNotEmpty(),
            historyCursor = cursor,
            historyDepth = cursor + redoStack.size,
            redoDepth = redoStack.size,
            canUndoColoring = document.operations.lastOrNull()?.isColoringOperation() == true,
            canRedoColoring = redoStack.lastOrNull()?.isColoringOperation() == true,
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
