package com.navin.kidsdrawing.lesson.content

import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString

/**
 * Read-only authoring projection over the same bundled catalog/runtime packages used by the product.
 *
 * This repository has no child document/session persistence dependency and exposes no mutation API.
 */
class ContentInspectionRepository(
    private val source: LessonCatalogSource,
) {
    fun load(): ContentInspectionSnapshot {
        val catalog = LessonCatalog(source).load()
        val report = ContentQualityAnalyzer().analyze(catalog)
        val summaryByIdentity = report.lessons.associateBy { it.identity }
        val lessons = catalog.entries.mapNotNull { entry ->
            val runtime = catalog.runtimePackage(entry.identity) ?: return@mapNotNull null
            ContentInspectionLesson(
                entry = entry,
                runtime = runtime,
                thumbnailSvg = source.readText(entry.thumbnailPath),
                previewSvg = source.readText(entry.previewPath),
                defaultStrings = readDefaultStrings(runtime),
                qualitySummary = summaryByIdentity[entry.identity],
            )
        }
        return ContentInspectionSnapshot(
            catalogDiagnostics = catalog.diagnostics,
            qualityReport = report,
            lessons = lessons,
        )
    }

    private fun readDefaultStrings(runtime: LessonRuntimePackage): Map<String, String> {
        val relativePath = runtime.lesson.assets.strings[LessonCatalog.DEFAULT_LOCALE] ?: return emptyMap()
        val path = "${runtime.packageRoot.trimEnd('/')}/${relativePath.trimStart('/')}"
        val text = source.readText(path) ?: return emptyMap()
        return try {
            LessonPackageLoader.DEFAULT_JSON.decodeFromString<Map<String, String>>(text).toSortedMap()
        } catch (_: SerializationException) {
            emptyMap()
        } catch (_: IllegalArgumentException) {
            emptyMap()
        }
    }
}

data class ContentInspectionSnapshot(
    val catalogDiagnostics: List<LessonCatalogDiagnostic>,
    val qualityReport: CatalogCoverageReport,
    val lessons: List<ContentInspectionLesson>,
)

data class ContentInspectionLesson(
    val entry: LessonCatalogEntry,
    val runtime: LessonRuntimePackage,
    val thumbnailSvg: String?,
    val previewSvg: String?,
    val defaultStrings: Map<String, String>,
    val qualitySummary: LessonContentSummary?,
)
