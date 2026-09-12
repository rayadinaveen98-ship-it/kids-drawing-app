package com.navin.kidsdrawing.lesson.session

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonRecoveryAndHandoffTest {
    @Test
    fun failedTeacherPlaybackRetriesWithFreshRequestAndRejectsOldCompletion() {
        val engine = engine()
        val started = engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.DRAW_WITH_ME, TeachingPace.NORMAL),
        ) as LessonCommandResult.Accepted
        val firstRequest = started.events.filterIsInstance<TeacherPlaybackRequested>().single().request

        val failed = engine.handle(
            LessonRuntimeSignal.TeacherPlaybackFailed(firstRequest.requestId, "renderer unavailable"),
        )
        assertTrue(failed is LessonSignalResult.Accepted)
        assertTrue(engine.state is LessonSessionState.RecoverableError)

        val retry = engine.dispatch(RetryRecoverable) as LessonCommandResult.Accepted
        val secondRequest = retry.events.filterIsInstance<TeacherPlaybackRequested>().single().request
        assertNotEquals(firstRequest.requestId, secondRequest.requestId)
        assertTrue(retry.events.any { it === LessonRuntimeResetRequested })
        assertTrue(engine.state is LessonSessionState.TeacherDemonstrating)

        val stale = engine.handle(
            LessonRuntimeSignal.TeacherPlaybackCompleted(firstRequest.requestId),
        )
        assertTrue(stale is LessonSignalResult.Rejected)
        stale as LessonSignalResult.Rejected
        assertEquals(LessonSignalRejectionCode.STALE_TEACHER_REQUEST, stale.rejection.code)
    }

    @Test
    fun failedWatchOverviewRetriesFromBeginningWithFreshRequest() {
        val engine = engine()
        val started = engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.WATCH_THEN_DRAW, TeachingPace.SLOW),
        ) as LessonCommandResult.Accepted
        val firstRequest = started.events.filterIsInstance<TeacherPlaybackRequested>().single().request

        engine.handle(
            LessonRuntimeSignal.TeacherPlaybackFailed(firstRequest.requestId, "surface reset"),
        )
        val retry = engine.dispatch(RetryRecoverable) as LessonCommandResult.Accepted
        val secondRequest = retry.events.filterIsInstance<TeacherPlaybackRequested>().single().request

        assertEquals(TeacherPlaybackScope.OVERVIEW, secondRequest.scope)
        assertNotEquals(firstRequest.requestId, secondRequest.requestId)
        assertTrue(engine.state is LessonSessionState.OverviewDemonstrating)
    }

    @Test
    fun drawingCompleteCanFinishForNowWithoutColoringHandoff() {
        val engine = restoredAt(LessonSnapshotPhase.DRAWING_COMPLETE)

        val result = engine.dispatch(FinishForNow)

        assertTrue(result is LessonCommandResult.Accepted)
        val finished = engine.state as LessonSessionState.Finished
        assertEquals(LessonFinishReason.FINISHED_FOR_NOW, finished.reason)
        assertTrue(result.events.any {
            it is LessonFinished && it.reason == LessonFinishReason.FINISHED_FOR_NOW
        })
    }

    @Test
    fun coloringFailureReturnsToChoiceAndRetryCanComplete() {
        val engine = restoredAt(LessonSnapshotPhase.DRAWING_COMPLETE)

        val first = engine.dispatch(ChooseColorWithMe) as LessonCommandResult.Accepted
        val firstRequest = first.events.filterIsInstance<ColoringHandoffRequested>().single()
        assertEquals(ColoringHandoffMode.COLOR_WITH_ME, firstRequest.mode)
        assertTrue(engine.state is LessonSessionState.HandingOffToColoring)

        val failed = engine.handle(
            ColoringHandoffFailed(CHILD_DOCUMENT_ID, "coloring engine unavailable"),
        )
        assertTrue(failed is LessonSignalResult.Accepted)
        assertTrue(engine.state is LessonSessionState.AwaitingPostDrawingChoice)
        assertTrue(failed.events.any { it is ColoringHandoffFailureObserved })

        val second = engine.dispatch(ChooseColorMyself) as LessonCommandResult.Accepted
        val secondRequest = second.events.filterIsInstance<ColoringHandoffRequested>().single()
        assertEquals(ColoringHandoffMode.COLOR_MYSELF, secondRequest.mode)

        val completed = engine.handle(ColoringHandoffCompleted(CHILD_DOCUMENT_ID))
        assertTrue(completed is LessonSignalResult.Accepted)
        val finished = engine.state as LessonSessionState.Finished
        assertEquals(LessonFinishReason.COMPLETED, finished.reason)
    }

    @Test
    fun processSnapshotDuringColoringHandoffNormalizesBackToChoice() {
        val engine = restoredAt(LessonSnapshotPhase.DRAWING_COMPLETE)
        engine.dispatch(ChooseColorWithMe)
        assertTrue(engine.state is LessonSessionState.HandingOffToColoring)

        val snapshot = (engine.createSnapshot() as LessonSnapshotResult.Created).snapshot
        assertEquals(LessonSnapshotPhase.AWAITING_POST_DRAWING_CHOICE, snapshot.phase)

        val restored = LessonSessionEngine.restore(packageData(), snapshot)
        assertTrue(restored is LessonRestoreResult.Restored)
        restored as LessonRestoreResult.Restored
        assertTrue(restored.engine.state is LessonSessionState.AwaitingPostDrawingChoice)
    }

    @Test
    fun coloringRuntimeSignalsRejectWrongDocumentWithoutStateMutation() {
        val engine = restoredAt(LessonSnapshotPhase.DRAWING_COMPLETE)
        engine.dispatch(ChooseColorWithMe)
        val before = engine.state

        val wrong = engine.handle(ColoringHandoffCompleted("other-document"))

        assertTrue(wrong is LessonSignalResult.Rejected)
        wrong as LessonSignalResult.Rejected
        assertEquals(LessonSignalRejectionCode.DOCUMENT_MISMATCH, wrong.rejection.code)
        assertEquals(before, engine.state)
    }

    private fun engine(): LessonSessionEngine = LessonSessionEngine.create(
        lessonPackage = packageData(),
        sessionId = "recovery-handoff-session",
        childDocumentId = CHILD_DOCUMENT_ID,
    )

    private fun restoredAt(phase: LessonSnapshotPhase): LessonSessionEngine {
        val snapshot = LessonSessionSnapshot(
            sessionId = "post-drawing-session",
            lessonId = "cute-cat",
            lessonRevision = 1,
            childDocumentId = CHILD_DOCUMENT_ID,
            mode = TeachingMode.DRAW_WITH_ME,
            pace = TeachingPace.NORMAL,
            phase = phase,
            currentStepIndex = 0,
            currentStepId = "head",
            overviewCompleted = true,
            savedAtEpochMillis = 100L,
        )
        val restored = LessonSessionEngine.restore(packageData(), snapshot)
        assertTrue(restored is LessonRestoreResult.Restored)
        return (restored as LessonRestoreResult.Restored).engine
    }

    private fun packageData(): LessonRuntimePackage {
        val loader = LessonPackageLoader(
            LessonPackageSource { path ->
                File("src/main/assets/$path").takeIf(File::isFile)?.readText()
            },
        )
        val loaded = loader.load(ROOT)
        assertTrue(loaded is LessonLoadResult.Success)
        return (loaded as LessonLoadResult.Success).packageData
    }

    private companion object {
        const val ROOT = "lessons/cute-cat"
        const val CHILD_DOCUMENT_ID = "post-drawing-document"
    }
}
