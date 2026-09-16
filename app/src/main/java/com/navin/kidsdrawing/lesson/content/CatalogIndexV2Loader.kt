package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.LessonStatus
import com.navin.kidsdrawing.lesson.model.TeachingMode
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class CatalogIndexV2Loader(
    private val source: LessonPackageSource,
    private val indexPath: String = DEFAULT_INDEX_PATH,
    private val supportedContentApi: Int = LessonPackageLoader.CURRENT_CONTENT_API,
    private val json: Json = LessonPackageLoader.DEFAULT_JSON,
) {
    fun load(): CatalogIndexV2LoadResult {
        val text = source.readText(indexPath)
            ?: return failure(
                CatalogIndexV2DiagnosticCode.MISSING_INDEX,
                indexPath,
                "Catalog Index V2 is missing.",
            )
        val decoded = try {
            json.decodeFromString<CatalogIndexV2Source>(text)
        } catch (error: SerializationException) {
            return failure(
                CatalogIndexV2DiagnosticCode.INVALID_JSON,
                indexPath,
                error.message ?: "Catalog Index V2 could not be decoded.",
            )
        } catch (error: IllegalArgumentException) {
            return failure(
                CatalogIndexV2DiagnosticCode.INVALID_JSON,
                indexPath,
                error.message ?: "Catalog Index V2 is invalid.",
            )
        }

        val diagnostics = validate(decoded)
        if (diagnostics.isNotEmpty()) return CatalogIndexV2LoadResult.Failure(diagnostics)

        val releaseEntries = decoded.entries
            .asSequence()
            .filter { it.status == LessonStatus.RELEASE }
            .map(::toEntry)
            .toList()
        return CatalogIndexV2LoadResult.Success(CatalogIndexV2Snapshot(releaseEntries))
    }

    internal fun validate(index: CatalogIndexV2Source): List<CatalogIndexV2Diagnostic> = buildList {
        if (index.schemaVersion != SCHEMA_VERSION) {
            add(
                diagnostic(
                    CatalogIndexV2DiagnosticCode.UNSUPPORTED_SCHEMA_VERSION,
                    "schemaVersion",
                    "Unsupported catalog schema ${index.schemaVersion}; expected $SCHEMA_VERSION.",
                ),
            )
        }
        if (index.contentApi !in 1..supportedContentApi) {
            add(
                diagnostic(
                    CatalogIndexV2DiagnosticCode.UNSUPPORTED_CONTENT_API,
                    "contentApi",
                    "Catalog requires content API ${index.contentApi}; runtime supports $supportedContentApi.",
                ),
            )
        }

        index.entries.forEachIndexed { entryIndex, entry ->
            validateEntry(entry, entryIndex).forEach(::add)
        }

        val releaseEntries = index.entries.filter { it.status == LessonStatus.RELEASE }
        releaseEntries
            .groupBy { it.lessonId to it.revision }
            .filterValues { it.size > 1 }
            .forEach { (identity, _) ->
                add(
                    diagnostic(
                        CatalogIndexV2DiagnosticCode.DUPLICATE_IDENTITY,
                        "entries",
                        "Duplicate release identity ${identity.first} r${identity.second}.",
                    ),
                )
            }
        releaseEntries
            .groupBy(CatalogIndexV2EntrySource::lessonId)
            .filterValues { it.size > 1 }
            .forEach { (lessonId, _) ->
                add(
                    diagnostic(
                        CatalogIndexV2DiagnosticCode.DUPLICATE_LESSON_ID,
                        "entries",
                        "Multiple release revisions for lesson $lessonId are not supported by the current prerequisite model.",
                    ),
                )
            }

        val releaseIds = releaseEntries.mapTo(linkedSetOf(), CatalogIndexV2EntrySource::lessonId)
        releaseEntries.forEachIndexed { releaseIndex, entry ->
            entry.prerequisiteLessonIds.forEach { prerequisite ->
                if (prerequisite !in releaseIds) {
                    add(
                        diagnostic(
                            CatalogIndexV2DiagnosticCode.MISSING_PREREQUISITE,
                            "releaseEntries[$releaseIndex].prerequisiteLessonIds",
                            "${entry.lessonId} references unavailable release prerequisite $prerequisite.",
                        ),
                    )
                }
            }
        }

        val graph = releaseEntries.associate { it.lessonId to it.prerequisiteLessonIds.toSet() }
        findCycle(graph)?.let { cycle ->
            add(
                diagnostic(
                    CatalogIndexV2DiagnosticCode.PREREQUISITE_CYCLE,
                    "entries.prerequisiteLessonIds",
                    "Prerequisite cycle detected: ${cycle.joinToString(" -> ")}.",
                ),
            )
        }
    }

    private fun validateEntry(
        entry: CatalogIndexV2EntrySource,
        index: Int,
    ): List<CatalogIndexV2Diagnostic> = buildList {
        val base = "entries[$index]"
        if (!LESSON_ID.matches(entry.lessonId)) add(invalid(base, "lessonId must be a stable lowercase lesson ID."))
        if (entry.revision < 1) add(invalid(base, "revision must be at least 1."))
        if (entry.minimumContentApi !in 1..supportedContentApi) add(invalid(base, "minimumContentApi is outside the supported range."))
        if (!isSafeAssetRef(entry.packageRef)) add(invalid(base, "packageRef must be a safe relative asset path."))
        if (!isSafeAssetRef(entry.thumbnailRef)) add(invalid(base, "thumbnailRef must be a safe relative asset path."))
        if (!isSafeAssetRef(entry.previewRef)) add(invalid(base, "previewRef must be a safe relative asset path."))
        if (entry.title.isBlank() || entry.titleKey.isBlank()) add(invalid(base, "title and titleKey must be present."))
        if (entry.summary.isBlank() || entry.summaryKey.isBlank()) add(invalid(base, "summary and summaryKey must be present."))
        if (entry.ageBands.isEmpty() || entry.ageBands.size != entry.ageBands.distinct().size) {
            add(invalid(base, "ageBands must be non-empty and unique."))
        }
        if (entry.difficulty !in 1..5) add(invalid(base, "difficulty must be 1..5."))
        if (entry.estimatedMinutes !in 1..90) add(invalid(base, "estimatedMinutes must be 1..90."))
        if (entry.drawingStepCount < 1) add(invalid(base, "drawingStepCount must be at least 1."))
        if (entry.categoryIds.isEmpty()) add(invalid(base, "at least one categoryId is required."))
        if (entry.skillIds.isEmpty()) add(invalid(base, "at least one skillId is required."))
        if (entry.supportedModes.isEmpty()) add(invalid(base, "at least one supported teaching mode is required."))
        if (entry.supportedModes.size != entry.supportedModes.distinct().size) add(invalid(base, "supportedModes must be unique."))
        if (entry.capabilitySummary.teachingModes.toSet() != entry.supportedModes.toSet()) {
            add(invalid(base, "capabilitySummary.teachingModes must exactly match supportedModes."))
        }
        if (entry.capabilitySummary.traceReady != (TeachingMode.TRACE_AND_LEARN in entry.supportedModes)) {
            add(invalid(base, "traceReady must exactly reflect validated Trace & Learn availability."))
        }
        if (entry.capabilitySummary.voiceAudio == CatalogVoiceAudioCapability.READY) {
            add(invalid(base, "voiceAudio READY is unsupported by the accepted runtime."))
        }

        val idGroups = listOf(
            "categoryIds" to entry.categoryIds,
            "skillIds" to entry.skillIds,
            "journeyIds" to entry.journeyIds,
            "collectionIds" to entry.collectionIds,
            "prerequisiteLessonIds" to entry.prerequisiteLessonIds,
            "tags" to entry.tags,
        )
        idGroups.forEach { (name, ids) ->
            if (ids.size != ids.distinct().size) add(invalid(base, "$name must not contain duplicates."))
            if (ids.any { !TAXONOMY_ID.matches(it) }) add(invalid(base, "$name contains an invalid ID."))
        }
        if (entry.contentFamilyId != null && !TAXONOMY_ID.matches(entry.contentFamilyId)) {
            add(invalid(base, "contentFamilyId is invalid."))
        }
        if (entry.lessonId in entry.prerequisiteLessonIds) add(invalid(base, "a lesson cannot require itself."))
    }

    private fun toEntry(source: CatalogIndexV2EntrySource): CatalogIndexV2Entry = CatalogIndexV2Entry(
        identity = LessonCatalogIdentity(source.lessonId, source.revision),
        status = source.status,
        minimumContentApi = source.minimumContentApi,
        packageRef = source.packageRef,
        title = source.title,
        titleKey = source.titleKey,
        summary = source.summary,
        summaryKey = source.summaryKey,
        ageBands = source.ageBands.toSet(),
        difficulty = source.difficulty,
        estimatedMinutes = source.estimatedMinutes,
        drawingStepCount = source.drawingStepCount,
        categoryIds = source.categoryIds.toSet(),
        skillIds = source.skillIds.toSet(),
        journeyIds = source.journeyIds.toSet(),
        collectionIds = source.collectionIds.toSet(),
        prerequisiteLessonIds = source.prerequisiteLessonIds.toSet(),
        tags = source.tags.toSet(),
        contentFamilyId = source.contentFamilyId,
        supportedModes = source.supportedModes.toSet(),
        capabilitySummary = CatalogCapabilitySummary(
            teachingModes = source.capabilitySummary.teachingModes.toSet(),
            helpAvailable = source.capabilitySummary.helpAvailable,
            traceReady = source.capabilitySummary.traceReady,
            coloring = source.capabilitySummary.coloring,
            voiceAudio = source.capabilitySummary.voiceAudio,
        ),
        thumbnailRef = source.thumbnailRef,
        previewRef = source.previewRef,
    )

    private fun findCycle(graph: Map<String, Set<String>>): List<String>? {
        val visiting = linkedSetOf<String>()
        val visited = mutableSetOf<String>()
        val path = mutableListOf<String>()

        fun visit(node: String): List<String>? {
            if (node in visited) return null
            if (node in visiting) {
                val start = path.indexOf(node).coerceAtLeast(0)
                return path.subList(start, path.size).toList() + node
            }
            visiting += node
            path += node
            graph[node].orEmpty().sorted().forEach { prerequisite ->
                if (prerequisite in graph) visit(prerequisite)?.let { return it }
            }
            path.removeAt(path.lastIndex)
            visiting -= node
            visited += node
            return null
        }

        graph.keys.sorted().forEach { node -> visit(node)?.let { return it } }
        return null
    }

    private fun isSafeAssetRef(value: String): Boolean {
        if (value.isBlank() || value.startsWith('/') || value.startsWith('\\')) return false
        val segments = value.replace('\\', '/').split('/')
        return segments.none { it.isBlank() || it == "." || it == ".." }
    }

    private fun invalid(path: String, message: String) =
        diagnostic(CatalogIndexV2DiagnosticCode.INVALID_ENTRY, path, message)

    private fun diagnostic(code: CatalogIndexV2DiagnosticCode, path: String, message: String) =
        CatalogIndexV2Diagnostic(code, path, message)

    private fun failure(code: CatalogIndexV2DiagnosticCode, path: String, message: String) =
        CatalogIndexV2LoadResult.Failure(listOf(diagnostic(code, path, message)))

    companion object {
        const val SCHEMA_VERSION = "2.0"
        const val DEFAULT_INDEX_PATH = "catalog/lesson-index-v2.json"
        private val LESSON_ID = Regex("^[a-z0-9]+(?:[.-][a-z0-9]+)*$")
        private val TAXONOMY_ID = Regex("^[a-z0-9]+(?:[._-][a-z0-9]+)*$")
    }
}
