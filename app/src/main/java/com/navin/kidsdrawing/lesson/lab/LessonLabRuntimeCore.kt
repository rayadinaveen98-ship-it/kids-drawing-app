package com.navin.kidsdrawing.lesson.lab

import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentMetadata
import com.navin.kidsdrawing.drawing.domain.DrawingToolEngine
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.StrokeAuthorRole
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackSession
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackStatus
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.lesson.assistance.GuideOverlayRequest
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.persistence.AtomicLessonSessionStore
import com.navin.kidsdrawing.lesson.persistence.LessonRecoveryCoordinator
import com.navin.kidsdrawing.lesson.persistence.LessonSessionAutosaveCoordinator
import com.navin.kidsdrawing.lesson.session.ColoringHandoffCompleted
import com.navin.kidsdrawing.lesson.session.ColoringHandoffFailed
import com.navin.kidsdrawing.lesson.session.ColoringHandoffFailureObserved
import com.navin.kidsdrawing.lesson.session.ColoringHandoffMode
import com.navin.kidsdrawing.lesson.session.ColoringHandoffRequested
import com.navin.kidsdrawing.lesson.session.GuideOverlayCleared
import com.navin.kidsdrawing.lesson.session.GuideOverlayRequested
import com.navin.kidsdrawing.lesson.session.LessonCommand
import com.navin.kidsdrawing.lesson.session.LessonCommandResult
import com.navin.kidsdrawing.lesson.session.LessonFinished
import com.navin.kidsdrawing.lesson.session.LessonRuntimeResetRequested
import com.navin.kidsdrawing.lesson.session.LessonRuntimeSignal
import com.navin.kidsdrawing.lesson.session.LessonSessionEngine
import com.navin.kidsdrawing.lesson.session.LessonSessionEvent
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import com.navin.kidsdrawing.lesson.session.LessonSignalResult
import com.navin.kidsdrawing.lesson.session.TeacherPlaybackCancelRequested
import com.navin.kidsdrawing.lesson.session.TeacherPlaybackPaceChangeRequested
import com.navin.kidsdrawing.lesson.session.TeacherPlaybackPauseRequested
import com.navin.kidsdrawing.lesson.session.TeacherPlaybackRequested
import com.navin.kidsdrawing.lesson.session.TeacherPlaybackResumeRequested
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Pure-Kotlin/file-system integration boundary behind the Android Lesson Lab launcher.
 *
 * The Android wrapper only supplies content and storage roots. This core is the actual controller
 * used by the APK and by the P2.6 JVM integration matrix, so tests exercise the same command/event
 * bridge that runs on device rather than a duplicate harness.
 */
