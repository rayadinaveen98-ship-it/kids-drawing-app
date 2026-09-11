package com.navin.kidsdrawing.drawing.quality

import com.navin.kidsdrawing.drawing.domain.StrokeAuthorRole
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackEngine
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackStatus
import com.navin.kidsdrawing.drawing.domain.TeacherStrokeSequence
import com.navin.kidsdrawing.drawing.domain.TeacherStrokeSource
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TeacherPlaybackQualityStressTest {
    @Test
    fun hundredReplaysPerPaceReachIdenticalCanonicalGeometry() {
        val sequence = w4Sequence()
        val canonical = sequence.strokes.map { it.stroke }

        TeachingPace.entries.forEach { pace ->
            repeat(100) {
                val engine = TeacherPlaybackEngine(sequence, initialPace = pace)
                engine.play()
                val completed = engine.advanceBy(20_000L)

                assertEquals(TeacherPlaybackStatus.COMPLETED, completed.status)
                assertEquals(canonical, completed.visibleStrokes)
                assertEquals(sequence.strokes.size, completed.completedStrokeCount)
                assertEquals(1f, completed.progress, 0.0001f)
            }
        }
    }

    @Test
    fun randomizedPauseResumePointsNeverAdvanceWhilePausedOrLoseFinalGeometry() {
        val random = Random(0x4B4457)
        val sequence = w4Sequence()
        val canonical = sequence.strokes.map { it.stroke }

        repeat(100) {
            val engine = TeacherPlaybackEngine(sequence, initialPace = TeachingPace.NORMAL)
            engine.play()
            engine.advanceBy(random.nextLong(1L, 1_000L))
            val paused = engine.pause()
            val pausedAgain = engine.advanceBy(random.nextLong(1_000L, 10_000L))

            assertEquals(paused.sourceTimeMillis, pausedAgain.sourceTimeMillis, 0.0)
            assertEquals(paused.visibleStrokes, pausedAgain.visibleStrokes)

            engine.resume()
            val completed = engine.advanceBy(20_000L)
            assertEquals(TeacherPlaybackStatus.COMPLETED, completed.status)
            assertEquals(canonical, completed.visibleStrokes)
        }
    }

    @Test
    fun hundredRandomizedPaceChangesPreservePositionAndCompleteWithoutSourceMutation() {
        val random = Random(0x50414345)
        val sequence = w4Sequence()
        val original = sequence.copy(strokes = sequence.strokes.map { source ->
            source.copy(stroke = source.stroke.copy(points = source.stroke.points.toList()))
        })
        val engine = TeacherPlaybackEngine(sequence, initialPace = TeachingPace.NORMAL)
        engine.play()

        var previousSourceTime = 0.0
        repeat(100) {
            val pace = TeachingPace.entries[random.nextInt(TeachingPace.entries.size)]
            val beforePaceChange = engine.frame.sourceTimeMillis
            val changed = engine.setPace(pace)
            assertEquals(beforePaceChange, changed.sourceTimeMillis, 0.0)

            val advanced = engine.advanceBy(random.nextLong(1L, 20L))
            assertTrue(advanced.sourceTimeMillis >= previousSourceTime)
            previousSourceTime = advanced.sourceTimeMillis
        }

        val completed = engine.advanceBy(20_000L)
        assertEquals(TeacherPlaybackStatus.COMPLETED, completed.status)
        assertEquals(sequence.strokes.map { it.stroke }, completed.visibleStrokes)
        assertEquals(original, sequence)
    }

    @Test
    fun differentFrameChunkingProducesEquivalentFinalGeometry() {
        val sequence = w4Sequence()
        val smooth = TeacherPlaybackEngine(sequence)
        val coarse = TeacherPlaybackEngine(sequence)
        smooth.play()
        coarse.play()

        repeat(1_000) { smooth.advanceBy(5L) }
        repeat(100) { coarse.advanceBy(50L) }

        val smoothDone = smooth.advanceBy(20_000L)
        val coarseDone = coarse.advanceBy(20_000L)
        assertEquals(smoothDone.visibleStrokes, coarseDone.visibleStrokes)
        assertEquals(1f, smoothDone.progress, 0.0001f)
        assertEquals(1f, coarseDone.progress, 0.0001f)
    }

    private fun w4Sequence(): TeacherStrokeSequence {
        val starts = listOf(0L, 650L, 1_300L, 1_950L)
        val strokes = List(4) { index ->
            val child = StressFixtureFactory.stroke(index = 10_000 + index, sampleCount = 64)
            TeacherStrokeSource(
                startTimeMillis = starts[index],
                stroke = child.copy(
                    strokeId = "w4-teacher-$index",
                    authorRole = StrokeAuthorRole.TEACHER_GENERATED,
                ),
            )
        }
        return TeacherStrokeSequence(
            sequenceId = "quality-w4",
            strokes = strokes,
        )
    }
}
