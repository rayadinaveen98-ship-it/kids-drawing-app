package com.navin.kidsdrawing.lesson.authoring

import com.navin.kidsdrawing.lesson.content.CatalogIndexV2LoadResult
import com.navin.kidsdrawing.lesson.content.CatalogIndexV2Loader
import com.navin.kidsdrawing.lesson.content.CatalogIndexV2ProjectionResult
import com.navin.kidsdrawing.lesson.content.CatalogIndexV2Projector
import com.navin.kidsdrawing.lesson.content.ContentQualityAnalyzer
import com.navin.kidsdrawing.lesson.content.ContentQualitySeverity
import com.navin.kidsdrawing.lesson.content.LessonCapabilityValidator
import com.navin.kidsdrawing.lesson.content.LessonCatalog
import com.navin.kidsdrawing.lesson.content.LessonCatalogSource
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader

/** Deterministic evidence emitted for one staged Studio candidate. */
data class ContentStudioValidationEvidence(
    val lessonId: String?,
    val revision: Int?,
    val manifest: List<String>,
    val capabilityStates: List<ContentStudioCapabilityState>,
    val reservedFieldUsage: List<ContentStudioReservedFieldUsage>,
    val qualityWarningCount: Int,
    val projectedIndexEntryCount: Int?,
)

sealed interface ContentStudioValidationResult {
    data class Ready(
        val stagedPackage: ContentStudioStagedPackage,
        val evidence: ContentStudioValidationEvidence,
        val projectedIndexText: String,
    ) : ContentStudioValidationResult

    data class Blocked(
        val stagedPackage: ContentStudioStagedPackage,
        val diagnostics: List<ContentStudioDiagnostic>,
        val evidence: ContentStudioValidationEvidence,
    ) : ContentStudioValidationResult
}

/**
 * Runs a staged Studio package through the same package, catalog, capability, quality and V2 index
 * machinery used by production/build tooling. The committed catalog is never mutated.
 */
