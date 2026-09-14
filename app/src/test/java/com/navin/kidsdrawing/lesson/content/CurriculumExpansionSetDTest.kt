package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumExpansionSetDTest {
    @Test
    fun animalBatchAGrowsReleaseCatalogToSixteenWithoutNewWarnings() {
        val snapshot = productionCatalog()
        assertTrue(snapshot.diagnostics.toString(), snapshot.diagnostics.isEmpty())
        assertEquals(16, snapshot.entries.size)
        val ids = snapshot.entries.map { it.identity.lessonId }.toSet()
        assertTrue(setOf("snail-garden", "elephant-from-shapes").all { it in ids })

        val report = ContentQualityAnalyzer().analyze(snapshot)
        assertEquals("Content quality errors: ${report.diagnostics}", 0, report.errorCount)
        val warnings = report.diagnostics.filter { it.severity == ContentQualitySeverity.WARNING }
        assertEquals("Unexpected warnings after Animal Batch A: $warnings", 3, warnings.size)
        assertEquals(setOf("rainbow-weather", "tree-through-seasons", "ice-cream-shop"), warnings.mapNotNull { it.lessonId }.toSet())
        assertTrue(warnings.all { it.code == ContentQualityDiagnosticCode.NO_JOURNEY_MEMBERSHIP })
        assertEquals(8, report.ageBandCounts.getValue(AgeBand.LITTLE_ARTISTS))
        assertEquals(15, report.ageBandCounts.getValue(AgeBand.CREATIVE_EXPLORERS))
        assertEquals(10, report.ageBandCounts.getValue(AgeBand.GROWING_ARTISTS))
        assertEquals(4, report.ageBandCounts.getValue(AgeBand.YOUNG_ARTISTS))
    }

    @Test
    fun animalBatchPackagesDirectLoadThroughStrictProductionLoader() {
        val loader = LessonPackageLoader(FileAssetCatalogSource(File("src/main/assets")))
        listOf("snail-garden", "elephant-from-shapes").forEach { id ->
            val result = loader.load("lessons/$id")
            assertTrue("$id failed to load: $result", result is LessonLoadResult.Success)
        }
    }

    @Test
    fun snailGardenMatchesLockedAnimalProgressionAndOpenAuthorshipContract() {
        val lesson = productionCatalog().runtime("snail-garden").lesson
        assertEquals(1, lesson.revision)
        assertEquals(setOf(AgeBand.LITTLE_ARTISTS, AgeBand.CREATIVE_EXPLORERS), lesson.metadata.ageBands.toSet())
        assertEquals(2, lesson.metadata.difficulty)
        assertEquals(setOf("animals", "nature", "nature.plants"), lesson.metadata.categoryIds.toSet())
        assertTrue(setOf("line.curve", "line.loop", "shape.organic", "overlap.basic", "creativity.variation").all { it in lesson.metadata.skillIds })
        assertEquals(listOf("journey.animal_artist"), lesson.metadata.journeyIds)
        assertEquals(listOf("little-fish"), lesson.metadata.prerequisiteLessonIds)
        assertEquals(setOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW), lesson.supportedModes.toSet())
        assertFalse(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertFalse(lesson.coloring?.enabled == true)
        assertEquals(listOf("shell_spiral", "body_and_feelers", "garden_overlap", "make_garden_yours"), lesson.drawing.steps.map { it.id })
        assertTrue(lesson.drawing.steps.flatMap { it.help }.none { it.kind == HelpKind.TRACE_PATH })
        val open = lesson.drawing.steps.single { it.id == "make_garden_yours" }
        assertTrue(open.teacher.strokeRefs.isNotEmpty())
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
        assertEquals("manual_done", open.childTurn.completionPolicy.name.lowercase())
    }

    @Test
    fun elephantFromShapesBridgesConstructionIntoProportionWithoutTrace() {
        val lesson = productionCatalog().runtime("elephant-from-shapes").lesson
        assertEquals(1, lesson.revision)
        assertEquals(setOf(AgeBand.CREATIVE_EXPLORERS, AgeBand.GROWING_ARTISTS, AgeBand.YOUNG_ARTISTS), lesson.metadata.ageBands.toSet())
        assertEquals(3, lesson.metadata.difficulty)
        assertEquals(setOf("animals", "animals.wild"), lesson.metadata.categoryIds.toSet())
        assertTrue(setOf("shape.combine", "scale.relative", "proportion.basic", "overlap.basic", "contour.simple").all { it in lesson.metadata.skillIds })
        assertEquals(listOf("journey.animal_artist"), lesson.metadata.journeyIds)
        assertEquals(listOf("friendly-owl"), lesson.metadata.prerequisiteLessonIds)
        assertEquals(setOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW), lesson.supportedModes.toSet())
        assertFalse(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertFalse(lesson.coloring?.enabled == true)
        assertEquals(listOf("body_mass", "head_ears_trunk", "legs_and_overlap", "face_and_contour", "make_elephant_yours"), lesson.drawing.steps.map { it.id })
        assertTrue(lesson.drawing.steps.flatMap { it.help }.none { it.kind == HelpKind.TRACE_PATH })
        val legs = lesson.drawing.steps.single { it.id == "legs_and_overlap" }
        assertTrue(legs.teacher.playAsGroup)
        assertEquals(listOf(1, 3), legs.help.map { it.level })
        val open = lesson.drawing.steps.single { it.id == "make_elephant_yours" }
        assertTrue(open.teacher.strokeRefs.isNotEmpty())
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
    }

    @Test
    fun animalJourneyContainsTheLockedP5_5ProgressionMembers() {
        val snapshot = productionCatalog()
        listOf("little-fish", "snail-garden", "cute-cat", "friendly-owl", "elephant-from-shapes", "fox-portrait").forEach { id ->
            val lesson = snapshot.runtime(id).lesson
            assertTrue("$id must belong to Animal Artist", "journey.animal_artist" in lesson.metadata.journeyIds)
        }
    }

    private fun productionCatalog(): LessonCatalogSnapshot = LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()
    private fun LessonCatalogSnapshot.runtime(id: String) = checkNotNull(runtimePackage(checkNotNull(byLessonId(id).singleOrNull()).identity))
    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
