package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AuthoredStroke
import com.navin.kidsdrawing.lesson.model.ColoringRegionCatalogSource
import com.navin.kidsdrawing.lesson.model.HelpKind
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.LessonSource
import com.navin.kidsdrawing.lesson.model.StrokeCatalogSource
import com.navin.kidsdrawing.lesson.model.TeachingMode
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

fun interface LessonPackageSource {
    fun readText(path: String): String?
}

enum class LessonDiagnosticCode {
    MISSING_LESSON_FILE,
    MISSING_ASSET,
    INVALID_JSON,
    UNSUPPORTED_SCHEMA_VERSION,
    UNSUPPORTED_CONTENT_API,
    INVALID_ID,
    DUPLICATE_ID,
    INVALID_VALUE,
    MISSING_REFERENCE,
    INVALID_TRACE_SUPPORT,
    UNSAFE_ASSET_PATH,
}

data class LessonDiagnostic(
    val code: LessonDiagnosticCode,
    val path: String,
    val message: String,
)

sealed interface LessonLoadResult {
    data class Success(val packageData: LessonRuntimePackage) : LessonLoadResult
    data class Failure(val diagnostics: List<LessonDiagnostic>) : LessonLoadResult
}

private sealed interface DecodeResult<out T> {
    data class Value<T>(val value: T) : DecodeResult<T>
    data class Failure(val result: LessonLoadResult.Failure) : DecodeResult<Nothing>
}

class LessonPackageLoader(
    private val source: LessonPackageSource,
    private val supportedContentApi: Int = CURRENT_CONTENT_API,
    private val json: Json = DEFAULT_JSON,
) {
    fun load(packageRoot: String): LessonLoadResult {
        val normalizedRoot = packageRoot.trim('/').takeIf { it.isNotBlank() }
            ?: return LessonLoadResult.Failure(
                listOf(
                    LessonDiagnostic(
                        LessonDiagnosticCode.INVALID_VALUE,
                        "packageRoot",
                        "Package root must not be blank.",
                    ),
                ),
            )

        val lessonPath = "$normalizedRoot/lesson.json"
        val lessonText = source.readText(lessonPath)
            ?: return LessonLoadResult.Failure(
                listOf(
                    LessonDiagnostic(
                        LessonDiagnosticCode.MISSING_LESSON_FILE,
                        lessonPath,
                        "lesson.json is missing from the lesson package.",
                    ),
                ),
            )

        val lesson = when (val decoded = decodeLesson(lessonPath, lessonText)) {
            is DecodeResult.Value -> decoded.value
            is DecodeResult.Failure -> return decoded.result
        }
        val earlyDiagnostics = LessonPackageValidator.validateLessonHeader(lesson, supportedContentApi)
        if (earlyDiagnostics.isNotEmpty()) return LessonLoadResult.Failure(earlyDiagnostics)

        val strokePath = safeJoin(normalizedRoot, lesson.assets.strokeFile)
            ?: return LessonLoadResult.Failure(
                listOf(
                    LessonDiagnostic(
                        LessonDiagnosticCode.UNSAFE_ASSET_PATH,
                        "assets.strokeFile",
                        "Stroke asset path is absolute or traverses outside the package.",
                    ),
                ),
            )
        val strokeText = source.readText(strokePath)
            ?: return LessonLoadResult.Failure(
                listOf(
                    LessonDiagnostic(
                        LessonDiagnosticCode.MISSING_ASSET,
                        strokePath,
                        "Declared stroke catalog is missing.",
                    ),
                ),
            )
        val strokeCatalog = when (val decoded = decodeStrokeCatalog(strokePath, strokeText)) {
            is DecodeResult.Value -> decoded.value
            is DecodeResult.Failure -> return decoded.result
        }

        val coloringRegionCatalog = lesson.assets.coloringRegions?.let { relativePath ->
            val regionPath = safeJoin(normalizedRoot, relativePath)
                ?: return LessonLoadResult.Failure(
                    listOf(
                        LessonDiagnostic(
                            LessonDiagnosticCode.UNSAFE_ASSET_PATH,
                            "assets.coloringRegions",
                            "Coloring region asset path is absolute or traverses outside the package.",
                        ),
                    ),
                )
            val regionText = source.readText(regionPath)
                ?: return LessonLoadResult.Failure(
                    listOf(
                        LessonDiagnostic(
                            LessonDiagnosticCode.MISSING_ASSET,
                            regionPath,
                            "Declared coloring region catalog is missing.",
                        ),
                    ),
                )
            when (val decoded = decodeColoringRegionCatalog(regionPath, regionText)) {
                is DecodeResult.Value -> decoded.value
                is DecodeResult.Failure -> return decoded.result
            }
        }

        val diagnostics = LessonPackageValidator.validate(
            lesson = lesson,
            catalog = strokeCatalog,
            coloringRegionCatalog = coloringRegionCatalog,
            supportedContentApi = supportedContentApi,
        )
        return if (diagnostics.isEmpty()) {
            LessonLoadResult.Success(
                LessonRuntimePackage(
                    packageRoot = normalizedRoot,
                    lesson = lesson,
                    strokeCatalog = strokeCatalog,
                    coloringRegionCatalog = coloringRegionCatalog,
                ),
            )
        } else {
            LessonLoadResult.Failure(diagnostics)
        }
    }

    private fun decodeLesson(path: String, text: String): DecodeResult<LessonSource> = try {
        DecodeResult.Value(json.decodeFromString<LessonSource>(text))
    } catch (error: SerializationException) {
        decodeFailure(path, error, "Lesson JSON could not be decoded.")
    } catch (error: IllegalArgumentException) {
        decodeFailure(path, error, "Lesson JSON is invalid.")
    }

    private fun decodeStrokeCatalog(path: String, text: String): DecodeResult<StrokeCatalogSource> = try {
        DecodeResult.Value(json.decodeFromString<StrokeCatalogSource>(text))
    } catch (error: SerializationException) {
        decodeFailure(path, error, "Stroke catalog JSON could not be decoded.")
    } catch (error: IllegalArgumentException) {
        decodeFailure(path, error, "Stroke catalog JSON is invalid.")
    }

    private fun decodeColoringRegionCatalog(
        path: String,
        text: String,
    ): DecodeResult<ColoringRegionCatalogSource> = try {
        DecodeResult.Value(json.decodeFromString<ColoringRegionCatalogSource>(text))
    } catch (error: SerializationException) {
        decodeFailure(path, error, "Coloring region catalog JSON could not be decoded.")
    } catch (error: IllegalArgumentException) {
        decodeFailure(path, error, "Coloring region catalog JSON is invalid.")
    }

    private fun decodeFailure(path: String, error: Exception, fallback: String): DecodeResult.Failure =
        DecodeResult.Failure(
            LessonLoadResult.Failure(
                listOf(
                    LessonDiagnostic(
                        LessonDiagnosticCode.INVALID_JSON,
                        path,
                        error.message ?: fallback,
                    ),
                ),
            ),
        )

    companion object {
        const val CURRENT_CONTENT_API = ColoringRegionValidator.PREPARED_REGION_CONTENT_API

        val DEFAULT_JSON = Json {
            ignoreUnknownKeys = false
            isLenient = false
            explicitNulls = false
            coerceInputValues = false
        }
    }
}

