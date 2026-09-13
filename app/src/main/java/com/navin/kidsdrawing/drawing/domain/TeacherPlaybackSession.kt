package com.navin.kidsdrawing.drawing.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TeacherPlaybackSessionState(
    val sequenceId: String? = null,
    val selectedPace: TeachingPace = TeachingPace.NORMAL,
    val frame: TeacherPlaybackFrame? = null,
) {
    val isLoaded: Boolean get() = frame != null
}

/**
 * Small product-owned orchestration layer around [TeacherPlaybackEngine]. It owns which canonical
 * source is loaded for playback so Art Lab controls do not keep authoritative playback state.
 *
 * Completed step geometry is presentation-only construction context: when a normal step completes
 * it becomes faint immediately and can carry into the next step. Full Watch Then Draw overview
 * geometry disappears on completion and is never carried forward. None of these teacher records
 * enter the child DrawingDocument, history, persistence, or Gallery truth.
 */
class TeacherPlaybackSession(
    initialPace: TeachingPace = TeachingPace.NORMAL,
) {
    private var selectedPace: TeachingPace = initialPace
    private var engine: TeacherPlaybackEngine? = null
    private val _state = MutableStateFlow(
        TeacherPlaybackSessionState(selectedPace = initialPace),
    )
    val state: StateFlow<TeacherPlaybackSessionState> = _state.asStateFlow()

    fun load(sequence: TeacherStrokeSequence): TeacherPlaybackSessionState {
        val effectiveSequence = withCompletedConstructionReference(sequence)
        engine = TeacherPlaybackEngine(sequence = effectiveSequence, initialPace = selectedPace)
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

    private fun withCompletedConstructionReference(
        sequence: TeacherStrokeSequence,
    ): TeacherStrokeSequence {
        if (sequence.sequenceId.isOverviewSequence()) return sequence
        val previous = _state.value.frame ?: return sequence
        if (previous.status != TeacherPlaybackStatus.COMPLETED) return sequence
        if (previous.sequenceId.isOverviewSequence()) return sequence

        val newStrokeIds = sequence.strokes.map { it.stroke.strokeId }.toSet()
        val carried = previous.visibleStrokes
            .filterNot { it.strokeId in newStrokeIds }
            .map { stroke ->
                TeacherStrokeSource(
                    stroke = stroke.asCompletedReference(),
                    startTimeMillis = 0L,
                )
            }
        if (carried.isEmpty()) return sequence

        return TeacherStrokeSequence(
            sequenceId = sequence.sequenceId,
            strokes = carried + sequence.strokes,
        )
    }

    private fun InkStrokeRecord.asCompletedReference(): InkStrokeRecord = copy(
        opacity = minOf(opacity, COMPLETED_REFERENCE_OPACITY),
        points = points.map { point -> point.copy(elapsedTimeMillis = 0L) },
    )

    private fun TeacherPlaybackFrame.forPresentation(): TeacherPlaybackFrame {
        if (status != TeacherPlaybackStatus.COMPLETED) return this
        return if (sequenceId.isOverviewSequence()) {
            // Source completion remains truthful; only the completed overview presentation clears.
            copy(visibleStrokes = emptyList())
        } else {
            // The demonstrated part stays as a gentle map during the child's turn.
            copy(visibleStrokes = visibleStrokes.map { it.asCompletedReference() })
        }
    }

    private fun String.isOverviewSequence(): Boolean = endsWith("-overview")

    private fun publish(frame: TeacherPlaybackFrame?): TeacherPlaybackSessionState {
        if (frame != null) selectedPace = frame.pace
        val presentationFrame = frame?.forPresentation()
        val state = TeacherPlaybackSessionState(
            sequenceId = presentationFrame?.sequenceId,
            selectedPace = selectedPace,
            frame = presentationFrame,
        )
        _state.value = state
        return state
    }

    companion object {
        /** Deliberately subtle: enough construction context without competing with child ink. */
        private const val COMPLETED_REFERENCE_OPACITY = 0.16f
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
