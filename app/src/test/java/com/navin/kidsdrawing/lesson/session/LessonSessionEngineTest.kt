package com.navin.kidsdrawing.lesson.session

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LessonSessionEngineTest {
    @Test
    fun newEngineStartsReady() {
        val engine = engine()

        assertSame(LessonSessionState.Ready, engine.state)
    }

    @Test
    fun drawWithMeStartsTeacherImmediatelyAcrossAllPaces() {
        TeachingPace.entries.forEach { pace ->
            val engine = engine()

            val result = engine.dispatch(
                LessonCommand.StartLesson(TeachingMode.DRAW_WITH_ME, pace),
            )

            assertTrue(result is LessonCommandResult.Accepted)
            val state = engine.state as LessonSessionState.TeacherDemonstrating
            assertEquals(TeachingMode.DRAW_WITH_ME, state.context.mode)
            assertEquals(pace, state.context.pace)
            assertEquals(0, state.context.currentStepIndex)
            assertEquals("head", state.context.currentStepId)
            assertEquals(0, state.context.helpLevel)
            assertTrue(state.context.overviewCompleted)
            assertTrue(result.events.filterIsInstance<TeacherPlaybackRequested>().size == 1)
        }
    }

    @Test
    fun traceModeRetainsPreparingStepBoundaryAcrossAllPacesUntilP24() {
        TeachingPace.entries.forEach { pace ->
            val engine = engine()

            val result = engine.dispatch(
                LessonCommand.StartLesson(TeachingMode.TRACE_AND_LEARN, pace),
            )

            assertTrue(result is LessonCommandResult.Accepted)
            val state = engine.state as LessonSessionState.PreparingStep
            assertEquals(TeachingMode.TRACE_AND_LEARN, state.context.mode)
            assertEquals(pace, state.context.pace)
            assertEquals(0, state.context.currentStepIndex)
            assertEquals("head", state.context.currentStepId)
            assertTrue(state.context.overviewCompleted)
        }
    }

    @Test
    fun watchThenDrawStartsInOverviewForAllPaces() {
        TeachingPace.entries.forEach { pace ->
            val engine = engine()

            engine.dispatch(LessonCommand.StartLesson(TeachingMode.WATCH_THEN_DRAW, pace))

            val state = engine.state as LessonSessionState.OverviewDemonstrating
            assertEquals(pace, state.context.pace)
            assertFalse(state.context.overviewCompleted)
        }
    }

    @Test
    fun unsupportedModeIsRejectedWithoutMutatingState() {
        val base = packageData()
        val restricted = base.copy(
            lesson = base.lesson.copy(supportedModes = listOf(TeachingMode.DRAW_WITH_ME)),
        )
        val engine = engine(restricted)

        val result = engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.TRACE_AND_LEARN, TeachingPace.NORMAL),
        )

        assertTrue(result is LessonCommandResult.Rejected)
        result as LessonCommandResult.Rejected
        assertEquals(LessonCommandRejectionCode.UNSUPPORTED_MODE, result.rejection.code)
        assertSame(LessonSessionState.Ready, engine.state)
    }

    @Test
    fun illegalCommandsAreRejectedDeterministicallyWithoutMutation() {
        val engine = engine()

        val pause = engine.dispatch(LessonCommand.Pause)
        val resume = engine.dispatch(LessonCommand.Resume)
        val pace = engine.dispatch(LessonCommand.SetPace(TeachingPace.FAST))

        listOf(pause, resume, pace).forEach { result ->
            assertTrue(result is LessonCommandResult.Rejected)
            assertEquals(
                LessonCommandRejectionCode.INVALID_STATE,
                (result as LessonCommandResult.Rejected).rejection.code,
            )
        }
        assertSame(LessonSessionState.Ready, engine.state)
    }

    @Test
    fun pauseResumeReturnsToExactPriorStableState() {
        val engine = engine()
        engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.WATCH_THEN_DRAW, TeachingPace.SLOW),
        )
        val beforePause = engine.state

        val pause = engine.dispatch(LessonCommand.Pause)
        assertTrue(pause is LessonCommandResult.Accepted)
        val paused = engine.state as LessonSessionState.Paused
        assertEquals(beforePause, paused.previousStableState)

        val resume = engine.dispatch(LessonCommand.Resume)
        assertTrue(resume is LessonCommandResult.Accepted)
        assertEquals(beforePause, engine.state)
    }

    @Test
    fun paceChangePreservesCursorAndTeacherRequestWhilePaused() {
        val engine = engine()
        engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.DRAW_WITH_ME, TeachingPace.NORMAL),
        )
        val originalTeacher = engine.state as LessonSessionState.TeacherDemonstrating
        engine.dispatch(LessonCommand.Pause)

        val result = engine.dispatch(LessonCommand.SetPace(TeachingPace.VERY_FAST))

        assertTrue(result is LessonCommandResult.Accepted)
        val paused = engine.state as LessonSessionState.Paused
        assertEquals(TeachingPace.VERY_FAST, paused.context.pace)
        assertEquals(0, paused.context.currentStepIndex)
        assertEquals("head", paused.context.currentStepId)
        val pausedTeacher = paused.previousStableState as LessonSessionState.TeacherDemonstrating
        assertEquals(originalTeacher.requestId, pausedTeacher.requestId)
        assertTrue(result.events.any {
            it is TeacherPlaybackPaceChangeRequested &&
                it.requestId == originalTeacher.requestId &&
                it.pace == TeachingPace.VERY_FAST
        })

        val resume = engine.dispatch(LessonCommand.Resume)
        val resumed = engine.state as LessonSessionState.TeacherDemonstrating
        assertEquals(TeachingPace.VERY_FAST, resumed.context.pace)
        assertEquals(originalTeacher.requestId, resumed.requestId)
        assertTrue(resume.events.any {
            it is TeacherPlaybackResumeRequested && it.requestId == originalTeacher.requestId
        })
    }

    @Test
    fun samePaceIsAcceptedAsDeterministicNoOp() {
        val engine = engine()
        engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.DRAW_WITH_ME, TeachingPace.NORMAL),
        )
        val before = engine.state

        val result = engine.dispatch(LessonCommand.SetPace(TeachingPace.NORMAL))

        assertTrue(result is LessonCommandResult.Accepted)
        assertTrue(result.events.isEmpty())
        assertEquals(before, engine.state)
    }

    @Test
    fun snapshotUsesInjectedClockAndRoundTripsEveryPace() {
        TeachingPace.entries.forEach { pace ->
            val engine = engine(clockValue = 123_456L)
            engine.dispatch(LessonCommand.StartLesson(TeachingMode.DRAW_WITH_ME, pace))

            val snapshot = snapshot(engine)
            assertEquals(123_456L, snapshot.savedAtEpochMillis)
            assertEquals(pace, snapshot.pace)
            assertEquals(LessonSnapshotPhase.PREPARING_STEP, snapshot.phase)

            val restored = LessonSessionEngine.restore(packageData(), snapshot)
            assertTrue(restored is LessonRestoreResult.Restored)
            val restoredState = (restored as LessonRestoreResult.Restored).engine.state
                as LessonSessionState.PreparingStep
            assertEquals(pace, restoredState.context.pace)
        }
    }

    @Test
    fun saveAndExitEmitsResumableSnapshotBeforeTerminalTransition() {
        val engine = engine(clockValue = 777L)
        engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.DRAW_WITH_ME, TeachingPace.FAST),
        )
        val requestId = (engine.state as LessonSessionState.TeacherDemonstrating).requestId

        val result = engine.dispatch(LessonCommand.SaveAndExit)

        assertTrue(result is LessonCommandResult.Accepted)
        result as LessonCommandResult.Accepted
        val event = result.events.filterIsInstance<LessonSessionEvent.SaveAndExitRequested>().single()
        assertEquals(LessonSnapshotPhase.PREPARING_STEP, event.snapshot.phase)
        assertEquals(777L, event.snapshot.savedAtEpochMillis)
        assertTrue(result.events.any {
            it is TeacherPlaybackCancelRequested && it.requestId == requestId
        })
        val finished = engine.state as LessonSessionState.Finished
        assertEquals(LessonFinishReason.SAVED_FOR_LATER, finished.reason)

        val restored = LessonSessionEngine.restore(packageData(), event.snapshot)
        assertTrue(restored is LessonRestoreResult.Restored)
        assertTrue((restored as LessonRestoreResult.Restored).engine.state is LessonSessionState.PreparingStep)
    }

    @Test
    fun stableSnapshotsRestoreToMatchingStablePhases() {
        val base = activeSnapshot()
        val cases = listOf(
            LessonSnapshotPhase.PREPARING_STEP to LessonSessionState.PreparingStep::class,
            LessonSnapshotPhase.AWAITING_CHILD to LessonSessionState.AwaitingChild::class,
            LessonSnapshotPhase.DRAWING_COMPLETE to LessonSessionState.DrawingComplete::class,
            LessonSnapshotPhase.AWAITING_POST_DRAWING_CHOICE to LessonSessionState.AwaitingPostDrawingChoice::class,
        )

        cases.forEach { (phase, expectedClass) ->
            val restored = LessonSessionEngine.restore(packageData(), base.copy(phase = phase))
            assertTrue("Expected restored result for $phase", restored is LessonRestoreResult.Restored)
            restored as LessonRestoreResult.Restored
            assertEquals(LessonRestoreNormalization.NONE, restored.normalization)
            assertEquals(expectedClass, restored.engine.state::class)
        }
    }

    @Test
    fun helpActiveRestoresOnlyWithPositiveHelpLevel() {
        val base = activeSnapshot()

        val good = LessonSessionEngine.restore(
            packageData(),
            base.copy(phase = LessonSnapshotPhase.HELP_ACTIVE, helpLevel = 2),
        )
        assertTrue(good is LessonRestoreResult.Restored)
        assertTrue((good as LessonRestoreResult.Restored).engine.state is LessonSessionState.HelpActive)

        val bad = LessonSessionEngine.restore(
            packageData(),
            base.copy(phase = LessonSnapshotPhase.HELP_ACTIVE, helpLevel = 0),
        )
        assertIncompatible(bad, LessonRestoreIncompatibilityCode.INVALID_HELP_LEVEL)
    }

    @Test
    fun transientTeacherStateNormalizesToPreparingStep() {
        val restored = LessonSessionEngine.restore(
            packageData(),
            activeSnapshot().copy(phase = LessonSnapshotPhase.TEACHER_DEMONSTRATING),
        )

        assertTrue(restored is LessonRestoreResult.Restored)
        restored as LessonRestoreResult.Restored
        assertEquals(
            LessonRestoreNormalization.RESTART_CURRENT_TEACHER_DEMONSTRATION,
            restored.normalization,
        )
        assertTrue(restored.engine.state is LessonSessionState.PreparingStep)
    }

    @Test
    fun transientCompletingStepReturnsToChildTurnInsteadOfAdvancing() {
        val restored = LessonSessionEngine.restore(
            packageData(),
            activeSnapshot().copy(phase = LessonSnapshotPhase.COMPLETING_STEP),
        )

        assertTrue(restored is LessonRestoreResult.Restored)
        restored as LessonRestoreResult.Restored
        assertEquals(LessonRestoreNormalization.RETURN_TO_CHILD_TURN, restored.normalization)
        assertTrue(restored.engine.state is LessonSessionState.AwaitingChild)
        val state = restored.engine.state as LessonSessionState.AwaitingChild
        assertEquals(0, state.context.currentStepIndex)
        assertEquals("head", state.context.currentStepId)
    }

    @Test
    fun coloringHandoffRestoresToChoiceSoHandoffCanRetrySafely() {
        val restored = LessonSessionEngine.restore(
            packageData(),
            activeSnapshot().copy(phase = LessonSnapshotPhase.HANDING_OFF_TO_COLORING),
        )

        assertTrue(restored is LessonRestoreResult.Restored)
        restored as LessonRestoreResult.Restored
        assertEquals(LessonRestoreNormalization.RETRY_POST_DRAWING_CHOICE, restored.normalization)
        assertTrue(restored.engine.state is LessonSessionState.AwaitingPostDrawingChoice)
    }

    @Test
    fun pausedTeacherSnapshotRestoresPausedAtSafePreparingStep() {
        val restored = LessonSessionEngine.restore(
            packageData(),
            activeSnapshot().copy(
                phase = LessonSnapshotPhase.PAUSED,
                pausedResumePhase = LessonSnapshotPhase.TEACHER_DEMONSTRATING,
            ),
        )

        assertTrue(restored is LessonRestoreResult.Restored)
        restored as LessonRestoreResult.Restored
        assertEquals(
            LessonRestoreNormalization.RESTART_CURRENT_TEACHER_DEMONSTRATION,
            restored.normalization,
        )
        val paused = restored.engine.state as LessonSessionState.Paused
        assertTrue(paused.previousStableState is LessonSessionState.PreparingStep)
    }

    @Test
    fun invalidPausedResumePhaseIsRejected() {
        val restored = LessonSessionEngine.restore(
            packageData(),
            activeSnapshot().copy(
                phase = LessonSnapshotPhase.PAUSED,
                pausedResumePhase = LessonSnapshotPhase.DRAWING_COMPLETE,
            ),
        )

        assertIncompatible(restored, LessonRestoreIncompatibilityCode.INVALID_PAUSED_PHASE)
    }

    @Test
    fun incompatibleLessonRevisionIsRejectedWithoutGuessing() {
        val restored = LessonSessionEngine.restore(
            packageData(),
            activeSnapshot().copy(lessonRevision = 99),
        )

        assertIncompatible(restored, LessonRestoreIncompatibilityCode.LESSON_REVISION_MISMATCH)
    }

    @Test
    fun mismatchedStepIdIsRejectedEvenWhenIndexExists() {
        val restored = LessonSessionEngine.restore(
            packageData(),
            activeSnapshot().copy(currentStepId = "ears"),
        )

        assertIncompatible(restored, LessonRestoreIncompatibilityCode.INVALID_STEP)
    }

    @Test
    fun unsupportedSnapshotVersionIsRejected() {
        val restored = LessonSessionEngine.restore(
            packageData(),
            activeSnapshot().copy(formatVersion = 999),
        )

        assertIncompatible(restored, LessonRestoreIncompatibilityCode.UNSUPPORTED_SNAPSHOT_VERSION)
    }

    @Test
    fun readySnapshotRestoresWithoutInventingModeOrCursor() {
        val engine = engine(clockValue = 42L)
        val snapshot = snapshot(engine)

        assertEquals(LessonSnapshotPhase.READY, snapshot.phase)
        assertEquals(null, snapshot.mode)
        assertEquals(null, snapshot.pace)
        assertEquals(null, snapshot.currentStepIndex)
        assertEquals(null, snapshot.currentStepId)

        val restored = LessonSessionEngine.restore(packageData(), snapshot)
        assertTrue(restored is LessonRestoreResult.Restored)
        assertSame(LessonSessionState.Ready, (restored as LessonRestoreResult.Restored).engine.state)
    }

    private fun engine(
        packageData: LessonRuntimePackage = packageData(),
        clockValue: Long = 100L,
    ): LessonSessionEngine = LessonSessionEngine.create(
        lessonPackage = packageData,
        sessionId = "session-1",
        childDocumentId = "document-1",
        clock = { clockValue },
    )

    private fun activeSnapshot(): LessonSessionSnapshot = LessonSessionSnapshot(
        sessionId = "session-restore",
        lessonId = "cute-cat",
        lessonRevision = 1,
        childDocumentId = "document-restore",
        mode = TeachingMode.DRAW_WITH_ME,
        pace = TeachingPace.NORMAL,
        phase = LessonSnapshotPhase.AWAITING_CHILD,
        currentStepIndex = 0,
        currentStepId = "head",
        helpLevel = 0,
        overviewCompleted = true,
        savedAtEpochMillis = 500L,
    )

    private fun snapshot(engine: LessonSessionEngine): LessonSessionSnapshot {
        val result = engine.createSnapshot()
        assertTrue(result is LessonSnapshotResult.Created)
        return (result as LessonSnapshotResult.Created).snapshot
    }

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

    private fun assertIncompatible(
        result: LessonRestoreResult,
        expectedCode: LessonRestoreIncompatibilityCode,
    ) {
        assertTrue("Expected incompatible restore, got $result", result is LessonRestoreResult.Incompatible)
        result as LessonRestoreResult.Incompatible
        assertEquals(expectedCode, result.incompatibility.code)
    }

    private companion object {
        const val ROOT = "lessons/cute-cat"
    }
}
