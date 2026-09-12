package com.navin.kidsdrawing.lesson.session

import com.navin.kidsdrawing.drawing.domain.TeacherStrokeSequence
import com.navin.kidsdrawing.drawing.domain.TeachingPace

data object ReplayDemonstration : LessonCommand

data object MarkChildTurnDone : LessonCommand

data object SkipStep : LessonCommand

data class TeacherPlaybackRequest(
    val requestId: String,
    val stepId: String,
    val sequence: TeacherStrokeSequence,
    val pace: TeachingPace,
    val replay: Boolean,
    val narrationKey: String?,
) {
    init {
        require(requestId.isNotBlank()) { "requestId cannot be blank." }
        require(stepId.isNotBlank()) { "stepId cannot be blank." }
    }
}

/** Signals originate from owned engine/infrastructure boundaries, never from arbitrary UI state. */
sealed interface LessonRuntimeSignal {
    data class TeacherPlaybackCompleted(
        val requestId: String,
    ) : LessonRuntimeSignal

    data class TeacherPlaybackFailed(
        val requestId: String,
        val reason: String,
    ) : LessonRuntimeSignal

    /** Emitted only after the Drawing Engine has committed a child operation successfully. */
    data class ChildStrokeCommitted(
        val childDocumentId: String,
        val operationId: String,
    ) : LessonRuntimeSignal
}

enum class LessonSignalRejectionCode {
    INVALID_STATE,
    STALE_TEACHER_REQUEST,
    DOCUMENT_MISMATCH,
    COMPLETION_POLICY_NOT_APPLICABLE,
}

data class LessonSignalRejection(
    val code: LessonSignalRejectionCode,
    val message: String,
)

sealed interface LessonSignalResult {
    val state: LessonSessionState
    val events: List<LessonSessionEvent>

    data class Accepted(
        override val state: LessonSessionState,
        override val events: List<LessonSessionEvent>,
    ) : LessonSignalResult

    data class Rejected(
        override val state: LessonSessionState,
        val rejection: LessonSignalRejection,
    ) : LessonSignalResult {
        override val events: List<LessonSessionEvent> = emptyList()
    }
}

data class TeacherPlaybackRequested(
    val request: TeacherPlaybackRequest,
) : LessonSessionEvent

data class TeacherPlaybackPauseRequested(
    val requestId: String,
) : LessonSessionEvent

data class TeacherPlaybackResumeRequested(
    val requestId: String,
) : LessonSessionEvent

data class TeacherPlaybackPaceChangeRequested(
    val requestId: String,
    val pace: TeachingPace,
) : LessonSessionEvent

data class TeacherPlaybackCancelRequested(
    val requestId: String,
) : LessonSessionEvent

data class TeacherPlaybackFailureObserved(
    val requestId: String,
    val reason: String,
) : LessonSessionEvent

data class ChildTurnStarted(
    val stepIndex: Int,
    val stepId: String,
) : LessonSessionEvent

data class StepCompleted(
    val stepIndex: Int,
    val stepId: String,
    val skipped: Boolean,
) : LessonSessionEvent

data class DrawingLessonCompleted(
    val lessonId: String,
) : LessonSessionEvent

data class LessonAutosaveRequested(
    val reason: LessonAutosaveReason,
) : LessonSessionEvent

enum class LessonAutosaveReason {
    STEP_COMPLETED,
    DRAWING_COMPLETED,
}
