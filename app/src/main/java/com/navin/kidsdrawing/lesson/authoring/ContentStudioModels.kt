package com.navin.kidsdrawing.lesson.authoring

import com.navin.kidsdrawing.lesson.content.LessonCapabilityDiagnosticCode
import com.navin.kidsdrawing.lesson.content.LessonCapabilityValidator
import com.navin.kidsdrawing.lesson.model.ColoringRegionCatalogSource
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.lesson.model.LessonSource
import com.navin.kidsdrawing.lesson.model.StrokeCatalogSource
import com.navin.kidsdrawing.lesson.model.TeachingMode

/** Truth state shown by internal authoring tooling for a child-facing capability. */
enum class ContentStudioCapabilityStatus {
    READY,
    INCOMPLETE,
    UNSUPPORTED_RUNTIME,
    NOT_DECLARED,
}

enum class ContentStudioCapability {
    DRAW_WITH_ME,
    WATCH_THEN_DRAW,
    TRACE_AND_LEARN,
    HELP_LADDER,
    COLORING,
    VOICE_AUDIO,
}

enum class ContentStudioAuthoringDisposition {
    AUTHORABLE,
    RESERVED_PRESERVE_ONLY,
    RELEASE_BLOCKED_UNSUPPORTED,
}

enum class ContentStudioReservedField {
    CHILD_TOOL_PRESET,
    TEACHER_PLAY_AS_GROUP,
    SUGGESTED_COLOR_ROLES,
}

data class ContentStudioReservedFieldUsage(
    val field: ContentStudioReservedField,
    val paths: List<String>,
    val disposition: ContentStudioAuthoringDisposition = ContentStudioAuthoringDisposition.RESERVED_PRESERVE_ONLY,
)

data class ContentStudioCapabilityState(
    val capability: ContentStudioCapability,
    val status: ContentStudioCapabilityStatus,
)

data class ContentStudioDraft(
    val packageRoot: String,
    val lesson: LessonSource,
    val strokeCatalog: StrokeCatalogSource,
    val stringsByLocale: Map<String, Map<String, String>>,
    val thumbnailSvg: String,
    val previewSvg: String,
    val coloringRegionCatalog: ColoringRegionCatalogSource? = null,
)

data class ContentStudioStagedPackage(
    val packageRoot: String,
    val files: Map<String, String>,
) {
    fun readText(path: String): String? = files[path]

    val manifest: List<String>
        get() = files.keys.sorted()
}

enum class ContentStudioDiagnosticCode {
    INVALID_PACKAGE_ROOT,
    INVALID_ASSET_PATH,
    MISSING_DRAFT_ASSET,
    LOCALIZATION_MISMATCH,
    INVALID_LOCALIZATION,
    UNSUPPORTED_BINARY_AUDIO,
    PRODUCTION_PACKAGE_INVALID,
    CAPABILITY_INVALID,
    CATALOG_INVALID,
    INDEX_INVALID,
    QUALITY_ERROR,
}

data class ContentStudioDiagnostic(
    val code: ContentStudioDiagnosticCode,
    val path: String,
    val message: String,
)

sealed interface ContentStudioImportResult {
    data class Success(val draft: ContentStudioDraft) : ContentStudioImportResult
    data class Failure(val diagnostics: List<ContentStudioDiagnostic>) : ContentStudioImportResult
}

sealed interface ContentStudioExportResult {
    data class Success(val stagedPackage: ContentStudioStagedPackage) : ContentStudioExportResult
    data class Failure(val diagnostics: List<ContentStudioDiagnostic>) : ContentStudioExportResult
}

/**
 * Author-facing capability truth. This intentionally separates legacy preserve-only fields from
 * working capabilities so schema presence can never be mistaken for runtime support.
 */
