package com.navin.kidsdrawing.lesson.authoring

import com.navin.kidsdrawing.lesson.content.CatalogColoringCapability
import com.navin.kidsdrawing.lesson.content.CatalogIndexV2EntrySource
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
import com.navin.kidsdrawing.lesson.model.LessonStatus
import com.navin.kidsdrawing.lesson.model.TeachingMode

enum class ContentStudioValidationGate {
    PACKAGE_LOADER,
    CAPABILITY,
    CATALOG_INTEGRITY,
    CONTENT_QUALITY,
    INDEX_PROJECTION,
    INDEX_VALIDATION,
}

enum class ContentStudioGateStatus {
    PASSED,
    BLOCKED,
    NOT_RUN,
}

enum class ContentStudioEvidenceSeverity {
    ERROR,
    WARNING,
}

data class ContentStudioGateDiagnostic(
    val severity: ContentStudioEvidenceSeverity,
    val sourceCode: String,
    val path: String,
    val message: String,
)

data class ContentStudioGateEvidence(
    val gate: ContentStudioValidationGate,
    val status: ContentStudioGateStatus,
    val diagnostics: List<ContentStudioGateDiagnostic> = emptyList(),
)

/** Deterministic evidence emitted for one staged Studio candidate. */
data class ContentStudioValidationEvidence(
    val lessonId: String?,
    val revision: Int?,
    val lessonStatus: LessonStatus?,
    val manifest: List<String>,
    val packageLoaderPassed: Boolean,
    val drawingStepCount: Int?,
    val strokeCount: Int?,
    val guideCount: Int?,
    val coloringRegionCount: Int?,
    val supportedModes: List<TeachingMode>,
    val capabilityStates: List<ContentStudioCapabilityState>,
    val helpReady: Boolean?,
    val traceReady: Boolean?,
    val coloringCapability: CatalogColoringCapability?,
    val reservedFieldUsage: List<ContentStudioReservedFieldUsage>,
    val gateEvidence: List<ContentStudioGateEvidence>,
    val qualityWarningCount: Int,
    val projectedIndexEntryCount: Int?,
    val projectedIndexEntry: CatalogIndexV2EntrySource?,
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
        val gates = mutableListOf<ContentStudioGateEvidence>()

        val packageResult = LessonPackageLoader(overlay).load(staged.packageRoot)
        val packageGateDiagnostics = when (packageResult) {
            is LessonLoadResult.Failure -> packageResult.diagnostics.map { diagnostic ->
                diagnostics += ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.PRODUCTION_PACKAGE_INVALID,
                    diagnostic.path,
                    "${diagnostic.code}: ${diagnostic.message}",
                )
                ContentStudioGateDiagnostic(
                    severity = ContentStudioEvidenceSeverity.ERROR,
                    sourceCode = diagnostic.code.name,
                    path = diagnostic.path,
                    message = diagnostic.message,
                )
            }
            is LessonLoadResult.Success -> emptyList()
        }
        val packageData = (packageResult as? LessonLoadResult.Success)?.packageData
        gates += ContentStudioGateEvidence(
            gate = ContentStudioValidationGate.PACKAGE_LOADER,
            status = if (packageData != null) ContentStudioGateStatus.PASSED else ContentStudioGateStatus.BLOCKED,
            diagnostics = packageGateDiagnostics,
        )

        val capabilityStates = packageData?.let(ContentStudioCapabilityStatusPolicy::evaluate).orEmpty()
        val reservedUsage = packageData?.lesson
            ?.let(ContentStudioCapabilityStatusPolicy::reservedFieldUsage)
            .orEmpty()
        val capabilityGateDiagnostics = if (packageData == null) {
            emptyList()
        } else {
            LessonCapabilityValidator.validate(packageData).map { diagnostic ->
                diagnostics += ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.CAPABILITY_INVALID,
                    diagnostic.path,
                    "${diagnostic.code}: ${diagnostic.message}",
                )
                ContentStudioGateDiagnostic(
                    severity = ContentStudioEvidenceSeverity.ERROR,
                    sourceCode = diagnostic.code.name,
                    path = diagnostic.path,
                    message = diagnostic.message,
                )
            }
        }
        gates += ContentStudioGateEvidence(
            gate = ContentStudioValidationGate.CAPABILITY,
            status = when {
                packageData == null -> ContentStudioGateStatus.NOT_RUN
                capabilityGateDiagnostics.isEmpty() -> ContentStudioGateStatus.PASSED
                else -> ContentStudioGateStatus.BLOCKED
            },
            diagnostics = capabilityGateDiagnostics,
        )

        val catalog = LessonCatalog(overlay).load()
        val catalogGateDiagnostics = buildList {
            catalog.diagnostics.forEach { diagnostic ->
                diagnostics += ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.CATALOG_INVALID,
                    diagnostic.packageRoot ?: "catalog",
                    "${diagnostic.code}: ${diagnostic.message}",
                )
                add(
                    ContentStudioGateDiagnostic(
                        severity = ContentStudioEvidenceSeverity.ERROR,
                        sourceCode = diagnostic.code.name,
                        path = diagnostic.packageRoot ?: "catalog",
                        message = diagnostic.message,
                    ),
                )
                diagnostic.lessonDiagnostics.forEach { lessonDiagnostic ->
                    diagnostics += ContentStudioDiagnostic(
                        ContentStudioDiagnosticCode.CATALOG_INVALID,
                        lessonDiagnostic.path,
                        "${lessonDiagnostic.code}: ${lessonDiagnostic.message}",
                    )
                    add(
                        ContentStudioGateDiagnostic(
                            severity = ContentStudioEvidenceSeverity.ERROR,
                            sourceCode = lessonDiagnostic.code.name,
                            path = lessonDiagnostic.path,
                            message = lessonDiagnostic.message,
                        ),
                    )
                }
            }
        }
        gates += ContentStudioGateEvidence(
            gate = ContentStudioValidationGate.CATALOG_INTEGRITY,
            status = if (catalogGateDiagnostics.isEmpty()) ContentStudioGateStatus.PASSED else ContentStudioGateStatus.BLOCKED,
            diagnostics = catalogGateDiagnostics,
        )

        val quality = ContentQualityAnalyzer().analyze(catalog)
        val qualityGateDiagnostics = quality.diagnostics.map { diagnostic ->
            val severity = when (diagnostic.severity) {
                ContentQualitySeverity.ERROR -> ContentStudioEvidenceSeverity.ERROR
                ContentQualitySeverity.WARNING -> ContentStudioEvidenceSeverity.WARNING
            }
            if (diagnostic.severity == ContentQualitySeverity.ERROR) {
                diagnostics += ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.QUALITY_ERROR,
                    diagnostic.packageRoot ?: diagnostic.lessonId ?: "catalog",
                    "${diagnostic.code}: ${diagnostic.message}",
                )
            }
            ContentStudioGateDiagnostic(
                severity = severity,
                sourceCode = diagnostic.code.name,
                path = diagnostic.packageRoot ?: diagnostic.lessonId ?: "catalog",
                message = diagnostic.message,
            )
        }
        gates += ContentStudioGateEvidence(
            gate = ContentStudioValidationGate.CONTENT_QUALITY,
            status = if (qualityGateDiagnostics.any { it.severity == ContentStudioEvidenceSeverity.ERROR }) {
                ContentStudioGateStatus.BLOCKED
            } else {
                ContentStudioGateStatus.PASSED
            },
            diagnostics = qualityGateDiagnostics,
        )

        var projectedEntryCount: Int? = null
        var projectedIndexText: String? = null
        var projectedIndexEntry: CatalogIndexV2EntrySource? = null
        var projectionSucceeded = false
        var indexValidationSucceeded = false
        val projectionGateDiagnostics = mutableListOf<ContentStudioGateDiagnostic>()
        val indexValidationGateDiagnostics = mutableListOf<ContentStudioGateDiagnostic>()

        when (val projection = CatalogIndexV2Projector.project(catalog)) {
            is CatalogIndexV2ProjectionResult.Failure -> projection.messages.forEach { message ->
                diagnostics += ContentStudioDiagnostic(
                    ContentStudioDiagnosticCode.INDEX_INVALID,
                    "catalog.index.projection",
                    message,
                )
                projectionGateDiagnostics += ContentStudioGateDiagnostic(
                    severity = ContentStudioEvidenceSeverity.ERROR,
                    sourceCode = "PROJECTION_FAILED",
                    path = "catalog.index.projection",
                    message = message,
                )
            }
            is CatalogIndexV2ProjectionResult.Success -> {
                projectionSucceeded = true
                projectedEntryCount = projection.index.entries.size
                projectedIndexEntry = packageData?.let { runtimePackage ->
                    projection.index.entries.firstOrNull { entry ->
                        entry.lessonId == runtimePackage.lesson.lessonId &&
                            entry.revision == runtimePackage.lesson.revision &&
                            entry.packageRef == staged.packageRoot
                    }
                }
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
                        indexValidationGateDiagnostics += ContentStudioGateDiagnostic(
                            severity = ContentStudioEvidenceSeverity.ERROR,
                            sourceCode = diagnostic.code.name,
                            path = diagnostic.path,
                            message = diagnostic.message,
                        )
                    }
                    is CatalogIndexV2LoadResult.Success -> indexValidationSucceeded = true
                }
            }
        }
        gates += ContentStudioGateEvidence(
            gate = ContentStudioValidationGate.INDEX_PROJECTION,
            status = if (projectionSucceeded) ContentStudioGateStatus.PASSED else ContentStudioGateStatus.BLOCKED,
            diagnostics = projectionGateDiagnostics,
        )
        gates += ContentStudioGateEvidence(
            gate = ContentStudioValidationGate.INDEX_VALIDATION,
            status = when {
                !projectionSucceeded -> ContentStudioGateStatus.NOT_RUN
                indexValidationSucceeded -> ContentStudioGateStatus.PASSED
                else -> ContentStudioGateStatus.BLOCKED
            },
            diagnostics = indexValidationGateDiagnostics,
        )

        val evidence = ContentStudioValidationEvidence(
            lessonId = packageData?.lesson?.lessonId,
            revision = packageData?.lesson?.revision,
            lessonStatus = packageData?.lesson?.status,
            manifest = staged.manifest,
            packageLoaderPassed = packageData != null,
            drawingStepCount = packageData?.lesson?.drawing?.steps?.size,
            strokeCount = packageData?.strokeCatalog?.strokes?.size,
            guideCount = packageData?.strokeCatalog?.guides?.size,
            coloringRegionCount = packageData?.coloringRegionCatalog?.regions?.size ?: if (packageData != null) 0 else null,
            supportedModes = packageData?.lesson?.supportedModes.orEmpty(),
            capabilityStates = capabilityStates,
            helpReady = projectedIndexEntry?.capabilitySummary?.helpAvailable,
            traceReady = projectedIndexEntry?.capabilitySummary?.traceReady,
            coloringCapability = projectedIndexEntry?.capabilitySummary?.coloring,
            reservedFieldUsage = reservedUsage,
            gateEvidence = gates,
            qualityWarningCount = qualityGateDiagnostics.count { it.severity == ContentStudioEvidenceSeverity.WARNING },
            projectedIndexEntryCount = projectedEntryCount,
            projectedIndexEntry = projectedIndexEntry,
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
