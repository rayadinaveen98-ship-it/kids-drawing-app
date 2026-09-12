package com.navin.kidsdrawing.product.lesson

import android.content.Context
import com.navin.kidsdrawing.lesson.content.AndroidAssetLessonSource
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.lab.LessonLabRuntimeCore
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import java.io.File

/**
 * Production Android adapter over the exact runtime core physically verified in Lesson Engine 0.2.
 * Storage roots and semantic IDs intentionally remain compatible with that milestone.
 */
class ProductLessonRuntime private constructor(
    context: Context,
    val contentResult: LessonLoadResult,
) : LessonLabRuntimeCore(
    documentRoot = File(context.filesDir, DOCUMENT_DIRECTORY),
    sessionRoot = File(context.filesDir, SESSION_DIRECTORY),
    lessonPackageResult = contentResult,
) {
    constructor(context: Context) : this(
        context = context.applicationContext,
        contentResult = loadContent(context.applicationContext),
    )

    val packageData: LessonRuntimePackage?
        get() = (contentResult as? LessonLoadResult.Success)?.packageData

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

        private fun loadContent(context: Context): LessonLoadResult = LessonPackageLoader(
            AndroidAssetLessonSource(context.assets),
        ).load(LESSON_ROOT)
    }
}
