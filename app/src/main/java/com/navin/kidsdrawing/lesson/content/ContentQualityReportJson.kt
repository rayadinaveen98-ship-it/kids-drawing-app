package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.TeachingMode
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Deterministic machine-readable projection of [CatalogCoverageReport] for CI and tooling. */
fun CatalogCoverageReport.renderJson(): String = JsonObject(
    linkedMapOf(
        "lessonCount" to JsonPrimitive(lessonCount),
        "errorCount" to JsonPrimitive(errorCount),
        "warningCount" to JsonPrimitive(warningCount),
        "ageBandCounts" to ageBandCounts.enumCountObject(AgeBand.entries),
        "difficultyCounts" to JsonObject(
            linkedMapOf<String, JsonElement>().apply {
                difficultyCounts.forEach { (key, value) -> put(key.toString(), JsonPrimitive(value)) }
            },
        ),
        "categoryCounts" to categoryCounts.stringCountObject(),
        "skillCounts" to skillCounts.stringCountObject(),
        "journeyCounts" to journeyCounts.stringCountObject(),
        "modeCounts" to modeCounts.enumCountObject(TeachingMode.entries),
        "coloringLessonCount" to JsonPrimitive(coloringLessonCount),
        "preparedColoringLessonCount" to JsonPrimitive(preparedColoringLessonCount),
        "phase5Progress" to phase5Progress.toJson(),
        "lessons" to JsonArray(lessons.map { it.toJson() }),
        "diagnostics" to JsonArray(diagnostics.map { it.toJson() }),
    ),
).toString()

private fun Phase5CoverageProgress.toJson(): JsonObject = JsonObject(
    linkedMapOf(
        "lessonCount" to JsonPrimitive(lessonCount),
        "lessonTarget" to JsonPrimitive(lessonTarget),
        "lessonTargetMet" to JsonPrimitive(lessonTargetMet),
        "ageBandCounts" to ageBandCounts.enumCountObject(AgeBand.entries),
        "ageBandTargets" to ageBandTargets.enumCountObject(AgeBand.entries),
        "ageBandTargetsMet" to JsonPrimitive(ageBandTargetsMet),
        "difficultyFourCount" to JsonPrimitive(difficultyFourCount),
        "difficultyFourTarget" to JsonPrimitive(difficultyFourTarget),
        "difficultyFiveCount" to JsonPrimitive(difficultyFiveCount),
        "difficultyFiveTarget" to JsonPrimitive(difficultyFiveTarget),
        "difficultyTargetsMet" to JsonPrimitive(difficultyTargetsMet),
        "watchThenDrawCount" to JsonPrimitive(watchThenDrawCount),
        "watchThenDrawTarget" to JsonPrimitive(watchThenDrawTarget),
        "watchThenDrawTargetMet" to JsonPrimitive(watchThenDrawTargetMet),
    ),
)

private fun LessonContentSummary.toJson(): JsonObject = JsonObject(
    linkedMapOf(
        "lessonId" to JsonPrimitive(identity.lessonId),
        "revision" to JsonPrimitive(identity.revision),
        "title" to JsonPrimitive(title),
        "ageBands" to JsonArray(ageBands.map { JsonPrimitive(it.name) }),
        "difficulty" to JsonPrimitive(difficulty),
        "categoryIds" to JsonArray(categoryIds.map { JsonPrimitive(it) }),
        "skillIds" to JsonArray(skillIds.map { JsonPrimitive(it) }),
        "journeyIds" to JsonArray(journeyIds.map { JsonPrimitive(it) }),
        "supportedModes" to JsonArray(supportedModes.map { JsonPrimitive(it.name) }),
        "drawingStepCount" to JsonPrimitive(drawingStepCount),
        "coloringEnabled" to JsonPrimitive(coloringEnabled),
        "preparedRegionCount" to JsonPrimitive(preparedRegionCount),
        "errorCount" to JsonPrimitive(errorCount),
        "warningCount" to JsonPrimitive(warningCount),
        "diagnostics" to JsonArray(diagnostics.map { it.toJson() }),
    ),
)

private fun ContentQualityDiagnostic.toJson(): JsonObject = JsonObject(
    linkedMapOf<String, JsonElement>().apply {
        put("severity", JsonPrimitive(severity.name))
        put("code", JsonPrimitive(code.name))
        lessonId?.let { put("lessonId", JsonPrimitive(it)) }
        packageRoot?.let { put("packageRoot", JsonPrimitive(it)) }
        put("message", JsonPrimitive(message))
    },
)

private fun Map<String, Int>.stringCountObject(): JsonObject = JsonObject(
    linkedMapOf<String, JsonElement>().apply {
        this@stringCountObject.forEach { (key, value) -> put(key, JsonPrimitive(value)) }
    },
)

private fun <T : Enum<T>> Map<T, Int>.enumCountObject(order: List<T>): JsonObject = JsonObject(
    linkedMapOf<String, JsonElement>().apply {
        order.forEach { key -> put(key.name, JsonPrimitive(this@enumCountObject[key] ?: 0)) }
    },
)
