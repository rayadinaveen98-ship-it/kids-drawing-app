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
    fun fullSetDGrowsReleaseCatalogToTwentyWithOnlyFiveReviewedStandaloneWarnings() {
        val snapshot = productionCatalog()
        assertTrue(snapshot.diagnostics.toString(), snapshot.diagnostics.isEmpty())
        assertEquals(20, snapshot.entries.size)
        val ids = snapshot.entries.map { it.identity.lessonId }.toSet()
        assertTrue(setOf("snail-garden", "elephant-from-shapes", "simple-car", "sailboat-scene", "planet-with-rings", "friendly-alien").all { it in ids })
        val report = ContentQualityAnalyzer().analyze(snapshot)
        assertEquals("Content quality errors: ${report.diagnostics}", 0, report.errorCount)
        val warnings = report.diagnostics.filter { it.severity == ContentQualitySeverity.WARNING }
        val reviewed = setOf("rainbow-weather", "tree-through-seasons", "ice-cream-shop", "simple-car", "sailboat-scene")
        assertEquals("Unexpected P5.5 warnings: $warnings", 5, warnings.size)
        assertEquals(reviewed, warnings.mapNotNull { it.lessonId }.toSet())
        assertTrue(warnings.all { it.code == ContentQualityDiagnosticCode.NO_JOURNEY_MEMBERSHIP })
        assertEquals(8, report.ageBandCounts.getValue(AgeBand.LITTLE_ARTISTS))
        assertEquals(18, report.ageBandCounts.getValue(AgeBand.CREATIVE_EXPLORERS))
        assertEquals(14, report.ageBandCounts.getValue(AgeBand.GROWING_ARTISTS))
        assertEquals(6, report.ageBandCounts.getValue(AgeBand.YOUNG_ARTISTS))
    }

    @Test
    fun allSetDPackagesDirectLoadThroughStrictProductionLoader() {
        val loader = LessonPackageLoader(FileAssetCatalogSource(File("src/main/assets")))
        listOf("snail-garden", "elephant-from-shapes", "simple-car", "sailboat-scene", "planet-with-rings", "friendly-alien").forEach { id ->
            val result = loader.load("lessons/$id")
            assertTrue("$id failed to load: $result", result is LessonLoadResult.Success)
        }
    }

    @Test
    fun snailGardenMatchesLockedAnimalProgressionAndOpenAuthorshipContract() {
        val lesson = productionCatalog().runtime("snail-garden").lesson
        assertEquals(setOf(AgeBand.LITTLE_ARTISTS, AgeBand.CREATIVE_EXPLORERS), lesson.metadata.ageBands.toSet())
        assertEquals(2, lesson.metadata.difficulty)
        assertEquals(listOf("journey.animal_artist"), lesson.metadata.journeyIds)
        assertEquals(listOf("little-fish"), lesson.metadata.prerequisiteLessonIds)
        assertEquals(setOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW), lesson.supportedModes.toSet())
        assertFalse(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertTrue(lesson.drawing.steps.flatMap { it.help }.none { it.kind == HelpKind.TRACE_PATH })
        val open = lesson.drawing.steps.single { it.id == "make_garden_yours" }
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
        assertEquals("manual_done", open.childTurn.completionPolicy.name.lowercase())
    }

    @Test
    fun elephantFromShapesBridgesConstructionIntoProportionWithoutTrace() {
        val lesson = productionCatalog().runtime("elephant-from-shapes").lesson
        assertEquals(setOf(AgeBand.CREATIVE_EXPLORERS, AgeBand.GROWING_ARTISTS, AgeBand.YOUNG_ARTISTS), lesson.metadata.ageBands.toSet())
        assertEquals(3, lesson.metadata.difficulty)
        assertEquals(listOf("journey.animal_artist"), lesson.metadata.journeyIds)
        assertEquals(listOf("friendly-owl"), lesson.metadata.prerequisiteLessonIds)
        assertEquals(setOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW), lesson.supportedModes.toSet())
        assertFalse(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertTrue(lesson.drawing.steps.flatMap { it.help }.none { it.kind == HelpKind.TRACE_PATH })
        val open = lesson.drawing.steps.single { it.id == "make_elephant_yours" }
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
        assertEquals("manual_done", open.childTurn.completionPolicy.name.lowercase())
    }

    @Test
    fun simpleCarMatchesStandaloneConstructionAndOpenDesignContract() {
        val lesson = productionCatalog().runtime("simple-car").lesson
        assertEquals(1, lesson.revision)
        assertEquals(setOf(AgeBand.CREATIVE_EXPLORERS, AgeBand.GROWING_ARTISTS), lesson.metadata.ageBands.toSet())
        assertEquals(2, lesson.metadata.difficulty)
        assertEquals(setOf("vehicles", "vehicles.land"), lesson.metadata.categoryIds.toSet())
        assertEquals(listOf("shape-friends"), lesson.metadata.prerequisiteLessonIds)
        assertTrue(lesson.metadata.journeyIds.isEmpty())
        assertEquals(setOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW), lesson.supportedModes.toSet())
        assertFalse(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertTrue(lesson.drawing.steps.flatMap { it.help }.none { it.kind == HelpKind.TRACE_PATH })
        assertEquals(listOf("body_base", "cabin_and_wheels", "windows_and_details", "design_your_car"), lesson.drawing.steps.map { it.id })
        val open = lesson.drawing.steps.last()
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
        assertEquals("manual_done", open.childTurn.completionPolicy.name.lowercase())
    }

    @Test
    fun sailboatSceneUsesWholeSceneObservationWithoutTraceAndEndsOpen() {
        val lesson = productionCatalog().runtime("sailboat-scene").lesson
        assertEquals(1, lesson.revision)
        assertEquals(setOf(AgeBand.GROWING_ARTISTS, AgeBand.YOUNG_ARTISTS), lesson.metadata.ageBands.toSet())
        assertEquals(3, lesson.metadata.difficulty)
        assertEquals(setOf("vehicles", "vehicles.water", "nature.landscapes"), lesson.metadata.categoryIds.toSet())
        assertEquals(listOf("simple-car"), lesson.metadata.prerequisiteLessonIds)
        assertTrue(lesson.metadata.journeyIds.isEmpty())
        assertEquals(setOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW), lesson.supportedModes.toSet())
        assertFalse(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertTrue(lesson.drawing.steps.flatMap { it.help }.none { it.kind == HelpKind.TRACE_PATH })
        assertEquals(listOf("hull_and_waterline", "mast_and_sail", "horizon_and_depth", "balance_the_scene", "make_scene_yours"), lesson.drawing.steps.map { it.id })
        val open = lesson.drawing.steps.last()
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
        assertEquals("manual_done", open.childTurn.completionPolicy.name.lowercase())
    }

    @Test
    fun planetWithRingsStartsSpaceArtistWithOverlapAndOpenPlanetDesign() {
        val lesson = productionCatalog().runtime("planet-with-rings").lesson
        assertEquals(1, lesson.revision)
        assertEquals(setOf(AgeBand.CREATIVE_EXPLORERS, AgeBand.GROWING_ARTISTS), lesson.metadata.ageBands.toSet())
        assertEquals(2, lesson.metadata.difficulty)
        assertEquals(setOf("space", "space.planets"), lesson.metadata.categoryIds.toSet())
        assertEquals(setOf("shape.circle", "shape.ellipse", "overlap.basic", "composition.centering", "color.palette_choice"), lesson.metadata.skillIds.toSet())
        assertEquals(listOf("journey.space_artist"), lesson.metadata.journeyIds)
        assertTrue(lesson.metadata.prerequisiteLessonIds.isEmpty())
        assertEquals(setOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW), lesson.supportedModes.toSet())
        assertFalse(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertTrue(lesson.drawing.steps.flatMap { it.help }.none { it.kind == HelpKind.TRACE_PATH })
        assertFalse(lesson.coloring?.enabled == true)
        assertEquals(listOf("planet_body", "ring_overlap", "moons_and_surface", "design_your_planet"), lesson.drawing.steps.map { it.id })
        val ring = lesson.drawing.steps.single { it.id == "ring_overlap" }
        assertTrue(ring.teacher.playAsGroup)
        assertEquals(2, ring.childTurn.expectedStrokeRefs.size)
        val open = lesson.drawing.steps.single { it.id == "design_your_planet" }
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
        assertEquals("manual_done", open.childTurn.completionPolicy.name.lowercase())
    }

    @Test
    fun friendlyAlienUsesSilhouetteAndSymmetryAsToolsThenYieldsToPersonality() {
        val lesson = productionCatalog().runtime("friendly-alien").lesson
        assertEquals(1, lesson.revision)
        assertEquals(setOf(AgeBand.CREATIVE_EXPLORERS, AgeBand.GROWING_ARTISTS, AgeBand.YOUNG_ARTISTS), lesson.metadata.ageBands.toSet())
        assertEquals(3, lesson.metadata.difficulty)
        assertEquals(setOf("space", "space.aliens", "characters", "characters.cartoon"), lesson.metadata.categoryIds.toSet())
        assertEquals(setOf("character.silhouette", "placement.symmetry", "creativity.variation", "storytelling.character"), lesson.metadata.skillIds.toSet())
        assertEquals(listOf("journey.space_artist"), lesson.metadata.journeyIds)
        assertEquals(listOf("simple-rocket"), lesson.metadata.prerequisiteLessonIds)
        assertEquals(setOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW), lesson.supportedModes.toSet())
        assertFalse(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertTrue(lesson.drawing.steps.flatMap { it.help }.none { it.kind == HelpKind.TRACE_PATH })
        assertFalse(lesson.coloring?.enabled == true)
        assertEquals(listOf("body_silhouette", "face_balance", "limbs_and_parts", "give_it_personality"), lesson.drawing.steps.map { it.id })
        val open = lesson.drawing.steps.single { it.id == "give_it_personality" }
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
        assertEquals("manual_done", open.childTurn.completionPolicy.name.lowercase())
    }

    @Test
    fun animalAndSpaceJourneysContainTheirLockedP5_5Members() {
        val snapshot = productionCatalog()
        listOf("little-fish", "snail-garden", "cute-cat", "friendly-owl", "elephant-from-shapes", "fox-portrait").forEach { id ->
            assertTrue("$id must belong to Animal Artist", "journey.animal_artist" in snapshot.runtime(id).lesson.metadata.journeyIds)
        }
        listOf("planet-with-rings", "simple-rocket", "friendly-alien", "design-your-spaceship").forEach { id ->
            assertTrue("$id must belong to Space Artist", "journey.space_artist" in snapshot.runtime(id).lesson.metadata.journeyIds)
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
