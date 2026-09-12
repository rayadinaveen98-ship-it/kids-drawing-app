package com.navin.kidsdrawing.lesson.persistence

import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentMetadata
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonFatalRecoveryTest {
    @Test
    fun missingLessonRevisionReturnsPreservedChildArtworkForSafeExit() = runBlocking {
        val snapshot = snapshot()
        val artwork = document()
        val coordinator = LessonRecoveryCoordinator(
            loadSession = {
                AtomicLessonSessionStore.LoadResult.Loaded(
                    snapshot,
                    AtomicLessonSessionStore.LoadSource.PRIMARY,
                )
            },
            loadChildDocument = {
                AtomicDrawingDocumentStore.LoadResult(
                    artwork,
                    AtomicDrawingDocumentStore.LoadSource.BACKUP,
                )
            },
            resolveLessonPackage = { _, _ -> null },
        )

        val result = coordinator.recover(snapshot.sessionId)

        assertTrue(result is LessonRecoveryCoordinator.RecoveryResult.MissingLessonRevision)
        result as LessonRecoveryCoordinator.RecoveryResult.MissingLessonRevision
        assertEquals(artwork, result.childDocument)
        assertEquals(AtomicDrawingDocumentStore.LoadSource.BACKUP, result.childDocumentSource)
        assertEquals(snapshot.lessonId, result.lessonId)
        assertEquals(snapshot.lessonRevision, result.lessonRevision)
    }

    private fun snapshot() = LessonSessionSnapshot(
        sessionId = "fatal-recovery-session",
        lessonId = "cute-cat",
        lessonRevision = 1,
        childDocumentId = "fatal-recovery-document",
        mode = TeachingMode.DRAW_WITH_ME,
        pace = TeachingPace.NORMAL,
        phase = LessonSnapshotPhase.AWAITING_CHILD,
        currentStepIndex = 0,
        currentStepId = "head",
        overviewCompleted = true,
        savedAtEpochMillis = 100L,
    )

    private fun document() = DrawingDocument(
        documentId = "fatal-recovery-document",
        logicalSize = DocumentSize(1000f, 1000f),
        createdAtEpochMillis = 10L,
        modifiedAtEpochMillis = 20L,
        metadata = DrawingDocumentMetadata(
            lessonId = "cute-cat",
            lessonRevision = 1,
        ),
    )
}
