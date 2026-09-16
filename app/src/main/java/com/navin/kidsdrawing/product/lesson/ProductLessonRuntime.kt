package com.navin.kidsdrawing.product.lesson

import android.content.Context
import com.navin.kidsdrawing.lesson.content.AndroidAssetLessonSource
import com.navin.kidsdrawing.lesson.content.CatalogIndexV2Loader
import com.navin.kidsdrawing.lesson.content.CatalogSelectedLessonLoader
import com.navin.kidsdrawing.lesson.content.LessonCatalogIdentity
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.content.SelectedLessonLoadResult
import com.navin.kidsdrawing.lesson.lab.LessonLabRuntimeCore
import com.navin.kidsdrawing.lesson.lab.LessonRuntimeIdentity
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import java.io.File

/**
 * Production Android adapter over the verified lesson runtime core.
 *
 * Content V2.2 resolves lightweight catalog metadata first and loads only the selected full lesson
 * package before execution. The package is identity- and capability-validated by
 * [CatalogSelectedLessonLoader] before it reaches the Lesson Engine.
 */
class ProductLessonRuntime private constructor(
    context: Context,
    val catalogIdentity: LessonCatalogIdentity,
    val contentResult: LessonLoadResult,
    runtimeIdentity: LessonRuntimeIdentity,
) : LessonLabRuntimeCore(
    documentRoot = File(context.filesDir, DOCUMENT_DIRECTORY),
    sessionRoot = File(context.filesDir, SESSION_DIRECTORY),
    lessonPackageResult = contentResult,
    runtimeIdentity = runtimeIdentity,
) {
    private constructor(
        context: Context,
        selection: RuntimeSelection,
    ) : this(
        context = context,
        catalogIdentity = selection.identity,
        contentResult = selection.contentResult,
        runtimeIdentity = selection.runtimeIdentity,
    )

    constructor(context: Context) : this(
        context = context.applicationContext,
        selection = resolveSelection(context.applicationContext, requestedIdentity = null),
    )

    val packageData: LessonRuntimePackage?
        get() = (contentResult as? LessonLoadResult.Success)?.packageData

    /** Product capability derived only from the fully validated selected lesson package. */
    val coloringAvailable: Boolean
        get() = packageData?.lesson?.coloring?.enabled == true

    /** Product-facing name for the already-verified typed ColoringHandoffCompleted bridge. */
    suspend fun acknowledgeColoringInitialized() {
        simulateColoringContractAck()
    }

    /** Product-facing failure path; Lesson Engine returns to AwaitingPostDrawingChoice safely. */
    suspend fun rejectColoringInitialization() {
        simulateColoringUnavailable()
    }

    companion object {
        const val DOCUMENT_DIRECTORY = "lesson-lab-documents"
        const val SESSION_DIRECTORY = "lesson-lab-sessions"

        fun forLesson(
            context: Context,
            identity: LessonCatalogIdentity,
        ): ProductLessonRuntime {
            val appContext = context.applicationContext
            return ProductLessonRuntime(
                context = appContext,
                selection = resolveSelection(appContext, requestedIdentity = identity),
            )
        }

        fun runtimeIdentityFor(identity: LessonCatalogIdentity): LessonRuntimeIdentity =
            ProductLessonIdentityPolicy.forLesson(identity)

        private fun resolveSelection(
            context: Context,
            requestedIdentity: LessonCatalogIdentity?,
        ): RuntimeSelection {
            val source = AndroidAssetLessonSource(context.assets)
            val selected = CatalogSelectedLessonLoader(
                indexLoader = CatalogIndexV2Loader(source),
                packageLoader = LessonPackageLoader(source),
            ).load(requestedIdentity)

            val identity = when (selected) {
                is SelectedLessonLoadResult.Success -> selected.entry.identity
                is SelectedLessonLoadResult.Failure -> requestedIdentity
                    ?: LessonCatalogIdentity(LEGACY_FALLBACK_LESSON_ID, LEGACY_FALLBACK_REVISION)
            }
            val contentResult = when (selected) {
                is SelectedLessonLoadResult.Success -> LessonLoadResult.Success(selected.packageData)
                is SelectedLessonLoadResult.Failure -> LessonLoadResult.Failure(selected.diagnostics)
            }
            return RuntimeSelection(
                identity = identity,
                contentResult = contentResult,
                runtimeIdentity = ProductLessonIdentityPolicy.forLesson(identity),
            )
        }

        private const val LEGACY_FALLBACK_LESSON_ID = "cute-cat"
        private const val LEGACY_FALLBACK_REVISION = 1
    }

    private data class RuntimeSelection(
        val identity: LessonCatalogIdentity,
        val contentResult: LessonLoadResult,
        val runtimeIdentity: LessonRuntimeIdentity,
    )
}