object ContentStudioCapabilityStatusPolicy {
    fun evaluate(packageData: LessonRuntimePackage): List<ContentStudioCapabilityState> {
        val lesson = packageData.lesson
        val diagnostics = LessonCapabilityValidator.validate(packageData)
        val unsupportedRuntime = diagnostics.any {
            it.code == LessonCapabilityDiagnosticCode.UNSUPPORTED_RUNTIME_CAPABILITY
        }

        fun teachingModeStatus(mode: TeachingMode): ContentStudioCapabilityStatus = when {
            mode !in lesson.supportedModes -> ContentStudioCapabilityStatus.NOT_DECLARED
            unsupportedRuntime -> ContentStudioCapabilityStatus.UNSUPPORTED_RUNTIME
            mode == TeachingMode.TRACE_AND_LEARN && diagnostics.any {
                it.code == LessonCapabilityDiagnosticCode.INCOMPLETE_TRACE_SUPPORT
            } -> ContentStudioCapabilityStatus.INCOMPLETE
            else -> ContentStudioCapabilityStatus.READY
        }

        val helpDeclared = lesson.drawing.steps.any { it.help.isNotEmpty() }
        val helpStatus = when {
            !helpDeclared -> ContentStudioCapabilityStatus.NOT_DECLARED
            diagnostics.any { it.code == LessonCapabilityDiagnosticCode.INCOMPLETE_HELP_OUTPUT } ->
                ContentStudioCapabilityStatus.INCOMPLETE
            else -> ContentStudioCapabilityStatus.READY
        }

        val coloringDeclared = lesson.coloring?.enabled == true
        val coloringStatus = when {
            !coloringDeclared -> ContentStudioCapabilityStatus.NOT_DECLARED
            diagnostics.any {
                it.code == LessonCapabilityDiagnosticCode.UNSUPPORTED_RUNTIME_CAPABILITY &&
                    it.path.startsWith("coloring.")
            } -> ContentStudioCapabilityStatus.UNSUPPORTED_RUNTIME
            diagnostics.any { it.code == LessonCapabilityDiagnosticCode.AMBIGUOUS_GUIDED_COLORING } ->
                ContentStudioCapabilityStatus.INCOMPLETE
            else -> ContentStudioCapabilityStatus.READY
        }

        val voiceStatus = if (lesson.assets.audio.isEmpty()) {
            ContentStudioCapabilityStatus.NOT_DECLARED
        } else {
            ContentStudioCapabilityStatus.UNSUPPORTED_RUNTIME
        }

        return listOf(
            ContentStudioCapabilityState(
                ContentStudioCapability.DRAW_WITH_ME,
                teachingModeStatus(TeachingMode.DRAW_WITH_ME),
            ),
            ContentStudioCapabilityState(
                ContentStudioCapability.WATCH_THEN_DRAW,
                teachingModeStatus(TeachingMode.WATCH_THEN_DRAW),
            ),
            ContentStudioCapabilityState(
                ContentStudioCapability.TRACE_AND_LEARN,
                teachingModeStatus(TeachingMode.TRACE_AND_LEARN),
            ),
            ContentStudioCapabilityState(ContentStudioCapability.HELP_LADDER, helpStatus),
            ContentStudioCapabilityState(ContentStudioCapability.COLORING, coloringStatus),
            ContentStudioCapabilityState(ContentStudioCapability.VOICE_AUDIO, voiceStatus),
        )
    }

    fun reservedFieldUsage(lesson: LessonSource): List<ContentStudioReservedFieldUsage> = buildList {
        val toolPresetPaths = lesson.drawing.steps.mapIndexedNotNull { index, step ->
            step.childTurn.toolPreset?.let { "drawing.steps[$index].childTurn.toolPreset" }
        }
        if (toolPresetPaths.isNotEmpty()) {
            add(ContentStudioReservedFieldUsage(ContentStudioReservedField.CHILD_TOOL_PRESET, toolPresetPaths))
        }

        val groupedPaths = lesson.drawing.steps.mapIndexedNotNull { index, step ->
            if (step.teacher.playAsGroup) "drawing.steps[$index].teacher.playAsGroup" else null
        }
        if (groupedPaths.isNotEmpty()) {
            add(ContentStudioReservedFieldUsage(ContentStudioReservedField.TEACHER_PLAY_AS_GROUP, groupedPaths))
        }

        val suggestedColorPaths = lesson.coloring?.steps.orEmpty().mapIndexedNotNull { index, step ->
            if (step.suggestedColorRoles.isNotEmpty()) "coloring.steps[$index].suggestedColorRoles" else null
        }
        if (suggestedColorPaths.isNotEmpty()) {
            add(ContentStudioReservedFieldUsage(ContentStudioReservedField.SUGGESTED_COLOR_ROLES, suggestedColorPaths))
        }
    }
}
