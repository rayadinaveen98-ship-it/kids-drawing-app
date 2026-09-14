package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.ChildCompletionPolicy
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Final Phase-5 integrated release contract.
 *
 * Existing engine/product suites continue to own deep behavior. This gate intentionally locks the
 * cross-cutting facts that must all remain true together before the final 0.5 binary is frozen.
 */
class P5_8IntegratedReleaseGateTest {
    @Test
    fun finalCatalogCoverageAndQualityContractRemainFrozenTogether() {
        val snapshot = productionCatalog()
        assertTrue("Catalog diagnostics: ${snapshot.diagnostics}", snapshot.diagnostics.isEmpty())
        assertEquals(24, snapshot.entries.size)

        val report = ContentQualityAnalyzer().analyze(snapshot)
        val warnings = report.diagnostics.filter { it.severity == ContentQualitySeverity.WARNING }
        val reviewedStandalone = setOf(
            "ice-cream-shop",
            "one-point-room",
            "rainbow-weather",
            "sailboat-scene",
            "simple-car",
            "tree-through-seasons",
        )

        assertEquals(0, report.errorCount)
        assertEquals(6, warnings.size)
        assertEquals(reviewedStandalone, warnings.mapNotNull { it.lessonId }.toSet())
        assertTrue(warnings.all { it.code == ContentQualityDiagnosticCode.NO_JOURNEY_MEMBERSHIP })

        assertEquals(8, report.ageBandCounts.getValue(AgeBand.LITTLE_ARTISTS))
        assertEquals(18, report.ageBandCounts.getValue(AgeBand.CREATIVE_EXPLORERS))
        assertEquals(17, report.ageBandCounts.getValue(AgeBand.GROWING_ARTISTS))
        assertEquals(10, report.ageBandCounts.getValue(AgeBand.YOUNG_ARTISTS))
        assertEquals(mapOf(1 to 5, 2 to 9, 3 to 6, 4 to 3, 5 to 1), report.difficultyCounts)

        assertTrue(report.phase5Progress.lessonTargetMet)
        assertTrue(report.phase5Progress.ageBandTargetsMet)
        assertTrue(report.phase5Progress.difficultyTargetsMet)
        assertTrue(report.phase5Progress.watchThenDrawTargetMet)
    }

    @Test
    fun allPrerequisitesResolveAndEveryTeachingModeRemainsRepresented() {
        val snapshot = productionCatalog()
        val lessons = snapshot.entries.map { entry ->
            checkNotNull(snapshot.runtimePackage(entry.identity)).lesson
        }
        val ids = lessons.map { it.lessonId }.toSet()

        lessons.forEach { lesson ->
            lesson.metadata.prerequisiteLessonIds.forEach { prerequisiteId ->
                assertTrue(
                    "${lesson.lessonId} references missing prerequisite $prerequisiteId",
                    prerequisiteId in ids,
                )
            }
            assertTrue("${lesson.lessonId} must support at least one teaching mode", lesson.supportedModes.isNotEmpty())
        }

        val representedModes = lessons.flatMap { it.supportedModes }.toSet()
        assertTrue(TeachingMode.DRAW_WITH_ME in representedModes)
        assertTrue(TeachingMode.WATCH_THEN_DRAW in representedModes)
        assertTrue(TeachingMode.TRACE_AND_LEARN in representedModes)
    }

    @Test
    fun openAuthorshipNeverAcquiresForcedTraceHelp() {
        val snapshot = productionCatalog()
        val lessons = snapshot.entries.map { entry ->
            checkNotNull(snapshot.runtimePackage(entry.identity)).lesson
        }
        var openTurns = 0

        lessons.forEach { lesson ->
            lesson.drawing.steps.forEach { step ->
                val isOpenAuthorship = step.childTurn.completionPolicy == ChildCompletionPolicy.MANUAL_DONE &&
                    step.childTurn.allowSkip &&
                    step.childTurn.expectedStrokeRefs.isEmpty()
                if (isOpenAuthorship) {
                    openTurns += 1
                    assertFalse(
                        "${lesson.lessonId}/${step.id} is open authorship and must not force Trace",
                        step.help.any { it.kind == HelpKind.TRACE_PATH },
                    )
                }
            }
        }

        assertTrue("Final curriculum should contain meaningful open-authorship turns", openTurns >= 8)
    }

    @Test
    fun everyJourneyAndPrerequisiteIdUsesCanonicalNonBlankMetadata() {
        val snapshot = productionCatalog()
        val lessons = snapshot.entries.map { entry ->
            checkNotNull(snapshot.runtimePackage(entry.identity)).lesson
        }

        lessons.forEach { lesson ->
            assertTrue(lesson.metadata.journeyIds.all { it.isNotBlank() && it == it.trim() })
            assertTrue(lesson.metadata.prerequisiteLessonIds.all { it.isNotBlank() && it == it.trim() })
            assertEquals(lesson.metadata.journeyIds.distinct(), lesson.metadata.journeyIds)
            assertEquals(lesson.metadata.prerequisiteLessonIds.distinct(), lesson.metadata.prerequisiteLessonIds)
            assertFalse("A lesson cannot require itself", lesson.lessonId in lesson.metadata.prerequisiteLessonIds)
        }
    }

    private fun productionCatalog(): LessonCatalogSnapshot =
        LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