object LessonPackageValidator {
    private val lessonIdRegex = Regex("^[a-z0-9]+(?:[.-][a-z0-9]+)*$")
    private val idRegex = Regex("^[a-z0-9]+(?:[._-][a-z0-9]+)*$")
    private val localeRegex = Regex("^[a-z]{2}(?:-[A-Z]{2})?$")

    fun validateLessonHeader(lesson: LessonSource, supportedContentApi: Int): List<LessonDiagnostic> = buildList {
        if (lesson.schemaVersion != "1.0") {
            add(
                LessonDiagnostic(
                    LessonDiagnosticCode.UNSUPPORTED_SCHEMA_VERSION,
                    "schemaVersion",
                    "Unsupported lesson schema ${lesson.schemaVersion}; expected 1.0.",
                ),
            )
        }
        if (lesson.minimumContentApi !in 1..supportedContentApi) {
            add(
                LessonDiagnostic(
                    LessonDiagnosticCode.UNSUPPORTED_CONTENT_API,
                    "minimumContentApi",
                    "Lesson requires content API ${lesson.minimumContentApi}, runtime supports $supportedContentApi.",
                ),
            )
        }
        if (!lessonIdRegex.matches(lesson.lessonId) || lesson.lessonId.length !in 3..120) {
            add(invalidId("lessonId", lesson.lessonId))
        }
        if (lesson.revision < 1) {
            add(invalidValue("revision", "Revision must be at least 1."))
        }
        if (lesson.supportedModes.isEmpty()) {
            add(invalidValue("supportedModes", "At least one teaching mode is required."))
        }
        if (lesson.supportedModes.size != lesson.supportedModes.distinct().size) {
            add(duplicateId("supportedModes", "Teaching modes must be unique."))
        }
        validateSafeAssetPaths(lesson).forEach(::add)
    }

