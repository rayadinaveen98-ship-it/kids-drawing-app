package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumExpansionSetETest {
    @Test
    fun batchAGrowsCatalogToTwentyTwoWithAcceptedQualityState() {
        val snapshot = productionCatalog()
        assertTrue(snapshot.diagnostics.toString(), snapshot.diagnostics.isEmpty())
        assertEquals(22, snapshot.entries.size)
        val ids = snapshot.entries.map { it.identity.lessonId }.toSet()
        assertTrue(setOf("face-and-expressions", "simple-body-and-pose").all { it in ids })
        val report = ContentQualityAnalyzer().analyze(snapshot)
        assertEquals("Content quality errors: ${report.diagnostics}", 0, report.errorCount)
        val warnings = report.diagnostics.filter { it.severity == ContentQualitySeverity.WARNING }
        val reviewed = setOf("rainbow-weather", "tree-through-seasons", "ice-cream-shop", "simple-car", "sailboat-scene")
        assertEquals("Unexpected Batch-A warnings: $warnings", 5, warnings.size)
        assertEquals(reviewed, warnings.mapNotNull { it.lessonId }.toSet())
        assertTrue(warnings.all { it.code == ContentQualityDiagnosticCode.NO_JOURNEY_MEMBERSHIP })
        assertEquals(8, report.ageBandCounts.getValue(AgeBand.LITTLE_ARTISTS))
        assertEquals(18, report.ageBandCounts.getValue(AgeBand.CREATIVE_EXPLORERS))
        assertEquals(16, report.ageBandCounts.getValue(AgeBand.GROWING_ARTISTS))
        assertEquals(8, report.ageBandCounts.getValue(AgeBand.YOUNG_ARTISTS))
        assertEquals(6, report.difficultyCounts.getValue(3))
        assertEquals(2, report.difficultyCounts.getValue(4))
    }

    @Test
    fun batchAPackagesDirectLoadThroughStrictProductionLoader() {
        val loader = LessonPackageLoader(FileAssetCatalogSource(File("src/main/assets")))
        listOf("face-and-expressions", "simple-body-and-pose").forEach { id ->
            val result = loader.load("lessons/$id")
            assertTrue("$id failed to load: $result", result is LessonLoadResult.Success)
        }
    }

    @Test
    fun faceAndExpressionsMatchesMatureConstructionAndOpenExpressionContract() {
        val lesson = productionCatalog().runtime("face-and-expressions").lesson
        assertEquals(1, lesson.revision)
        assertEquals(setOf(AgeBand.GROWING_ARTISTS, AgeBand.YOUNG_ARTISTS), lesson.metadata.ageBands.toSet())
        assertEquals(3, lesson.metadata.difficulty)
        assertEquals(setOf("characters", "portrait"), lesson.metadata.categoryIds.toSet())
        assertEquals(setOf("anatomy.face_basic", "placement.symmetry", "expression.face", "proportion.basic", "creativity.variation"), lesson.metadata.skillIds.toSet())
        assertEquals(listOf("journey.character_creator"), lesson.metadata.journeyIds)
        assertTrue(lesson.metadata.prerequisiteLessonIds.isEmpty())
        assertEquals(setOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW), lesson.supportedModes.toSet())
        assertFalse(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertTrue(lesson.drawing.steps.flatMap { it.help }.none { it.kind == HelpKind.TRACE_PATH })
        assertEquals(listOf("head_construction", "eyes_and_brows", "nose_and_mouth", "choose_expression"), lesson.drawing.steps.map { it.id })
        val open = lesson.drawing.steps.last()
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
        assertEquals("manual_done", open.childTurn.completionPolicy.name.lowercase())
    }

    @Test
    fun simpleBodyAndPoseAdvancesCharacterCreatorWithoutTrace() {
        val lesson = productionCatalog().runtime("simple-body-and-pose").lesson
        assertEquals(1, lesson.revision)
        assertEquals(setOf(AgeBand.GROWING_ARTISTS, AgeBand.YOUNG_ARTISTS), lesson.metadata.ageBands.toSet())
        assertEquals(4, lesson.metadata.difficulty)
        assertEquals(setOf("characters", "design"), lesson.metadata.categoryIds.toSet())
        assertEquals(setOf("anatomy.body_basic", "pose.simple", "proportion.basic", "character.silhouette", "creativity.variation"), lesson.metadata.skillIds.toSet())
        assertEquals(listOf("journey.character_creator"), lesson.metadata.journeyIds)
        assertEquals(listOf("face-and-expressions"), lesson.metadata.prerequisiteLessonIds)
        assertEquals(setOf(TeachingMode.DRAW_WITH_ME, TeachingMode.WATCH_THEN_DRAW), lesson.supportedModes.toSet())
        assertFalse(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertTrue(lesson.drawing.steps.flatMap { it.help }.none { it.kind == HelpKind.TRACE_PATH })
        assertEquals(listOf("torso_and_pelvis", "arms_and_legs", "simple_pose", "contour_and_balance", "make_pose_yours"), lesson.drawing.steps.map { it.id })
        val open = lesson.drawing.steps.last()
        assertTrue(open.childTurn.expectedStrokeRefs.isEmpty())
        assertTrue(open.childTurn.allowSkip)
        assertEquals("manual_done", open.childTurn.completionPolicy.name.lowercase())
    }

    @Test
    fun batchACharacterCreatorMembershipIsCanonical() {
        val snapshot = productionCatalog()
        listOf("face-and-expressions", "simple-body-and-pose").forEach { id ->
            assertTrue("$id must belong to Character Creator", "journey.character_creator" in snapshot.runtime(id).lesson.metadata.journeyIds)
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
