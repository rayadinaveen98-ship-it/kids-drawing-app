package com.navin.kidsdrawing.drawing.domain

import kotlin.math.floor

/** The five product teaching speeds. Geometry never changes with pace; only virtual source time does. */
enum class TeachingPace(val multiplier: Double) {
    EXTRA_SLOW(0.40),
    SLOW(0.70),
    NORMAL(1.00),
    FAST(1.50),
    VERY_FAST(2.00),
}

enum class TeacherPlaybackStatus {
    IDLE,
    PLAYING,
    PAUSED,
    COMPLETED,
    CANCELLED,
    FAILED,
}

data class TeacherStrokeSource(
    val stroke: InkStrokeRecord,
    val startTimeMillis: Long,
) {
    init {
        require(stroke.authorRole == StrokeAuthorRole.TEACHER_GENERATED) {
            "Teacher playback accepts only teacher-generated source strokes."
        }
        require(startTimeMillis >= 0L) { "Teacher stroke start time cannot be negative." }
        require(stroke.points.zipWithNext().all { (a, b) -> b.elapsedTimeMillis >= a.elapsedTimeMillis }) {
            "Teacher stroke point timestamps must be monotonic."
        }
    }

    val endTimeMillis: Long
        get() = startTimeMillis + stroke.points.last().elapsedTimeMillis
}

data class TeacherStrokeSequence(
    val sequenceId: String,
    val strokes: List<TeacherStrokeSource>,
) {
    init {
        require(sequenceId.isNotBlank()) { "sequenceId cannot be blank." }
        require(strokes.isNotEmpty()) { "Teacher sequence must contain at least one stroke." }
        require(strokes.zipWithNext().all { (a, b) -> b.startTimeMillis >= a.startTimeMillis }) {
            "Teacher sequence strokes must be ordered by start time."
        }
        require(strokes.map { it.stroke.strokeId }.toSet().size == strokes.size) {
            "Teacher sequence stroke IDs must be unique."
        }
    }

    val sourceDurationMillis: Long
        get() = strokes.maxOf { it.endTimeMillis }
}

data class TeacherPlaybackFrame(
    val sequenceId: String,
    val status: TeacherPlaybackStatus,
    val pace: TeachingPace,
    val sourceTimeMillis: Double,
    val sourceDurationMillis: Long,
    val visibleStrokes: List<InkStrokeRecord>,
    val completedStrokeCount: Int,
    val progress: Float,
    val failureReason: String? = null,
)

sealed interface TeacherPlaybackEvent {
    data class Started(val pace: TeachingPace) : TeacherPlaybackEvent
    data class Progress(val sourceTimeMillis: Double, val progress: Float) : TeacherPlaybackEvent
    data object Paused : TeacherPlaybackEvent
    data object Resumed : TeacherPlaybackEvent
    data object Completed : TeacherPlaybackEvent
    data object Cancelled : TeacherPlaybackEvent
    data class Failed(val reason: String) : TeacherPlaybackEvent
}

/**
 * Product-owned deterministic playback clock for teacher demonstration strokes.
 *
 * The caller supplies real elapsed deltas (for example from a frame clock). This engine converts
 * them into canonical source time using the selected pace. UI timers are never authoritative.
 */
