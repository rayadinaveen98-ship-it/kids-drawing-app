package com.navin.kidsdrawing.drawing.domain

import kotlin.math.ceil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TeacherPlaybackEngineTest {
    @Test
    fun allFivePacesReachEquivalentCanonicalGeometry() {
        val sequence = sequence()
        val expected = sequence.strokes.map { it.stroke }

        TeachingPace.entries.forEach { pace ->
            val engine = TeacherPlaybackEngine(sequence, initialPace = pace)
            engine.play()
            val realDuration = ceil(sequence.sourceDurationMillis / pace.multiplier).toLong() + 1L
            val finalFrame = engine.advanceBy(realDuration)

            assertEquals(TeacherPlaybackStatus.COMPLETED, finalFrame.status)
            assertEquals(expected, finalFrame.visibleStrokes)
            assertEquals(sequence.strokes.size, finalFrame.completedStrokeCount)
            assertEquals(1f, finalFrame.progress, 0.0001f)
        }
    }

    @Test
    fun pauseAndResumeCannotAdvanceOrDuplicateCanonicalSamples() {
        val engine = TeacherPlaybackEngine(sequence())
        engine.play()
        val beforePause = engine.advanceBy(500L)
        val paused = engine.pause()
        val stillPaused = engine.advanceBy(5_000L)

        assertEquals(TeacherPlaybackStatus.PAUSED, paused.status)
        assertEquals(paused.sourceTimeMillis, stillPaused.sourceTimeMillis, 0.0)
        assertEquals(paused.visibleStrokes, stillPaused.visibleStrokes)

        engine.resume()
        val resumed = engine.advanceBy(250L)
        assertEquals(beforePause.sourceTimeMillis + 250.0, resumed.sourceTimeMillis, 0.0)
        assertTrue(resumed.visibleStrokes.first().points.size >= beforePause.visibleStrokes.first().points.size)
    }

    @Test
    fun paceChangeMidStrokePreservesPositionAndUsesNewMultiplier() {
        val engine = TeacherPlaybackEngine(sequence(), initialPace = TeachingPace.NORMAL)
        engine.play()
        val normal = engine.advanceBy(400L)
        val changed = engine.setPace(TeachingPace.VERY_FAST)

        assertEquals(normal.sourceTimeMillis, changed.sourceTimeMillis, 0.0)

        val faster = engine.advanceBy(100L)
        assertEquals(normal.sourceTimeMillis + 200.0, faster.sourceTimeMillis, 0.0)
        assertEquals(TeachingPace.VERY_FAST, faster.pace)
    }

    @Test
    fun replayStartsCleanOverlayWithoutMutatingSource() {
        val sequence = sequence()
        val original = sequence.strokes.map { it.stroke }
        val engine = TeacherPlaybackEngine(sequence)

        engine.play()
        engine.advanceBy(sequence.sourceDurationMillis + 100L)
        val replayed = engine.replay()

        assertEquals(TeacherPlaybackStatus.PLAYING, replayed.status)
        assertEquals(0.0, replayed.sourceTimeMillis, 0.0)
        assertTrue(replayed.visibleStrokes.isNotEmpty())
        assertEquals(original, sequence.strokes.map { it.stroke })
    }

    @Test
    fun inactiveAndCancelledPlaybackExposeNoTeacherOverlay() {
        val engine = TeacherPlaybackEngine(sequence())
        assertEquals(TeacherPlaybackStatus.IDLE, engine.frame.status)
        assertTrue(engine.frame.visibleStrokes.isEmpty())

        engine.play()
        assertTrue(engine.advanceBy(400L).visibleStrokes.isNotEmpty())
        val cancelled = engine.cancel()

        assertEquals(TeacherPlaybackStatus.CANCELLED, cancelled.status)
        assertTrue(cancelled.visibleStrokes.isEmpty())
    }

    @Test
    fun frameChunkingProducesSameCanonicalPosition() {
        val singleAdvance = TeacherPlaybackEngine(sequence()).apply { play() }
        val chunkedAdvance = TeacherPlaybackEngine(sequence()).apply { play() }

        val single = singleAdvance.advanceBy(1_000L)
        repeat(10) { chunkedAdvance.advanceBy(100L) }
        val chunked = chunkedAdvance.frame

        assertEquals(single.sourceTimeMillis, chunked.sourceTimeMillis, 0.0)
        assertEquals(single.visibleStrokes, chunked.visibleStrokes)
        assertEquals(single.progress, chunked.progress, 0.0001f)
    }

    @Test
    fun lifecycleEventsAreTypedAndOrdered() {
        val events = mutableListOf<TeacherPlaybackEvent>()
        val sequence = sequence()
        val engine = TeacherPlaybackEngine(sequence, eventSink = events::add)

        engine.play()
        engine.advanceBy(200L)
        engine.pause()
        engine.resume()
        engine.advanceBy(sequence.sourceDurationMillis + 1L)

        assertTrue(events.first() is TeacherPlaybackEvent.Started)
        assertTrue(events.any { it is TeacherPlaybackEvent.Progress })
        assertTrue(events.any { it is TeacherPlaybackEvent.Paused })
        assertTrue(events.any { it is TeacherPlaybackEvent.Resumed })
        assertTrue(events.last() is TeacherPlaybackEvent.Completed)
    }

    @Test
    fun teacherPlaybackNeverMutatesChildDocumentHistory() {
        val sequence = sequence()
        val childEngine = DrawingDocumentEngine(
            initialDocument = DrawingDocumentEngine.newDocument(
                documentId = "child-doc",
                nowEpochMillis = 1_000L,
            ),
        )
        val teacherEngine = TeacherPlaybackEngine(sequence)

        teacherEngine.play()
        teacherEngine.advanceBy(sequence.sourceDurationMillis + 1L)
        teacherEngine.replay()
        teacherEngine.advanceBy(600L)
        teacherEngine.pause()

        assertTrue(childEngine.state.value.document.operations.isEmpty())
        assertFalse(childEngine.state.value.canUndo)
        assertFalse(childEngine.state.value.canRedo)
        assertTrue(sequence.strokes.all { it.stroke.authorRole == StrokeAuthorRole.TEACHER_GENERATED })
    }

    private fun sequence(): TeacherStrokeSequence = TeacherStrokeSequence(
        sequenceId = "teacher-demo",
        strokes = listOf(
            TeacherStrokeSource(
                startTimeMillis = 0L,
                stroke = teacherStroke(
                    id = "teacher-1",
                    points = listOf(
                        point(100f, 100f, 0L),
                        point(200f, 150f, 500L),
                        point(300f, 250f, 1_000L),
                    ),
                ),
            ),
            TeacherStrokeSource(
                startTimeMillis = 1_200L,
                stroke = teacherStroke(
                    id = "teacher-2",
                    points = listOf(
                        point(350f, 300f, 0L),
                        point(450f, 400f, 400L),
                        point(550f, 450f, 800L),
                    ),
                ),
            ),
        ),
    )

    private fun teacherStroke(
        id: String,
        points: List<StrokePoint>,
    ) = InkStrokeRecord(
        strokeId = id,
        brushPresetId = "pencil.standard",
        colorArgb = 0xFF5C6F52.toInt(),
        opacity = 0.85f,
        baseSize = 10f,
        tool = PointerTool.STYLUS,
        points = points,
        authorRole = StrokeAuthorRole.TEACHER_GENERATED,
    )

    private fun point(x: Float, y: Float, time: Long) = StrokePoint(
        x = x,
        y = y,
        elapsedTimeMillis = time,
        pressure = 0.7f,
    )
}
