package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.AuthoredPoint
import com.navin.kidsdrawing.lesson.model.AuthoredRegionPoint
import com.navin.kidsdrawing.lesson.model.AuthoredStroke
import com.navin.kidsdrawing.lesson.model.HelpEntry
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.TeachingMode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentQualityAnalyzerTest {
    @Test
    fun productionNineLessonCatalogHasZeroQualityErrorsAndDeterministicCoverage() {
        val snapshot = productionCatalog()
        val analyzer = ContentQualityAnalyzer()

        val first = analyzer.analyze(snapshot)
        val second = analyzer.analyze(snapshot)

        assertTrue(snapshot.diagnostics.toString(), snapshot.diagnostics.isEmpty())
        assertEquals(9, first.lessonCount)
        assertEquals(0, first.errorCount)
        AgeBand.entries.forEach { ageBand ->
            assertTrue("No production lessons reported for $ageBand", (first.ageBandCounts[ageBand] ?: 0) > 0)
        }
        (1..4).forEach { difficulty ->
            assertTrue("Difficulty $difficulty disappeared from production coverage", (first.difficultyCounts[difficulty] ?: 0) > 0)
        }
        TeachingMode.entries.forEach { mode ->
            assertTrue("No production coverage reported for $mode", (first.modeCounts[mode] ?: 0) > 0)
        }
        assertTrue(first.coloringLessonCount > 0)
        assertTrue(first.preparedColoringLessonCount > 0)
        assertEquals(9, first.phase5Progress.lessonCount)
        assertEquals(24, first.phase5Progress.lessonTarget)
        assertFalse(first.phase5Progress.lessonTargetMet)
        assertEquals(first, second)
        assertEquals(first.renderText(), second.renderText())
    }

    @Test
    fun catalogDiagnosticsProjectToReleaseErrors() {
        val snapshot = LessonCatalogSnapshot(
            entries = emptyList(),
            diagnostics = listOf(
                LessonCatalogDiagnostic(
                    code = LessonCatalogDiagnosticCode.ROOT_UNAVAILABLE,
                    packageRoot = "lessons",
                    message = "Catalog unavailable for fixture.",
                ),
            ),
            runtimePackages = emptyMap(),
        )

        val report = ContentQualityAnalyzer().analyze(snapshot)

        assertEquals(1, report.errorCount)
        assertEquals(0, report.warningCount)
        assertTrue(
            report.diagnostics.any {
                it.severity == ContentQualitySeverity.ERROR &&
                    it.code == ContentQualityDiagnosticCode.CATALOG_DIAGNOSTIC
            },
        )
    }

    @Test
    fun excessiveLittleArtistStepCountIsWarningNotError() {
        val production = productionCatalog()
        val baseEntry = production.entries.first { AgeBand.LITTLE_ARTISTS in it.ageBands }
        val baseRuntime = checkNotNull(production.runtimePackage(baseEntry.identity))
        val template = baseRuntime.lesson.drawing.steps.first()
        val longLesson = baseRuntime.lesson.copy(
            metadata = baseRuntime.lesson.metadata.copy(ageBands = listOf(AgeBand.LITTLE_ARTISTS)),
            drawing = baseRuntime.lesson.drawing.copy(
                steps = List(8) { index -> template.copy(id = "fixture-step-$index") },
            ),
        )
        val entry = baseEntry.copy(ageBands = setOf(AgeBand.LITTLE_ARTISTS))
        val snapshot = LessonCatalogSnapshot(
            entries = listOf(entry),
            diagnostics = emptyList(),
            runtimePackages = mapOf(entry.identity to baseRuntime.copy(lesson = longLesson)),
        )

        val report = ContentQualityAnalyzer().analyze(snapshot)

        assertEquals(0, report.errorCount)
        assertTrue(
            report.diagnostics.any {
                it.severity == ContentQualitySeverity.WARNING &&
                    it.code == ContentQualityDiagnosticCode.EXCESSIVE_STEP_COUNT
            },
        )
    }

    @Test
    fun tinyExpectedTargetIsWarningNotError() {
        val production = productionCatalog()
        val baseEntry = production.entries.first { AgeBand.LITTLE_ARTISTS in it.ageBands }
        val baseRuntime = checkNotNull(production.runtimePackage(baseEntry.identity))
        val template = baseRuntime.lesson.drawing.steps.first()
        val tinyStroke = AuthoredStroke(
            id = "tiny-target-fixture",
            points = listOf(
                AuthoredPoint(x = 100f, y = 100f, timeMs = 0L),
                AuthoredPoint(x = 105f, y = 105f, timeMs = 100L),
            ),
        )
        val tinyStep = template.copy(
            id = "tiny-fixture-step",
            childTurn = template.childTurn.copy(expectedStrokeRefs = listOf(tinyStroke.id)),
        )
        val lesson = baseRuntime.lesson.copy(
            metadata = baseRuntime.lesson.metadata.copy(ageBands = listOf(AgeBand.LITTLE_ARTISTS)),
            drawing = baseRuntime.lesson.drawing.copy(steps = listOf(tinyStep)),
        )
        val runtime = baseRuntime.copy(
            lesson = lesson,
            strokeCatalog = baseRuntime.strokeCatalog.copy(
                strokes = baseRuntime.strokeCatalog.strokes + tinyStroke,
            ),
        )
        val entry = baseEntry.copy(ageBands = setOf(AgeBand.LITTLE_ARTISTS))
        val snapshot = LessonCatalogSnapshot(
            entries = listOf(entry),
            diagnostics = emptyList(),
            runtimePackages = mapOf(entry.identity to runtime),
        )

        val report = ContentQualityAnalyzer().analyze(snapshot)

        assertEquals(0, report.errorCount)
        assertTrue(
            report.diagnostics.any {
                it.severity == ContentQualitySeverity.WARNING &&
                    it.code == ContentQualityDiagnosticCode.TINY_EXPECTED_TARGET
            },
        )
    }

    @Test
    fun tinyTeacherDemoIsReportedAsAuthoringWarning() {
        val production = productionCatalog()
        val baseEntry = production.entries.first { AgeBand.LITTLE_ARTISTS in it.ageBands }
        val baseRuntime = checkNotNull(production.runtimePackage(baseEntry.identity))
        val template = baseRuntime.lesson.drawing.steps.first()
        val tinyStroke = AuthoredStroke(
            id = "tiny-teacher-fixture",
            points = listOf(
                AuthoredPoint(x = 200f, y = 200f, timeMs = 0L),
                AuthoredPoint(x = 203f, y = 204f, timeMs = 100L),
            ),
        )
        val step = template.copy(
            id = "tiny-teacher-step",
            teacher = template.teacher.copy(strokeRefs = listOf(tinyStroke.id)),
        )
        val lesson = baseRuntime.lesson.copy(
            metadata = baseRuntime.lesson.metadata.copy(ageBands = listOf(AgeBand.LITTLE_ARTISTS)),
            drawing = baseRuntime.lesson.drawing.copy(steps = listOf(step)),
        )
        val runtime = baseRuntime.copy(
            lesson = lesson,
            strokeCatalog = baseRuntime.strokeCatalog.copy(
                strokes = baseRuntime.strokeCatalog.strokes + tinyStroke,
            ),
        )
        val entry = baseEntry.copy(ageBands = setOf(AgeBand.LITTLE_ARTISTS))
        val report = ContentQualityAnalyzer().analyze(singleLessonSnapshot(entry, runtime))

        assertEquals(0, report.errorCount)
        assertTrue(report.diagnostics.any { it.code == ContentQualityDiagnosticCode.TINY_TEACHER_DEMO })
    }

    @Test
    fun duplicateRefsAndSingleStrokeGroupAreReportedAsWarnings() {
        val production = productionCatalog()
        val baseEntry = production.entries.first()
        val baseRuntime = checkNotNull(production.runtimePackage(baseEntry.identity))
        val template = baseRuntime.lesson.drawing.steps.first()
        val ref = template.teacher.strokeRefs.first()
        val step = template.copy(
            id = "duplicate-ref-step",
            teacher = template.teacher.copy(
                strokeRefs = listOf(ref, ref),
                playAsGroup = true,
            ),
            childTurn = template.childTurn.copy(expectedStrokeRefs = listOf(ref, ref)),
        )
        val runtime = baseRuntime.copy(
            lesson = baseRuntime.lesson.copy(
                drawing = baseRuntime.lesson.drawing.copy(steps = listOf(step)),
            ),
        )

        val report = ContentQualityAnalyzer().analyze(singleLessonSnapshot(baseEntry, runtime))

        assertEquals(0, report.errorCount)
        assertTrue(report.diagnostics.any { it.code == ContentQualityDiagnosticCode.DUPLICATE_TEACHER_STROKE_REFERENCE })
        assertTrue(report.diagnostics.any { it.code == ContentQualityDiagnosticCode.DUPLICATE_EXPECTED_STROKE_REFERENCE })
        assertTrue(report.diagnostics.any { it.code == ContentQualityDiagnosticCode.GROUPED_DEMO_SINGLE_STROKE })
    }

    @Test
    fun nonContiguousHelpLadderIsReportedAsWarning() {
        val production = productionCatalog()
        val baseEntry = production.entries.first()
        val baseRuntime = checkNotNull(production.runtimePackage(baseEntry.identity))
        val template = baseRuntime.lesson.drawing.steps.first()
        val step = template.copy(
            id = "help-gap-step",
            help = listOf(
                HelpEntry(level = 1, kind = HelpKind.GENTLE_HINT),
                HelpEntry(level = 3, kind = HelpKind.DIRECTION_ANCHORS),
            ),
        )
        val runtime = baseRuntime.copy(
            lesson = baseRuntime.lesson.copy(
                drawing = baseRuntime.lesson.drawing.copy(steps = listOf(step)),
            ),
        )

        val report = ContentQualityAnalyzer().analyze(singleLessonSnapshot(baseEntry, runtime))

        assertEquals(0, report.errorCount)
        assertTrue(report.diagnostics.any { it.code == ContentQualityDiagnosticCode.NON_CONTIGUOUS_HELP_LADDER })
    }

    @Test
    fun tinyPreparedColorRegionIsReportedAsWarning() {
        val production = productionCatalog()
        val baseEntry = production.entries.single { it.identity.lessonId == "little-fish" }
        val baseRuntime = checkNotNull(production.runtimePackage(baseEntry.identity))
        val catalog = checkNotNull(baseRuntime.coloringRegionCatalog)
        val firstRegion = catalog.regions.first()
        val tinyRegion = firstRegion.copy(
            points = listOf(
                AuthoredRegionPoint(100f, 100f),
                AuthoredRegionPoint(102f, 100f),
                AuthoredRegionPoint(100f, 102f),
            ),
        )
        val runtime = baseRuntime.copy(
            coloringRegionCatalog = catalog.copy(
                regions = listOf(tinyRegion) + catalog.regions.drop(1),
            ),
        )

        val report = ContentQualityAnalyzer().analyze(singleLessonSnapshot(baseEntry, runtime))

        assertEquals(0, report.errorCount)
        assertTrue(report.diagnostics.any { it.code == ContentQualityDiagnosticCode.TINY_PREPARED_REGION })
    }

    @Test
    fun preparedRegionCoverageIsReportedFromValidatedRuntimePackages() {
        val snapshot = productionCatalog()
        val report = ContentQualityAnalyzer().analyze(snapshot)
        val littleFish = report.lessons.single { it.identity.lessonId == "little-fish" }
        val hotAirBalloon = report.lessons.single { it.identity.lessonId == "hot-air-balloon" }

        assertTrue(littleFish.coloringEnabled)
        assertTrue(littleFish.preparedRegionCount > 0)
        assertTrue(hotAirBalloon.coloringEnabled)
        assertTrue(hotAirBalloon.preparedRegionCount > 0)
        assertTrue(report.preparedColoringLessonCount >= 2)
    }

    @Test
    fun noJourneyWarningCanBeDisabledForLegitimateStandaloneContent() {
        val production = productionCatalog()
        val baseEntry = production.entries.first()
        val baseRuntime = checkNotNull(production.runtimePackage(baseEntry.identity))
        val standaloneLesson = baseRuntime.lesson.copy(
            metadata = baseRuntime.lesson.metadata.copy(journeyIds = emptyList()),
        )
        val standaloneEntry = baseEntry.copy(journeyIds = emptySet())
        val snapshot = LessonCatalogSnapshot(
            entries = listOf(standaloneEntry),
            diagnostics = emptyList(),
            runtimePackages = mapOf(
                standaloneEntry.identity to baseRuntime.copy(lesson = standaloneLesson),
            ),
        )

        val withWarning = ContentQualityAnalyzer().analyze(snapshot)
        val withoutWarning = ContentQualityAnalyzer(
            policy = ContentQualityPolicy(warnOnNoJourneyMembership = false),
        ).analyze(snapshot)

        assertTrue(withWarning.diagnostics.any { it.code == ContentQualityDiagnosticCode.NO_JOURNEY_MEMBERSHIP })
        assertFalse(withoutWarning.diagnostics.any { it.code == ContentQualityDiagnosticCode.NO_JOURNEY_MEMBERSHIP })
    }

    private fun singleLessonSnapshot(
        entry: LessonCatalogEntry,
        runtime: com.navin.kidsdrawing.lesson.model.LessonRuntimePackage,
    ): LessonCatalogSnapshot = LessonCatalogSnapshot(
        entries = listOf(entry),
        diagnostics = emptyList(),
        runtimePackages = mapOf(entry.identity to runtime),
    )

    private fun productionCatalog(): LessonCatalogSnapshot =
        LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
