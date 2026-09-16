package com.navin.kidsdrawing.lesson.content

/**
 * Lazy bridge from metadata discovery to full lesson execution.
 *
 * Loading the index never loads a teaching package. Only the selected packageRef is passed to the
 * accepted LessonPackageLoader, then identity/capability truth is rechecked before execution.
 */
class CatalogSelectedLessonLoader(
    private val indexLoader: CatalogIndexV2Loader,
    private val packageLoader: LessonPackageLoader,
) {
    fun load(identity: LessonCatalogIdentity? = null): SelectedLessonLoadResult {
        val indexResult = indexLoader.load()
        if (indexResult is CatalogIndexV2LoadResult.Failure) {
            return SelectedLessonLoadResult.Failure(
                indexResult.diagnostics.map { diagnostic ->
                    LessonDiagnostic(
                        code = LessonDiagnosticCode.INVALID_VALUE,
                        path = "catalog.index.${diagnostic.path}",
                        message = diagnostic.message,
                    )
                },
            )
        }
        val snapshot = (indexResult as CatalogIndexV2LoadResult.Success).snapshot
        val entry = if (identity == null) {
            snapshot.entries.firstOrNull()
        } else {
            snapshot.entry(identity)
        } ?: return SelectedLessonLoadResult.Failure(
            listOf(
                LessonDiagnostic(
                    code = LessonDiagnosticCode.INVALID_VALUE,
                    path = "catalog.selection",
                    message = identity?.let {
                        "Requested lesson ${it.lessonId} r${it.revision} is not available."
                    } ?: "No release lesson is available.",
                ),
            ),
        )

        return when (val packageResult = packageLoader.load(entry.packageRef)) {
            is LessonLoadResult.Failure -> SelectedLessonLoadResult.Failure(packageResult.diagnostics)
            is LessonLoadResult.Success -> {
                val packageData = packageResult.packageData
                val actual = LessonCatalogIdentity(packageData.lesson.lessonId, packageData.lesson.revision)
                if (actual != entry.identity) {
                    return SelectedLessonLoadResult.Failure(
                        listOf(
                            LessonDiagnostic(
                                code = LessonDiagnosticCode.INVALID_VALUE,
                                path = "catalog.selection.identity",
                                message = "Index identity ${entry.identity.lessonId} r${entry.identity.revision} does not match package ${actual.lessonId} r${actual.revision}.",
                            ),
                        ),
                    )
                }
                val capabilityDiagnostics = LessonCapabilityValidator.validate(packageData)
                if (capabilityDiagnostics.isNotEmpty()) {
                    return SelectedLessonLoadResult.Failure(
                        capabilityDiagnostics.map { diagnostic ->
                            LessonDiagnostic(
                                code = LessonDiagnosticCode.INVALID_VALUE,
                                path = "catalog.selection.${diagnostic.path}",
                                message = diagnostic.message,
                            )
                        },
                    )
                }
                SelectedLessonLoadResult.Success(entry, packageData)
            }
        }
    }
}

sealed interface SelectedLessonLoadResult {
    data class Success(
        val entry: CatalogIndexV2Entry,
        val packageData: com.navin.kidsdrawing.lesson.model.LessonRuntimePackage,
    ) : SelectedLessonLoadResult

    data class Failure(val diagnostics: List<LessonDiagnostic>) : SelectedLessonLoadResult
}