    /** Compatibility overload used by existing tests and non-region package fixtures. */
    fun validate(
        lesson: LessonSource,
        catalog: StrokeCatalogSource,
        supportedContentApi: Int,
    ): List<LessonDiagnostic> = validate(
        lesson = lesson,
        catalog = catalog,
        coloringRegionCatalog = null,
        supportedContentApi = supportedContentApi,
    )

    fun validate(
        lesson: LessonSource,
        catalog: StrokeCatalogSource,
        coloringRegionCatalog: ColoringRegionCatalogSource?,
        supportedContentApi: Int,
    ): List<LessonDiagnostic> = buildList {
        addAll(validateLessonHeader(lesson, supportedContentApi))

        if (catalog.schemaVersion != "1.0") {
            add(
                LessonDiagnostic(
                    LessonDiagnosticCode.UNSUPPORTED_SCHEMA_VERSION,
                    "assets.strokeFile.schemaVersion",
                    "Unsupported stroke catalog schema ${catalog.schemaVersion}; expected 1.0.",
                ),
            )
        }

        validateMetadata(lesson).forEach(::add)
        validateCanvas(lesson).forEach(::add)
        validateStrokeCatalog(lesson, catalog).forEach(::add)
        validateSteps(lesson, catalog).forEach(::add)
        validateColoring(lesson).forEach(::add)
        ColoringRegionValidator.validate(lesson, coloringRegionCatalog).forEach(::add)
    }

    private fun validateMetadata(lesson: LessonSource): List<LessonDiagnostic> = buildList {
        val metadata = lesson.metadata
        validateSingleId("metadata.titleKey", metadata.titleKey)?.let(::add)
        validateSingleId("metadata.summaryKey", metadata.summaryKey)?.let(::add)
        if (metadata.difficulty !in 1..5) add(invalidValue("metadata.difficulty", "Difficulty must be 1..5."))
        if (metadata.estimatedMinutes !in 1..90) {
            add(invalidValue("metadata.estimatedMinutes", "Estimated minutes must be 1..90."))
        }
        if (metadata.ageBands.isEmpty()) add(invalidValue("metadata.ageBands", "At least one age band is required."))
        if (metadata.ageBands.size != metadata.ageBands.distinct().size) add(duplicateId("metadata.ageBands", "Age bands must be unique."))
        if (metadata.categoryIds.isEmpty()) add(invalidValue("metadata.categoryIds", "At least one category is required."))
        if (metadata.skillIds.isEmpty()) add(invalidValue("metadata.skillIds", "At least one skill is required."))
        validateIds("metadata.categoryIds", metadata.categoryIds).forEach(::add)
        validateIds("metadata.skillIds", metadata.skillIds).forEach(::add)
        validateIds("metadata.journeyIds", metadata.journeyIds).forEach(::add)
        validateLessonIds("metadata.prerequisiteLessonIds", metadata.prerequisiteLessonIds).forEach(::add)
        validateIds("metadata.tags", metadata.tags).forEach(::add)
        if (metadata.tags.size > 20) add(invalidValue("metadata.tags", "At most 20 tags are allowed."))
    }

    private fun validateCanvas(lesson: LessonSource): List<LessonDiagnostic> = buildList {
        if (lesson.canvas.width !in 100..10_000) add(invalidValue("canvas.width", "Canvas width must be 100..10000."))
        if (lesson.canvas.height !in 100..10_000) add(invalidValue("canvas.height", "Canvas height must be 100..10000."))
    }

