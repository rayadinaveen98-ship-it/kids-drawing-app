package com.navin.kidsdrawing.lesson.persistence

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonCommand
import com.navin.kidsdrawing.lesson.session.LessonCommandResult
import com.navin.kidsdrawing.lesson.session.LessonRestoreNormalization
import com.navin.kidsdrawing.lesson.session.LessonRestoreResult
import com.navin.kidsdrawing.lesson.session.LessonRuntimeSignal
import com.navin.kidsdrawing.lesson.session.LessonSessionEngine
import com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot
import com.navin.kidsdrawing.lesson.session.LessonSessionState
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import com.navin.kidsdrawing.lesson.session.TeacherPlaybackRequested
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonRecoverableAutosaveTest {
    @Test
    fun teacherFailureAutosaveCarriesRestartIntentAcrossProcessRecreation() = runBlocking {
        val saved = mutableListOf<LessonSessionSnapshot>()
        val autosave = LessonSessionAutosaveCoordinator(saved::add)
        val engine = engine()
        val started = engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.DRAW_WITH_ME, TeachingPace.NORMAL),
        ) as LessonCommandResult.Accepted
        val requestId = started.events.filterIsInstance<TeacherPlaybackRequested>().single().request.requestId
        val failed = engine.handle(
            LessonRuntimeSignal.TeacherPlaybackFailed(requestId, "temporary renderer failure"),
        )
        assertTrue(engine.state is LessonSessionState.RecoverableError)

        val persisted = autosave.afterEvents(engine, failed.events)

        assertEquals(LessonSessionAutosaveCoordinator.PersistResult.Saved, persisted)
        val snapshot = saved.single()
        assertEquals(LessonSnapshotPhase.PREPARING_STEP, snapshot.phase)
        assertEquals(LessonSnapshotPhase.TEACHER_DEMONSTRATING, snapshot.transientRuntimePhase)

        val restored = LessonSessionEngine.restore(packageData(), snapshot)
        assertTrue(restored is LessonRestoreResult.Restored)
        restored as LessonRestoreResult.Restored
        assertEquals(
            LessonRestoreNormalization.RESTART_CURRENT_TEACHER_DEMONSTRATION,
            restored.normalization,
        )
        val events = restored.engine.activateRestoredRuntime(restored.normalization)
        assertTrue(events.any { it is TeacherPlaybackRequested })
        assertTrue(restored.engine.state is LessonSessionState.TeacherDemonstrating)
    }

    @Test
    fun saveAndExitFromTeacherFailurePreservesRestartIntent() = runBlocking {
        val saved = mutableListOf<LessonSessionSnapshot>()
        val autosave = LessonSessionAutosaveCoordinator(saved::add)
        val engine = engine()
        val started = engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.DRAW_WITH_ME, TeachingPace.NORMAL),
        ) as LessonCommandResult.Accepted
        val requestId = started.events.filterIsInstance<TeacherPlaybackRequested>().single().request.requestId
        engine.handle(LessonRuntimeSignal.TeacherPlaybackFailed(requestId, "temporary failure"))

        val exit = engine.dispatch(LessonCommand.SaveAndExit)
        val persisted = autosave.afterAcceptedCommand(engine, exit)

        assertEquals(LessonSessionAutosaveCoordinator.PersistResult.Saved, persisted)
        assertEquals(LessonSnapshotPhase.PREPARING_STEP, saved.single().phase)
        assertEquals(
            LessonSnapshotPhase.TEACHER_DEMONSTRATING,
            saved.single().transientRuntimePhase,
        )
    }

    private fun engine(): LessonSessionEngine = LessonSessionEngine.create(
        lessonPackage = packageData(),
        sessionId = "recoverable-autosave-session",
        childDocumentId = "recoverable-autosave-document",
    )

    private fun packageData(): LessonRuntimePackage {
        val loader = LessonPackageLoader(
            LessonPackageSource { path ->
                File("src/main/assets/$path").takeIf(File::isFile)?.readText()
            },
        )
        val loaded = loader.load(ROOT)
        assertTrue(loaded is LessonLoadResult.Success)
        return (loaded as LessonLoadResult.Success).packageData
    }

    private companion object {
        const val ROOT = "lessons/cute-cat"
    }
}