open class LessonLabRuntimeCore internal constructor(
    documentRoot: File,
    sessionRoot: File,
    private val lessonPackageResult: LessonLoadResult,
) {
    val documentEngine = DrawingDocumentEngine(newLessonDocument())
    val toolEngine = DrawingToolEngine()
    val teacherSession = TeacherPlaybackSession()

    private val documentStore = AtomicDrawingDocumentStore(documentRoot)
    private val sessionStore = AtomicLessonSessionStore(sessionRoot)
    private val lessonPackage: LessonRuntimePackage? =
        (lessonPackageResult as? LessonLoadResult.Success)?.packageData

    private val autosave = LessonSessionAutosaveCoordinator(sessionStore::save)
    private val recovery = LessonRecoveryCoordinator.fromStores(
        sessionStore = sessionStore,
        drawingStore = documentStore,
        resolveLessonPackage = { lessonId, revision ->
            lessonPackage?.takeIf {
                it.lesson.lessonId == lessonId && it.lesson.revision == revision
            }
        },
    )

    private var lessonEngine: LessonSessionEngine? = null
    private var activeTeacherRequestId: String? = null
    private var pendingColoringMode: ColoringHandoffMode? = null

    private val _sessionState = MutableStateFlow<LessonSessionState?>(null)
    val sessionState: StateFlow<LessonSessionState?> = _sessionState.asStateFlow()

    private val _guideOverlay = MutableStateFlow<GuideOverlayRequest?>(null)
    val guideOverlay: StateFlow<GuideOverlayRequest?> = _guideOverlay.asStateFlow()

    private val _diagnostics = MutableStateFlow(
        LessonLabDiagnostics(
            packageStatus = when (val loaded = lessonPackageResult) {
                is LessonLoadResult.Success ->
                    "Loaded ${loaded.packageData.lesson.lessonId} r${loaded.packageData.lesson.revision}"
                is LessonLoadResult.Failure ->
                    "Lesson load failed: ${loaded.diagnostics.firstOrNull()?.message ?: "unknown"}"
            },
        ),
    )
    val diagnostics: StateFlow<LessonLabDiagnostics> = _diagnostics.asStateFlow()

    suspend fun recover(): LessonLabRecoveryOutcome {
        teacherSession.cancel()
        activeTeacherRequestId = null
        pendingColoringMode = null
        _guideOverlay.value = null

        return when (val result = recovery.recover(SESSION_ID)) {
            LessonRecoveryCoordinator.RecoveryResult.MissingSession -> {
                val persistedDocument = documentStore.load(DOCUMENT_ID)
                if (persistedDocument != null) {
                    documentEngine.replaceDocument(persistedDocument.document)
                    publishDiagnostics("Artwork found; no resumable lesson session")
                    LessonLabRecoveryOutcome.ARTWORK_ONLY
                } else {
                    documentEngine.replaceDocument(newLessonDocument())
                    publishDiagnostics("No saved Lesson Lab session")
                    LessonLabRecoveryOutcome.NONE
                }
            }

            is LessonRecoveryCoordinator.RecoveryResult.CorruptSession -> {
                publishDiagnostics("Session corrupt; child document was not guessed")
                LessonLabRecoveryOutcome.SESSION_CORRUPT
            }

            is LessonRecoveryCoordinator.RecoveryResult.MissingChildDocument -> {
                publishDiagnostics("Lesson session exists but child artwork is missing")
                LessonLabRecoveryOutcome.CHILD_DOCUMENT_MISSING
            }

            is LessonRecoveryCoordinator.RecoveryResult.IncompatibleChildDocument -> {
                documentEngine.replaceDocument(result.childDocument)
                publishDiagnostics("Child artwork preserved; metadata is incompatible")
                LessonLabRecoveryOutcome.ARTWORK_PRESERVED_FATAL
            }

            is LessonRecoveryCoordinator.RecoveryResult.MissingLessonRevision -> {
                documentEngine.replaceDocument(result.childDocument)
                publishDiagnostics("Child artwork preserved; lesson revision unavailable")
                LessonLabRecoveryOutcome.ARTWORK_PRESERVED_FATAL
            }

            is LessonRecoveryCoordinator.RecoveryResult.IncompatibleSession -> {
                documentEngine.replaceDocument(result.childDocument)
                publishDiagnostics("Child artwork preserved; lesson snapshot incompatible")
                LessonLabRecoveryOutcome.ARTWORK_PRESERVED_FATAL
            }

            is LessonRecoveryCoordinator.RecoveryResult.Restored -> {
                documentEngine.replaceDocument(result.childDocument)
                lessonEngine = result.engine
                processEvents(result.restoreEvents)
                _sessionState.value = result.engine.state
                publishDiagnostics(
                    "Recovered ${result.normalization.name.lowercase()} · " +
                        "session=${result.sessionSource.name.lowercase()} · " +
                        "art=${result.childDocumentSource.name.lowercase()}",
                )
                LessonLabRecoveryOutcome.RESTORED
            }
        }
    }

    suspend fun newSessionDocument() {
        teacherSession.cancel()
        activeTeacherRequestId = null
        pendingColoringMode = null
        _guideOverlay.value = null
        lessonEngine = null
        _sessionState.value = null
        sessionStore.delete(SESSION_ID)
        val blank = newLessonDocument()
        documentEngine.replaceDocument(blank)
        documentStore.save(blank)
        publishDiagnostics("New blank Lesson Lab document")
    }

    suspend fun start(mode: TeachingMode, pace: TeachingPace): LessonCommandResult? {
        val packageData = lessonPackage
        if (packageData == null) {
            publishDiagnostics("Cannot start: bundled lesson package is invalid")
            return null
        }
        teacherSession.cancel()
        activeTeacherRequestId = null
        pendingColoringMode = null
        _guideOverlay.value = null

        val engine = LessonSessionEngine.create(
            lessonPackage = packageData,
            sessionId = SESSION_ID,
            childDocumentId = DOCUMENT_ID,
        )
        lessonEngine = engine
        val result = engine.dispatch(LessonCommand.StartLesson(mode, pace))
        consumeCommandResult(engine, result)
        return result
    }

    suspend fun dispatch(command: LessonCommand): LessonCommandResult? {
        val engine = lessonEngine ?: run {
            publishDiagnostics("No active lesson session")
            return null
        }
        val result = engine.dispatch(command)
        consumeCommandResult(engine, result)
        return result
    }

    suspend fun commitChildStroke(stroke: InkStrokeRecord) {
        val operation = documentEngine.commitChildStroke(stroke)
        documentStore.save(documentEngine.state.value.document)
        val engine = lessonEngine
        if (engine != null) {
            consumeSignalResult(
                engine,
                engine.handle(
                    LessonRuntimeSignal.ChildStrokeCommitted(
                        childDocumentId = DOCUMENT_ID,
                        operationId = operation.operationId,
                    ),
                ),
            )
        } else {
            publishDiagnostics("Child stroke committed outside active lesson")
        }
    }

    suspend fun commitEraseMask(mask: EraseMaskRecord) {
        documentEngine.commitEraseMask(mask)
        documentStore.save(documentEngine.state.value.document)
        publishDiagnostics("Erase mask committed")
    }

    suspend fun advanceTeacherBy(realElapsedMillis: Long) {
        val requestId = activeTeacherRequestId ?: return
        val before = teacherSession.state.value.frame?.status
        val after = teacherSession.advanceBy(realElapsedMillis).frame?.status
        if (before == TeacherPlaybackStatus.PLAYING && after == TeacherPlaybackStatus.COMPLETED) {
            activeTeacherRequestId = null
            deliverTeacherCompletion(requestId)
        }
    }

    internal suspend fun deliverTeacherCompletion(requestId: String): LessonSignalResult? {
        val engine = lessonEngine ?: return null
        val result = engine.handle(LessonRuntimeSignal.TeacherPlaybackCompleted(requestId))
        consumeSignalResult(engine, result)
        return result
    }

    suspend fun injectTeacherFailure(reason: String = "Lesson Lab injected playback failure") {
        val requestId = activeTeacherRequestId ?: run {
            publishDiagnostics("No active teacher request to fail")
            return
        }
        teacherSession.cancel()
        activeTeacherRequestId = null
        val engine = lessonEngine ?: return
        consumeSignalResult(
            engine,
            engine.handle(LessonRuntimeSignal.TeacherPlaybackFailed(requestId, reason)),
        )
    }

    suspend fun simulateColoringUnavailable() {
        if (pendingColoringMode == null) {
            publishDiagnostics("No coloring handoff is pending")
            return
        }
        val engine = lessonEngine ?: return
        pendingColoringMode = null
        consumeSignalResult(
            engine,
            engine.handle(
                ColoringHandoffFailed(
                    childDocumentId = DOCUMENT_ID,
                    reason = "Coloring Engine is intentionally not implemented in Lesson Engine 0.2",
                ),
            ),
        )
    }

    suspend fun simulateColoringContractAck() {
        if (pendingColoringMode == null) {
            publishDiagnostics("No coloring handoff is pending")
            return
        }
        val engine = lessonEngine ?: return
        pendingColoringMode = null
        consumeSignalResult(engine, engine.handle(ColoringHandoffCompleted(DOCUMENT_ID)))
    }

    suspend fun onBackground() {
        runCatching { documentStore.save(documentEngine.state.value.document) }
        lessonEngine?.let { engine -> autosave.onBackground(engine) }
        publishDiagnostics("Background snapshot requested")
    }

    suspend fun saveNow() {
        documentStore.save(documentEngine.state.value.document)
        val sessionResult = lessonEngine?.let { autosave.onBackground(it) }
        publishDiagnostics("Saved artwork + session (${sessionResult ?: "no session"})")
    }

    fun activeTeacherRequest(): String? = activeTeacherRequestId

    fun pendingColoringMode(): ColoringHandoffMode? = pendingColoringMode

    internal fun activeLessonEngine(): LessonSessionEngine? = lessonEngine

    private suspend fun consumeCommandResult(
        engine: LessonSessionEngine,
        result: LessonCommandResult,
    ) {
        when (result) {
            is LessonCommandResult.Accepted -> {
                processEvents(result.events)
                autosave.afterAcceptedCommand(engine, result)
                _sessionState.value = engine.state
                publishDiagnostics("Command accepted · ${engine.state.shortName()}")
            }
            is LessonCommandResult.Rejected -> {
                _sessionState.value = engine.state
                publishDiagnostics("Rejected ${result.rejection.code}: ${result.rejection.message}")
            }
        }
    }

    private suspend fun consumeSignalResult(
        engine: LessonSessionEngine,
        result: LessonSignalResult,
    ) {
        when (result) {
            is LessonSignalResult.Accepted -> {
                processEvents(result.events)
                autosave.afterEvents(engine, result.events)
                _sessionState.value = engine.state
                publishDiagnostics("Runtime signal accepted · ${engine.state.shortName()}")
            }
            is LessonSignalResult.Rejected -> {
                _sessionState.value = engine.state
                publishDiagnostics("Signal rejected ${result.rejection.code}: ${result.rejection.message}")
            }
        }
    }

    private fun processEvents(events: List<LessonSessionEvent>) {
        events.forEach { event ->
            when (event) {
                LessonRuntimeResetRequested -> {
                    teacherSession.cancel()
                    activeTeacherRequestId = null
                    _guideOverlay.value = null
                }

                is TeacherPlaybackRequested -> {
                    activeTeacherRequestId = event.request.requestId
                    teacherSession.load(event.request.sequence)
                    teacherSession.setPace(event.request.pace)
                    teacherSession.play()
                }

                is TeacherPlaybackPauseRequested -> {
                    if (event.requestId == activeTeacherRequestId) teacherSession.pause()
                }

                is TeacherPlaybackResumeRequested -> {
                    if (event.requestId == activeTeacherRequestId) teacherSession.resume()
                }

                is TeacherPlaybackPaceChangeRequested -> {
                    if (event.requestId == activeTeacherRequestId) teacherSession.setPace(event.pace)
                }

                is TeacherPlaybackCancelRequested -> {
                    if (event.requestId == activeTeacherRequestId) {
                        teacherSession.cancel()
                        activeTeacherRequestId = null
                    }
                }

                is GuideOverlayRequested -> _guideOverlay.value = event.request
                is GuideOverlayCleared -> {
                    if (_guideOverlay.value?.stepId == event.stepId) _guideOverlay.value = null
                }

                is ColoringHandoffRequested -> pendingColoringMode = event.mode
                is ColoringHandoffFailureObserved,
                is LessonFinished,
                -> pendingColoringMode = null

                else -> Unit
            }
        }
    }

    private fun publishDiagnostics(message: String) {
        val document = documentEngine.state.value.document
        val contextual = lessonEngine?.state as? LessonSessionState.Contextual
        val nonChildInk = document.operations
            .filterIsInstance<DocumentOperation.AddInkStroke>()
            .count { it.stroke.authorRole != StrokeAuthorRole.CHILD }
        _diagnostics.value = LessonLabDiagnostics(
            packageStatus = _diagnostics.value.packageStatus,
            message = message,
            lessonState = lessonEngine?.state?.shortName() ?: "No session",
            step = contextual?.context?.let { "${it.currentStepIndex + 1}:${it.currentStepId}" } ?: "—",
            helpLevel = contextual?.context?.helpLevel ?: 0,
            overviewCompleted = contextual?.context?.overviewCompleted,
            activeTeacherRequestId = activeTeacherRequestId,
            activeGuideId = _guideOverlay.value?.overlayId,
            childOperationCount = document.operations.size,
            activeChildInkCount = document.activeInkStrokes().size,
            nonChildInkOperationCount = nonChildInk,
            overlayIsolationPass = nonChildInk == 0,
            pendingColoringMode = pendingColoringMode,
        )
    }

    private fun LessonSessionState.shortName(): String = this::class.simpleName ?: "Unknown"

    companion object {
        internal const val LESSON_ROOT = "lessons/cute-cat"
        internal const val SESSION_ID = "lesson-lab-cute-cat-session"
        internal const val DOCUMENT_ID = "lesson-lab-cute-cat-document"

        internal fun forTest(
            rootDirectory: File,
            source: LessonPackageSource,
        ): LessonLabRuntimeCore = LessonLabRuntimeCore(
            documentRoot = File(rootDirectory, "documents"),
            sessionRoot = File(rootDirectory, "sessions"),
            lessonPackageResult = LessonPackageLoader(source).load(LESSON_ROOT),
        )

        private fun newLessonDocument() = DrawingDocumentEngine.newDocument(
            documentId = DOCUMENT_ID,
            metadata = DrawingDocumentMetadata(
                lessonId = "cute-cat",
                lessonRevision = 1,
            ),
        )
    }
}

data class LessonLabDiagnostics(
    val packageStatus: String,
    val message: String = "Ready",
    val lessonState: String = "No session",
    val step: String = "—",
    val helpLevel: Int = 0,
    val overviewCompleted: Boolean? = null,
    val activeTeacherRequestId: String? = null,
    val activeGuideId: String? = null,
    val childOperationCount: Int = 0,
    val activeChildInkCount: Int = 0,
    val nonChildInkOperationCount: Int = 0,
    val overlayIsolationPass: Boolean = true,
    val pendingColoringMode: ColoringHandoffMode? = null,
)

enum class LessonLabRecoveryOutcome {
    NONE,
    RESTORED,
    ARTWORK_ONLY,
    SESSION_CORRUPT,
    CHILD_DOCUMENT_MISSING,
    ARTWORK_PRESERVED_FATAL,
}
