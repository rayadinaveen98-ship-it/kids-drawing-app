package com.navin.kidsdrawing.lesson.persistence

import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.session.LessonRestoreIncompatibility
import com.navin.kidsdrawing.lesson.session.LessonRestoreNormalization
import com.navin.kidsdrawing.lesson.session.LessonRestoreResult
import com.navin.kidsdrawing.lesson.session.LessonSessionEngine
import com.navin.kidsdrawing.lesson.session.LessonSessionEvent
import com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot

/**
 * Restores a persisted lesson session without allowing semantic lesson progress to outrun child
 * artwork recovery.
 *
 * Recovery is deliberately ordered as:
 * 1. load the semantic lesson snapshot;
 * 2. load the child drawing document referenced by that snapshot;
 * 3. resolve the exact lesson revision;
 * 4. restore the Lesson Engine;
 * 5. only then reactivate transient teacher/guide runtime work.
 *
 * This ordering keeps child artwork authoritative and prevents stale pre-recreation overlays from
 * being mistaken for persisted child content. Once artwork has loaded, every later failure result
 * carries that document back to the product boundary so a missing/bad lesson can never strand the
 * child's drawing.
 */
class LessonRecoveryCoordinator(
    private val loadSession: suspend (String) -> AtomicLessonSessionStore.LoadResult,
    private val loadChildDocument: suspend (String) -> AtomicDrawingDocumentStore.LoadResult?,
    private val resolveLessonPackage: suspend (lessonId: String, lessonRevision: Int) -> LessonRuntimePackage?,
    private val restoreEngine: (LessonRuntimePackage, LessonSessionSnapshot) -> LessonRestoreResult =
        { lessonPackage, snapshot -> LessonSessionEngine.restore(lessonPackage, snapshot) },
) {
    suspend fun recover(sessionId: String): RecoveryResult {
        require(sessionId.isNotBlank()) { "sessionId cannot be blank." }

        val loadedSession = when (val session = loadSession(sessionId)) {
            AtomicLessonSessionStore.LoadResult.Missing -> return RecoveryResult.MissingSession
            is AtomicLessonSessionStore.LoadResult.Corrupt -> {
                return RecoveryResult.CorruptSession(
                    primaryFailure = session.primaryFailure,
                    backupFailure = session.backupFailure,
                )
            }
            is AtomicLessonSessionStore.LoadResult.Loaded -> session
        }

        val snapshot = loadedSession.snapshot

        // Child artwork must be recovered before any Lesson Engine state is recreated or activated.
        val loadedDocument = loadChildDocument(snapshot.childDocumentId)
            ?: return RecoveryResult.MissingChildDocument(snapshot)
        val childDocument = loadedDocument.document

        if (childDocument.documentId != snapshot.childDocumentId) {
            return RecoveryResult.IncompatibleChildDocument(
                snapshot = snapshot,
                childDocument = childDocument,
                childDocumentSource = loadedDocument.source,
                reason = "Loaded child document ID ${childDocument.documentId} does not match ${snapshot.childDocumentId}.",
            )
        }

        childDocument.metadata.lessonId?.let { lessonId ->
            if (lessonId != snapshot.lessonId) {
                return RecoveryResult.IncompatibleChildDocument(
                    snapshot = snapshot,
                    childDocument = childDocument,
                    childDocumentSource = loadedDocument.source,
                    reason = "Child document lesson $lessonId does not match snapshot lesson ${snapshot.lessonId}.",
                )
            }
        }
        childDocument.metadata.lessonRevision?.let { revision ->
            if (revision != snapshot.lessonRevision) {
                return RecoveryResult.IncompatibleChildDocument(
                    snapshot = snapshot,
                    childDocument = childDocument,
                    childDocumentSource = loadedDocument.source,
                    reason = "Child document lesson revision $revision does not match snapshot revision ${snapshot.lessonRevision}.",
                )
            }
        }

        val lessonPackage = resolveLessonPackage(snapshot.lessonId, snapshot.lessonRevision)
            ?: return RecoveryResult.MissingLessonRevision(
                lessonId = snapshot.lessonId,
                lessonRevision = snapshot.lessonRevision,
                snapshot = snapshot,
                childDocument = childDocument,
                childDocumentSource = loadedDocument.source,
            )

        return when (val restored = restoreEngine(lessonPackage, snapshot)) {
            is LessonRestoreResult.Incompatible -> RecoveryResult.IncompatibleSession(
                snapshot = snapshot,
                incompatibility = restored.incompatibility,
                childDocument = childDocument,
                childDocumentSource = loadedDocument.source,
            )
            is LessonRestoreResult.Restored -> {
                val runtimeEvents = restored.engine.activateRestoredRuntime(restored.normalization)
                RecoveryResult.Restored(
                    snapshot = snapshot,
                    sessionSource = loadedSession.source,
                    childDocument = childDocument,
                    childDocumentSource = loadedDocument.source,
                    engine = restored.engine,
                    normalization = restored.normalization,
                    restoreEvents = restored.events + runtimeEvents,
                )
            }
        }
    }

    sealed interface RecoveryResult {
        data object MissingSession : RecoveryResult

        data class CorruptSession(
            val primaryFailure: String?,
            val backupFailure: String?,
        ) : RecoveryResult

        data class MissingChildDocument(
            val snapshot: LessonSessionSnapshot,
        ) : RecoveryResult

        data class IncompatibleChildDocument(
            val snapshot: LessonSessionSnapshot,
            val childDocument: DrawingDocument,
            val childDocumentSource: AtomicDrawingDocumentStore.LoadSource,
            val reason: String,
        ) : RecoveryResult

        data class MissingLessonRevision(
            val lessonId: String,
            val lessonRevision: Int,
            val snapshot: LessonSessionSnapshot,
            val childDocument: DrawingDocument,
            val childDocumentSource: AtomicDrawingDocumentStore.LoadSource,
        ) : RecoveryResult

        data class IncompatibleSession(
            val snapshot: LessonSessionSnapshot,
            val incompatibility: LessonRestoreIncompatibility,
            val childDocument: DrawingDocument,
            val childDocumentSource: AtomicDrawingDocumentStore.LoadSource,
        ) : RecoveryResult

        data class Restored(
            val snapshot: LessonSessionSnapshot,
            val sessionSource: AtomicLessonSessionStore.LoadSource,
            val childDocument: DrawingDocument,
            val childDocumentSource: AtomicDrawingDocumentStore.LoadSource,
            val engine: LessonSessionEngine,
            val normalization: LessonRestoreNormalization,
            val restoreEvents: List<LessonSessionEvent>,
        ) : RecoveryResult
    }

    companion object {
        fun fromStores(
            sessionStore: AtomicLessonSessionStore,
            drawingStore: AtomicDrawingDocumentStore,
            resolveLessonPackage: suspend (lessonId: String, lessonRevision: Int) -> LessonRuntimePackage?,
        ): LessonRecoveryCoordinator = LessonRecoveryCoordinator(
            loadSession = sessionStore::load,
            loadChildDocument = drawingStore::load,
            resolveLessonPackage = resolveLessonPackage,
        )
    }
}
