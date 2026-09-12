package com.navin.kidsdrawing.lesson.persistence

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.ChooseColorWithMe
import com.navin.kidsdrawing.lesson.session.ColoringHandoffFailed
import com.navin.kidsdrawing.lesson.session.FinishForNow
import com.navin.kidsdrawing.lesson.session.LessonCommandResult
import com.navin.kidsdrawing.lesson.session.LessonFinishReason
import com.navin.kidsdrawing.lesson.session.LessonRestoreResult
import com.navin.kidsdrawing.lesson.session.LessonSessionEngine
import com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonPostDrawingAutosaveTest {
    @Test
    fun coloringLaunchPersistsSafeChoiceStateNotTransientHandoff() = runBlocking {
        val saved = mutableListOf<LessonSessionSnapshot>()
        val coordinator = LessonSessionAutosaveCoordinator(saved::add)
        val engine = drawingCompleteEngine()
        val result = engine.dispatch(ChooseColorWithMe) as LessonCommandResult.Accepted

        val persisted = coordinator.afterAcceptedCommand(engine, result)

        assertEquals(LessonSessionAutosaveCoordinator.PersistResult.Saved, persisted)
        assertEquals(LessonSnapshotPhase.AWAITING_POST_DRAWING_CHOICE, saved.single().phase)
    }

    @Test
    fun coloringFailurePersistsRetryableChoiceState() = runBlocking {
        val saved = mutableListOf<LessonSessionSnapshot>()
        val coordinator = LessonSessionAutosaveCoordinator(saved::add)
        val engine = drawingCompleteEngine()
        engine.dispatch(ChooseColorWithMe)
        val failed = engine.handle(
            ColoringHandoffFailed(CHILD_DOCUMENT_ID, "temporary coloring failure"),
        )

        val persisted = coordinator.afterEvents(engine, failed.events)

        assertEquals(LessonSessionAutosaveCoordinator.PersistResult.Saved, persisted)
        assertEquals(LessonSnapshotPhase.AWAITING_POST_DRAWING_CHOICE, saved.single().phase)
    }

    @Test
    fun finishForNowPersistsTerminalReason() = runBlocking {
        val saved = mutableListOf<LessonSessionSnapshot>()
        val coordinator = LessonSessionAutosaveCoordinator(saved::add)
        val engine = drawingCompleteEngine()
        val finished = engine.dispatch(FinishForNow) as LessonCommandResult.Accepted

        val persisted = coordinator.afterAcceptedCommand(engine, finished)

        assertEquals(LessonSessionAutosaveCoordinator.PersistResult.Saved, persisted)
        assertEquals(LessonSnapshotPhase.FINISHED, saved.single().phase)
        assertEquals(LessonFinishReason.FINISHED_FOR_NOW, saved.single().finishReason)
    }

    private fun drawingCompleteEngine(): LessonSessionEngine {
        val snapshot = LessonSessionSnapshot(
            sessionId = "post-drawing-autosave-session",
            lessonId = "cute-cat",
            lessonRevision = 1,
            childDocumentId = CHILD_DOCUMENT_ID,
            mode = TeachingMode.DRAW_WITH_ME,
            pace = TeachingPace.NORMAL,
            phase = LessonSnapshotPhase.DRAWING_COMPLETE,
            currentStepIndex = 0,
            currentStepId = "head",
            overviewCompleted = true,
            savedAtEpochMillis = 100L,
        )
        val restored = LessonSessionEngine.restore(packageData(), snapshot)
        assertTrue(restored is LessonRestoreResult.Restored)
        return (restored as LessonRestoreResult.Restored).engine
    }

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
        const val CHILD_DOCUMENT_ID = "post-drawing-autosave-document"
    }
}
