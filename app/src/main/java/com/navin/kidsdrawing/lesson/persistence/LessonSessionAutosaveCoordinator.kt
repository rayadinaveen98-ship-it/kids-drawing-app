package com.navin.kidsdrawing.lesson.persistence

import com.navin.kidsdrawing.lesson.session.ChildTurnStarted
import com.navin.kidsdrawing.lesson.session.ColoringHandoffFailureObserved
import com.navin.kidsdrawing.lesson.session.ColoringHandoffRequested
import com.navin.kidsdrawing.lesson.session.DrawingLessonCompleted
import com.navin.kidsdrawing.lesson.session.HelpLevelChanged
import com.navin.kidsdrawing.lesson.session.LessonAutosaveRequested
import com.navin.kidsdrawing.lesson.session.LessonCommandResult
import com.navin.kidsdrawing.lesson.session.LessonFinished
import com.navin.kidsdrawing.lesson.session.LessonSessionEngine
import com.navin.kidsdrawing.lesson.session.LessonSessionEvent
import com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import com.navin.kidsdrawing.lesson.session.LessonSnapshotResult
import com.navin.kidsdrawing.lesson.session.StepCompleted
import com.navin.kidsdrawing.lesson.session.TeacherPlaybackFailureObserved

/**
 * Converts safe semantic Lesson Engine boundaries into persisted snapshots.
 * Pointer input and rendering frames never call this coordinator.
 */
class LessonSessionAutosaveCoordinator(
    private val saveSnapshot: suspend (LessonSessionSnapshot) -> Unit,
) {
    sealed interface PersistResult {
        data object NotRequired : PersistResult
        data object Saved : PersistResult
        data class Unavailable(val reason: String) : PersistResult
        data class Failed(val message: String) : PersistResult
    }

    suspend fun afterAcceptedCommand(
        engine: LessonSessionEngine,
        result: LessonCommandResult,
    ): PersistResult {
        if (result !is LessonCommandResult.Accepted) return PersistResult.NotRequired
        return afterEvents(engine, result.events)
    }

    suspend fun afterEvents(
        engine: LessonSessionEngine,
        events: List<LessonSessionEvent>,
    ): PersistResult {
        val explicit = events
            .filterIsInstance<LessonSessionEvent.SaveAndExitRequested>()
            .lastOrNull()
            ?.snapshot
        if (explicit != null) {
            return save(withRecoverableRestartIntent(engine, explicit, events))
        }
        if (events.none(::isSemanticPersistenceBoundary)) {
            return PersistResult.NotRequired
        }
        return persistCurrent(engine, events)
    }

    /** Called by the lifecycle boundary when the app is moving to background. */
    suspend fun onBackground(engine: LessonSessionEngine): PersistResult = persistCurrent(engine)

    private suspend fun persistCurrent(
        engine: LessonSessionEngine,
        events: List<LessonSessionEvent> = emptyList(),
    ): PersistResult = when (val snapshotResult = engine.createSnapshot()) {
        is LessonSnapshotResult.Created -> save(
            withRecoverableRestartIntent(engine, snapshotResult.snapshot, events),
        )
        is LessonSnapshotResult.Unavailable -> PersistResult.Unavailable(snapshotResult.reason.name)
    }

    /**
     * Recoverable teacher failures normalize semantically to PREPARING_STEP, but persistence must
     * also remember that teacher runtime work needs to be recreated. Without this hint, a process
     * death between failure and Retry could restore a PreparingStep with no active playback.
     */
    private fun withRecoverableRestartIntent(
        engine: LessonSessionEngine,
        snapshot: LessonSessionSnapshot,
        events: List<LessonSessionEvent>,
    ): LessonSessionSnapshot {
        val currentNeedsTeacherRestart = (engine.state as? LessonSessionState.RecoverableError)
            ?.code == TEACHER_PLAYBACK_FAILED
        val exitedTeacherFailure = events
            .filterIsInstance<LessonSessionEvent.StateChanged>()
            .any { changed ->
                (changed.previous as? LessonSessionState.RecoverableError)
                    ?.code == TEACHER_PLAYBACK_FAILED
            }
        if (!currentNeedsTeacherRestart && !exitedTeacherFailure) return snapshot
        if (snapshot.phase != LessonSnapshotPhase.PREPARING_STEP) return snapshot
        return snapshot.copy(transientRuntimePhase = LessonSnapshotPhase.TEACHER_DEMONSTRATING)
    }

    private suspend fun save(snapshot: LessonSessionSnapshot): PersistResult = try {
        saveSnapshot(snapshot)
        PersistResult.Saved
    } catch (failure: Throwable) {
        PersistResult.Failed(failure.message ?: failure::class.simpleName.orEmpty())
    }

    private fun isSemanticPersistenceBoundary(event: LessonSessionEvent): Boolean = when (event) {
        is LessonSessionEvent.SessionStarted,
        is LessonSessionEvent.SessionPaused,
        is LessonSessionEvent.SessionResumed,
        is LessonSessionEvent.PaceChanged,
        is ChildTurnStarted,
        is HelpLevelChanged,
        is StepCompleted,
        is DrawingLessonCompleted,
        is LessonAutosaveRequested,
        is TeacherPlaybackFailureObserved,
        is ColoringHandoffRequested,
        is ColoringHandoffFailureObserved,
        is LessonFinished,
        -> true
        else -> false
    }

    private companion object {
        const val TEACHER_PLAYBACK_FAILED = "teacher_playback_failed"
    }
}
