package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.ColoringMode
import com.navin.kidsdrawing.lesson.model.TeachingMode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed interface CatalogIndexV2ProjectionResult {
    data class Success(val index: CatalogIndexV2Source) : CatalogIndexV2ProjectionResult
    data class Failure(val messages: List<String>) : CatalogIndexV2ProjectionResult
}

/**
 * Projects the V2 discovery index only from the already validated full release catalog.
 *
 * This is intentionally a build/tooling path. Production discovery consumes the committed index;
 * it does not invoke the eager legacy catalog just to rebuild metadata at app startup.
 */
object CatalogIndexV2Projector {
    private val renderJson = Json {
        encodeDefaults = true
        explicitNulls = false
        prettyPrint = true
    }

    fun project(snapshot: LessonCatalogSnapshot): CatalogIndexV2ProjectionResult {
        if (snapshot.diagnostics.isNotEmpty()) {
            return CatalogIndexV2ProjectionResult.Failure(
                snapshot.diagnostics.map { diagnostic ->
                    "Legacy catalog ${diagnostic.code}: ${diagnostic.packageRoot ?: "catalog"}: ${diagnostic.message}"
                },
            )
        }

        val messages = mutableListOf<String>()
        val projected = snapshot.entries.mapNotNull { entry ->
            val packageData = snapshot.runtimePackage(entry.identity)
            if (packageData == null) {
                messages += "Missing validated runtime package for ${entry.identity.lessonId} r${entry.identity.revision}."
                return@mapNotNull null
            }
            val capabilityDiagnostics = LessonCapabilityValidator.validate(packageData)
            if (capabilityDiagnostics.isNotEmpty()) {
                capabilityDiagnostics.forEach { diagnostic ->
                    messages += "${entry.identity.lessonId} ${diagnostic.code} ${diagnostic.path}: ${diagnostic.message}"
                }
                return@mapNotNull null
            }

            val lesson = packageData.lesson
            CatalogIndexV2EntrySource(
                lessonId = lesson.lessonId,
                revision = lesson.revision,
                status = lesson.status,
                minimumContentApi = lesson.minimumContentApi,
                packageRef = packageData.packageRoot,
                title = entry.title,
                titleKey = lesson.metadata.titleKey,
                summary = entry.summary,
                summaryKey = lesson.metadata.summaryKey,
                ageBands = lesson.metadata.ageBands,
                difficulty = lesson.metadata.difficulty,
                estimatedMinutes = lesson.metadata.estimatedMinutes,
                drawingStepCount = lesson.drawing.steps.size,
                categoryIds = lesson.metadata.categoryIds,
                skillIds = lesson.metadata.skillIds,
                journeyIds = lesson.metadata.journeyIds,
                collectionIds = emptyList(),
                prerequisiteLessonIds = lesson.metadata.prerequisiteLessonIds,
                tags = lesson.metadata.tags,
                contentFamilyId = null,
                supportedModes = lesson.supportedModes,
                capabilitySummary = CatalogCapabilitySummarySource(
                    teachingModes = lesson.supportedModes,
                    helpAvailable = lesson.drawing.steps.any { it.help.isNotEmpty() },
                    traceReady = TeachingMode.TRACE_AND_LEARN in lesson.supportedModes,
                    coloring = coloringCapability(packageData),
                    voiceAudio = CatalogVoiceAudioCapability.NOT_SUPPORTED,
                ),
                thumbnailRef = join(packageData.packageRoot, lesson.assets.thumbnail),
                previewRef = join(packageData.packageRoot, lesson.assets.preview),
            )
        }

        if (messages.isNotEmpty()) return CatalogIndexV2ProjectionResult.Failure(messages.sorted())

        return CatalogIndexV2ProjectionResult.Success(
            CatalogIndexV2Source(
                schemaVersion = CatalogIndexV2Loader.SCHEMA_VERSION,
                contentApi = LessonPackageLoader.CURRENT_CONTENT_API,
                entries = projected.sortedWith(
                    compareBy<CatalogIndexV2EntrySource>(
                        CatalogIndexV2EntrySource::lessonId,
                        CatalogIndexV2EntrySource::revision,
                        CatalogIndexV2EntrySource::packageRef,
                    ),
                ),
            ),
        )
    }

    fun render(index: CatalogIndexV2Source): String = renderJson.encodeToString(index) + "\n"

    private fun coloringCapability(
        packageData: com.navin.kidsdrawing.lesson.model.LessonRuntimePackage,
    ): CatalogColoringCapability {
        val coloring = packageData.lesson.coloring
        if (coloring?.enabled != true) return CatalogColoringCapability.NONE

        val hasPreparedRegions = packageData.coloringRegionCatalog?.regions?.isNotEmpty() == true
        return when {
            hasPreparedRegions && coloring.defaultMode == ColoringMode.GUIDED ->
                CatalogColoringCapability.GUIDED_PREPARED
            hasPreparedRegions -> CatalogColoringCapability.PREPARED
            else -> CatalogColoringCapability.FREEHAND
        }
    }

    private fun join(root: String, relative: String): String =
        "${root.trimEnd('/')}/${relative.trimStart('/')}"
}
