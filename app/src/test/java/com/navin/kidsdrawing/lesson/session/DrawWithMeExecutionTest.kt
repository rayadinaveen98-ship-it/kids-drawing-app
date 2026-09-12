package com.navin.kidsdrawing.lesson.session

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DrawWithMeExecutionTest {
    @Test
    fun cuteCatExecutesAllFourStepsToDrawingComplete() {
        val engine = engine()
        var request = startDrawWithMe(engine)
        val completedSteps = mutableListOf<String>()

        listOf("head", "ears", "face", "body_tail").forEachIndexed { index, expectedStepId ->
            assertEquals(expectedStepId, request.request.stepId)
            completeTeacher(engine, request)

            val childState = engine.state as LessonSessionState.AwaitingChild
            assertEquals(index, childState.context.currentStepIndex)
            assertEquals(expectedStepId, childState.context.currentStepId)

            val done = engine.dispatch(MarkChildTurnDone) as LessonCommandResult.Accepted
            completedSteps += done.events
                .filterIsInstance<StepCompleted>()
                .single()
                .stepId

            if (index == 3) {
                assertTrue(engine.state is LessonSessionState.DrawingComplete)
                assertEquals(
                    1,
                    done.events.filterIsInstance<DrawingLessonCompleted>().size,
                )
            } else {
                request = done.events.filterIsInstance<TeacherPlaybackRequested>().single()
                assertEquals(index + 1, (engine.state as LessonSessionState.TeacherDemonstrating).context.currentStepIndex)
            }
        }

        assertEquals(listOf("head", "ears", "face", "body_tail"), completedSteps)
    }

    @Test
    fun teacherPlaybackRequestCarriesSelectedPaceAcrossAllFiveProfiles() {
        TeachingPace.entries.forEach { pace ->
            val engine = engine()

            val request = startDrawWithMe(engine, pace)

            assertEquals(pace, request.request.pace)
            assertEquals(pace, (engine.state as LessonSessionState.TeacherDemonstrating).context.pace)
            assertEquals("head", request.request.stepId)
            assertTrue(request.request.sequence.strokes.isNotEmpty())
        }
    }

    @Test
    fun changingPaceDuringTeacherPlaybackPreservesRequestAndEmitsClockUpdate() {
        val engine = engine()
        val request = startDrawWithMe(engine, TeachingPace.NORMAL)

        val result = engine.dispatch(LessonCommand.SetPace(TeachingPace.VERY_FAST))

        assertTrue(result is LessonCommandResult.Accepted)
        result as LessonCommandResult.Accepted
        val paceEvent = result.events.filterIsInstance<TeacherPlaybackPaceChangeRequested>().single()
        assertEquals(request.request.requestId, paceEvent.requestId)
        assertEquals(TeachingPace.VERY_FAST, paceEvent.pace)
        val state = engine.state as LessonSessionState.TeacherDemonstrating
        assertEquals(request.request.requestId, state.requestId)
        assertEquals(TeachingPace.VERY_FAST, state.context.pace)
    }

    @Test
    fun replayCreatesANewRequestAndStaleCompletionCannotAdvanceSession() {
        val engine = engine()
        val initial = startDrawWithMe(engine)
        completeTeacher(engine, initial)
        assertTrue(engine.state is LessonSessionState.AwaitingChild)

        val replayResult = engine.dispatch(ReplayDemonstration) as LessonCommandResult.Accepted
        val replay = replayResult.events.filterIsInstance<TeacherPlaybackRequested>().single()
        assertTrue(replay.request.replay)
        assertNotEquals(initial.request.requestId, replay.request.requestId)

        val stale = engine.handle(
            LessonRuntimeSignal.TeacherPlaybackCompleted(initial.request.requestId),
        )
        assertTrue(stale is LessonSignalResult.Rejected)
        stale as LessonSignalResult.Rejected
        assertEquals(LessonSignalRejectionCode.STALE_TEACHER_REQUEST, stale.rejection.code)
        assertEquals(replay.request.requestId, (engine.state as LessonSessionState.TeacherDemonstrating).requestId)

        completeTeacher(engine, replay)
        assertTrue(engine.state is LessonSessionState.AwaitingChild)
    }

    @Test
    fun replayIsRejectedOutsideChildTurn() {
        val engine = engine()
        startDrawWithMe(engine)

        val result = engine.dispatch(ReplayDemonstration)

        assertTrue(result is LessonCommandResult.Rejected)
        result as LessonCommandResult.Rejected
        assertEquals(LessonCommandRejectionCode.INVALID_STATE, result.rejection.code)
        assertTrue(engine.state is LessonSessionState.TeacherDemonstrating)
    }

    @Test
    fun skipIsRejectedForRequiredHeadStepWithoutMutation() {
        val engine = engine()
        val request = startDrawWithMe(engine)
        completeTeacher(engine, request)
        val before = engine.state

        val result = engine.dispatch(SkipStep)

        assertTrue(result is LessonCommandResult.Rejected)
        result as LessonCommandResult.Rejected
        assertEquals(LessonCommandRejectionCode.SKIP_NOT_ALLOWED, result.rejection.code)
        assertEquals(before, engine.state)
    }

    @Test
    fun authoredOptionalFaceStepCanBeSkippedAndAdvancesToBodyTail() {
        val engine = engine()
        var request = startDrawWithMe(engine)

        // head
        completeTeacher(engine, request)
        request = (engine.dispatch(MarkChildTurnDone) as LessonCommandResult.Accepted)
            .events.filterIsInstance<TeacherPlaybackRequested>().single()
        // ears
        completeTeacher(engine, request)
        request = (engine.dispatch(MarkChildTurnDone) as LessonCommandResult.Accepted)
            .events.filterIsInstance<TeacherPlaybackRequested>().single()
        // face
        assertEquals("face", request.request.stepId)
        completeTeacher(engine, request)

        val skipped = engine.dispatch(SkipStep) as LessonCommandResult.Accepted
        val completed = skipped.events.filterIsInstance<StepCompleted>().single()
        assertEquals("face", completed.stepId)
        assertTrue(completed.skipped)
        val nextRequest = skipped.events.filterIsInstance<TeacherPlaybackRequested>().single()
        assertEquals("body_tail", nextRequest.request.stepId)
    }

    @Test
    fun anyStrokePolicyAdvancesOnlyAfterCommittedChildOperationFromBoundDocument() {
        val base = packageData()
        val first = base.lesson.drawing.steps.first()
        val anyStrokeFirst = first.copy(
            childTurn = first.childTurn.copy(completionPolicy = ChildCompletionPolicy.ANY_STROKE),
        )
        val packageData = base.copy(
            lesson = base.lesson.copy(
                drawing = base.lesson.drawing.copy(
                    steps = listOf(anyStrokeFirst) + base.lesson.drawing.steps.drop(1),
                ),
            ),
        )
        val engine = engine(packageData)
        val request = startDrawWithMe(engine)
        completeTeacher(engine, request)

        val manualDone = engine.dispatch(MarkChildTurnDone)
        assertTrue(manualDone is LessonCommandResult.Rejected)
        manualDone as LessonCommandResult.Rejected
        assertEquals(
            LessonCommandRejectionCode.COMPLETION_POLICY_NOT_SATISFIED,
            manualDone.rejection.code,
        )

        val wrongDocument = engine.handle(
            LessonRuntimeSignal.ChildStrokeCommitted(
                childDocumentId = "other-document",
                operationId = "op-1",
            ),
        )
        assertTrue(wrongDocument is LessonSignalResult.Rejected)
        wrongDocument as LessonSignalResult.Rejected
        assertEquals(LessonSignalRejectionCode.DOCUMENT_MISMATCH, wrongDocument.rejection.code)
        assertTrue(engine.state is LessonSessionState.AwaitingChild)

        val committed = engine.handle(
            LessonRuntimeSignal.ChildStrokeCommitted(
                childDocumentId = "document-1",
                operationId = "op-2",
            ),
        ) as LessonSignalResult.Accepted
        val completed = committed.events.filterIsInstance<StepCompleted>().single()
        assertEquals("head", completed.stepId)
        assertTrue(!completed.skipped)
        val next = committed.events.filterIsInstance<TeacherPlaybackRequested>().single()
        assertEquals("ears", next.request.stepId)
    }

    @Test
    fun manualDonePolicyDoesNotAutoAdvanceOnChildStrokeCommit() {
        val engine = engine()
        val request = startDrawWithMe(engine)
        completeTeacher(engine, request)
        val before = engine.state

        val signal = engine.handle(
            LessonRuntimeSignal.ChildStrokeCommitted(
                childDocumentId = "document-1",
                operationId = "op-manual",
            ),
        )

        assertTrue(signal is LessonSignalResult.Accepted)
        assertTrue(signal.events.isEmpty())
        assertEquals(before, engine.state)
    }

    @Test
    fun teacherFailureBecomesRecoverableWithoutAdvancingChildStep() {
        val engine = engine()
        val request = startDrawWithMe(engine)

        val result = engine.handle(
            LessonRuntimeSignal.TeacherPlaybackFailed(
                requestId = request.request.requestId,
                reason = "renderer unavailable",
            ),
        )

        assertTrue(result is LessonSignalResult.Accepted)
        val error = engine.state as LessonSessionState.RecoverableError
        assertEquals(0, error.context.currentStepIndex)
        assertEquals("head", error.context.currentStepId)
        assertEquals(LessonSnapshotPhase.PREPARING_STEP, error.recoveryPhase)
    }

    private fun startDrawWithMe(
        engine: LessonSessionEngine,
        pace: TeachingPace = TeachingPace.NORMAL,
    ): TeacherPlaybackRequested {
        val result = engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.DRAW_WITH_ME, pace),
        )
        assertTrue(result is LessonCommandResult.Accepted)
        result as LessonCommandResult.Accepted
        return result.events.filterIsInstance<TeacherPlaybackRequested>().single()
    }

    private fun completeTeacher(
        engine: LessonSessionEngine,
        request: TeacherPlaybackRequested,
    ): LessonSignalResult.Accepted {
        val result = engine.handle(
            LessonRuntimeSignal.TeacherPlaybackCompleted(request.request.requestId),
        )
        assertTrue(result is LessonSignalResult.Accepted)
        return result as LessonSignalResult.Accepted
    }

    private fun engine(
        packageData: LessonRuntimePackage = packageData(),
    ): LessonSessionEngine = LessonSessionEngine.create(
        lessonPackage = packageData,
        sessionId = "session-1",
        childDocumentId = "document-1",
        clock = { 100L },
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