    private fun validateStrokeCatalog(
        lesson: LessonSource,
        catalog: StrokeCatalogSource,
    ): List<LessonDiagnostic> = buildList {
        val strokeIds = catalog.strokes.map(AuthoredStroke::id)
        if (strokeIds.size != strokeIds.distinct().size) add(duplicateId("strokeCatalog.strokes", "Stroke IDs must be unique."))
        validateIds("strokeCatalog.strokes", strokeIds).forEach(::add)

        val guideIds = catalog.guides.map { it.id }
        if (guideIds.size != guideIds.distinct().size) add(duplicateId("strokeCatalog.guides", "Guide IDs must be unique."))
        validateIds("strokeCatalog.guides", guideIds).forEach(::add)

        val strokeIdSet = strokeIds.toSet()
        catalog.guides.forEachIndexed { index, guide ->
            if (guide.strokeRefs.isEmpty()) {
                add(invalidValue("strokeCatalog.guides[$index].strokeRefs", "A guide must reference at least one stroke."))
            }
            guide.strokeRefs.forEach { ref ->
                if (ref !in strokeIdSet) add(missingRef("strokeCatalog.guides[$index].strokeRefs", ref))
            }
        }

        catalog.strokes.forEachIndexed { index, stroke ->
            if (stroke.points.size < 2) add(invalidValue("strokeCatalog.strokes[$index].points", "A stroke needs at least two points."))
            var previousTime = Long.MIN_VALUE
            stroke.points.forEachIndexed { pointIndex, point ->
                if (!point.x.isFinite() || !point.y.isFinite()) {
                    add(invalidValue("strokeCatalog.strokes[$index].points[$pointIndex]", "Point coordinates must be finite."))
                }
                if (point.x !in 0f..lesson.canvas.width.toFloat() || point.y !in 0f..lesson.canvas.height.toFloat()) {
                    add(invalidValue("strokeCatalog.strokes[$index].points[$pointIndex]", "Point must stay inside the authored canvas."))
                }
                if (point.timeMs < previousTime) {
                    add(invalidValue("strokeCatalog.strokes[$index].points[$pointIndex].timeMs", "Point timestamps must be monotonic."))
                }
                if (point.pressure !in 0f..1f) {
                    add(invalidValue("strokeCatalog.strokes[$index].points[$pointIndex].pressure", "Pressure must be 0..1."))
                }
                previousTime = point.timeMs
            }
        }
    }

    private fun validateSteps(
        lesson: LessonSource,
        catalog: StrokeCatalogSource,
    ): List<LessonDiagnostic> = buildList {
        val steps = lesson.drawing.steps
        if (steps.isEmpty()) add(invalidValue("drawing.steps", "At least one drawing step is required."))

        val stepIds = steps.map { it.id }
        if (stepIds.size != stepIds.distinct().size) add(duplicateId("drawing.steps", "Drawing step IDs must be unique."))
        validateIds("drawing.steps", stepIds).forEach(::add)

        val strokeIds = catalog.strokes.map { it.id }.toSet()
        val guideIds = catalog.guides.map { it.id }.toSet()
        steps.forEachIndexed { index, step ->
            val base = "drawing.steps[$index]"
            if (step.objectiveSkillIds.isEmpty()) add(invalidValue("$base.objectiveSkillIds", "At least one objective skill is required."))
            validateIds("$base.objectiveSkillIds", step.objectiveSkillIds).forEach(::add)
            if (step.teacher.strokeRefs.isEmpty()) add(invalidValue("$base.teacher.strokeRefs", "Teacher demonstration needs at least one stroke."))
            step.teacher.strokeRefs.forEach { if (it !in strokeIds) add(missingRef("$base.teacher.strokeRefs", it)) }
            step.teacher.normalDurationMs?.let { duration ->
                if (duration !in 100..120_000) add(invalidValue("$base.teacher.normalDurationMs", "Teacher duration must be 100..120000ms."))
            }
            step.childTurn.toolPreset?.let { validateSingleId("$base.childTurn.toolPreset", it)?.let(::add) }
            step.childTurn.expectedStrokeRefs.forEach { if (it !in strokeIds) add(missingRef("$base.childTurn.expectedStrokeRefs", it)) }

            if (step.help.size > 5) add(invalidValue("$base.help", "A step may contain at most five help entries."))
            val helpLevels = step.help.map { it.level }
            if (helpLevels.size != helpLevels.distinct().size) add(duplicateId("$base.help", "Help levels must be unique within a step."))
            step.help.forEachIndexed { helpIndex, help ->
                if (help.level !in 1..5) add(invalidValue("$base.help[$helpIndex].level", "Help level must be 1..5."))
                help.guideRefs.forEach { if (it !in guideIds) add(missingRef("$base.help[$helpIndex].guideRefs", it)) }
            }

            step.completionNarrationKey?.let { validateSingleId("$base.completionNarrationKey", it)?.let(::add) }
            step.teacher.narrationKey?.let { validateSingleId("$base.teacher.narrationKey", it)?.let(::add) }
            step.help.forEachIndexed { helpIndex, help ->
                help.narrationKey?.let { validateSingleId("$base.help[$helpIndex].narrationKey", it)?.let(::add) }
            }

            if (TeachingMode.TRACE_AND_LEARN in lesson.supportedModes) {
                val hasTraceHelp = step.help.any { it.kind == HelpKind.TRACE_PATH && it.guideRefs.isNotEmpty() }
                val hasExpectedTrace = step.childTurn.expectedStrokeRefs.isNotEmpty()
                if (!hasTraceHelp && !hasExpectedTrace) {
                    add(
                        LessonDiagnostic(
                            LessonDiagnosticCode.INVALID_TRACE_SUPPORT,
                            base,
                            "Trace & Learn requires an authored trace_path guide or expectedStrokeRefs for every drawing step.",
                        ),
                    )
                }
            }
        }
    }

