package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.AuthoredColorRegion
import com.navin.kidsdrawing.lesson.model.AuthoredStroke
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.TeachingMode
import kotlin.math.abs
import kotlin.math.max

enum class ContentQualitySeverity {
    ERROR,
    WARNING,
}

enum class ContentQualityDiagnosticCode {
    CATALOG_DIAGNOSTIC,
    MISSING_RUNTIME_PACKAGE,
    EXCESSIVE_STEP_COUNT,
    TINY_EXPECTED_TARGET,
    TINY_TEACHER_DEMO,
    DUPLICATE_TEACHER_STROKE_REFERENCE,
    DUPLICATE_EXPECTED_STROKE_REFERENCE,
    NON_MONOTONIC_HELP_LADDER,
    GROUPED_DEMO_SINGLE_STROKE,
    TINY_PREPARED_REGION,
    NO_JOURNEY_MEMBERSHIP,
}

data class ContentQualityDiagnostic(
    val severity: ContentQualitySeverity,
    val code: ContentQualityDiagnosticCode,
    val lessonId: String? = null,
    val packageRoot: String? = null,
    val message: String,
)

data class LessonContentSummary(
    val identity: LessonCatalogIdentity,
    val title: String,
    val ageBands: Set<AgeBand>,
    val difficulty: Int,
    val categoryIds: Set<String>,
    val skillIds: Set<String>,
    val journeyIds: Set<String>,
    val supportedModes: Set<TeachingMode>,
    val drawingStepCount: Int,
    val coloringEnabled: Boolean,
    val preparedRegionCount: Int,
    val diagnostics: List<ContentQualityDiagnostic>,
) {
    val errorCount: Int
        get() = diagnostics.count { it.severity == ContentQualitySeverity.ERROR }

    val warningCount: Int
        get() = diagnostics.count { it.severity == ContentQualitySeverity.WARNING }
}

data class Phase5CoverageTargets(
    val lessonCount: Int = 24,
    val ageBandMinimums: Map<AgeBand, Int> = linkedMapOf(
        AgeBand.LITTLE_ARTISTS to 8,
        AgeBand.CREATIVE_EXPLORERS to 14,
        AgeBand.GROWING_ARTISTS to 14,
        AgeBand.YOUNG_ARTISTS to 10,
    ),
    val difficultyFourMinimum: Int = 3,
    val difficultyFiveMinimum: Int = 1,
    val watchThenDrawMinimum: Int = 6,
)

data class Phase5CoverageProgress(
    val lessonCount: Int,
    val lessonTarget: Int,
    val ageBandCounts: Map<AgeBand, Int>,
    val ageBandTargets: Map<AgeBand, Int>,
    val difficultyFourCount: Int,
    val difficultyFourTarget: Int,
    val difficultyFiveCount: Int,
    val difficultyFiveTarget: Int,
    val watchThenDrawCount: Int,
    val watchThenDrawTarget: Int,
) {
    val lessonTargetMet: Boolean
        get() = lessonCount >= lessonTarget

    val ageBandTargetsMet: Boolean
        get() = ageBandTargets.all { (ageBand, target) -> (ageBandCounts[ageBand] ?: 0) >= target }

    val difficultyTargetsMet: Boolean
        get() = difficultyFourCount >= difficultyFourTarget && difficultyFiveCount >= difficultyFiveTarget

    val watchThenDrawTargetMet: Boolean
        get() = watchThenDrawCount >= watchThenDrawTarget
}

