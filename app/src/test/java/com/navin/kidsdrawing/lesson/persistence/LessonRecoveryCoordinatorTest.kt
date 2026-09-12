package com.navin.kidsdrawing.lesson.persistence

import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentMetadata
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.drawing.infrastructure.persistence.AtomicDrawingDocumentStore
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.GuideOverlayRequested
import com.navin.kidsdrawing.lesson.session.LessonCommand
import com.navin.kidsdrawing.lesson.session.LessonCommandResult
import com.navin.kidsdrawing.lesson.session.LessonRestoreResult
import com.navin.kidsdrawing.lesson.session.LessonRuntimeResetRequested
import com.navin.kidsdrawing.lesson.session.LessonRuntimeSignal
import com.navin.kidsdrawing.lesson.session.LessonSessionEngine
import com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import com.navin.kidsdrawing.lesson.session.LessonSignalRejectionCode
import com.navin.kidsdrawing.lesson.session.LessonSignalResult
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import com.navin.kidsdrawing.lesson.session.TeacherPlaybackRequested
import com.navin.kidsdrawing.lesson.session.TeacherPlaybackScope
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonRecoveryCoordinatorTest {
    @Test
    fun corruptSessionStopsBeforeChildDocumentOrLessonResolution() = runBlocking {
        var documentTouched = false
        var lessonTouched = false
        val coordinator = LessonRecoveryCoordinator(
            loadSession = {
                AtomicLessonSessionStore.LoadResult.Corrupt("bad primary", "bad backup")
            },
            loadChildDocument = {
                documentTouched = true
                null
            },
            resolveLessonPackage = { _, _ ->
                lessonTouched = true
                packageData()
            },
        )

        val result = coordinator.recover("session-recovery")

        assertTrue(result is LessonRecoveryCoordinator.RecoveryResult.CorruptSession)
        assertFalse(documentTouched)
        assertFalse(lessonTouched)
    }

    @Test
    fun missingChildDocumentStopsBeforeLessonEngineRestore() = runBlocking {
        var lessonTouched = false
        val snapshot = snapshot()
        val coordinator = LessonRecoveryCoordinator(
            loadSession = { loadedSession(snapshot) },
            loadChildDocument = { null },
            resolveLessonPackage = { _, _ ->
                lessonTouched = true
                packageData()
            },
        )

        val result = coordinator.recover(snapshot.sessionId)

        assertTrue(result is LessonRecoveryCoordinator.RecoveryResult.MissingChildDocument)
        assertFalse(lessonTouched)
    }

    @Test
    fun recoveryOrderIsSessionThenChildDocumentThenLessonThenEngine() = runBlocking {
        val order = mutableListOf<String>()
        val snapshot = snapshot()
        val lessonPackage = packageData()
        val coordinator = LessonRecoveryCoordinator(
            loadSession = {
                order += "session"
                loadedSession(snapshot)
            },
            loadChildDocument = {
                order += "document"
                loadedDocument(document())
            },
            resolveLessonPackage = { _, _ ->
                order += "lesson"
                lessonPackage
            },
            restoreEngine = { resolved, saved ->
                order += "engine"
                LessonSessionEngine.restore(resolved, saved)
            },
        )

        val result = coordinator.recover(snapshot.sessionId)

        assertTrue(result is LessonRecoveryCoordinator.RecoveryResult.Restored)
        assertEquals(listOf("session", "document", "lesson", "engine"), order)
    }

    @Test
    fun allThreeTeachingModesRecoverWithChildArtworkIntact() = runBlocking {
        TeachingMode.entries.forEach { mode ->
            val snapshot = snapshot(
                mode = mode,
                phase = LessonSnapshotPhase.AWAITING_CHILD,
                overviewCompleted = true,
            )
            val childDocument = document()
            val result = coordinator(snapshot, childDocument).recover(snapshot.sessionId)

            assertTrue("Expected restored result for $mode", result is LessonRecoveryCoordinator.RecoveryResult.Restored)
            result as LessonRecoveryCoordinator.RecoveryResult.Restored
            assertEquals(childDocument, result.childDocument)
            assertTrue(result.childDocument.operations.isEmpty())
            assertEquals(mode, (result.engine.state as LessonSessionState.AwaitingChild).context.mode)
            assertTrue(result.restoreEvents.any { it === LessonRuntimeResetRequested })

            if (mode == TeachingMode.TRACE_AND_LEARN) {
                assertTrue(result.restoreEvents.any { it is GuideOverlayRequested })
                assertTrue(result.childDocument.operations.isEmpty())
            }
        }
    }

    @Test
    fun restoredTeacherDemoUsesNewGenerationAndRejectsOldCompletion() = runBlocking {
        val snapshot = snapshot(
            mode = TeachingMode.DRAW_WITH_ME,
            phase = LessonSnapshotPhase.PREPARING_STEP,
            transientRuntimePhase = LessonSnapshotPhase.TEACHER_DEMONSTRATING,
            runtimeGeneration = 4,
        )

        val result = coordinator(snapshot, document()).recover(snapshot.sessionId)
            as LessonRecoveryCoordinator.RecoveryResult.Restored
        val request = result.restoreEvents.filterIsInstance<TeacherPlaybackRequested>().single().request

        assertTrue(request.requestId.contains(":g5:"))
        val stale = result.engine.handle(
            LessonRuntimeSignal.TeacherPlaybackCompleted(
                "session-recovery:r1:g4:head:teacher:1",
            ),
        )
        assertTrue(stale is LessonSignalResult.Rejected)
        stale as LessonSignalResult.Rejected
        assertEquals(LessonSignalRejectionCode.STALE_TEACHER_REQUEST, stale.rejection.code)
        assertTrue(result.engine.state is LessonSessionState.TeacherDemonstrating)
    }

    @Test
    fun pausedTeacherRecoveryDefersTeacherRestartUntilResume() = runBlocking {
        val snapshot = snapshot(
            mode = TeachingMode.DRAW_WITH_ME,
            phase = LessonSnapshotPhase.PAUSED,
            pausedResumePhase = LessonSnapshotPhase.PREPARING_STEP,
            transientRuntimePhase = LessonSnapshotPhase.TEACHER_DEMONSTRATING,
            runtimeGeneration = 2,
        )

        val result = coordinator(snapshot, document()).recover(snapshot.sessionId)
            as LessonRecoveryCoordinator.RecoveryResult.Restored

        assertTrue(result.engine.state is LessonSessionState.Paused)
        assertTrue(result.restoreEvents.none { it is TeacherPlaybackRequested })
        assertTrue(result.restoreEvents.any { it === LessonRuntimeResetRequested })

        val resumed = result.engine.dispatch(LessonCommand.Resume)
        assertTrue(resumed is LessonCommandResult.Accepted)
        resumed as LessonCommandResult.Accepted
        val request = resumed.events.filterIsInstance<TeacherPlaybackRequested>().single().request
        assertTrue(request.requestId.contains(":g3:"))
    }

    @Test
    fun watchThenDrawOverviewRestartsAfterRecreationWithFreshRequest() = runBlocking {
        val snapshot = snapshot(
            mode = TeachingMode.WATCH_THEN_DRAW,
            phase = LessonSnapshotPhase.OVERVIEW_DEMONSTRATING,
            overviewCompleted = false,
            runtimeGeneration = 6,
        )

        val result = coordinator(snapshot, document()).recover(snapshot.sessionId)
            as LessonRecoveryCoordinator.RecoveryResult.Restored
        val request = result.restoreEvents.filterIsInstance<TeacherPlaybackRequested>().single().request

        assertEquals(TeacherPlaybackScope.OVERVIEW, request.scope)
        assertTrue(request.requestId.contains(":g7:"))
        assertTrue(result.engine.state is LessonSessionState.OverviewDemonstrating)
    }

    @Test
    fun incompatibleChildLessonMetadataFailsBeforeEngineRestore() = runBlocking {
        var engineTouched = false
        val snapshot = snapshot()
        val mismatched = document(
            metadata = DrawingDocumentMetadata(
                lessonId = "other-lesson",
                lessonRevision = 1,
            ),
        )
        val coordinator = LessonRecoveryCoordinator(
            loadSession = { loadedSession(snapshot) },
            loadChildDocument = { loadedDocument(mismatched) },
            resolveLessonPackage = { _, _ -> packageData() },
            restoreEngine = { packageData, saved ->
                engineTouched = true
                LessonSessionEngine.restore(packageData, saved)
            },
        )

        val result = coordinator.recover(snapshot.sessionId)

        assertTrue(result is LessonRecoveryCoordinator.RecoveryResult.IncompatibleChildDocument)
        assertFalse(engineTouched)
    }

    private fun coordinator(
        snapshot: LessonSessionSnapshot,
        childDocument: DrawingDocument,
    ) = LessonRecoveryCoordinator(
        loadSession = { loadedSession(snapshot) },
        loadChildDocument = { loadedDocument(childDocument) },
        resolveLessonPackage = { lessonId, revision ->
            packageData().takeIf {
                it.lesson.lessonId == lessonId && it.lesson.revision == revision
            }
        },
    )

    private fun loadedSession(snapshot: LessonSessionSnapshot) =
        AtomicLessonSessionStore.LoadResult.Loaded(
            snapshot = snapshot,
            source = AtomicLessonSessionStore.LoadSource.PRIMARY,
        )

    private fun loadedDocument(document: DrawingDocument) =
        AtomicDrawingDocumentStore.LoadResult(
            document = document,
            source = AtomicDrawingDocumentStore.LoadSource.PRIMARY,
        )

    private fun snapshot(
        mode: TeachingMode = TeachingMode.DRAW_WITH_ME,
        phase: LessonSnapshotPhase = LessonSnapshotPhase.AWAITING_CHILD,
        pausedResumePhase: LessonSnapshotPhase? = null,
        transientRuntimePhase: LessonSnapshotPhase? = null,
        overviewCompleted: Boolean = true,
        runtimeGeneration: Int = 0,
    ) = LessonSessionSnapshot(
        sessionId = "session-recovery",
        lessonId = "cute-cat",
        lessonRevision = 1,
        childDocumentId = "document-recovery",
        mode = mode,
        pace = TeachingPace.NORMAL,
        phase = phase,
        pausedResumePhase = pausedResumePhase,
        transientRuntimePhase = transientRuntimePhase,
        currentStepIndex = 0,
        currentStepId = "head",
        helpLevel = 0,
        overviewCompleted = overviewCompleted,
        runtimeGeneration = runtimeGeneration,
        savedAtEpochMillis = 500L,
    )

    private fun document(
        metadata: DrawingDocumentMetadata = DrawingDocumentMetadata(
            lessonId = "cute-cat",
            lessonRevision = 1,
        ),
    ) = DrawingDocument(
        documentId = "document-recovery",
        logicalSize = DocumentSize(1000f, 1000f),
        createdAtEpochMillis = 100L,
        modifiedAtEpochMillis = 200L,
        metadata = metadata,
    )

    private fun packageData(): LessonRuntimePackage {
        val loader = LessonPackageLoader(
            LessonPackageSource { path ->
                File("src/main/assets/$path").takeIf(File::isFile)?.readText()
            },
        )
        val result = loader.load(ROOT)
        assertTrue("Expected bundled lesson to load, got $result", result is LessonLoadResult.Success)
        return (result as LessonLoadResult.Success).packageData
    }

    private companion object {
        const val ROOT = "lessons/cute-cat"
    }
}
