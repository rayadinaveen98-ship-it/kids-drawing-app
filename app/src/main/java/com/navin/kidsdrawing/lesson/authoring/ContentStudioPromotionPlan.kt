package com.navin.kidsdrawing.lesson.authoring

import com.navin.kidsdrawing.lesson.content.CatalogIndexV2Loader

/** Complete review transaction required to promote one validated lesson replacement. */
data class ContentStudioPromotionPlan(
    val packageRoot: String,
    val writeFiles: Map<String, String>,
    val deleteFiles: List<String>,
) {
    init {
        require(CatalogIndexV2Loader.DEFAULT_INDEX_PATH in writeFiles)
        require(writeFiles.keys.none(deleteFiles::contains))
    }
}

sealed interface ContentStudioPromotionPlanResult {
    data class Ready(val plan: ContentStudioPromotionPlan) : ContentStudioPromotionPlanResult
    data class Rejected(val diagnostics: List<ContentStudioDiagnostic>) : ContentStudioPromotionPlanResult
}

/**
 * Builds a complete immutable changeset only from a production-READY validation result.
 *
 * The plan includes the regenerated Catalog Index V2 and explicit deletion of stale files from the
 * previous package. Applying the map/list must be one repository/filesystem transaction; the core
 * never exposes a partial package-only release plan.
 */
object ContentStudioPromotionPlanner {
    fun plan(
        validation: ContentStudioValidationResult,
        existingPackageFiles: Collection<String>,
    ): ContentStudioPromotionPlanResult = when (validation) {
        is ContentStudioValidationResult.Blocked -> ContentStudioPromotionPlanResult.Rejected(validation.diagnostics)
        is ContentStudioValidationResult.Ready -> {
            val root = validation.stagedPackage.packageRoot.trimEnd('/')
            val prefix = "$root/"
            val stagedFiles = validation.stagedPackage.files
            val invalidStagedPath = stagedFiles.keys.firstOrNull { !it.startsWith(prefix) }
            if (invalidStagedPath != null) {
                ContentStudioPromotionPlanResult.Rejected(
                    listOf(
                        ContentStudioDiagnostic(
                            ContentStudioDiagnosticCode.INVALID_ASSET_PATH,
                            invalidStagedPath,
                            "Staged package contains a file outside '$root'.",
                        ),
                    ),
                )
            } else {
                val existing = existingPackageFiles
                    .filter { it.startsWith(prefix) }
                    .distinct()
                    .sorted()
                val deletes = existing.filterNot(stagedFiles::containsKey)
                val writes = sortedMapOf<String, String>().apply {
                    putAll(stagedFiles)
                    put(CatalogIndexV2Loader.DEFAULT_INDEX_PATH, validation.projectedIndexText)
                }
                ContentStudioPromotionPlanResult.Ready(
                    ContentStudioPromotionPlan(
                        packageRoot = root,
                        writeFiles = writes.toMap(),
                        deleteFiles = deletes,
                    ),
                )
            }
        }
    }
}

/**
 * Transactional target boundary. Implementations must either commit every requested write/delete or
 * leave their previous state unchanged.
 */
fun interface ContentStudioPromotionTarget {
    fun applyAtomically(plan: ContentStudioPromotionPlan): Boolean
}

sealed interface ContentStudioPromotionResult {
    data object Applied : ContentStudioPromotionResult
    data class Rejected(val diagnostics: List<ContentStudioDiagnostic>) : ContentStudioPromotionResult
    data object TargetFailedWithoutCommit : ContentStudioPromotionResult
}

object ContentStudioPromotionService {
    fun promote(
        validation: ContentStudioValidationResult,
        existingPackageFiles: Collection<String>,
        target: ContentStudioPromotionTarget,
    ): ContentStudioPromotionResult = when (
        val planned = ContentStudioPromotionPlanner.plan(validation, existingPackageFiles)
    ) {
        is ContentStudioPromotionPlanResult.Rejected -> ContentStudioPromotionResult.Rejected(planned.diagnostics)
        is ContentStudioPromotionPlanResult.Ready -> {
            if (target.applyAtomically(planned.plan)) {
                ContentStudioPromotionResult.Applied
            } else {
                ContentStudioPromotionResult.TargetFailedWithoutCommit
            }
        }
    }
}
