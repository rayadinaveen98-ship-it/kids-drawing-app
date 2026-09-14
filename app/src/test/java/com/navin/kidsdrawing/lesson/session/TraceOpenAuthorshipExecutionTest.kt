package com.navin.kidsdrawing.lesson.session

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.assistance.GuideOverlayPurpose
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TraceOpenAuthorshipExecutionTest {
    @Test
    fun happyLinesTraceSupportsStructuredPracticeThenYieldsToOpenAuthorship() {
        assertTraceYieldsAtOpenFinal(
            lessonId = "happy-lines",
            structuredStepIds = listOf("straight_lines", "curves_and_waves", "zigzag", "loops"),
            openStepId = "make_marks_yours",
        )
    }

    @Test
    fun shapeFriendsTraceFallsBackToExpectedGeometryThenYieldsToOpenAuthorship() {
        val packageData = packageData("shape-friends")
        val engine = engine(packageData, "shape")
        val start = engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.TRACE_AND_LEARN, TeachingPace.NORMAL),
        ) as LessonCommandResult.Accepted

        var request = start.events.filterIsInstance<TeacherPlaybackRequested>().single().request
        listOf("circle", "square", "triangle").forEach { stepId ->
            assertEquals(stepId, request.stepId)
            val completed = engine.handle(
                LessonRuntimeSignal.TeacherPlaybackCompleted(request.requestId),
            ) as LessonSignalResult.Accepted
            assertEquals(
                GuideOverlayPurpose.TRACE_MODE,
                completed.events.filterIsInstance<GuideOverlayRequested>().single().request.purpose,
            )
            request = (
                engine.dispatch(MarkChildTurnDone) as LessonCommandResult.Accepted
                ).events.filterIsInstance<TeacherPlaybackRequested>().single().request
        }

        assertEquals("build_friend", request.stepId)
        val construction = engine.handle(
            LessonRuntimeSignal.TeacherPlaybackCompleted(request.requestId),
        ) as LessonSignalResult.Accepted
        val constructionGuide = construction.events.filterIsInstance<GuideOverlayRequested>().single().request
        assertEquals(GuideOverlayPurpose.TRACE_MODE, constructionGuide.purpose)
        assertEquals("build_friend", constructionGuide.stepId)
        assertEquals(3, constructionGuide.sequence.strokes.size)

        val openTeacher = (
            engine.dispatch(MarkChildTurnDone) as LessonCommandResult.Accepted
            ).events.filterIsInstance<TeacherPlaybackRequested>().single().request
        assertEquals("make_friend_yours", openTeacher.stepId)

        val open = engine.handle(
            LessonRuntimeSignal.TeacherPlaybackCompleted(openTeacher.requestId),
        ) as LessonSignalResult.Accepted
        assertTrue(open.events.none { it is GuideOverlayRequested })
        assertTrue(engine.state is LessonSessionState.AwaitingChild)

        val help = engine.dispatch(RequestHelp) as LessonCommandResult.Accepted
        assertTrue(help.events.none { it is GuideOverlayRequested })

        val done = engine.dispatch(MarkChildTurnDone) as LessonCommandResult.Accepted
        assertTrue(done.events.any { it is DrawingLessonCompleted })
        assertTrue(engine.state is LessonSessionState.DrawingComplete)
    }

    private fun assertTraceYieldsAtOpenFinal(
        lessonId: String,
        structuredStepIds: List<String>,
        openStepId: String,
    ) {
        val packageData = packageData(lessonId)
        val engine = engine(packageData, lessonId)
        val start = engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.TRACE_AND_LEARN, TeachingPace.NORMAL),
        ) as LessonCommandResult.Accepted
        var request = start.events.filterIsInstance<TeacherPlaybackRequested>().single().request

        structuredStepIds.forEach { stepId ->
            assertEquals(stepId, request.stepId)
            val completed = engine.handle(
                LessonRuntimeSignal.TeacherPlaybackCompleted(request.requestId),
            ) as LessonSignalResult.Accepted
            val guide = completed.events.filterIsInstance<GuideOverlayRequested>().single().request
            assertEquals(GuideOverlayPurpose.TRACE_MODE, guide.purpose)
            assertEquals(stepId, guide.stepId)

            request = (
                engine.dispatch(MarkChildTurnDone) as LessonCommandResult.Accepted
                ).events.filterIsInstance<TeacherPlaybackRequested>().single().request
        }

        assertEquals(openStepId, request.stepId)
        val open = engine.handle(
            LessonRuntimeSignal.TeacherPlaybackCompleted(request.requestId),
        ) as LessonSignalResult.Accepted
        assertTrue(open.events.none { it is GuideOverlayRequested })
        assertTrue(engine.state is LessonSessionState.AwaitingChild)

        val help = engine.dispatch(RequestHelp) as LessonCommandResult.Accepted
        assertTrue(help.events.none { it is GuideOverlayRequested })

        val done = engine.dispatch(MarkChildTurnDone) as LessonCommandResult.Accepted
        assertTrue(done.events.any { it is DrawingLessonCompleted })
        assertTrue(engine.state is LessonSessionState.DrawingComplete)
    }

    private fun engine(packageData: LessonRuntimePackage, suffix: String): LessonSessionEngine =
        LessonSessionEngine.create(
            lessonPackage = packageData,
            sessionId = "p5-4-trace-$suffix",
            childDocumentId = "p5-4-doc-$suffix",
            clock = { 1_000L },
        )

    private fun packageData(lessonId: String): LessonRuntimePackage {
        val loader = LessonPackageLoader(
            LessonPackageSource { path ->
                File("src/main/assets/$path").takeIf(File::isFile)?.readText()
            },
        )
        val result = loader.load("lessons/$lessonId")
        assertTrue("$lessonId failed to load: $result", result is LessonLoadResult.Success)
        return (result as LessonLoadResult.Success).packageData
    }
}
