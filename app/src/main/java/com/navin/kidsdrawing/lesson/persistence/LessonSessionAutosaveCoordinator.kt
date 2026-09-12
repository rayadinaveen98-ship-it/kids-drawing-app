package com.navin.kidsdrawing.lesson.persistence

import com.navin.kidsdrawing.lesson.session.ChildTurnStarted
import com.navin.kidsdrawing.lesson.session.DrawingLessonCompleted
import com.navin.kidsdrawing.lesson.session.HelpLevelChanged
import com.navin.kidsdrawing.lesson.session.LessonAutosaveRequested
import com.navin.kidsdrawing.lesson.session.LessonCommandResult
import com.navin.kidsdrawing.lesson.session.LessonSessionEngine
import com.navin.kidsdrawing.lesson.session.LessonSessionEvent
import com.navin.kidsdrawing.lesson.session.LessonSnapshotResult
import com.navin.kidsdrawing.lesson.session.StepCompleted

/**
 * Converts safe semantic Lesson Engine boundaries into persisted snapshots.
 * Pointer input and rendering frames never call this coordinator.
 */
class LessonSessionAutosaveCoordinator(
    private val saveSnapshot: suspend (com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot) -> Unit,
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
            return save(explicit)
        }
        if (events.none(::isSemanticPersistenceBoundary)) {
            return PersistResult.NotRequired
        }
        return persistCurrent(engine)
    }

    /** Called by the lifecycle boundary when the app is moving to background. */
    suspend fun onBackground(engine: LessonSessionEngine): PersistResult = persistCurrent(engine)

    private suspend fun persistCurrent(engine: LessonSessionEngine): PersistResult = when (
        val snapshotResult = engine.createSnapshot()
    ) {
        is LessonSnapshotResult.Created -> save(snapshotResult.snapshot)
        is LessonSnapshotResult.Unavailable -> PersistResult.Unavailable(snapshotResult.reason.name)
    }

    private suspend fun save(
        snapshot: com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot,
    ): PersistResult = try {
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
        -> true
        else -> false
    }
}
