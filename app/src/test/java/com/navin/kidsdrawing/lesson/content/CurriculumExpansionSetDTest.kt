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
    private val setDIds = setOf("snail-garden", "elephant-from-shapes", "simple-car", "sailboat-scene", "planet-with-rings", "friendly-alien")
    private val reviewedP55Standalone = setOf("rainbow-weather", "tree-through-seasons", "ice-cream-shop", "simple-car", "sailboat-scene")

    @Test
    fun acceptedSetDBaselineRemainsPresentAsCatalogExpands() {
        val snapshot = productionCatalog()
        assertTrue(snapshot.diagnostics.toString(), snapshot.diagnostics.isEmpty())
        assertTrue("P5.5 established a 20-lesson floor", snapshot.entries.size >= 20)
        val ids = snapshot.entries.map { it.identity.lessonId }.toSet()
        assertTrue(setDIds.all { it in ids })
        val report = ContentQualityAnalyzer().analyze(snapshot)
        assertEquals("Content quality errors: ${report.diagnostics}", 0, report.errorCount)
        val warnings = report.diagnostics.filter { it.severity == ContentQualitySeverity.WARNING }
        assertTrue("P5.5 reviewed warnings disappeared unexpectedly: $warnings", reviewedP55Standalone.all { id -> warnings.any { it.lessonId == id && it.code == ContentQualityDiagnosticCode.NO_JOURNEY_MEMBERSHIP } })
        assertTrue("Unexpected warning type introduced: $warnings", warnings.all { it.code == ContentQualityDiagnosticCode.NO_JOURNEY_MEMBERSHIP })
        assertTrue(report.ageBandCounts.getValue(AgeBand.LITTLE_ARTISTS) >= 8)
        assertTrue(report.ageBandCounts.getValue(AgeBand.CREATIVE_EXPLORERS) >= 18)
        assertTrue(report.ageBandCounts.getValue(AgeBand.GROWING_ARTISTS) >= 14)
        assertTrue(report.ageBandCounts.getValue(AgeBand.YOUNG_ARTISTS) >= 6)
    }

    @Test
    fun allSetDPackagesStillDirectLoadThroughStrictProductionLoader() {
        val loader = LessonPackageLoader(FileAssetCatalogSource(File("src/main/assets")))
        setDIds.forEach { id -> assertTrue("$id failed strict load", loader.load("lessons/$id") is LessonLoadResult.Success) }
    }

    @Test
    fun setDTraceAndOpenAuthorshipBoundariesRemainFrozen() {
        val openSteps = mapOf(
            "snail-garden" to "make_garden_yours",
            "elephant-from-shapes" to "make_elephant_yours",
            "simple-car" to "design_your_car",
            "sailboat-scene" to "make_scene_yours",
            "planet-with-rings" to "design_your_planet",
            "friendly-alien" to "give_it_personality",
        )
        openSteps.forEach { (id, openStepId) ->
            val lesson = productionCatalog().runtime(id).lesson
            assertFalse("$id must not advertise Trace", TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
            assertTrue("$id must not contain Trace Help", lesson.drawing.steps.flatMap { it.help }.none { it.kind == HelpKind.TRACE_PATH })
            val open = lesson.drawing.steps.single { it.id == openStepId }
            assertTrue("$id open turn must have no expected geometry", open.childTurn.expectedStrokeRefs.isEmpty())
            assertTrue("$id open turn must allow Skip", open.childTurn.allowSkip)
            assertEquals("manual_done", open.childTurn.completionPolicy.name.lowercase())
        }
    }

    @Test
    fun setDJourneyAndPrerequisiteContractsRemainCanonical() {
        val snapshot = productionCatalog()
        listOf("little-fish", "snail-garden", "cute-cat", "friendly-owl", "elephant-from-shapes", "fox-portrait").forEach { id ->
            assertTrue("$id must remain in Animal Artist", "journey.animal_artist" in snapshot.runtime(id).lesson.metadata.journeyIds)
        }
        listOf("planet-with-rings", "simple-rocket", "friendly-alien", "design-your-spaceship").forEach { id ->
            assertTrue("$id must remain in Space Artist", "journey.space_artist" in snapshot.runtime(id).lesson.metadata.journeyIds)
        }
        assertEquals(listOf("little-fish"), snapshot.runtime("snail-garden").lesson.metadata.prerequisiteLessonIds)
        assertEquals(listOf("friendly-owl"), snapshot.runtime("elephant-from-shapes").lesson.metadata.prerequisiteLessonIds)
        assertEquals(listOf("shape-friends"), snapshot.runtime("simple-car").lesson.metadata.prerequisiteLessonIds)
        assertEquals(listOf("simple-car"), snapshot.runtime("sailboat-scene").lesson.metadata.prerequisiteLessonIds)
        assertTrue(snapshot.runtime("planet-with-rings").lesson.metadata.prerequisiteLessonIds.isEmpty())
        assertEquals(listOf("simple-rocket"), snapshot.runtime("friendly-alien").lesson.metadata.prerequisiteLessonIds)
        assertTrue(snapshot.runtime("simple-car").lesson.metadata.journeyIds.isEmpty())
        assertTrue(snapshot.runtime("sailboat-scene").lesson.metadata.journeyIds.isEmpty())
    }

    private fun productionCatalog(): LessonCatalogSnapshot = LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()
    private fun LessonCatalogSnapshot.runtime(id: String) = checkNotNull(runtimePackage(checkNotNull(byLessonId(id).singleOrNull()).identity))
    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
