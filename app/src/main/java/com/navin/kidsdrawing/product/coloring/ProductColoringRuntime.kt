package com.navin.kidsdrawing.product.coloring

import android.content.Context
import com.navin.kidsdrawing.coloring.persistence.AtomicColoringSessionStore
import com.navin.kidsdrawing.coloring.session.ColoringSessionEngine
import com.navin.kidsdrawing.coloring.session.ColoringSessionMode
import com.navin.kidsdrawing.coloring.session.ColoringSessionPhase
import com.navin.kidsdrawing.coloring.session.ColoringSessionState
import com.navin.kidsdrawing.coloring.session.ColoringSessionTool
import com.navin.kidsdrawing.drawing.domain.DrawingTool
import com.navin.kidsdrawing.drawing.domain.DrawingToolEngine
import com.navin.kidsdrawing.drawing.domain.DrawingToolSettings
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.lesson.session.ChooseColorMyself
import com.navin.kidsdrawing.lesson.session.ChooseColorWithMe
import com.navin.kidsdrawing.lesson.session.ColoringHandoffMode
import com.navin.kidsdrawing.lesson.session.LessonCommandResult
import com.navin.kidsdrawing.product.lesson.ProductLessonRuntime
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ProductColoringStartResult {
    data class Ready(val state: ColoringSessionState) : ProductColoringStartResult
    data class Failed(val message: String) : ProductColoringStartResult
}

enum class ProductColoringRecoveryResult {
    RESTORED,
    MISSING,
    FINISHED,
    CORRUPT,
    ARTWORK_MISSING,
    ARTWORK_INCOMPATIBLE,
}

/**
 * Production vertical-slice coordinator for coloring.
 *
 * DrawingDocumentEngine remains the sole artwork authority. Coloring deliberately shares the
 * editable document with ProductLessonRuntime, but owns an independent DrawingToolEngine so
 * coloring palette/brush choices can never leak back into lesson drawing tools.
 */
