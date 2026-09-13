package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RepresentativeContentSetATest {
    @Test
    fun productionCatalogLoadsExistingCuteCatAndFourRepresentativeLessonsWithoutDiagnostics() {
        val snapshot = productionCatalog()

        assertTrue(snapshot.diagnostics.toString(), snapshot.diagnostics.isEmpty())
        assertEquals(
            listOf("cute-cat", "easy-flower", "friendly-owl", "simple-rocket", "smiling-sun"),
            snapshot.entries.map { it.identity.lessonId },
        )
    }

    @Test
    fun everyNewProductionPackageDirectLoadsThroughTheStrictLoader() {
        val source = FileAssetCatalogSource(File("src/main/assets"))
        val loader = LessonPackageLoader(source)

        listOf("smiling-sun", "friendly-owl", "simple-rocket", "easy-flower").forEach { lessonId ->
            val result = loader.load("lessons/$lessonId")
            assertTrue("$lessonId failed to load: $result", result is LessonLoadResult.Success)
        }
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
        val lesson = checkNotNull(snapshot.runtimePackage(entry.identity)).lesson

        assertEquals(listOf("smiling-sun"), lesson.metadata.prerequisiteLessonIds)
        val petals = lesson.drawing.steps.single { it.id == "petals" }
        val stemAndLeaves = lesson.drawing.steps.single { it.id == "stem_and_leaves" }
        assertTrue(petals.teacher.playAsGroup)
        assertTrue(petals.teacher.strokeRefs.size >= 6)
        assertTrue(stemAndLeaves.teacher.playAsGroup)
        assertTrue(stemAndLeaves.teacher.strokeRefs.size >= 3)
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
