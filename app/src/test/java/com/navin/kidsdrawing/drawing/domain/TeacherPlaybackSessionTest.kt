package com.navin.kidsdrawing.drawing.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TeacherPlaybackSessionTest {
    @Test
    fun loadedSourceOwnsPlaybackStateAndPreservesSelectedPaceAcrossSources() {
        val session = TeacherPlaybackSession(initialPace = TeachingPace.SLOW)
        val first = sequence("first")
        val second = sequence("second")

        val loaded = session.load(first)
        assertTrue(loaded.isLoaded)
        assertEquals("first", loaded.sequenceId)
        assertEquals(TeachingPace.SLOW, loaded.frame?.pace)

        session.setPace(TeachingPace.VERY_FAST)
        session.play()
        session.advanceBy(100L)

        val reloaded = session.load(second)
        assertEquals("second", reloaded.sequenceId)
        assertEquals(TeachingPace.VERY_FAST, reloaded.frame?.pace)
        assertEquals(0.0, reloaded.frame?.sourceTimeMillis ?: -1.0, 0.0)
    }

    @Test
    fun completedStepIsCarriedIntoNextStepAsFaintImmediateConstructionReference() {
        val session = TeacherPlaybackSession()
        session.load(sequence("lesson-demo-r1-head"))
        session.play()
        session.advanceBy(1_000L)
        assertEquals(TeacherPlaybackStatus.COMPLETED, session.state.value.frame?.status)

        session.load(sequence("lesson-demo-r1-ears"))
        val loaded = session.play().frame ?: error("Expected loaded teacher frame")
        val head = loaded.visibleStrokes.first { it.strokeId == "lesson-demo-r1-head-stroke" }

        assertEquals(2, loaded.visibleStrokes.size)
        assertEquals(0.16f, head.opacity, 0f)
        assertTrue(head.points.all { it.elapsedTimeMillis == 0L })
        assertTrue(loaded.visibleStrokes.any { it.strokeId == "lesson-demo-r1-ears-stroke" })
    }

    @Test
    fun replayDoesNotDuplicateCurrentStepWhileKeepingEarlierConstructionReference() {
        val session = TeacherPlaybackSession()
        session.load(sequence("lesson-demo-r1-head"))
        session.play()
        session.advanceBy(1_000L)

        val ears = sequence("lesson-demo-r1-ears")
        session.load(ears)
        session.play()
        session.advanceBy(1_000L)
        assertEquals(TeacherPlaybackStatus.COMPLETED, session.state.value.frame?.status)

        session.load(ears)
        val replay = session.play().frame ?: error("Expected replay frame")
        val ids = replay.visibleStrokes.map { it.strokeId }

        assertEquals(ids.toSet().size, ids.size)
        assertTrue("lesson-demo-r1-head-stroke" in ids)
        assertTrue("lesson-demo-r1-ears-stroke" in ids)
    }

    @Test
    fun overviewGeometryIsNeverCarriedIntoStepPlayback() {
        val session = TeacherPlaybackSession()
        session.load(sequence("lesson-demo-r1-overview"))
        session.play()
        session.advanceBy(1_000L)
        assertEquals(TeacherPlaybackStatus.COMPLETED, session.state.value.frame?.status)

        session.load(sequence("lesson-demo-r1-head"))
        val next = session.play().frame ?: error("Expected next frame")

        assertEquals(listOf("lesson-demo-r1-head-stroke"), next.visibleStrokes.map { it.strokeId })
    }

    @Test
    fun unloadClearsVisibleTeacherState() {
        val session = TeacherPlaybackSession()
        session.load(sequence("demo"))
        session.play()
        session.advanceBy(200L)

        val unloaded = session.unload()

        assertFalse(unloaded.isLoaded)
        assertEquals(null, unloaded.frame)
        assertEquals(null, unloaded.sequenceId)
    }

    @Test
    fun childStrokeCaptureCreatesIndependentTeacherCopyWithNormalizedTiming() {
        val child = InkStrokeRecord(
            strokeId = "child-1",
            brushPresetId = "pencil.standard",
            colorArgb = 0xFF242321.toInt(),
            opacity = 1f,
            baseSize = 12f,
            tool = PointerTool.FINGER,
            points = listOf(
                point(10f, 20f, 120L),
                point(30f, 40f, 220L),
                point(50f, 60f, 420L),
            ),
        )

        val sequence = TeacherSequenceFactory.fromChildStroke(child)
        val copied = sequence.strokes.single().stroke

        assertNotSame(child, copied)
        assertEquals(StrokeAuthorRole.CHILD, child.authorRole)
        assertEquals(StrokeAuthorRole.TEACHER_GENERATED, copied.authorRole)
        assertEquals(listOf(0L, 100L, 300L), copied.points.map { it.elapsedTimeMillis })
        assertEquals(child.points.map { it.x to it.y }, copied.points.map { it.x to it.y })
    }

    private fun sequence(id: String): TeacherStrokeSequence = TeacherStrokeSequence(
        sequenceId = id,
        strokes = listOf(
            TeacherStrokeSource(
                startTimeMillis = 0L,
                stroke = InkStrokeRecord(
                    strokeId = "$id-stroke",
                    brushPresetId = "pencil.standard",
                    colorArgb = 0xFF5C6F52.toInt(),
                    opacity = 0.8f,
                    baseSize = 10f,
                    tool = PointerTool.STYLUS,
                    points = listOf(point(0f, 0f, 0L), point(100f, 100f, 500L)),
                    authorRole = StrokeAuthorRole.TEACHER_GENERATED,
                ),
            ),
        ),
    )

    private fun point(x: Float, y: Float, time: Long) = StrokePoint(
        x = x,
        y = y,
        elapsedTimeMillis = time,
        pressure = 0.7f,
    )
}
