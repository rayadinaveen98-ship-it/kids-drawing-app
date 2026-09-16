package com.navin.kidsdrawing.product.lesson

import android.content.Context
import com.navin.kidsdrawing.lesson.content.AndroidAssetLessonSource
import com.navin.kidsdrawing.lesson.content.LessonCatalog
import com.navin.kidsdrawing.lesson.content.LessonCatalogIdentity
import com.navin.kidsdrawing.lesson.content.LessonDiagnostic
import com.navin.kidsdrawing.lesson.content.LessonDiagnosticCode
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.lab.LessonLabRuntimeCore
import com.navin.kidsdrawing.lesson.lab.LessonRuntimeIdentity
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import com.navin.kidsdrawing.product.quality.ProductTimingEvidence
import com.navin.kidsdrawing.product.quality.ProductTimingMetric
import java.io.File

/**
 * Production Android adapter over the verified lesson runtime core.
 *
 * P4.2 resolves a stable catalog identity first, then supplies a lesson-specific infrastructure
 * identity to the core. Cute Cat r1 intentionally keeps the exact Phase 3 storage IDs.
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

    /**
     * Product capability derived only from authored lesson content.
     *
     * Keeping this next to [packageData] gives every product surface the same answer and prevents
     * the completion UI or coloring runtime from inventing coloring support for a lesson that did
     * not author it.
     */
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
        ): ProductLessonRuntime = ProductTimingEvidence.measureBlocking(ProductTimingMetric.LESSON_RECOVERY) {
            val appContext = context.applicationContext
            ProductLessonRuntime(
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
            val snapshot = LessonCatalog(source).load()
            val entry = if (requestedIdentity == null) {
                snapshot.entries.firstOrNull()
            } else {
                snapshot.entries.firstOrNull { it.identity == requestedIdentity }
            }

            val identity = entry?.identity
                ?: requestedIdentity
                ?: LessonCatalogIdentity(LEGACY_FALLBACK_LESSON_ID, LEGACY_FALLBACK_REVISION)
            val packageData = entry?.let { snapshot.runtimePackage(it.identity) }
            val contentResult = if (packageData != null) {
                LessonLoadResult.Success(packageData)
            } else {
                LessonLoadResult.Failure(
                    listOf(
                        LessonDiagnostic(
                            code = LessonDiagnosticCode.INVALID_VALUE,
                            path = "catalog",
                            message = requestedIdentity?.let {
                                "Requested lesson ${it.lessonId} r${it.revision} is not available."
                            } ?: snapshot.diagnostics.firstOrNull()?.message
                                ?: "No release lesson is available.",
                        ),
                    ),
                )
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