class ProductColoringRuntime(
    context: Context,
    private val lessonRuntime: ProductLessonRuntime,
    private val clockMillis: () -> Long = System::currentTimeMillis,
) {
    private val appContext = context.applicationContext
    private val drawingStore = AtomicDrawingDocumentStore(
        File(appContext.filesDir, ProductLessonRuntime.DOCUMENT_DIRECTORY),
    )
    private val coloringStore = AtomicColoringSessionStore(
        File(appContext.filesDir, COLORING_SESSION_DIRECTORY),
    )
    private val coloringToolEngine = DrawingToolEngine()

    private var engine: ColoringSessionEngine? = null
    private val _sessionState = MutableStateFlow<ColoringSessionState?>(null)
    val sessionState: StateFlow<ColoringSessionState?> = _sessionState.asStateFlow()

    val documentEngine
        get() = lessonRuntime.documentEngine

    val toolEngine: DrawingToolEngine
        get() = coloringToolEngine

    /**
     * Executes the complete drawing→coloring handoff transaction in product order.
     *
     * A failed coloring initialization is reported back through the real Lesson Engine typed
     * failure signal; the completed drawing is never replaced or deleted.
     */
    suspend fun beginFromLesson(mode: ColoringSessionMode): ProductColoringStartResult {
        var initializedSessionId: String? = null
        var handoffAcknowledged = false
        return try {
            lessonRuntime.saveNow()

            val commandResult = lessonRuntime.dispatch(
                when (mode) {
                    ColoringSessionMode.COLOR_WITH_ME -> ChooseColorWithMe
                    ColoringSessionMode.COLOR_MYSELF -> ChooseColorMyself
                },
            )
            if (commandResult !is LessonCommandResult.Accepted) {
                return ProductColoringStartResult.Failed("Coloring choice is not available yet.")
            }

            val requestedMode = lessonRuntime.pendingColoringMode()
                ?: return failPendingHandoff("Lesson Engine did not expose a coloring handoff.")
            if (requestedMode.toColoringMode() != mode) {
                return failPendingHandoff("Coloring handoff mode did not match the selected choice.")
            }

            val document = lessonRuntime.documentEngine.state.value.document
            val validation = validateCompletedDocument(document.documentId)
            if (validation != null) return failPendingHandoff(validation)

            val packageData = lessonRuntime.packageData
                ?: return failPendingHandoff("The lesson package is unavailable for coloring.")
            val coloringEngine = ColoringSessionEngine.start(
                childDocumentId = document.documentId,
                lessonId = packageData.lesson.lessonId,
                lessonRevision = packageData.lesson.revision,
                mode = mode,
            )
            engine = coloringEngine
            _sessionState.value = coloringEngine.state.value
            syncToolEngine(coloringEngine.state.value)

            persistColoringState()
            initializedSessionId = coloringEngine.state.value.sessionId

            lessonRuntime.acknowledgeColoringInitialized()
            handoffAcknowledged = true

            runCatching { lessonRuntime.saveNow() }
            ProductColoringStartResult.Ready(coloringEngine.state.value)
        } catch (failure: Throwable) {
            if (!handoffAcknowledged) {
                initializedSessionId?.let { sessionId ->
                    runCatching { coloringStore.delete(sessionId) }
                }
                runCatching { lessonRuntime.rejectColoringInitialization() }
                runCatching { lessonRuntime.saveNow() }
                engine = null
                _sessionState.value = null
            }
            if (handoffAcknowledged) {
                val current = engine
                if (current != null) {
                    ProductColoringStartResult.Ready(current.state.value)
                } else {
                    ProductColoringStartResult.Failed(
                        failure.message ?: "Coloring could not open, but your drawing is safe.",
                    )
                }
            } else {
                ProductColoringStartResult.Failed(
                    failure.message ?: "Coloring could not open, but your drawing is safe.",
                )
            }
        }
    }

    /** Restores an active coloring session after process/app recreation. */
    suspend fun recoverActive(): ProductColoringRecoveryResult {
        val sessionId = ColoringSessionEngine.sessionIdFor(lessonRuntime.runtimeIdentity.documentId)
        return when (val loaded = coloringStore.load(sessionId)) {
            AtomicColoringSessionStore.LoadResult.Missing -> ProductColoringRecoveryResult.MISSING
            is AtomicColoringSessionStore.LoadResult.Corrupt -> ProductColoringRecoveryResult.CORRUPT
            is AtomicColoringSessionStore.LoadResult.Loaded -> {
                val snapshot = loaded.snapshot
                if (snapshot.phase != ColoringSessionPhase.ACTIVE) {
                    return ProductColoringRecoveryResult.FINISHED
                }
                val persistedDocument = drawingStore.load(snapshot.childDocumentId)
                    ?: return ProductColoringRecoveryResult.ARTWORK_MISSING
                val document = persistedDocument.document
                val packageData = lessonRuntime.packageData
                    ?: return ProductColoringRecoveryResult.ARTWORK_INCOMPATIBLE
                val compatible = document.documentId == snapshot.childDocumentId &&
                    snapshot.childDocumentId == lessonRuntime.runtimeIdentity.documentId &&
                    document.metadata.lessonId == snapshot.lessonId &&
                    document.metadata.lessonRevision == snapshot.lessonRevision &&
                    snapshot.lessonId == packageData.lesson.lessonId &&
                    snapshot.lessonRevision == packageData.lesson.revision &&
                    document.activeInkStrokes().isNotEmpty()
                if (!compatible) return ProductColoringRecoveryResult.ARTWORK_INCOMPATIBLE

                lessonRuntime.documentEngine.replaceDocument(document)
                val restored = ColoringSessionEngine.restore(snapshot)
                engine = restored
                _sessionState.value = restored.state.value
                syncToolEngine(restored.state.value)
                ProductColoringRecoveryResult.RESTORED
            }
        }
    }

    suspend fun commitColorStroke(stroke: InkStrokeRecord) {
        requireActive()
        lessonRuntime.documentEngine.commitColorStroke(stroke)
        persistArtworkAndSession()
    }

    suspend fun commitColorEraseMask(mask: EraseMaskRecord) {
        requireActive()
        lessonRuntime.documentEngine.commitColorEraseMask(mask)
        persistArtworkAndSession()
    }

    suspend fun undo(): Boolean {
        requireActive()
        val changed = lessonRuntime.documentEngine.undoColoring()
        if (changed) persistArtworkAndSession()
        return changed
    }

    suspend fun redo(): Boolean {
        requireActive()
        val changed = lessonRuntime.documentEngine.redoColoring()
        if (changed) persistArtworkAndSession()
        return changed
    }

    suspend fun selectColor(colorArgb: Int): Boolean {
        val coloringEngine = requireActive()
        val changed = coloringEngine.selectColor(colorArgb)
        if (changed) {
            publishAndSync(coloringEngine)
            persistColoringState()
        }
        return changed
    }

    suspend fun selectTool(tool: ColoringSessionTool): Boolean {
        val coloringEngine = requireActive()
        val changed = coloringEngine.selectTool(tool)
        if (changed) {
            publishAndSync(coloringEngine)
            persistColoringState()
        }
        return changed
    }

    suspend fun setBrushWidth(width: Float): Boolean {
        val coloringEngine = requireActive()
        val changed = coloringEngine.setBrushWidth(width)
        if (changed) {
            publishAndSync(coloringEngine)
            persistColoringState()
        }
        return changed
    }

    suspend fun saveNow() {
        val current = engine ?: return
        drawingStore.save(lessonRuntime.documentEngine.state.value.document)
        coloringStore.save(current.snapshot(clockMillis()))
    }

    suspend fun onBackground() = saveNow()

    suspend fun finish(): Boolean {
        val coloringEngine = requireActive()
        if (!coloringEngine.finish()) return false
        publishAndSync(coloringEngine)
        saveNow()
        return true
    }

    private suspend fun failPendingHandoff(message: String): ProductColoringStartResult.Failed {
        runCatching { lessonRuntime.rejectColoringInitialization() }
        runCatching { lessonRuntime.saveNow() }
        engine = null
        _sessionState.value = null
        return ProductColoringStartResult.Failed(message)
    }

    private fun validateCompletedDocument(documentId: String): String? {
        val document = lessonRuntime.documentEngine.state.value.document
        val packageData = lessonRuntime.packageData
            ?: return "The lesson package is unavailable for coloring."
        return when {
            document.documentId != documentId -> "The coloring document identity changed unexpectedly."
            document.metadata.lessonId != packageData.lesson.lessonId ->
                "The completed drawing belongs to a different lesson."
            document.metadata.lessonRevision != packageData.lesson.revision ->
                "The completed drawing revision is not compatible with this lesson."
            document.activeInkStrokes().isEmpty() ->
                "There is no completed child line art to protect for coloring."
            else -> null
        }
    }

    private suspend fun persistArtworkAndSession() {
        drawingStore.save(lessonRuntime.documentEngine.state.value.document)
        persistColoringState()
    }

    private suspend fun persistColoringState() {
        val current = engine ?: return
        coloringStore.save(current.snapshot(clockMillis()))
    }

    private fun publishAndSync(coloringEngine: ColoringSessionEngine) {
        val state = coloringEngine.state.value
        _sessionState.value = state
        syncToolEngine(state)
    }

    private fun syncToolEngine(state: ColoringSessionState) {
        toolEngine.replace(
            DrawingToolSettings(
                tool = when (state.selectedTool) {
                    ColoringSessionTool.BRUSH -> DrawingTool.PENCIL
                    ColoringSessionTool.ERASER -> DrawingTool.ERASER
                },
                colorArgb = state.selectedColorArgb,
                width = when (state.selectedTool) {
                    ColoringSessionTool.BRUSH -> state.brushWidth
                    ColoringSessionTool.ERASER -> maxOf(
                        state.brushWidth,
                        DrawingToolSettings.DEFAULT_ERASER_WIDTH,
                    )
                },
            ),
        )
    }

    private fun requireActive(): ColoringSessionEngine {
        val current = engine ?: error("No active coloring session.")
        check(current.state.value.phase == ColoringSessionPhase.ACTIVE) {
            "Coloring session is not active."
        }
        return current
    }

    private fun ColoringHandoffMode.toColoringMode(): ColoringSessionMode = when (this) {
        ColoringHandoffMode.COLOR_WITH_ME -> ColoringSessionMode.COLOR_WITH_ME
        ColoringHandoffMode.COLOR_MYSELF -> ColoringSessionMode.COLOR_MYSELF
    }

    companion object {
        const val COLORING_SESSION_DIRECTORY = "coloring-sessions"
    }
}