data class CatalogCoverageReport(
    val lessonCount: Int,
    val ageBandCounts: Map<AgeBand, Int>,
    val difficultyCounts: Map<Int, Int>,
    val categoryCounts: Map<String, Int>,
    val skillCounts: Map<String, Int>,
    val journeyCounts: Map<String, Int>,
    val modeCounts: Map<TeachingMode, Int>,
    val coloringLessonCount: Int,
    val preparedColoringLessonCount: Int,
    val lessons: List<LessonContentSummary>,
    val diagnostics: List<ContentQualityDiagnostic>,
    val phase5Progress: Phase5CoverageProgress,
) {
    val errorCount: Int
        get() = diagnostics.count { it.severity == ContentQualitySeverity.ERROR }

    val warningCount: Int
        get() = diagnostics.count { it.severity == ContentQualitySeverity.WARNING }

    fun renderText(): String = buildString {
        appendLine("Content quality report")
        appendLine("lessons=$lessonCount errors=$errorCount warnings=$warningCount")
        appendLine("ageBands=${ageBandCounts.renderEnumCounts()}")
        appendLine("difficulty=${difficultyCounts.entries.joinToString(",") { "${it.key}:${it.value}" }}")
        appendLine("categories=${categoryCounts.renderStringCounts()}")
        appendLine("skills=${skillCounts.renderStringCounts()}")
        appendLine("journeys=${journeyCounts.renderStringCounts()}")
        appendLine("modes=${modeCounts.renderEnumCounts()}")
        appendLine("coloring=$coloringLessonCount preparedColoring=$preparedColoringLessonCount")
        appendLine(
            "phase5=" +
                "lessons:${phase5Progress.lessonCount}/${phase5Progress.lessonTarget} " +
                "difficulty4:${phase5Progress.difficultyFourCount}/${phase5Progress.difficultyFourTarget} " +
                "difficulty5:${phase5Progress.difficultyFiveCount}/${phase5Progress.difficultyFiveTarget} " +
                "watchThenDraw:${phase5Progress.watchThenDrawCount}/${phase5Progress.watchThenDrawTarget}",
        )
        appendLine(
            "phase5AgeBands=" + AgeBand.entries.joinToString(",") { ageBand ->
                "${ageBand.name}:${phase5Progress.ageBandCounts[ageBand] ?: 0}/${phase5Progress.ageBandTargets[ageBand] ?: 0}"
            },
        )
        lessons.forEach { lesson ->
            appendLine(
                "lesson=${lesson.identity.lessonId}@${lesson.identity.revision} " +
                    "difficulty=${lesson.difficulty} steps=${lesson.drawingStepCount} " +
                    "coloring=${lesson.coloringEnabled} regions=${lesson.preparedRegionCount} " +
                    "errors=${lesson.errorCount} warnings=${lesson.warningCount}",
            )
        }
        diagnostics.forEach { diagnostic ->
            appendLine(
                "${diagnostic.severity}:${diagnostic.code}:" +
                    "${diagnostic.lessonId ?: "catalog"}:${diagnostic.message}",
            )
        }
    }.trimEnd()

    private fun <T : Enum<T>> Map<T, Int>.renderEnumCounts(): String =
        entries.joinToString(",") { "${it.key.name}:${it.value}" }

    private fun Map<String, Int>.renderStringCounts(): String =
        entries.joinToString(",") { "${it.key}:${it.value}" }
}

data class ContentQualityPolicy(
    val warnOnNoJourneyMembership: Boolean = true,
    val tinyTargetThresholdByYoungestAge: Map<AgeBand, Float> = linkedMapOf(
        AgeBand.LITTLE_ARTISTS to 0.04f,
        AgeBand.CREATIVE_EXPLORERS to 0.03f,
        AgeBand.GROWING_ARTISTS to 0.02f,
        AgeBand.YOUNG_ARTISTS to 0.015f,
    ),
    val tinyTeacherDemoThresholdByYoungestAge: Map<AgeBand, Float> = linkedMapOf(
        AgeBand.LITTLE_ARTISTS to 0.03f,
        AgeBand.CREATIVE_EXPLORERS to 0.025f,
        AgeBand.GROWING_ARTISTS to 0.018f,
        AgeBand.YOUNG_ARTISTS to 0.012f,
    ),
    val tinyPreparedRegionAreaFractionByYoungestAge: Map<AgeBand, Float> = linkedMapOf(
        AgeBand.LITTLE_ARTISTS to 0.003f,
        AgeBand.CREATIVE_EXPLORERS to 0.0025f,
        AgeBand.GROWING_ARTISTS to 0.002f,
        AgeBand.YOUNG_ARTISTS to 0.0015f,
    ),
    val maximumStepCountByYoungestAge: Map<AgeBand, Int> = linkedMapOf(
        AgeBand.LITTLE_ARTISTS to 7,
        AgeBand.CREATIVE_EXPLORERS to 9,
        AgeBand.GROWING_ARTISTS to 12,
        AgeBand.YOUNG_ARTISTS to 14,
    ),
)

/**
 * Read-only quality projection over the same validated catalog/runtime packages the product uses.
 *
 * The analyzer deliberately does not decode lesson JSON, resolve assets, or reimplement package
 * semantics. Production [LessonCatalog] / [LessonPackageLoader] diagnostics are projected as
 * release-blocking errors; additional heuristics in this class are conservative authoring warnings.
 */
