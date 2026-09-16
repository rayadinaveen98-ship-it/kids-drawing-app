package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.AgeBand
import com.navin.kidsdrawing.lesson.model.LessonStatus
import com.navin.kidsdrawing.lesson.model.TeachingMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Metadata-only discovery data. Full lesson teaching packages stay outside this model. */
@Serializable
data class CatalogIndexV2Source(
    val schemaVersion: String,
    val contentApi: Int,
    val entries: List<CatalogIndexV2EntrySource>,
)

@Serializable
data class CatalogIndexV2EntrySource(
    val lessonId: String,
    val revision: Int,
    val status: LessonStatus,
    val minimumContentApi: Int,
    val packageRef: String,
    val title: String,
    val titleKey: String,
    val summary: String,
    val summaryKey: String,
    val ageBands: List<AgeBand>,
    val difficulty: Int,
    val estimatedMinutes: Int,
    val drawingStepCount: Int,
    val categoryIds: List<String>,
    val skillIds: List<String>,
    val journeyIds: List<String> = emptyList(),
    val collectionIds: List<String> = emptyList(),
    val prerequisiteLessonIds: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val contentFamilyId: String? = null,
    val supportedModes: List<TeachingMode>,
    val capabilitySummary: CatalogCapabilitySummarySource,
    val thumbnailRef: String,
    val previewRef: String,
)

@Serializable
data class CatalogCapabilitySummarySource(
    val teachingModes: List<TeachingMode>,
    val helpAvailable: Boolean,
    val traceReady: Boolean,
    val coloring: CatalogColoringCapability,
    val voiceAudio: CatalogVoiceAudioCapability = CatalogVoiceAudioCapability.NOT_SUPPORTED,
)

@Serializable
enum class CatalogColoringCapability {
    @SerialName("none") NONE,
    @SerialName("freehand") FREEHAND,
    @SerialName("prepared") PREPARED,
    @SerialName("guided_prepared") GUIDED_PREPARED,
}

@Serializable
enum class CatalogVoiceAudioCapability {
    @SerialName("not_supported") NOT_SUPPORTED,
    @SerialName("ready") READY,
}

data class CatalogIndexV2Entry(
    val identity: LessonCatalogIdentity,
    val status: LessonStatus,
    val minimumContentApi: Int,
    val packageRef: String,
    val title: String,
    val titleKey: String,
    val summary: String,
    val summaryKey: String,
    val ageBands: Set<AgeBand>,
    val difficulty: Int,
    val estimatedMinutes: Int,
    val drawingStepCount: Int,
    val categoryIds: Set<String>,
    val skillIds: Set<String>,
    val journeyIds: Set<String>,
    val collectionIds: Set<String>,
    val prerequisiteLessonIds: Set<String>,
    val tags: Set<String>,
    val contentFamilyId: String?,
    val supportedModes: Set<TeachingMode>,
    val capabilitySummary: CatalogCapabilitySummary,
    val thumbnailRef: String,
    val previewRef: String,
)

data class CatalogCapabilitySummary(
    val teachingModes: Set<TeachingMode>,
    val helpAvailable: Boolean,
    val traceReady: Boolean,
    val coloring: CatalogColoringCapability,
    val voiceAudio: CatalogVoiceAudioCapability,
)

enum class CatalogIndexV2DiagnosticCode {
    MISSING_INDEX,
    INVALID_JSON,
    UNSUPPORTED_SCHEMA_VERSION,
    UNSUPPORTED_CONTENT_API,
    INVALID_ENTRY,
    DUPLICATE_IDENTITY,
    DUPLICATE_LESSON_ID,
    MISSING_PREREQUISITE,
    PREREQUISITE_CYCLE,
}

data class CatalogIndexV2Diagnostic(
    val code: CatalogIndexV2DiagnosticCode,
    val path: String,
    val message: String,
)

sealed interface CatalogIndexV2LoadResult {
    data class Success(val snapshot: CatalogIndexV2Snapshot) : CatalogIndexV2LoadResult
    data class Failure(val diagnostics: List<CatalogIndexV2Diagnostic>) : CatalogIndexV2LoadResult
}

/** Immutable discovery snapshot. It intentionally owns no LessonRuntimePackage map. */
class CatalogIndexV2Snapshot internal constructor(entries: List<CatalogIndexV2Entry>) {
    val entries: List<CatalogIndexV2Entry> = entries.sortedWith(ENTRY_ORDER)
    private val byIdentity = this.entries.associateBy(CatalogIndexV2Entry::identity)

    fun entry(identity: LessonCatalogIdentity): CatalogIndexV2Entry? = byIdentity[identity]
    fun byLessonId(lessonId: String) = entries.filter { it.identity.lessonId == lessonId }
    fun forAgeBand(ageBand: AgeBand) = entries.filter { ageBand in it.ageBands }
    fun byCategory(categoryId: String) = entries.filter { categoryId in it.categoryIds }
    fun bySkill(skillId: String) = entries.filter { skillId in it.skillIds }
    fun byDifficulty(difficulty: Int) = entries.filter { it.difficulty == difficulty }
    fun byJourney(journeyId: String) = entries.filter { journeyId in it.journeyIds }
    fun byCollection(collectionId: String) = entries.filter { collectionId in it.collectionIds }
    fun byContentFamily(contentFamilyId: String) = entries.filter { it.contentFamilyId == contentFamilyId }
    fun supporting(mode: TeachingMode) = entries.filter { mode in it.supportedModes }
    fun withHelp() = entries.filter { it.capabilitySummary.helpAvailable }
    fun traceReady() = entries.filter { it.capabilitySummary.traceReady }
    fun withColoring(capability: CatalogColoringCapability) =
        entries.filter { it.capabilitySummary.coloring == capability }

    private companion object {
        val ENTRY_ORDER = compareBy<CatalogIndexV2Entry>(
            { it.identity.lessonId },
            { it.identity.revision },
            { it.packageRef },
        )
    }
}
