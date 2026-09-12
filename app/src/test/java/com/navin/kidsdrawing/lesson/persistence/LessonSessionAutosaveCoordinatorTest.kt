package com.navin.kidsdrawing.lesson.persistence

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.LessonPackageSource
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.GuideOverlayRequested
import com.navin.kidsdrawing.lesson.session.LessonCommand
import com.navin.kidsdrawing.lesson.session.LessonSessionEngine
import com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonSessionAutosaveCoordinatorTest {
    @Test
    fun startAndLifecycleBackgroundPersistSemanticSnapshots() = runBlocking {
        val saved = mutableListOf<LessonSessionSnapshot>()
        val coordinator = LessonSessionAutosaveCoordinator(saved::add)
        val engine = engine(clockValue = 900L)

        val started = engine.dispatch(
            LessonCommand.StartLesson(TeachingMode.DRAW_WITH_ME, TeachingPace.NORMAL),
        )
        assertEquals(
            LessonSessionAutosaveCoordinator.PersistResult.Saved,
            coordinator.afterAcceptedCommand(engine, started),
        )
        assertEquals(1, saved.size)

        assertEquals(
            LessonSessionAutosaveCoordinator.PersistResult.Saved,
            coordinator.onBackground(engine),
        )
        assertEquals(2, saved.size)
        assertEquals("session-autosave", saved.last().sessionId)
    }

    @Test
    fun overlayOnlyEventsDoNotCauseSessionWrite() = runBlocking {
        val saved = mutableListOf<LessonSessionSnapshot>()
        val coordinator = LessonSessionAutosaveCoordinator(saved::add)
        val engine = engine()

        val result = coordinator.afterEvents(
            engine,
            listOf(
                GuideOverlayRequested(
                    com.navin.kidsdrawing.lesson.assistance.LessonGuideOverlayFactory.traceForStep(
                        engine.lessonPackage,
                        engine.lessonPackage.lesson.drawing.steps.first(),
                    )!!,
                ),
            ),
        )

        assertEquals(LessonSessionAutosaveCoordinator.PersistResult.NotRequired, result)
        assertTrue(saved.isEmpty())
    }

    @Test
    fun rejectedCommandNeverWritesSession() = runBlocking {
        val saved = mutableListOf<LessonSessionSnapshot>()
        val coordinator = LessonSessionAutosaveCoordinator(saved::add)
        val engine = engine()

        val rejected = engine.dispatch(LessonCommand.Pause)
        val result = coordinator.afterAcceptedCommand(engine, rejected)

        assertEquals(LessonSessionAutosaveCoordinator.PersistResult.NotRequired, result)
        assertTrue(saved.isEmpty())
    }

    private fun engine(clockValue: Long = 100L): LessonSessionEngine {
        val loader = LessonPackageLoader(
            LessonPackageSource { path ->
                File("src/main/assets/$path").takeIf(File::isFile)?.readText()
            },
        )
        val loaded = loader.load(ROOT)
        assertTrue(loaded is LessonLoadResult.Success)
        return LessonSessionEngine.create(
            lessonPackage = (loaded as LessonLoadResult.Success).packageData,
            sessionId = "session-autosave",
            childDocumentId = "document-autosave",
            clock = { clockValue },
        )
    }

    private companion object {
        const val ROOT = "lessons/cute-cat"
    }
}