class ContentStudioProductionValidator(
    private val baseSource: LessonCatalogSource,
) {
    fun validate(staged: ContentStudioStagedPackage): ContentStudioValidationResult {
        val overlay = ContentStudioOverlayCatalogSource(baseSource, staged)
        val diagnostics = mutableListOf<ContentStudioDiagnostic>()

        val packageResult = LessonPackageLoader(overlay).load(staged.packageRoot)
        val packageData = when (packageResult) {
            is LessonLoadResult.Failure -> {
                packageResult.diagnostics.forEach { diagnostic ->
                    diagnostics += ContentStudioDiagnostic(
                        ContentStudioDiagnosticCode.PRODUCTION_PACKAGE_INVALID,
                        diagnostic.path,
                        diagnostic.message,
                    )
                }
                null
            }
            is LessonLoadResult.Success -> packageResult.packageData
        }

        val capabilityStates = packageData?.let(ContentStudioCapabilityStatusPolicy::evaluate).orEmpty()
        val reservedUsage = packageData?.lesson
            ?.let(ContentStudioCapabilityStatusPolicy::reservedFieldUsage)
            .orEmpty()

        packageData?.let { runtimePackage ->
            LessonCapabilityValidator.validate(runtimePackage).forEach { diagnostic ->
                diagnostics += ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.CAPABILITY_INVALID,
                    diagnostic.path,
                    "${diagnostic.code}: ${diagnostic.message}",
                )
            }
        }

        val catalog = LessonCatalog(overlay).load()
        catalog.diagnostics.forEach { diagnostic ->
            diagnostics += ContentStudioDiagnostic(
                ContentStudioDiagnosticCode.CATALOG_INVALID,
                diagnostic.packageRoot ?: "catalog",
                "${diagnostic.code}: ${diagnostic.message}",
            )
            diagnostic.lessonDiagnostics.forEach { lessonDiagnostic ->
                diagnostics += ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.CATALOG_INVALID,
                    lessonDiagnostic.path,
                    lessonDiagnostic.message,
                )
            }
        }

        val quality = ContentQualityAnalyzer().analyze(catalog)
        quality.diagnostics
            .filter { it.severity == ContentQualitySeverity.ERROR }
            .forEach { diagnostic ->
                diagnostics += ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.QUALITY_ERROR,
                    diagnostic.packageRoot ?: diagnostic.lessonId ?: "catalog",
                    "${diagnostic.code}: ${diagnostic.message}",
                )
            }

        var projectedEntryCount: Int? = null
        var projectedIndexText: String? = null
        when (val projection = CatalogIndexV2Projector.project(catalog)) {
            is CatalogIndexV2ProjectionResult.Failure -> projection.messages.forEach { message ->
                diagnostics += ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.INDEX_INVALID,
                    "catalog.index.projection",
                    message,
                )
            }
            is CatalogIndexV2ProjectionResult.Success -> {
                projectedEntryCount = projection.index.entries.size
                val rendered = CatalogIndexV2Projector.render(projection.index)
                projectedIndexText = rendered
                val indexSource = com.navin.kidsdrawing.lesson.content.LessonPackageSource { path ->
                    if (path == CatalogIndexV2Loader.DEFAULT_INDEX_PATH) rendered else overlay.readText(path)
                }
                when (val indexResult = CatalogIndexV2Loader(indexSource).load()) {
                    is CatalogIndexV2LoadResult.Failure -> indexResult.diagnostics.forEach { diagnostic ->
                        diagnostics += ContentStudioDiagnostic(
                            ContentStudioDiagnosticCode.INDEX_INVALID,
                            diagnostic.path,
                            "${diagnostic.code}: ${diagnostic.message}",
                        )
                    }
                    is CatalogIndexV2LoadResult.Success -> Unit
                }
            }
        }

        val evidence = ContentStudioValidationEvidence(
            lessonId = packageData?.lesson?.lessonId,
            revision = packageData?.lesson?.revision,
            manifest = staged.manifest,
            capabilityStates = capabilityStates,
            reservedFieldUsage = reservedUsage,
            qualityWarningCount = quality.diagnostics.count { it.severity == ContentQualitySeverity.WARNING },
            projectedIndexEntryCount = projectedEntryCount,
        )
        val stableDiagnostics = diagnostics
            .distinct()
            .sortedBy { "${it.code}:${it.path}:${it.message}" }
        return if (stableDiagnostics.isEmpty()) {
            ContentStudioValidationResult.Ready(
                stagedPackage = staged,
                evidence = evidence,
                projectedIndexText = checkNotNull(projectedIndexText),
            )
        } else {
            ContentStudioValidationResult.Blocked(staged, stableDiagnostics, evidence)
        }
    }
}

/**
 * Overlay semantics deliberately hide every old file below the staged package root. A replacement
 * candidate therefore cannot accidentally pass because a file deleted from the candidate still
 * exists in the base package.
 */
internal class ContentStudioOverlayCatalogSource(
    private val base: LessonCatalogSource,
    private val staged: ContentStudioStagedPackage,
) : LessonCatalogSource {
    private val packagePrefix = staged.packageRoot.trimEnd('/') + "/"

    override fun readText(path: String): String? = when {
        path == staged.packageRoot || path.startsWith(packagePrefix) -> staged.files[path]
        else -> base.readText(path)
    }

    override fun exists(path: String): Boolean = when {
        path == staged.packageRoot || path.startsWith(packagePrefix) -> path in staged.files
        else -> base.exists(path)
    }

    override fun list(path: String): List<String>? {
        val normalized = path.trim('/')
        val baseListing = base.list(normalized)
        val baseChildren = baseListing.orEmpty()
        val stagedChildren = staged.files.keys.mapNotNull { file ->
            val prefix = "$normalized/"
            if (!file.startsWith(prefix)) return@mapNotNull null
            file.removePrefix(prefix).substringBefore('/').takeIf(String::isNotBlank)
        }
        if (baseChildren.isEmpty() && stagedChildren.isEmpty() && baseListing == null) return null
        return (baseChildren + stagedChildren).distinct().sorted()
    }
}
