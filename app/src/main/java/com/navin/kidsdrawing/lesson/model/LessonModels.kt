package com.navin.kidsdrawing.lesson.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LessonSource(
    val schemaVersion: String,
    val lessonId: String,
    val revision: Int,
    val status: LessonStatus,
    val minimumContentApi: Int,
    val metadata: LessonMetadata,
    val canvas: LessonCanvas,
    val supportedModes: List<TeachingMode>,
    val assets: LessonAssets,
    val drawing: LessonDrawing,
    val coloring: LessonColoring? = null,
)

@Serializable
enum class LessonStatus {
    @SerialName("draft") DRAFT,
    @SerialName("review") REVIEW,
    @SerialName("release") RELEASE,
}

@Serializable
enum class TeachingMode {
    @SerialName("draw_with_me") DRAW_WITH_ME,
    @SerialName("watch_then_draw") WATCH_THEN_DRAW,
    @SerialName("trace_and_learn") TRACE_AND_LEARN,
}

@Serializable
data class LessonMetadata(
    val titleKey: String,
    val summaryKey: String,
    val ageBands: List<AgeBand>,
    val difficulty: Int,
    val estimatedMinutes: Int,
    val categoryIds: List<String>,
    val skillIds: List<String>,
    val journeyIds: List<String> = emptyList(),
    val prerequisiteLessonIds: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
)

@Serializable
enum class AgeBand {
    @SerialName("little_artists") LITTLE_ARTISTS,
    @SerialName("creative_explorers") CREATIVE_EXPLORERS,
    @SerialName("growing_artists") GROWING_ARTISTS,
    @SerialName("young_artists") YOUNG_ARTISTS,
}

@Serializable
data class LessonCanvas(
    val width: Int,
    val height: Int,
    val backgroundRole: BackgroundRole? = null,
)

@Serializable
enum class BackgroundRole {
    @SerialName("paper_light") PAPER_LIGHT,
    @SerialName("paper_warm") PAPER_WARM,
    @SerialName("paper_dark") PAPER_DARK,
    @SerialName("transparent") TRANSPARENT,
}

@Serializable
data class LessonAssets(
    val strokeFile: String,
    val thumbnail: String,
    val preview: String,
    val strings: Map<String, String>,
    val audio: Map<String, String> = emptyMap(),
    val coloringRegions: String? = null,
)

@Serializable
data class LessonDrawing(
    val steps: List<DrawingStep>,
)

@Serializable
data class DrawingStep(
    val id: String,
    val objectiveSkillIds: List<String>,
    val teacher: TeacherDemo,
    val childTurn: ChildTurn,
    val help: List<HelpEntry> = emptyList(),
    val completionNarrationKey: String? = null,
)

@Serializable
data class TeacherDemo(
    val narrationKey: String? = null,
    val strokeRefs: List<String>,
    val normalDurationMs: Int? = null,
    val playAsGroup: Boolean = false,
)

@Serializable
data class ChildTurn(
    val completionPolicy: ChildCompletionPolicy,
    val allowReplay: Boolean,
    val allowSkip: Boolean,
    val toolPreset: String? = null,
    val expectedStrokeRefs: List<String> = emptyList(),
)

@Serializable
enum class ChildCompletionPolicy {
    @SerialName("manual_done") MANUAL_DONE,
    @SerialName("any_stroke") ANY_STROKE,
    @SerialName("authored_signal") AUTHORED_SIGNAL,
}

@Serializable
data class HelpEntry(
    val level: Int,
    val kind: HelpKind,
    val narrationKey: String? = null,
    val guideRefs: List<String> = emptyList(),
)

@Serializable
enum class HelpKind {
    @SerialName("gentle_hint") GENTLE_HINT,
    @SerialName("visual_guide") VISUAL_GUIDE,
    @SerialName("direction_anchors") DIRECTION_ANCHORS,
    @SerialName("trace_path") TRACE_PATH,
    @SerialName("assisted_success") ASSISTED_SUCCESS,
}

@Serializable
data class LessonColoring(
    val enabled: Boolean,
    val defaultMode: ColoringMode? = null,
    val steps: List<ColoringStep> = emptyList(),
)

@Serializable
enum class ColoringMode {
    @SerialName("guided") GUIDED,
    @SerialName("self") SELF,
}

@Serializable
data class ColoringStep(
    val id: String,
    val regionIds: List<String>,
    val narrationKey: String? = null,
    val suggestedColorRoles: List<String> = emptyList(),
    val enforceSuggestedColors: Boolean = false,
)

@Serializable
data class StrokeCatalogSource(
    val schemaVersion: String,
    val strokes: List<AuthoredStroke>,
    val guides: List<AuthoredGuide> = emptyList(),
)

@Serializable
data class AuthoredStroke(
    val id: String,
    val points: List<AuthoredPoint>,
)

@Serializable
data class AuthoredPoint(
    val x: Float,
    val y: Float,
    val timeMs: Long,
    val pressure: Float = 1f,
)

@Serializable
data class AuthoredGuide(
    val id: String,
    val strokeRefs: List<String>,
)

/** Validated prepared-coloring geometry authored in the lesson logical coordinate system. */
@Serializable
data class ColoringRegionCatalogSource(
    val schemaVersion: String,
    val regions: List<AuthoredColorRegion>,
)

@Serializable
data class AuthoredColorRegion(
    val id: String,
    val points: List<AuthoredRegionPoint>,
)

@Serializable
data class AuthoredRegionPoint(
    val x: Float,
    val y: Float,
)

data class LessonRuntimePackage(
    val packageRoot: String,
    val lesson: LessonSource,
    val strokeCatalog: StrokeCatalogSource,
    val coloringRegionCatalog: ColoringRegionCatalogSource? = null,
)
