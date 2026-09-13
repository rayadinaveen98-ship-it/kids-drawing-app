package com.navin.kidsdrawing.product.freedraw

import com.navin.kidsdrawing.drawing.domain.DrawingBrushPreset
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentMetadata
import com.navin.kidsdrawing.drawing.domain.DrawingTool
import com.navin.kidsdrawing.drawing.domain.DrawingToolEngine
import com.navin.kidsdrawing.drawing.domain.DrawingToolSettings
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore

sealed interface FreeDrawRecoveryOutcome {
    data object BlankCreated : FreeDrawRecoveryOutcome
    data class Restored(val source: AtomicDrawingDocumentStore.LoadSource) : FreeDrawRecoveryOutcome
    data object CorruptOrUnreadable : FreeDrawRecoveryOutcome
    data object IncompatibleDocument : FreeDrawRecoveryOutcome
}

sealed interface FreeDrawClearResult {
    data object ConfirmationRequired : FreeDrawClearResult
    data object Cleared : FreeDrawClearResult
}

/**
 * Product-independent Free Draw owner over the verified Drawing Engine and atomic document store.
 *
 * The working document remains the only artwork truth. Tool state is deliberately persisted outside
 * the document because a selected brush/color is presentation state, not drawing history.
 */
internal class FreeDrawRuntimeCore(
    private val documentStore: AtomicDrawingDocumentStore,
    private val toolSettingsStore: FreeDrawToolSettingsPersistence,
    private val clockMillis: () -> Long = System::currentTimeMillis,
) {
    val documentEngine = DrawingDocumentEngine(newWorkingDocument())
    val toolEngine = DrawingToolEngine(initialFreeDrawSettings())

    suspend fun recover(): FreeDrawRecoveryOutcome {
        toolEngine.replace(toolSettingsStore.load().normalizedForFreeDraw())
        val hadPersistedFiles = documentStore.hasRecoverableDocument(WORKING_DOCUMENT_ID)
        val loaded = documentStore.load(WORKING_DOCUMENT_ID)
        if (loaded == null) {
            val blank = newWorkingDocument()
            documentEngine.replaceDocument(blank)
            if (!hadPersistedFiles) documentStore.save(blank)
            return if (hadPersistedFiles) {
                FreeDrawRecoveryOutcome.CorruptOrUnreadable
            } else {
                FreeDrawRecoveryOutcome.BlankCreated
            }
        }

        val document = loaded.document
        if (!document.isCompatibleFreeDrawWorkingDocument()) {
            documentEngine.replaceDocument(newWorkingDocument())
            return FreeDrawRecoveryOutcome.IncompatibleDocument
        }

        documentEngine.replaceDocument(document)
        return FreeDrawRecoveryOutcome.Restored(loaded.source)
    }

    suspend fun commitChildStroke(rawStroke: InkStrokeRecord) {
        val settings = toolEngine.state.value
        require(settings.tool == DrawingTool.PENCIL) {
            "Ink strokes can only be committed while a drawing brush is selected."
        }
        val normalized = rawStroke.copy(
            brushPresetId = settings.brushPreset.persistedPresetId,
            colorArgb = settings.colorArgb,
            opacity = settings.brushPreset.opacity,
            baseSize = settings.width,
        )
        documentEngine.commitChildStroke(normalized)
        saveDocument()
    }

    suspend fun commitEraseMask(mask: EraseMaskRecord) {
        documentEngine.commitEraseMask(mask)
        saveDocument()
    }

    suspend fun undo(): Boolean {
        val changed = documentEngine.undo()
        if (changed) saveDocument()
        return changed
    }

    suspend fun redo(): Boolean {
        val changed = documentEngine.redo()
        if (changed) saveDocument()
        return changed
    }

    suspend fun clear(confirmed: Boolean): FreeDrawClearResult {
        if (!confirmed) return FreeDrawClearResult.ConfirmationRequired
        documentEngine.clear()
        saveDocument()
        return FreeDrawClearResult.Cleared
    }

    suspend fun selectBrush(preset: DrawingBrushPreset) {
        toolEngine.selectBrushPreset(preset)
        saveToolSettings()
    }

    suspend fun selectEraser() {
        toolEngine.selectTool(DrawingTool.ERASER)
        saveToolSettings()
    }

    suspend fun setColor(colorArgb: Int) {
        toolEngine.setColor(colorArgb)
        saveToolSettings()
    }

    suspend fun setWidth(width: Float) {
        toolEngine.setWidth(width)
        saveToolSettings()
    }

    suspend fun saveNow() {
        saveDocument()
        saveToolSettings()
    }

    suspend fun onBackground() = saveNow()

    suspend fun resetAfterGalleryPromotion() {
        // The promoted Gallery document is already independent at this point. Persist the fresh
        // working canvas before exposing it in memory so a rare storage failure can never leave
        // the UI blank while the durable resume file still contains the pre-finish artwork.
        val blank = newWorkingDocument()
        documentStore.save(blank)
        documentEngine.replaceDocument(blank)
    }

    fun hasVisibleArtwork(): Boolean = documentEngine.state.value.document.activeInkStrokes().isNotEmpty()

    private suspend fun saveDocument() {
        documentStore.save(documentEngine.state.value.document)
    }

    private suspend fun saveToolSettings() {
        toolSettingsStore.save(toolEngine.state.value)
    }

    private fun newWorkingDocument(): DrawingDocument = DrawingDocumentEngine.newDocument(
        documentId = WORKING_DOCUMENT_ID,
        nowEpochMillis = clockMillis().coerceAtLeast(0L),
        metadata = DrawingDocumentMetadata(),
    )

    private fun DrawingDocument.isCompatibleFreeDrawWorkingDocument(): Boolean =
        documentId == WORKING_DOCUMENT_ID &&
            metadata.lessonId == null &&
            metadata.lessonRevision == null

    private fun DrawingToolSettings.normalizedForFreeDraw(): DrawingToolSettings = copy(
        width = width.coerceIn(DrawingToolSettings.MIN_TOOL_WIDTH, DrawingToolSettings.MAX_TOOL_WIDTH),
    )

    companion object {
        const val WORKING_DOCUMENT_ID = "free-draw-working-v1"

        fun initialFreeDrawSettings(): DrawingToolSettings = DrawingToolSettings(
            tool = DrawingTool.PENCIL,
            brushPreset = DrawingBrushPreset.PENCIL,
            colorArgb = DrawingToolSettings.DEFAULT_PENCIL_COLOR_ARGB,
            width = DrawingBrushPreset.PENCIL.defaultWidth,
        )
    }
}
