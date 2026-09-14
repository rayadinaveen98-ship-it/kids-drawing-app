package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumExpansionSetCTest {
    @Test
    fun fullSetCGrowsReleaseCatalogToFourteenWithOnlyReviewedStandaloneWarnings() {
        val snapshot = productionCatalog()
        assertTrue(snapshot.diagnostics.toString(), snapshot.diagnostics.isEmpty())
        assertEquals(14, snapshot.entries.size)
        val ids = snapshot.entries.map { it.identity.lessonId }.toSet()
        assertTrue(setOf("happy-lines", "shape-friends", "rainbow-weather", "tree-through-seasons", "ice-cream-shop").all { it in ids })
        val report = ContentQualityAnalyzer().analyze(snapshot)
        assertEquals("Content quality errors: ${report.diagnostics}", 0, report.errorCount)
        val warnings = report.diagnostics.filter { it.severity == ContentQualitySeverity.WARNING }
        assertEquals("Unexpected content quality warnings: $warnings", 3, warnings.size)
        assertEquals(setOf("rainbow-weather", "tree-through-seasons", "ice-cream-shop"), warnings.mapNotNull { it.lessonId }.toSet())
        assertTrue(warnings.all { it.code == ContentQualityDiagnosticCode.NO_JOURNEY_MEMBERSHIP })
    }

    @Test
    fun allSetCPackagesDirectLoadThroughStrictProductionLoader() {
        val loader = LessonPackageLoader(FileAssetCatalogSource(File("src/main/assets")))
        listOf("happy-lines", "shape-friends", "rainbow-weather", "tree-through-seasons", "ice-cream-shop").forEach { id ->
            val result = loader.load("lessons/$id")
            assertTrue("$id failed to load: $result", result is LessonLoadResult.Success)
        }
    }

    @Test
    fun happyLinesMatchesLockedFoundationAndAuthorshipContract() {
        val lesson = productionCatalog().runtime("happy-lines").lesson
        assertEquals(1, lesson.revision)
        assertEquals(setOf(AgeBand.LITTLE_ARTISTS, AgeBand.CREATIVE_EXPLORERS), lesson.metadata.ageBands.toSet())
        assertEquals(1, lesson.metadata.difficulty)
        assertEquals(setOf("foundations", "foundations.lines"), lesson.metadata.categoryIds.toSet())
        assertTrue(setOf("line.straight", "line.curve", "line.zigzag", "line.loop", "line.control").all { it in lesson.metadata.skillIds })
        assertEquals(listOf("journey.first_shapes_to_pictures"), lesson.metadata.journeyIds)
        assertTrue(TeachingMode.DRAW_WITH_ME in lesson.supportedModes)
        assertTrue(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertFalse(lesson.coloring?.enabled == true)
        assertEquals(listOf("straight_lines", "curves_and_waves", "zigzag", "loops", "make_marks_yours"), lesson.drawing.steps.map { it.id })
        val straight = lesson.drawing.steps.single { it.id == "straight_lines" }
        assertTrue(straight.teacher.playAsGroup)
        assertEquals(listOf(1, 4), straight.help.map { it.level })
        val curves = lesson.drawing.steps.single { it.id == "curves_and_waves" }
        assertEquals(listOf(1, 2, 4), curves.help.map { it.level })
        val open = lesson.drawing.steps.single { it.id == "make_marks_yours" }
        assertTrue(open.teacher.strokeRefs.isNotEmpty())
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
        assertEquals("manual_done", open.childTurn.completionPolicy.name.lowercase())
    }

    @Test
    fun shapeFriendsRequiresHappyLinesAndEndsWithRealAuthorship() {
        val lesson = productionCatalog().runtime("shape-friends").lesson
        assertEquals(1, lesson.revision)
        assertEquals(setOf(AgeBand.LITTLE_ARTISTS, AgeBand.CREATIVE_EXPLORERS), lesson.metadata.ageBands.toSet())
        assertEquals(1, lesson.metadata.difficulty)
        assertEquals(listOf("happy-lines"), lesson.metadata.prerequisiteLessonIds)
        assertEquals(listOf("journey.first_shapes_to_pictures"), lesson.metadata.journeyIds)
        assertTrue(TeachingMode.DRAW_WITH_ME in lesson.supportedModes)
        assertTrue(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertFalse(lesson.coloring?.enabled == true)
        assertEquals(listOf("circle", "square", "triangle", "build_friend", "make_friend_yours"), lesson.drawing.steps.map { it.id })
        listOf("circle", "square", "triangle").forEach { stepId ->
            val step = lesson.drawing.steps.single { it.id == stepId }
            assertTrue(step.childTurn.expectedStrokeRefs.isNotEmpty())
            assertEquals(listOf(1, 4), step.help.map { it.level })
        }
        val construction = lesson.drawing.steps.single { it.id == "build_friend" }
        assertTrue(construction.teacher.playAsGroup)
        assertEquals(listOf(1, 2), construction.help.map { it.level })
        assertTrue(construction.childTurn.expectedStrokeRefs.isNotEmpty())
        val open = lesson.drawing.steps.single { it.id == "make_friend_yours" }
        assertTrue(open.teacher.strokeRefs.isNotEmpty())
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
    }

    @Test
    fun rainbowWeatherMatchesPreparedColorAndOpenWeatherContract() {
        val runtime = productionCatalog().runtime("rainbow-weather")
        val lesson = runtime.lesson
        assertEquals(1, lesson.revision)
        assertEquals(2, lesson.minimumContentApi)
        assertEquals(setOf(AgeBand.LITTLE_ARTISTS, AgeBand.CREATIVE_EXPLORERS), lesson.metadata.ageBands.toSet())
        assertEquals(2, lesson.metadata.difficulty)
        assertEquals(setOf("nature", "nature.weather", "scenes"), lesson.metadata.categoryIds.toSet())
        assertEquals(listOf("smiling-sun"), lesson.metadata.prerequisiteLessonIds)
        assertTrue(lesson.metadata.journeyIds.isEmpty())
        assertEquals(listOf(TeachingMode.DRAW_WITH_ME), lesson.supportedModes)
        assertEquals(listOf("rainbow_arcs", "clouds", "weather_details"), lesson.drawing.steps.map { it.id })
        val arcs = lesson.drawing.steps.single { it.id == "rainbow_arcs" }
        assertTrue(arcs.teacher.playAsGroup)
        assertEquals(3, arcs.childTurn.expectedStrokeRefs.size)
        assertEquals(listOf(1, 2), arcs.help.map { it.level })
        val clouds = lesson.drawing.steps.single { it.id == "clouds" }
        assertEquals(2, clouds.childTurn.expectedStrokeRefs.size)
        assertEquals(listOf(1, 2), clouds.help.map { it.level })
        val open = lesson.drawing.steps.single { it.id == "weather_details" }
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
        val regions = checkNotNull(runtime.coloringRegionCatalog)
        assertEquals(setOf("rainbow-outer-region", "rainbow-middle-region", "rainbow-inner-region"), regions.regions.map { it.id }.toSet())
        assertEquals(3, regions.regions.size)
        assertTrue(lesson.coloring?.enabled == true)
        assertEquals(listOf("rainbow-outer-region", "rainbow-middle-region", "rainbow-inner-region"), lesson.coloring?.steps?.flatMap { it.regionIds })
        assertTrue(lesson.coloring?.steps?.all { !it.enforceSuggestedColors } == true)
    }

    @Test
    fun treeThroughSeasonsUsesObservationSupportWithoutTraceAndEndsWithStoryChoice() {
        val lesson = productionCatalog().runtime("tree-through-seasons").lesson
        assertEquals(1, lesson.revision)
        assertEquals(setOf(AgeBand.CREATIVE_EXPLORERS, AgeBand.GROWING_ARTISTS, AgeBand.YOUNG_ARTISTS), lesson.metadata.ageBands.toSet())
        assertEquals(3, lesson.metadata.difficulty)
        assertEquals(setOf("nature", "nature.trees", "scenes"), lesson.metadata.categoryIds.toSet())
        assertEquals(listOf("easy-flower"), lesson.metadata.prerequisiteLessonIds)
        assertTrue(lesson.metadata.journeyIds.isEmpty())
        assertEquals(setOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW), lesson.supportedModes.toSet())
        assertFalse(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertFalse(lesson.coloring?.enabled == true)
        assertEquals(listOf("trunk", "branches", "canopy", "season_story"), lesson.drawing.steps.map { it.id })
        assertTrue(lesson.drawing.steps.flatMap { it.help }.none { it.kind == HelpKind.TRACE_PATH })
        val branches = lesson.drawing.steps.single { it.id == "branches" }
        assertTrue(branches.teacher.playAsGroup)
        assertEquals(listOf(1, 3), branches.help.map { it.level })
        assertEquals(HelpKind.DIRECTION_ANCHORS, branches.help.last().kind)
        val open = lesson.drawing.steps.single { it.id == "season_story" }
        assertTrue(open.teacher.strokeRefs.isNotEmpty())
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
    }

    @Test
    fun iceCreamShopUsesShapeConstructionAndEndsWithOpenDesignChoice() {
        val lesson = productionCatalog().runtime("ice-cream-shop").lesson
        assertEquals(1, lesson.revision)
        assertEquals(setOf(AgeBand.CREATIVE_EXPLORERS, AgeBand.GROWING_ARTISTS), lesson.metadata.ageBands.toSet())
        assertEquals(2, lesson.metadata.difficulty)
        assertEquals(setOf("everyday", "food", "design"), lesson.metadata.categoryIds.toSet())
        assertEquals(setOf("shape.combine", "placement.relative", "pattern", "creativity.variation"), lesson.metadata.skillIds.toSet())
        assertEquals(listOf("shape-friends"), lesson.metadata.prerequisiteLessonIds)
        assertTrue(lesson.metadata.journeyIds.isEmpty())
        assertEquals(listOf(TeachingMode.DRAW_WITH_ME), lesson.supportedModes)
        assertFalse(lesson.coloring?.enabled == true)
        assertEquals(listOf("cone_or_cup", "scoops", "shop_sign", "make_it_yours"), lesson.drawing.steps.map { it.id })
        val cone = lesson.drawing.steps.single { it.id == "cone_or_cup" }
        assertTrue(cone.teacher.playAsGroup)
        assertEquals(listOf(1, 2), cone.help.map { it.level })
        assertTrue(cone.childTurn.expectedStrokeRefs.isNotEmpty())
        val scoops = lesson.drawing.steps.single { it.id == "scoops" }
        assertTrue(scoops.teacher.playAsGroup)
        assertEquals(listOf(1, 2), scoops.help.map { it.level })
        assertTrue(scoops.childTurn.expectedStrokeRefs.isNotEmpty())
        val sign = lesson.drawing.steps.single { it.id == "shop_sign" }
        assertEquals(listOf(1), sign.help.map { it.level })
        assertTrue(sign.childTurn.expectedStrokeRefs.isNotEmpty())
        val open = lesson.drawing.steps.single { it.id == "make_it_yours" }
        assertTrue(open.teacher.strokeRefs.isNotEmpty())
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
    }

    private fun productionCatalog(): LessonCatalogSnapshot = LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()
    private fun LessonCatalogSnapshot.runtime(id: String) = checkNotNull(runtimePackage(checkNotNull(byLessonId(id).singleOrNull()).identity))
    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