    private fun validateColoring(lesson: LessonSource): List<LessonDiagnostic> = buildList {
        val coloring = lesson.coloring ?: return@buildList
        if (coloring.enabled) {
            if (coloring.defaultMode == null) add(invalidValue("coloring.defaultMode", "Enabled coloring requires a default mode."))
            if (coloring.steps.isEmpty()) add(invalidValue("coloring.steps", "Enabled coloring requires at least one step."))
        }
        val ids = coloring.steps.map { it.id }
        if (ids.size != ids.distinct().size) add(duplicateId("coloring.steps", "Coloring step IDs must be unique."))
        validateIds("coloring.steps", ids).forEach(::add)
        coloring.steps.forEachIndexed { index, step ->
            validateIds("coloring.steps[$index].regionIds", step.regionIds).forEach(::add)
            if (step.regionIds.isNotEmpty() && lesson.assets.coloringRegions == null) {
                add(
                    invalidValue(
                        "coloring.steps[$index].regionIds",
                        "Region IDs require an authored assets.coloringRegions file; freehand coloring must leave regionIds empty.",
                    ),
                )
            }
            validateIds("coloring.steps[$index].suggestedColorRoles", step.suggestedColorRoles).forEach(::add)
            step.narrationKey?.let { validateSingleId("coloring.steps[$index].narrationKey", it)?.let(::add) }
        }
    }

    private fun validateSafeAssetPaths(lesson: LessonSource): List<LessonDiagnostic> = buildList {
        val values = buildList {
            add("assets.strokeFile" to lesson.assets.strokeFile)
            add("assets.thumbnail" to lesson.assets.thumbnail)
            add("assets.preview" to lesson.assets.preview)
            lesson.assets.strings.forEach { (locale, path) ->
                if (!localeRegex.matches(locale)) add("assets.strings.$locale" to "")
                add("assets.strings.$locale" to path)
            }
            lesson.assets.audio.forEach { (locale, path) ->
                if (!localeRegex.matches(locale)) add("assets.audio.$locale" to "")
                add("assets.audio.$locale" to path)
            }
            lesson.assets.coloringRegions?.let { add("assets.coloringRegions" to it) }
        }
        values.forEach { (path, value) ->
            if (value.isBlank() || value.length > 240 || safeJoin("package", value) == null) {
                add(
                    LessonDiagnostic(
                        LessonDiagnosticCode.UNSAFE_ASSET_PATH,
                        path,
                        "Asset path must be a non-empty relative path of at most 240 characters contained within the lesson package.",
                    ),
                )
            }
        }
    }

    private fun validateIds(path: String, ids: List<String>): List<LessonDiagnostic> = buildList {
        ids.forEachIndexed { index, id -> validateSingleId("$path[$index]", id)?.let(::add) }
        if (ids.size != ids.distinct().size) add(duplicateId(path, "IDs must be unique."))
    }

    private fun validateLessonIds(path: String, ids: List<String>): List<LessonDiagnostic> = buildList {
        ids.forEachIndexed { index, id ->
            if (!lessonIdRegex.matches(id) || id.length !in 1..120) add(invalidId("$path[$index]", id))
        }
        if (ids.size != ids.distinct().size) add(duplicateId(path, "Lesson IDs must be unique."))
    }

    private fun validateSingleId(path: String, value: String): LessonDiagnostic? =
        if (!idRegex.matches(value) || value.length !in 1..120) invalidId(path, value) else null

    private fun invalidId(path: String, value: String) = LessonDiagnostic(
        LessonDiagnosticCode.INVALID_ID,
        path,
        "Invalid identifier: $value",
    )

    private fun duplicateId(path: String, message: String) = LessonDiagnostic(
        LessonDiagnosticCode.DUPLICATE_ID,
        path,
        message,
    )

    private fun invalidValue(path: String, message: String) = LessonDiagnostic(
        LessonDiagnosticCode.INVALID_VALUE,
        path,
        message,
    )

    private fun missingRef(path: String, ref: String) = LessonDiagnostic(
        LessonDiagnosticCode.MISSING_REFERENCE,
        path,
        "Missing authored reference: $ref",
    )
}

private fun safeJoin(root: String, relative: String): String? {
    if (relative.isBlank() || relative.startsWith('/') || relative.startsWith('\\')) return null
    val segments = relative.replace('\\', '/').split('/')
    if (segments.any { it.isBlank() || it == "." || it == ".." }) return null
    return (listOf(root.trim('/')) + segments).joinToString("/")
}
