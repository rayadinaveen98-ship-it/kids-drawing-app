package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackEngine
import com.navin.kidsdrawing.drawing.domain.TeacherPlaybackStatus
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.execution.LessonTeacherSequenceFactory
import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.ColoringMode
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RepresentativeContentSetATest {
    @Test
    fun productionCatalogLoadsRepresentativeLessonsAndPreparedColoringProofWithoutDiagnostics() {
        val snapshot = productionCatalog()

        assertTrue(snapshot.diagnostics.toString(), snapshot.diagnostics.isEmpty())
        assertEquals(
            listOf(
                "cute-cat",
                "easy-flower",
                "friendly-owl",
                "little-fish",
                "simple-rocket",
                "smiling-sun",
            ),
            snapshot.entries.map { it.identity.lessonId },
        )
    }

    @Test
    fun everyExpandedProductionPackageDirectLoadsThroughTheStrictLoader() {
        val source = FileAssetCatalogSource(File("src/main/assets"))
        val loader = LessonPackageLoader(source)

        listOf(
            "smiling-sun",
            "friendly-owl",
            "simple-rocket",
            "easy-flower",
            "little-fish",
        ).forEach { lessonId ->
            val result = loader.load("lessons/$lessonId")
            assertTrue("$lessonId failed to load: $result", result is LessonLoadResult.Success)
        }
    }

    @Test
    fun littleFishExercisesPreparedGuidedColoringThroughProductionCatalog() {
        val snapshot = productionCatalog()
        val entry = snapshot.singleEntry("little-fish")
        val runtimePackage = checkNotNull(snapshot.runtimePackage(entry.identity))
        val lesson = runtimePackage.lesson
        val regions = checkNotNull(runtimePackage.coloringRegionCatalog)

        assertEquals(2, lesson.minimumContentApi)
        assertTrue(lesson.coloring?.enabled == true)
        assertEquals(ColoringMode.GUIDED, lesson.coloring?.defaultMode)
        assertEquals(
            listOf("color_body", "color_tail_and_fin"),
            lesson.coloring?.steps?.map { it.id },
        )
        assertEquals(
            setOf("fish-body-region", "fish-tail-region", "fish-fin-region"),
            regions.regions.map { it.id }.toSet(),
        )
        assertEquals(
            setOf("fish-body-region", "fish-tail-region", "fish-fin-region"),
            lesson.coloring.orEmptyRegionIds(),
        )
        assertTrue(AgeBand.LITTLE_ARTISTS in lesson.metadata.ageBands)
        assertTrue(TeachingMode.DRAW_WITH_ME in lesson.supportedModes)
    }

    @Test
    fun smilingSunIsRealTraceContentForLittleArtists() {
        val snapshot = productionCatalog()
        val entry = snapshot.singleEntry("smiling-sun")
        val lesson = checkNotNull(snapshot.runtimePackage(entry.identity)).lesson

        assertTrue(AgeBand.LITTLE_ARTISTS in lesson.metadata.ageBands)
        assertTrue(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertTrue(lesson.drawing.steps.all { it.childTurn.expectedStrokeRefs.isNotEmpty() })
        assertTrue(
            lesson.drawing.steps.all { step ->
                step.help.any { it.kind == HelpKind.TRACE_PATH && it.guideRefs.isNotEmpty() }
            },
        )
    }

    @Test
    fun friendlyOwlAuthorsTheCompleteHelpLadderOnEveryStep() {
        val snapshot = productionCatalog()
        val entry = snapshot.singleEntry("friendly-owl")
        val lesson = checkNotNull(snapshot.runtimePackage(entry.identity)).lesson
        val expectedKinds = listOf(
            HelpKind.GENTLE_HINT,
            HelpKind.VISUAL_GUIDE,
            HelpKind.DIRECTION_ANCHORS,
            HelpKind.TRACE_PATH,
            HelpKind.ASSISTED_SUCCESS,
        )

        assertTrue(lesson.drawing.steps.isNotEmpty())
        lesson.drawing.steps.forEach { step ->
            assertEquals(listOf(1, 2, 3, 4, 5), step.help.map { it.level })
            assertEquals(expectedKinds, step.help.map { it.kind })
        }
    }

    @Test
    fun simpleRocketIsReplayableWatchThenDrawContent() {
        val snapshot = productionCatalog()
        val entry = snapshot.singleEntry("simple-rocket")
        val lesson = checkNotNull(snapshot.runtimePackage(entry.identity)).lesson

        assertEquals(listOf(TeachingMode.WATCH_THEN_DRAW), lesson.supportedModes)
        assertTrue(lesson.drawing.steps.all { it.childTurn.allowReplay })
        assertTrue(lesson.drawing.steps.all { it.teacher.strokeRefs.isNotEmpty() })
    }

    @Test
    fun easyFlowerUsesAuthoredPrerequisiteAndMultiStrokeGroupedDemonstrations() {
        val snapshot = productionCatalog()
        val entry = snapshot.singleEntry("easy-flower")
        val runtimePackage = checkNotNull(snapshot.runtimePackage(entry.identity))
        val lesson = runtimePackage.lesson

        assertEquals(listOf("smiling-sun"), lesson.metadata.prerequisiteLessonIds)
        val petals = lesson.drawing.steps.single { it.id == "petals" }
        val stemAndLeaves = lesson.drawing.steps.single { it.id == "stem_and_leaves" }
        assertTrue(petals.teacher.playAsGroup)
        assertTrue(petals.teacher.strokeRefs.size >= 6)
        assertTrue(stemAndLeaves.teacher.playAsGroup)
        assertTrue(stemAndLeaves.teacher.strokeRefs.size >= 3)

        val sequence = LessonTeacherSequenceFactory.create(runtimePackage, petals)
        assertEquals(petals.teacher.strokeRefs.size, sequence.strokes.size)
        assertEquals(
            petals.teacher.strokeRefs,
            sequence.strokes.map { source ->
                source.stroke.strokeId.substringAfter("teacher-easy-flower-petals-")
            },
        )

        TeachingPace.values().forEach { pace ->
            val playback = TeacherPlaybackEngine(sequence, initialPace = pace)
            playback.play()
            val completed = playback.advanceBy(1_000_000L)
            assertEquals("Grouped petals did not complete at $pace", TeacherPlaybackStatus.COMPLETED, completed.status)
            assertEquals(sequence.strokes.size, completed.completedStrokeCount)
            assertEquals(1f, completed.progress, 0.0001f)

            playback.replay()
            val replayed = playback.advanceBy(1_000_000L)
            assertEquals("Grouped petals replay failed at $pace", TeacherPlaybackStatus.COMPLETED, replayed.status)
            assertEquals(sequence.strokes.size, replayed.completedStrokeCount)
        }
    }

    @Test
    fun p43LessonsRemainDrawingOnlyWhileCuteCatKeepsLegacyFreehandColoring() {
        val snapshot = productionCatalog()

        val cuteCat = snapshot.singleRuntimeLesson("cute-cat")
        assertTrue(cuteCat.coloring?.enabled == true)
        assertTrue(cuteCat.coloring?.steps?.all { it.regionIds.isEmpty() } == true)

        listOf("smiling-sun", "friendly-owl", "simple-rocket", "easy-flower").forEach { lessonId ->
            val lesson = snapshot.singleRuntimeLesson(lessonId)
            assertFalse("$lessonId must remain drawing-only", lesson.coloring?.enabled == true)
        }
    }

    @Test
    fun representativeSetCoversLittleCreativeAndGrowingArtistsAndRequiredModes() {
        val snapshot = productionCatalog()

        assertTrue(snapshot.forAgeBand(AgeBand.LITTLE_ARTISTS).isNotEmpty())
        assertTrue(snapshot.forAgeBand(AgeBand.CREATIVE_EXPLORERS).isNotEmpty())
        assertTrue(snapshot.forAgeBand(AgeBand.GROWING_ARTISTS).isNotEmpty())
        assertTrue(snapshot.supporting(TeachingMode.TRACE_AND_LEARN).isNotEmpty())
        assertTrue(snapshot.supporting(TeachingMode.WATCH_THEN_DRAW).isNotEmpty())
        assertTrue(snapshot.supporting(TeachingMode.DRAW_WITH_ME).isNotEmpty())
    }

    private fun productionCatalog(): LessonCatalogSnapshot =
        LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()

    private fun LessonCatalogSnapshot.singleEntry(lessonId: String): LessonCatalogEntry {
        val entry = byLessonId(lessonId).singleOrNull()
        assertNotNull("Expected exactly one production entry for $lessonId", entry)
        return checkNotNull(entry)
    }

    private fun LessonCatalogSnapshot.singleRuntimeLesson(lessonId: String) =
        checkNotNull(runtimePackage(singleEntry(lessonId).identity)).lesson

    private fun com.navin.kidsdrawing.lesson.model.LessonColoring?.orEmptyRegionIds(): Set<String> =
        this?.steps.orEmpty().flatMap { it.regionIds }.toSet()

    private class FileAssetCatalogSource(
        private val assetRoot: File,
    ) : LessonCatalogSource {
        override fun readText(path: String): String? =
            File(assetRoot, path).takeIf(File::isFile)?.readText()

        override fun list(path: String): List<String>? =
            File(assetRoot, path).list()?.toList()

        override fun exists(path: String): Boolean = File(assetRoot, path).isFile
    }
}