class TeacherPlaybackEngine(
    private val sequence: TeacherStrokeSequence,
    initialPace: TeachingPace = TeachingPace.NORMAL,
    private val eventSink: (TeacherPlaybackEvent) -> Unit = {},
) {
    private var pace: TeachingPace = initialPace
    private var sourceTimeMillis: Double = 0.0
    private var status: TeacherPlaybackStatus = TeacherPlaybackStatus.IDLE
    private var failureReason: String? = null

    val frame: TeacherPlaybackFrame
        get() = buildFrame()

    fun play(): TeacherPlaybackFrame {
        if (status == TeacherPlaybackStatus.COMPLETED || status == TeacherPlaybackStatus.CANCELLED || status == TeacherPlaybackStatus.FAILED) {
            resetInternal()
        }
        if (status == TeacherPlaybackStatus.IDLE) {
            status = TeacherPlaybackStatus.PLAYING
            eventSink(TeacherPlaybackEvent.Started(pace))
        } else if (status == TeacherPlaybackStatus.PAUSED) {
            status = TeacherPlaybackStatus.PLAYING
            eventSink(TeacherPlaybackEvent.Resumed)
        }
        return buildFrame()
    }

    fun pause(): TeacherPlaybackFrame {
        if (status == TeacherPlaybackStatus.PLAYING) {
            status = TeacherPlaybackStatus.PAUSED
            eventSink(TeacherPlaybackEvent.Paused)
        }
        return buildFrame()
    }

    fun resume(): TeacherPlaybackFrame = play()

    fun replay(): TeacherPlaybackFrame {
        resetInternal()
        status = TeacherPlaybackStatus.PLAYING
        eventSink(TeacherPlaybackEvent.Started(pace))
        return buildFrame()
    }

    fun cancel(): TeacherPlaybackFrame {
        if (status != TeacherPlaybackStatus.CANCELLED) {
            status = TeacherPlaybackStatus.CANCELLED
            eventSink(TeacherPlaybackEvent.Cancelled)
        }
        return buildFrame()
    }

    fun fail(reason: String): TeacherPlaybackFrame {
        require(reason.isNotBlank()) { "Playback failure reason cannot be blank." }
        failureReason = reason
        status = TeacherPlaybackStatus.FAILED
        eventSink(TeacherPlaybackEvent.Failed(reason))
        return buildFrame()
    }

    fun setPace(newPace: TeachingPace): TeacherPlaybackFrame {
        pace = newPace
        return buildFrame()
    }

    fun advanceBy(realElapsedMillis: Long): TeacherPlaybackFrame {
        require(realElapsedMillis >= 0L) { "Playback elapsed delta cannot be negative." }
        if (status != TeacherPlaybackStatus.PLAYING || realElapsedMillis == 0L) return buildFrame()

        sourceTimeMillis = (sourceTimeMillis + realElapsedMillis * pace.multiplier)
            .coerceAtMost(sequence.sourceDurationMillis.toDouble())

        val current = buildFrame()
        eventSink(TeacherPlaybackEvent.Progress(current.sourceTimeMillis, current.progress))

        if (sourceTimeMillis >= sequence.sourceDurationMillis) {
            status = TeacherPlaybackStatus.COMPLETED
            eventSink(TeacherPlaybackEvent.Completed)
        }
        return buildFrame()
    }

    private fun resetInternal() {
        sourceTimeMillis = 0.0
        status = TeacherPlaybackStatus.IDLE
        failureReason = null
    }

    private fun buildFrame(): TeacherPlaybackFrame {
        val visible = sequence.strokes.mapNotNull { source ->
            visibleStrokeAt(source, sourceTimeMillis)
        }
        val completed = sequence.strokes.count { sourceTimeMillis >= it.endTimeMillis }
        val progress = if (sequence.sourceDurationMillis == 0L) {
            1f
        } else {
            (sourceTimeMillis / sequence.sourceDurationMillis.toDouble())
                .coerceIn(0.0, 1.0)
                .toFloat()
        }
        return TeacherPlaybackFrame(
            sequenceId = sequence.sequenceId,
            status = status,
            pace = pace,
            sourceTimeMillis = sourceTimeMillis,
            sourceDurationMillis = sequence.sourceDurationMillis,
            visibleStrokes = visible,
            completedStrokeCount = completed,
            progress = progress,
            failureReason = failureReason,
        )
    }

    private fun visibleStrokeAt(
        source: TeacherStrokeSource,
        sequenceTimeMillis: Double,
    ): InkStrokeRecord? {
        val localTime = sequenceTimeMillis - source.startTimeMillis
        if (localTime < 0.0) return null
        if (sequenceTimeMillis >= source.endTimeMillis) return source.stroke

        val points = source.stroke.points
        if (points.size == 1) return source.stroke

        val completedPoints = points.takeWhile { it.elapsedTimeMillis <= localTime }
        if (completedPoints.isEmpty()) {
            return source.stroke.copy(points = listOf(points.first()))
        }
        if (completedPoints.size == points.size) return source.stroke

        val previous = completedPoints.last()
        val next = points[completedPoints.size]
        val interval = (next.elapsedTimeMillis - previous.elapsedTimeMillis).coerceAtLeast(1L)
        val fraction = ((localTime - previous.elapsedTimeMillis) / interval.toDouble()).coerceIn(0.0, 1.0)
        val interpolated = interpolatePoint(previous, next, fraction, floor(localTime).toLong())
        return source.stroke.copy(points = completedPoints + interpolated)
    }

    private fun interpolatePoint(
        from: StrokePoint,
        to: StrokePoint,
        fraction: Double,
        elapsedTimeMillis: Long,
    ): StrokePoint = StrokePoint(
        x = lerp(from.x, to.x, fraction),
        y = lerp(from.y, to.y, fraction),
        elapsedTimeMillis = elapsedTimeMillis.coerceAtLeast(from.elapsedTimeMillis),
        pressure = lerp(from.pressure, to.pressure, fraction).coerceIn(0f, 1f),
        tiltRadians = lerpNullable(from.tiltRadians, to.tiltRadians, fraction),
        orientationRadians = lerpNullable(from.orientationRadians, to.orientationRadians, fraction),
    )

    private fun lerp(from: Float, to: Float, fraction: Double): Float =
        (from + (to - from) * fraction).toFloat()

    private fun lerpNullable(from: Float?, to: Float?, fraction: Double): Float? =
        if (from == null || to == null) from ?: to else lerp(from, to, fraction)
}
