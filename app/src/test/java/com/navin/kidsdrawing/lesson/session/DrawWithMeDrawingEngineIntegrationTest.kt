package com.navin.kidsdrawing.lesson.session

import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackEngine
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackStatus
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.ceil

class DrawWithMeDrawingEngineIntegrationTest {
    @Test
    fun authoredHeadTeacherRequestRunsThroughFrozenPlaybackEngineAtEveryPace() {
        TeachingPace.entries.forEach { pace ->
            val lessonEngine = lessonEngine()
            val start = lessonEngine.dispatch(
                LessonCommand.StartLesson(TeachingMode.DRAW_WITH_ME, pace),
            ) as LessonCommandResult.Accepted
            val request = start.events.filterIsInstance<TeacherPlaybackRequested>().single().request

            val playback = TeacherPlaybackEngine(
                sequence = request.sequence,
                initialPace = request.pace,
            )
            playback.play()
            val realMillisToComplete = ceil(
                request.sequence.sourceDurationMillis.toDouble() / pace.multiplier,
            ).toLong() + 1L
            val completedFrame = playback.advanceBy(realMillisToComplete)

            assertEquals(TeacherPlaybackStatus.COMPLETED, completedFrame.status)
            assertEquals(1f, completedFrame.progress)
            assertEquals(request.sequence.strokes.size, completedFrame.completedStrokeCount)

            val handshake = lessonEngine.handle(
                LessonRuntimeSignal.TeacherPlaybackCompleted(request.requestId),
            )
            assertTrue(handshake is LessonSignalResult.Accepted)
            val child = lessonEngine.state as LessonSessionState.AwaitingChild
            assertEquals("head", child.context.currentStepId)
            assertEquals(pace, child.context.pace)
        }
    }

    @Test
    fun fullCuteCatLoopCanUseRealPlaybackEngineForEveryTeacherStep() {
        val lessonEngine = lessonEngine()
        var commandResult = lessonEngine.dispatch(
            LessonCommand.StartLesson(TeachingMode.DRAW_WITH_ME, TeachingPace.FAST),
        ) as LessonCommandResult.Accepted

        val visited = mutableListOf<String>()
        while (lessonEngine.state !is LessonSessionState.DrawingComplete) {
            val request = commandResult.events.filterIsInstance<TeacherPlaybackRequested>().single().request
            visited += request.stepId

            val playback = TeacherPlaybackEngine(request.sequence, request.pace)
            playback.play()
            val duration = ceil(
                request.sequence.sourceDurationMillis.toDouble() / request.pace.multiplier,
            ).toLong() + 1L
            assertEquals(
                TeacherPlaybackStatus.COMPLETED,
                playback.advanceBy(duration).status,
            )

            val handshake = lessonEngine.handle(
                LessonRuntimeSignal.TeacherPlaybackCompleted(request.requestId),
            )
            assertTrue(handshake is LessonSignalResult.Accepted)
            assertTrue(lessonEngine.state is LessonSessionState.AwaitingChild)

            commandResult = lessonEngine.dispatch(MarkChildTurnDone) as LessonCommandResult.Accepted
        }

        assertEquals(listOf("head", "ears", "face", "body_tail"), visited)
        assertTrue(lessonEngine.state is LessonSessionState.DrawingComplete)
    }

    private fun lessonEngine(): LessonSessionEngine = LessonSessionEngine.create(
        lessonPackage = packageData(),
        sessionId = "integration-session",
        childDocumentId = "integration-child-doc",
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
