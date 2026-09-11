package com.navin.kidsdrawing.drawing.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TeacherPlaybackSessionState(
    val sequenceId: String? = null,
    val frame: TeacherPlaybackFrame? = null,
) {
    val isLoaded: Boolean get() = frame != null
}

/**
 * Small product-owned orchestration layer around [TeacherPlaybackEngine]. It owns which canonical
 * source is loaded for playback so Art Lab controls do not keep authoritative playback state.
 */
class TeacherPlaybackSession(
    initialPace: TeachingPace = TeachingPace.NORMAL,
) {
    private var selectedPace: TeachingPace = initialPace
    private var engine: TeacherPlaybackEngine? = null
    private val _state = MutableStateFlow(TeacherPlaybackSessionState())
    val state: StateFlow<TeacherPlaybackSessionState> = _state.asStateFlow()

    fun load(sequence: TeacherStrokeSequence): TeacherPlaybackSessionState {
        engine = TeacherPlaybackEngine(sequence = sequence, initialPace = selectedPace)
        return publish(engine?.frame)
    }

    fun unload(): TeacherPlaybackSessionState {
        engine?.cancel()
        engine = null
        return publish(null)
    }

    fun play(): TeacherPlaybackSessionState = publish(engine?.play())

    fun pause(): TeacherPlaybackSessionState = publish(engine?.pause())

    fun resume(): TeacherPlaybackSessionState = publish(engine?.resume())

    fun replay(): TeacherPlaybackSessionState = publish(engine?.replay())

    fun cancel(): TeacherPlaybackSessionState = publish(engine?.cancel())

    fun setPace(pace: TeachingPace): TeacherPlaybackSessionState {
        selectedPace = pace
        return publish(engine?.setPace(pace))
    }

    fun advanceBy(realElapsedMillis: Long): TeacherPlaybackSessionState =
        publish(engine?.advanceBy(realElapsedMillis))

    private fun publish(frame: TeacherPlaybackFrame?): TeacherPlaybackSessionState {
        if (frame != null) selectedPace = frame.pace
        val state = TeacherPlaybackSessionState(
            sequenceId = frame?.sequenceId,
            frame = frame,
        )
        _state.value = state
        return state
    }
}

object TeacherSequenceFactory {
    /**
     * Copies a child stroke into an isolated teacher source without mutating the child stroke or
     * document. Timing is normalized so replay begins at source time zero.
     */
    fun fromChildStroke(
        stroke: InkStrokeRecord,
        sequenceId: String = "captured-${stroke.strokeId}",
    ): TeacherStrokeSequence {
        require(stroke.authorRole == StrokeAuthorRole.CHILD) {
            "Only child strokes can be captured through the Art Lab child→teacher test path."
        }
        val firstTime = stroke.points.first().elapsedTimeMillis
        val normalized = stroke.copy(
            strokeId = "teacher-copy-${stroke.strokeId}",
            points = stroke.points.map { point ->
                point.copy(elapsedTimeMillis = (point.elapsedTimeMillis - firstTime).coerceAtLeast(0L))
            },
            authorRole = StrokeAuthorRole.TEACHER_GENERATED,
        )
        return TeacherStrokeSequence(
            sequenceId = sequenceId,
            strokes = listOf(TeacherStrokeSource(stroke = normalized, startTimeMillis = 0L)),
        )
    }
}
