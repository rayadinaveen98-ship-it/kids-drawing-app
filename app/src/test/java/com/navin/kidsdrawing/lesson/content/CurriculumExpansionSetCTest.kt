package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumExpansionSetCTest {
    @Test
    fun foundationPairGrowsReleaseCatalogToElevenWithoutDiagnostics() {
        val snapshot = productionCatalog()
        assertTrue(snapshot.diagnostics.toString(), snapshot.diagnostics.isEmpty())
        assertEquals(11, snapshot.entries.size)
        assertTrue("happy-lines" in snapshot.entries.map { it.identity.lessonId })
        assertTrue("shape-friends" in snapshot.entries.map { it.identity.lessonId })

        val report = ContentQualityAnalyzer().analyze(snapshot)
        assertEquals("Content quality errors: ${report.diagnostics}", 0, report.errorCount)
        assertEquals("Content quality warnings: ${report.diagnostics}", 0, report.warningCount)
    }

    @Test
    fun foundationPairDirectLoadsThroughStrictProductionLoader() {
        val loader = LessonPackageLoader(FileAssetCatalogSource(File("src/main/assets")))
        listOf("happy-lines", "shape-friends").forEach { id ->
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
        assertTrue(
            setOf("line.straight", "line.curve", "line.zigzag", "line.loop", "line.control")
                .all { it in lesson.metadata.skillIds },
        )
        assertEquals(listOf("journey.first_shapes_to_pictures"), lesson.metadata.journeyIds)
        assertTrue(TeachingMode.DRAW_WITH_ME in lesson.supportedModes)
        assertTrue(TeachingMode.TRACE_AND_LEARN in lesson.supportedModes)
        assertFalse(lesson.coloring?.enabled == true)
        assertEquals(
            listOf("straight_lines", "curves_and_waves", "zigzag", "loops", "make_marks_yours"),
            lesson.drawing.steps.map { it.id },
        )

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
        assertEquals(
            listOf("circle", "square", "triangle", "build_friend", "make_friend_yours"),
            lesson.drawing.steps.map { it.id },
        )

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

    private fun productionCatalog(): LessonCatalogSnapshot =
        LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()

    private fun LessonCatalogSnapshot.runtime(id: String) =
        checkNotNull(runtimePackage(checkNotNull(byLessonId(id).singleOrNull()).identity))

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
