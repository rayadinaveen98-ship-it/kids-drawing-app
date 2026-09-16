package com.navin.kidsdrawing.lesson.authoring

import com.navin.kidsdrawing.lesson.content.CatalogIndexV2Loader
import com.navin.kidsdrawing.lesson.model.LessonStatus

enum class ContentStudioReplacementRevisionAction {
    KEEP_REVISION,
    INCREMENT_REVISION,
}

sealed interface ContentStudioRevisionDecision {
    data object NewLesson : ContentStudioRevisionDecision

    data class ReplaceExisting(
        val previousRevision: Int,
        val action: ContentStudioReplacementRevisionAction,
    ) : ContentStudioRevisionDecision
}

/** Complete review transaction required to promote one validated lesson replacement. */
data class ContentStudioPromotionPlan(
    val packageRoot: String,
    val revisionDecision: ContentStudioRevisionDecision,
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
 * Builds a complete immutable changeset only from a production-READY validation result plus an
 * explicit revision decision.
 *
 * The plan includes the regenerated Catalog Index V2 and explicit deletion of stale files from the
 * previous package. Applying the map/list must be one repository/filesystem transaction; the core
 * never exposes a partial package-only release plan.
 */
object ContentStudioPromotionPlanner {
    fun plan(
        validation: ContentStudioValidationResult,
        existingPackageFiles: Collection<String>,
        revisionDecision: ContentStudioRevisionDecision,
    ): ContentStudioPromotionPlanResult = when (validation) {
        is ContentStudioValidationResult.Blocked -> ContentStudioPromotionPlanResult.Rejected(validation.diagnostics)
        is ContentStudioValidationResult.Ready -> planReady(validation, existingPackageFiles, revisionDecision)
    }

    private fun planReady(
        validation: ContentStudioValidationResult.Ready,
        existingPackageFiles: Collection<String>,
        revisionDecision: ContentStudioRevisionDecision,
    ): ContentStudioPromotionPlanResult {
        if (validation.evidence.lessonStatus != LessonStatus.RELEASE) {
            return rejected(
                ContentStudioDiagnosticCode.RELEASE_STATUS_INVALID,
                "lesson.status",
                "Release promotion requires lesson status RELEASE; staged status is ${validation.evidence.lessonStatus ?: "unavailable"}.",
            )
        }

        val root = validation.stagedPackage.packageRoot.trimEnd('/')
        val prefix = "$root/"
        val stagedFiles = validation.stagedPackage.files
        val invalidStagedPath = stagedFiles.keys.firstOrNull { !it.startsWith(prefix) }
        if (invalidStagedPath != null) {
            return rejected(
                ContentStudioDiagnosticCode.INVALID_ASSET_PATH,
                invalidStagedPath,
                "Staged package contains a file outside '$root'.",
            )
        }

        val existing = existingPackageFiles
            .filter { it.startsWith(prefix) }
            .distinct()
            .sorted()
        val candidateRevision = validation.evidence.revision
            ?: return rejected(
                ContentStudioDiagnosticCode.REVISION_DECISION_INVALID,
                "lesson.revision",
                "Validated candidate revision is unavailable.",
            )

        val revisionDiagnostic = when (revisionDecision) {
            ContentStudioRevisionDecision.NewLesson -> {
                if (existing.isNotEmpty()) {
                    "NewLesson was selected but an existing package is present at '$root'."
                } else {
                    null
                }
            }

            is ContentStudioRevisionDecision.ReplaceExisting -> {
                when {
                    existing.isEmpty() ->
                        "ReplaceExisting was selected but no existing package is present at '$root'."
                    revisionDecision.previousRevision < 1 ->
                        "Previous revision must be positive."
                    revisionDecision.action == ContentStudioReplacementRevisionAction.KEEP_REVISION &&
                        candidateRevision != revisionDecision.previousRevision ->
                        "KEEP_REVISION requires candidate revision ${revisionDecision.previousRevision}; found $candidateRevision."
                    revisionDecision.action == ContentStudioReplacementRevisionAction.INCREMENT_REVISION &&
                        candidateRevision != revisionDecision.previousRevision + 1 ->
                        "INCREMENT_REVISION requires candidate revision ${revisionDecision.previousRevision + 1}; found $candidateRevision."
                    else -> null
                }
            }
        }
        if (revisionDiagnostic != null) {
            return rejected(
                ContentStudioDiagnosticCode.REVISION_DECISION_INVALID,
                "lesson.revision",
                revisionDiagnostic,
            )
        }

        val deletes = existing.filterNot(stagedFiles::containsKey)
        val writes = sortedMapOf<String, String>().apply {
            putAll(stagedFiles)
            put(CatalogIndexV2Loader.DEFAULT_INDEX_PATH, validation.projectedIndexText)
        }
        return ContentStudioPromotionPlanResult.Ready(
            ContentStudioPromotionPlan(
                packageRoot = root,
                revisionDecision = revisionDecision,
                writeFiles = writes.toMap(),
                deleteFiles = deletes,
            ),
        )
    }

    private fun rejected(
        code: ContentStudioDiagnosticCode,
        path: String,
        message: String,
    ) = ContentStudioPromotionPlanResult.Rejected(
        listOf(ContentStudioDiagnostic(code, path, message)),
    )
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
        revisionDecision: ContentStudioRevisionDecision,
        target: ContentStudioPromotionTarget,
    ): ContentStudioPromotionResult = when (
        val planned = ContentStudioPromotionPlanner.plan(validation, existingPackageFiles, revisionDecision)
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
