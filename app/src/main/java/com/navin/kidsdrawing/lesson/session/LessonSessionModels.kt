package com.navin.kidsdrawing.lesson.session

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode

data class LessonSessionIdentity(
    val sessionId: String,
    val lessonId: String,
    val lessonRevision: Int,
    val childDocumentId: String,
)

data class LessonActiveContext(
    val mode: TeachingMode,
    val pace: TeachingPace,
    val currentStepIndex: Int,
    val currentStepId: String,
    val helpLevel: Int = 0,
    val overviewCompleted: Boolean,
) {
    init {
        require(currentStepIndex >= 0) { "currentStepIndex cannot be negative." }
        require(currentStepId.isNotBlank()) { "currentStepId cannot be blank." }
        require(helpLevel in 0..5) { "helpLevel must be 0..5." }
    }
}

sealed interface LessonSessionState {
    data object Ready : LessonSessionState

    sealed interface Contextual : LessonSessionState {
        val context: LessonActiveContext
    }

    sealed interface Pausable : Contextual

    data class OverviewDemonstrating(
        override val context: LessonActiveContext,
    ) : Pausable

    data class PreparingStep(
        override val context: LessonActiveContext,
    ) : Pausable

    data class TeacherDemonstrating(
        override val context: LessonActiveContext,
        val requestId: String,
        val replay: Boolean = false,
    ) : Pausable {
        init {
            require(requestId.isNotBlank()) { "Teacher demonstration requestId cannot be blank." }
        }
    }

    data class AwaitingChild(
        override val context: LessonActiveContext,
    ) : Pausable

    data class HelpActive(
        override val context: LessonActiveContext,
    ) : Pausable {
        init {
            require(context.helpLevel > 0) { "HelpActive requires helpLevel > 0." }
        }
    }

    data class CompletingStep(
        override val context: LessonActiveContext,
    ) : Contextual

    data class Paused(
        val previousStableState: Pausable,
    ) : Contextual {
        override val context: LessonActiveContext
            get() = previousStableState.context
    }

    data class DrawingComplete(
        override val context: LessonActiveContext,
    ) : Contextual

    data class AwaitingPostDrawingChoice(
        override val context: LessonActiveContext,
    ) : Contextual

    data class HandingOffToColoring(
        override val context: LessonActiveContext,
    ) : Contextual

    data class Finished(
        val reason: LessonFinishReason,
        val finalContext: LessonActiveContext? = null,
    ) : LessonSessionState

    data class RecoverableError(
        override val context: LessonActiveContext,
        val recoveryPhase: LessonSnapshotPhase,
        val code: String,
    ) : Contextual

    data class FatalContentError(
        val code: String,
        val message: String,
    ) : LessonSessionState
}

enum class LessonFinishReason {
    COMPLETED,
    SAVED_FOR_LATER,
    FINISHED_FOR_NOW,
}

sealed interface LessonCommand {
    data class StartLesson(
        val mode: TeachingMode,
        val pace: TeachingPace,
    ) : LessonCommand

    data object Pause : LessonCommand
    data object Resume : LessonCommand
    data object SaveAndExit : LessonCommand

    data class SetPace(
        val pace: TeachingPace,
    ) : LessonCommand
}

enum class LessonCommandRejectionCode {
    INVALID_STATE,
    UNSUPPORTED_MODE,
    SNAPSHOT_UNAVAILABLE,
    REPLAY_NOT_ALLOWED,
    SKIP_NOT_ALLOWED,
    HELP_NOT_AVAILABLE,
    COMPLETION_POLICY_NOT_SATISFIED,
    UNSUPPORTED_COMPLETION_POLICY,
}

data class LessonCommandRejection(
    val code: LessonCommandRejectionCode,
    val message: String,
)

sealed interface LessonCommandResult {
    val state: LessonSessionState
    val events: List<LessonSessionEvent>

    data class Accepted(
        override val state: LessonSessionState,
        override val events: List<LessonSessionEvent>,
    ) : LessonCommandResult

    data class Rejected(
        override val state: LessonSessionState,
        val rejection: LessonCommandRejection,
    ) : LessonCommandResult {
        override val events: List<LessonSessionEvent> = emptyList()
    }
}