class ContentQualityAnalyzer(
    private val policy: ContentQualityPolicy = ContentQualityPolicy(),
    private val phase5Targets: Phase5CoverageTargets = Phase5CoverageTargets(),
) {
    fun analyze(snapshot: LessonCatalogSnapshot): CatalogCoverageReport {
        val catalogDiagnostics = snapshot.diagnostics.map { diagnostic ->
            ContentQualityDiagnostic(
                severity = ContentQualitySeverity.ERROR,
                code = ContentQualityDiagnosticCode.CATALOG_DIAGNOSTIC,
                packageRoot = diagnostic.packageRoot,
                message = "${diagnostic.code}: ${diagnostic.message}",
            )
        }

        val lessonSummaries = snapshot.entries.map { entry ->
            val runtime = snapshot.runtimePackage(entry.identity)
            if (runtime == null) {
                LessonContentSummary(
                    identity = entry.identity,
                    title = entry.title,
                    ageBands = entry.ageBands.sortedEnumSet(),
                    difficulty = entry.difficulty,
                    categoryIds = entry.categoryIds.toSortedSet(),
                    skillIds = entry.skillIds.toSortedSet(),
                    journeyIds = entry.journeyIds.toSortedSet(),
                    supportedModes = entry.supportedModes.sortedEnumSet(),
                    drawingStepCount = 0,
                    coloringEnabled = false,
                    preparedRegionCount = 0,
                    diagnostics = listOf(
                        ContentQualityDiagnostic(
                            severity = ContentQualitySeverity.ERROR,
                            code = ContentQualityDiagnosticCode.MISSING_RUNTIME_PACKAGE,
                            lessonId = entry.identity.lessonId,
                            packageRoot = entry.packageRoot,
                            message = "Accepted catalog entry has no validated runtime package.",
                        ),
                    ),
                )
            } else {
                summarize(entry, runtime)
            }
        }

        val allDiagnostics = (catalogDiagnostics + lessonSummaries.flatMap { it.diagnostics })
            .sortedWith(DIAGNOSTIC_ORDER)

        val ageBandCounts = linkedMapOf<AgeBand, Int>().apply {
            AgeBand.entries.forEach { ageBand -> put(ageBand, snapshot.entries.count { ageBand in it.ageBands }) }
        }
        val difficultyCounts = linkedMapOf<Int, Int>().apply {
            (1..5).forEach { difficulty -> put(difficulty, snapshot.entries.count { it.difficulty == difficulty }) }
        }
        val categoryCounts = snapshot.entries
            .flatMap { it.categoryIds }
            .groupingBy { it }
            .eachCount()
            .toSortedMap()
        val skillCounts = snapshot.entries
            .flatMap { it.skillIds }
            .groupingBy { it }
            .eachCount()
            .toSortedMap()
        val journeyCounts = snapshot.entries
            .flatMap { it.journeyIds }
            .groupingBy { it }
            .eachCount()
            .toSortedMap()
        val modeCounts = linkedMapOf<TeachingMode, Int>().apply {
            TeachingMode.entries.forEach { mode -> put(mode, snapshot.entries.count { mode in it.supportedModes }) }
        }

        val coloringLessonCount = lessonSummaries.count { it.coloringEnabled }
        val preparedColoringLessonCount = lessonSummaries.count { it.preparedRegionCount > 0 }
        val phase5Progress = Phase5CoverageProgress(
            lessonCount = snapshot.entries.size,
            lessonTarget = phase5Targets.lessonCount,
            ageBandCounts = ageBandCounts,
            ageBandTargets = linkedMapOf<AgeBand, Int>().apply {
                AgeBand.entries.forEach { ageBand -> put(ageBand, phase5Targets.ageBandMinimums[ageBand] ?: 0) }
            },
            difficultyFourCount = difficultyCounts[4] ?: 0,
            difficultyFourTarget = phase5Targets.difficultyFourMinimum,
            difficultyFiveCount = difficultyCounts[5] ?: 0,
            difficultyFiveTarget = phase5Targets.difficultyFiveMinimum,
            watchThenDrawCount = modeCounts[TeachingMode.WATCH_THEN_DRAW] ?: 0,
            watchThenDrawTarget = phase5Targets.watchThenDrawMinimum,
        )

        return CatalogCoverageReport(
            lessonCount = snapshot.entries.size,
            ageBandCounts = ageBandCounts,
            difficultyCounts = difficultyCounts,
            categoryCounts = categoryCounts,
            skillCounts = skillCounts,
            journeyCounts = journeyCounts,
            modeCounts = modeCounts,
            coloringLessonCount = coloringLessonCount,
            preparedColoringLessonCount = preparedColoringLessonCount,
            lessons = lessonSummaries.sortedWith(
                compareBy<LessonContentSummary>({ it.identity.lessonId }, { it.identity.revision }),
            ),
            diagnostics = allDiagnostics,
            phase5Progress = phase5Progress,
        )
    }

    private fun summarize(
        entry: LessonCatalogEntry,
        runtime: LessonRuntimePackage,
    ): LessonContentSummary {
        val diagnostics = mutableListOf<ContentQualityDiagnostic>()
        val youngestAge = youngestAge(entry.ageBands)

        val maxSteps = youngestAge?.let { policy.maximumStepCountByYoungestAge[it] }
        if (maxSteps != null && runtime.lesson.drawing.steps.size > maxSteps) {
            diagnostics += warning(
                ContentQualityDiagnosticCode.EXCESSIVE_STEP_COUNT,
                entry,
                "${runtime.lesson.drawing.steps.size} drawing steps exceed the Phase-5 authoring guideline of $maxSteps for youngest supported band ${youngestAge.name}.",
            )
        }

        if (policy.warnOnNoJourneyMembership && entry.journeyIds.isEmpty()) {
            diagnostics += warning(
                ContentQualityDiagnosticCode.NO_JOURNEY_MEMBERSHIP,
                entry,
                "Release lesson is not currently assigned to an Art Journey; standalone lessons are allowed but require author review.",
            )
        }

        val tinyTargetThreshold = youngestAge?.let { policy.tinyTargetThresholdByYoungestAge[it] }
        val tinyTeacherThreshold = youngestAge?.let { policy.tinyTeacherDemoThresholdByYoungestAge[it] }
        runtime.lesson.drawing.steps.forEach { step ->
            val expected = step.childTurn.expectedStrokeRefs
            if (tinyTargetThreshold != null && expected.isNotEmpty()) {
                val extent = normalizedStrokeExtent(runtime, expected)
                if (extent != null && extent < tinyTargetThreshold) {
                    diagnostics += warning(
                        ContentQualityDiagnosticCode.TINY_EXPECTED_TARGET,
                        entry,
                        "Step '${step.id}' expected geometry occupies only ${(extent * 100f).formatOneDecimal()}% of the larger canvas axis; review target size for ${youngestAge?.name}.",
                    )
                }
            }

            val teacherRefs = step.teacher.strokeRefs
            if (tinyTeacherThreshold != null && teacherRefs.isNotEmpty()) {
                val extent = normalizedStrokeExtent(runtime, teacherRefs)
                if (extent != null && extent < tinyTeacherThreshold) {
                    diagnostics += warning(
                        ContentQualityDiagnosticCode.TINY_TEACHER_DEMO,
                        entry,
                        "Step '${step.id}' teacher demonstration occupies only ${(extent * 100f).formatOneDecimal()}% of the larger canvas axis; review visibility for ${youngestAge?.name}.",
                    )
                }
            }

            if (teacherRefs.size != teacherRefs.distinct().size) {
                diagnostics += warning(
                    ContentQualityDiagnosticCode.DUPLICATE_TEACHER_STROKE_REFERENCE,
                    entry,
                    "Step '${step.id}' repeats one or more teacher stroke references; review for accidental duplicate playback.",
                )
            }
            if (expected.size != expected.distinct().size) {
                diagnostics += warning(
                    ContentQualityDiagnosticCode.DUPLICATE_EXPECTED_STROKE_REFERENCE,
                    entry,
                    "Step '${step.id}' repeats one or more expected child stroke references; review for accidental duplicate target geometry.",
                )
            }
            if (step.teacher.playAsGroup && teacherRefs.distinct().size < 2) {
                diagnostics += warning(
                    ContentQualityDiagnosticCode.GROUPED_DEMO_SINGLE_STROKE,
                    entry,
                    "Step '${step.id}' is marked playAsGroup but contains fewer than two distinct teacher strokes.",
                )
            }

            val helpLevels = step.help.map { it.level }
            if (helpLevels != helpLevels.sorted()) {
                diagnostics += warning(
                    ContentQualityDiagnosticCode.NON_MONOTONIC_HELP_LADDER,
                    entry,
                    "Step '${step.id}' Help Ladder levels are authored out of ascending order: ${helpLevels.joinToString()}.",
                )
            }
        }

        val tinyRegionThreshold = youngestAge?.let { policy.tinyPreparedRegionAreaFractionByYoungestAge[it] }
        if (tinyRegionThreshold != null) {
            runtime.coloringRegionCatalog?.regions.orEmpty().forEach { region ->
                val areaFraction = normalizedRegionArea(runtime, region)
                if (areaFraction < tinyRegionThreshold) {
                    diagnostics += warning(
                        ContentQualityDiagnosticCode.TINY_PREPARED_REGION,
                        entry,
                        "Prepared coloring region '${region.id}' occupies only ${(areaFraction * 100f).formatOneDecimal()}% of canvas area; review tap/fill usability for ${youngestAge?.name}.",
                    )
                }
            }
        }

        return LessonContentSummary(
            identity = entry.identity,
            title = entry.title,
            ageBands = entry.ageBands.sortedEnumSet(),
            difficulty = entry.difficulty,
            categoryIds = entry.categoryIds.toSortedSet(),
            skillIds = entry.skillIds.toSortedSet(),
            journeyIds = entry.journeyIds.toSortedSet(),
            supportedModes = entry.supportedModes.sortedEnumSet(),
            drawingStepCount = runtime.lesson.drawing.steps.size,
            coloringEnabled = runtime.lesson.coloring?.enabled == true,
            preparedRegionCount = runtime.coloringRegionCatalog?.regions?.size ?: 0,
            diagnostics = diagnostics.sortedWith(DIAGNOSTIC_ORDER),
        )
    }

    private fun normalizedStrokeExtent(
        runtime: LessonRuntimePackage,
        strokeRefs: List<String>,
    ): Float? {
        val strokesById = runtime.strokeCatalog.strokes.associateBy(AuthoredStroke::id)
        val points = strokeRefs
            .distinct()
            .mapNotNull(strokesById::get)
            .flatMap { it.points }
        if (points.isEmpty()) return null

        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }
        val width = (maxX - minX).coerceAtLeast(0f)
        val height = (maxY - minY).coerceAtLeast(0f)
        val canvasWidth = runtime.lesson.canvas.width.toFloat().coerceAtLeast(1f)
        val canvasHeight = runtime.lesson.canvas.height.toFloat().coerceAtLeast(1f)
        return max(width / canvasWidth, height / canvasHeight)
    }

    private fun normalizedRegionArea(
        runtime: LessonRuntimePackage,
        region: AuthoredColorRegion,
    ): Float {
        if (region.points.size < 3) return 0f
        var twiceArea = 0.0
        region.points.indices.forEach { index ->
            val current = region.points[index]
            val next = region.points[(index + 1) % region.points.size]
            twiceArea += current.x.toDouble() * next.y.toDouble() - next.x.toDouble() * current.y.toDouble()
        }
        val area = abs(twiceArea) / 2.0
        val canvasArea = runtime.lesson.canvas.width.toDouble().coerceAtLeast(1.0) *
            runtime.lesson.canvas.height.toDouble().coerceAtLeast(1.0)
        return (area / canvasArea).toFloat()
    }

    private fun youngestAge(ageBands: Set<AgeBand>): AgeBand? =
        AgeBand.entries.firstOrNull { it in ageBands }

    private fun warning(
        code: ContentQualityDiagnosticCode,
        entry: LessonCatalogEntry,
        message: String,
    ) = ContentQualityDiagnostic(
        severity = ContentQualitySeverity.WARNING,
        code = code,
        lessonId = entry.identity.lessonId,
        packageRoot = entry.packageRoot,
        message = message,
    )

    private fun Float.formatOneDecimal(): String = String.format(java.util.Locale.ROOT, "%.1f", this)

    private fun <T : Enum<T>> Set<T>.sortedEnumSet(): Set<T> =
        sortedBy { it.ordinal }.toCollection(linkedSetOf())

    private companion object {
        val DIAGNOSTIC_ORDER = compareBy<ContentQualityDiagnostic>(
            { it.severity.ordinal },
            { it.code.name },
            { it.lessonId ?: "" },
            { it.packageRoot ?: "" },
            { it.message },
        )
    }
}
