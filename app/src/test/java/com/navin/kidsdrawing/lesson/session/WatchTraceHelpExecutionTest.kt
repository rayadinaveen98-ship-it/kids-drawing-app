package com.navin.kidsdrawing.lesson.session

import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentEngine
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentMetadata
import com.navin.kidsdrawing.drawing.domain.StrokeAuthorRole
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackEngine
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackStatus
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.assistance.GuideOverlayPurpose
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.ceil

class WatchTraceHelpExecutionTest {
    @Test
    fun watchThenDrawRunsOneFullOverviewBeforeFirstChildTurn() {
        val engine = engine()

        val start = engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.WATCH_THEN_DRAW, TeachingPace.NORMAL),
        ) as LessonCommandResult.Accepted
        val request = start.events.filterIsInstance<TeacherPlaybackRequested>().single().request

        assertEquals(TeacherPlaybackScope.OVERVIEW, request.scope)
        assertEquals("__overview__", request.stepId)
        assertEquals(8, request.sequence.strokes.size)
        assertTrue(request.sequence.strokes.all {
            it.stroke.authorRole == StrokeAuthorRole.TEACHER_GENERATED
        })
        val overviewState = engine.state as LessonSessionState.OverviewDemonstrating
        assertFalse(overviewState.context.overviewCompleted)

        val playback = TeacherPlaybackEngine(request.sequence, request.pace)
        playback.play()
        val duration = ceil(request.sequence.sourceDurationMillis.toDouble()).toLong() + 1L
        assertEquals(TeacherPlaybackStatus.COMPLETED, playback.advanceBy(duration).status)

        val complete = engine.handle(
            LessonRuntimeSignal.TeacherPlaybackCompleted(request.requestId),
        ) as LessonSignalResult.Accepted

        val child = engine.state as LessonSessionState.AwaitingChild
        assertEquals("head", child.context.currentStepId)
        assertTrue(child.context.overviewCompleted)
        assertTrue(complete.events.any { it === OverviewCompleted })
        assertTrue(complete.events.any { it is ChildTurnStarted && it.stepId == "head" })
    }

    @Test
    fun watchOverviewPausePaceResumeAndSkipUseSameRequest() {
        val engine = engine()
        val start = engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.WATCH_THEN_DRAW, TeachingPace.SLOW),
        ) as LessonCommandResult.Accepted
        val requestId = start.events.filterIsInstance<TeacherPlaybackRequested>().single().request.requestId

        val pause = engine.dispatch(LessonCommand.Pause) as LessonCommandResult.Accepted
        assertTrue(pause.events.any {
            it is TeacherPlaybackPauseRequested && it.requestId == requestId
        })

        val pace = engine.dispatch(LessonCommand.SetPace(TeachingPace.FAST)) as LessonCommandResult.Accepted
        assertTrue(pace.events.any {
            it is TeacherPlaybackPaceChangeRequested && it.requestId == requestId && it.pace == TeachingPace.FAST
        })

        val resume = engine.dispatch(LessonCommand.Resume) as LessonCommandResult.Accepted
        assertTrue(resume.events.any {
            it is TeacherPlaybackResumeRequested && it.requestId == requestId
        })

        val skip = engine.dispatch(SkipOverview) as LessonCommandResult.Accepted
        assertTrue(skip.events.any {
            it is TeacherPlaybackCancelRequested && it.requestId == requestId
        })
        assertTrue(skip.events.any { it === OverviewSkipped })
        val child = engine.state as LessonSessionState.AwaitingChild
        assertTrue(child.context.overviewCompleted)
        assertEquals("head", child.context.currentStepId)
    }

    @Test
    fun watchDrawingPassDoesNotAutoRepeatTeacherBetweenSteps() {
        val engine = engine()
        val overview = startWatch(engine)
        engine.handle(LessonRuntimeSignal.TeacherPlaybackCompleted(overview.request.requestId))

        val visited = mutableListOf<String>()
        repeat(4) { index ->
            val child = engine.state as LessonSessionState.AwaitingChild
            visited += child.context.currentStepId
            val done = engine.dispatch(MarkChildTurnDone) as LessonCommandResult.Accepted
            assertTrue(done.events.none { it is TeacherPlaybackRequested })
            if (index < 3) {
                assertTrue(engine.state is LessonSessionState.AwaitingChild)
            }
        }

        assertEquals(listOf("head", "ears", "face", "body_tail"), visited)
        assertTrue(engine.state is LessonSessionState.DrawingComplete)
    }

    @Test
    fun watchChildCanReplayOneStepWithoutResettingOverviewOrCursor() {
        val engine = engine()
        val overview = startWatch(engine)
        engine.handle(LessonRuntimeSignal.TeacherPlaybackCompleted(overview.request.requestId))

        val replay = engine.dispatch(ReplayDemonstration) as LessonCommandResult.Accepted
        val request = replay.events.filterIsInstance<TeacherPlaybackRequested>().single().request
        assertEquals(TeacherPlaybackScope.STEP, request.scope)
        assertEquals("head", request.stepId)
        assertTrue(request.replay)

        engine.handle(LessonRuntimeSignal.TeacherPlaybackCompleted(request.requestId))
        val child = engine.state as LessonSessionState.AwaitingChild
        assertEquals(0, child.context.currentStepIndex)
        assertEquals("head", child.context.currentStepId)
        assertTrue(child.context.overviewCompleted)
    }

    @Test
    fun traceModeTeacherCompletionShowsAuthoredTraceGuide() {
        val engine = engine()
        val request = startTrace(engine)

        val completed = engine.handle(
            LessonRuntimeSignal.TeacherPlaybackCompleted(request.request.requestId),
        ) as LessonSignalResult.Accepted

        assertTrue(engine.state is LessonSessionState.AwaitingChild)
        val guide = completed.events.filterIsInstance<GuideOverlayRequested>().single().request
        assertEquals(GuideOverlayPurpose.TRACE_MODE, guide.purpose)
        assertEquals("head", guide.stepId)
        assertEquals(0, guide.helpLevel)
        assertEquals(HelpKind.TRACE_PATH, guide.helpKind)
        assertTrue(guide.sequence.strokes.all {
            it.stroke.authorRole == StrokeAuthorRole.TEACHER_GENERATED
        })
    }

    @Test
    fun traceReplayClearsGuideThenRestoresItAfterPlayback() {
        val engine = engine()
        val first = startTrace(engine)
        engine.handle(LessonRuntimeSignal.TeacherPlaybackCompleted(first.request.requestId))

        val replay = engine.dispatch(ReplayDemonstration) as LessonCommandResult.Accepted
        assertTrue(replay.events.any { it is GuideOverlayCleared && it.stepId == "head" })
        val request = replay.events.filterIsInstance<TeacherPlaybackRequested>().single().request

        val complete = engine.handle(
            LessonRuntimeSignal.TeacherPlaybackCompleted(request.requestId),
        ) as LessonSignalResult.Accepted
        val restored = complete.events.filterIsInstance<GuideOverlayRequested>().single().request
        assertEquals(GuideOverlayPurpose.TRACE_MODE, restored.purpose)
        assertEquals("head", restored.stepId)
        assertTrue(engine.state is LessonSessionState.AwaitingChild)
    }

    @Test
    fun traceStepCompletionClearsGuideAndAutoStartsNextTeacher() {
        val engine = engine()
        val head = startTrace(engine)
        engine.handle(LessonRuntimeSignal.TeacherPlaybackCompleted(head.request.requestId))

        val done = engine.dispatch(MarkChildTurnDone) as LessonCommandResult.Accepted

        assertTrue(done.events.any { it is GuideOverlayCleared && it.stepId == "head" })
        val next = done.events.filterIsInstance<TeacherPlaybackRequested>().single().request
        assertEquals("ears", next.stepId)
        assertEquals(TeacherPlaybackScope.STEP, next.scope)
        val state = engine.state as LessonSessionState.TeacherDemonstrating
        assertEquals("ears", state.context.currentStepId)
    }

    @Test
    fun helpLadderSkipsMissingLevelsAndStopsAtHighestAuthoredLevel() {
        val engine = drawWithMeAtHeadChildTurn()

        val help1 = engine.dispatch(RequestHelp) as LessonCommandResult.Accepted
        val level1 = engine.state as LessonSessionState.HelpActive
        assertEquals(1, level1.context.helpLevel)
        val change1 = help1.events.filterIsInstance<HelpLevelChanged>().single()
        assertEquals(0, change1.previousLevel)
        assertEquals(1, change1.currentLevel)
        assertEquals(HelpKind.GENTLE_HINT, change1.kind)
        assertTrue(help1.events.none { it is GuideOverlayRequested })

        val help4 = engine.dispatch(RequestHelp) as LessonCommandResult.Accepted
        val level4 = engine.state as LessonSessionState.HelpActive
        assertEquals(4, level4.context.helpLevel)
        val change4 = help4.events.filterIsInstance<HelpLevelChanged>().single()
        assertEquals(1, change4.previousLevel)
        assertEquals(4, change4.currentLevel)
        assertEquals(HelpKind.TRACE_PATH, change4.kind)
        val guide = help4.events.filterIsInstance<GuideOverlayRequested>().single().request
        assertEquals(GuideOverlayPurpose.HELP, guide.purpose)
        assertEquals(4, guide.helpLevel)

        val exhausted = engine.dispatch(RequestHelp)
        assertTrue(exhausted is LessonCommandResult.Rejected)
        exhausted as LessonCommandResult.Rejected
        assertEquals(LessonCommandRejectionCode.HELP_NOT_AVAILABLE, exhausted.rejection.code)
        assertEquals(4, (engine.state as LessonSessionState.HelpActive).context.helpLevel)
    }

    @Test
    fun reduceAndDismissHelpFollowAuthoredLevels() {
        val engine = drawWithMeAtHeadChildTurn()
        engine.dispatch(RequestHelp)
        engine.dispatch(RequestHelp)

        val reduce = engine.dispatch(ReduceHelp) as LessonCommandResult.Accepted
        assertEquals(1, (engine.state as LessonSessionState.HelpActive).context.helpLevel)
        assertTrue(reduce.events.any { it is GuideOverlayCleared })
        assertTrue(reduce.events.none { it is GuideOverlayRequested })

        val dismiss = engine.dispatch(DismissHelp) as LessonCommandResult.Accepted
        assertEquals(0, (engine.state as LessonSessionState.AwaitingChild).context.helpLevel)
        val change = dismiss.events.filterIsInstance<HelpLevelChanged>().single()
        assertEquals(1, change.previousLevel)
        assertEquals(0, change.currentLevel)
    }

    @Test
    fun traceHelpReductionRestoresBaseTraceGuide() {
        val engine = engine()
        val teacher = startTrace(engine)
        engine.handle(LessonRuntimeSignal.TeacherPlaybackCompleted(teacher.request.requestId))

        engine.dispatch(RequestHelp) // 0 -> 1, base trace remains
        val toFour = engine.dispatch(RequestHelp) as LessonCommandResult.Accepted
        assertEquals(
            GuideOverlayPurpose.HELP,
            toFour.events.filterIsInstance<GuideOverlayRequested>().single().request.purpose,
        )

        val reduced = engine.dispatch(ReduceHelp) as LessonCommandResult.Accepted
        val restored = reduced.events.filterIsInstance<GuideOverlayRequested>().single().request
        assertEquals(GuideOverlayPurpose.TRACE_MODE, restored.purpose)
        assertEquals(1, (engine.state as LessonSessionState.HelpActive).context.helpLevel)

        val dismissed = engine.dispatch(DismissHelp) as LessonCommandResult.Accepted
        val base = dismissed.events.filterIsInstance<GuideOverlayRequested>().single().request
        assertEquals(GuideOverlayPurpose.TRACE_MODE, base.purpose)
        assertEquals(0, (engine.state as LessonSessionState.AwaitingChild).context.helpLevel)
    }

    @Test
    fun sameCuteCatPackageCompletesInAllThreeModes() {
        TeachingMode.entries.forEach { mode ->
            val engine = engine()
            val start = engine.dispatch(LessonCommand.StartLesson(mode, TeachingPace.NORMAL))
                as LessonCommandResult.Accepted

            when (mode) {
                TeachingMode.WATCH_THEN_DRAW -> {
                    val overview = start.events.filterIsInstance<TeacherPlaybackRequested>().single().request
                    engine.handle(LessonRuntimeSignal.TeacherPlaybackCompleted(overview.requestId))
                    repeat(4) { engine.dispatch(MarkChildTurnDone) }
                }
                TeachingMode.DRAW_WITH_ME,
                TeachingMode.TRACE_AND_LEARN,
                -> repeat(4) {
                    val active = engine.state as LessonSessionState.TeacherDemonstrating
                    engine.handle(LessonRuntimeSignal.TeacherPlaybackCompleted(active.requestId))
                    engine.dispatch(MarkChildTurnDone)
                }
            }

            assertTrue("Expected $mode to reach DrawingComplete", engine.state is LessonSessionState.DrawingComplete)
        }
    }

    @Test
    fun guideStrokesCannotEnterRealChildHistory() = runBlocking {
        val packageData = packageData()
        val childDocument = DrawingDocument(
            documentId = "child-doc",
            logicalSize = DocumentSize(1000f, 1000f),
            createdAtEpochMillis = 1L,
            modifiedAtEpochMillis = 1L,
            metadata = DrawingDocumentMetadata(
                lessonId = packageData.lesson.lessonId,
                lessonRevision = packageData.lesson.revision,
            ),
        )
        val childEngine = DrawingDocumentEngine(
            initialDocument = childDocument,
            clockMillis = { 2L },
            idFactory = { "must-not-commit" },
        )
        val engine = engine(packageData)
        val teacher = startTrace(engine)
        val completed = engine.handle(
            LessonRuntimeSignal.TeacherPlaybackCompleted(teacher.request.requestId),
        ) as LessonSignalResult.Accepted
        val guideStroke = completed.events
            .filterIsInstance<GuideOverlayRequested>()
            .single()
            .request
            .sequence
            .strokes
            .first()
            .stroke

        var rejected = false
        try {
            childEngine.commitChildStroke(guideStroke)
        } catch (_: IllegalArgumentException) {
            rejected = true
        }

        assertTrue(rejected)
        assertEquals(childDocument, childEngine.state.value.document)
        assertTrue(childEngine.state.value.document.operations.isEmpty())
    }

    private fun startWatch(engine: LessonSessionEngine): TeacherPlaybackRequested {
        val start = engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.WATCH_THEN_DRAW, TeachingPace.NORMAL),
        ) as LessonCommandResult.Accepted
        return start.events.filterIsInstance<TeacherPlaybackRequested>().single()
    }

    private fun startTrace(engine: LessonSessionEngine): TeacherPlaybackRequested {
        val start = engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.TRACE_AND_LEARN, TeachingPace.NORMAL),
        ) as LessonCommandResult.Accepted
        return start.events.filterIsInstance<TeacherPlaybackRequested>().single()
    }

    private fun drawWithMeAtHeadChildTurn(): LessonSessionEngine {
        val engine = engine()
        val start = engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.DRAW_WITH_ME, TeachingPace.NORMAL),
        ) as LessonCommandResult.Accepted
        val teacher = start.events.filterIsInstance<TeacherPlaybackRequested>().single().request
        engine.handle(LessonRuntimeSignal.TeacherPlaybackCompleted(teacher.requestId))
        return engine
    }

    private fun engine(
        packageData: LessonRuntimePackage = packageData(),
    ): LessonSessionEngine = LessonSessionEngine.create(
        lessonPackage = packageData,
        sessionId = "p2-4-session",
        childDocumentId = "p2-4-document",
        clock = { 1_000L },
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