sealed interface LessonSessionEvent {
    data class StateChanged(
        val previous: LessonSessionState,
        val current: LessonSessionState,
    ) : LessonSessionEvent

    data class SessionStarted(
        val mode: TeachingMode,
        val pace: TeachingPace,
    ) : LessonSessionEvent

    data class SessionPaused(
        val previousPhase: LessonSnapshotPhase,
    ) : LessonSessionEvent

    data class SessionResumed(
        val resumedPhase: LessonSnapshotPhase,
    ) : LessonSessionEvent

    data class PaceChanged(
        val previous: TeachingPace,
        val current: TeachingPace,
    ) : LessonSessionEvent

    data class SaveAndExitRequested(
        val snapshot: LessonSessionSnapshot,
    ) : LessonSessionEvent

    data class SessionRestored(
        val normalization: LessonRestoreNormalization,
    ) : LessonSessionEvent
}

enum class LessonSnapshotPhase {
    READY,
    OVERVIEW_DEMONSTRATING,
    PREPARING_STEP,
    TEACHER_DEMONSTRATING,
    AWAITING_CHILD,
    HELP_ACTIVE,
    COMPLETING_STEP,
    PAUSED,
    DRAWING_COMPLETE,
    AWAITING_POST_DRAWING_CHOICE,
    HANDING_OFF_TO_COLORING,
    FINISHED,
}

data class LessonSessionSnapshot(
    val formatVersion: Int = CURRENT_FORMAT_VERSION,
    val sessionId: String,
    val lessonId: String,
    val lessonRevision: Int,
    val childDocumentId: String,
    val mode: TeachingMode? = null,
    val pace: TeachingPace? = null,
    val phase: LessonSnapshotPhase,
    val pausedResumePhase: LessonSnapshotPhase? = null,
    val transientRuntimePhase: LessonSnapshotPhase? = null,
    val currentStepIndex: Int? = null,
    val currentStepId: String? = null,
    val helpLevel: Int = 0,
    val overviewCompleted: Boolean = false,
    val finishReason: LessonFinishReason? = null,
    val runtimeGeneration: Int = 0,
    val savedAtEpochMillis: Long,
) {
    init {
        require(runtimeGeneration >= 0) { "runtimeGeneration cannot be negative." }
    }

    companion object {
        const val CURRENT_FORMAT_VERSION = 1
    }
}

enum class LessonSnapshotUnavailableReason {
    FATAL_CONTENT_STATE,
}

sealed interface LessonSnapshotResult {
    data class Created(val snapshot: LessonSessionSnapshot) : LessonSnapshotResult
    data class Unavailable(val reason: LessonSnapshotUnavailableReason) : LessonSnapshotResult
}

enum class LessonRestoreIncompatibilityCode {
    UNSUPPORTED_SNAPSHOT_VERSION,
    LESSON_ID_MISMATCH,
    LESSON_REVISION_MISMATCH,
    UNSUPPORTED_MODE,
    MISSING_ACTIVE_CONTEXT,
    INVALID_STEP,
    INVALID_HELP_LEVEL,
    INVALID_PAUSED_PHASE,
    INVALID_TRANSIENT_PHASE,
    INVALID_RUNTIME_GENERATION,
}

data class LessonRestoreIncompatibility(
    val code: LessonRestoreIncompatibilityCode,
    val message: String,
)

enum class LessonRestoreNormalization {
    NONE,
    RESTART_OVERVIEW,
    RESTART_CURRENT_TEACHER_DEMONSTRATION,
    RETURN_TO_CHILD_TURN,
    RETRY_POST_DRAWING_CHOICE,
}

sealed interface LessonRestoreResult {
    data class Restored(
        val engine: LessonSessionEngine,
        val normalization: LessonRestoreNormalization,
        val events: List<LessonSessionEvent>,
    ) : LessonRestoreResult

    data class Incompatible(
        val snapshot: LessonSessionSnapshot,
        val incompatibility: LessonRestoreIncompatibility,
    ) : LessonRestoreResult
}
