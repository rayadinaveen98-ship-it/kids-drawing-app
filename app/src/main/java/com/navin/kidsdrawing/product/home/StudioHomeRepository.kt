package com.navin.kidsdrawing.product.home

import android.content.Context
import com.navin.kidsdrawing.coloring.persistence.AtomicColoringSessionStore
import com.navin.kidsdrawing.coloring.session.ColoringSessionEngine
import com.navin.kidsdrawing.coloring.session.ColoringSessionPhase
import com.navin.kidsdrawing.lesson.content.AndroidAssetLessonSource
import com.navin.kidsdrawing.lesson.content.LessonLoadResult
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.lab.LessonLabRuntimeCore
import com.navin.kidsdrawing.lesson.persistence.AtomicLessonSessionStore
import com.navin.kidsdrawing.product.coloring.ProductColoringRuntime
import com.navin.kidsdrawing.product.profile.ChildProfile
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Read-only product projection over authored content + persisted Lesson/Coloring engine state.
 *
 * Home never owns a second session state machine. Active coloring takes presentation priority over
 * a drawing resume because a successful coloring handoff has already finished the lesson session.
 */
class StudioHomeRepository(context: Context) {
    private val appContext = context.applicationContext
    private val source = AndroidAssetLessonSource(appContext.assets)
    private val loader = LessonPackageLoader(source)
    private val sessionStore = AtomicLessonSessionStore(
        File(appContext.filesDir, P2_SESSION_DIRECTORY),
    )
    private val coloringStore = AtomicColoringSessionStore(
        File(appContext.filesDir, ProductColoringRuntime.COLORING_SESSION_DIRECTORY),
    )

    suspend fun load(profile: ChildProfile): StudioHomeModel = withContext(Dispatchers.IO) {
        when (val loaded = loader.load(LessonLabRuntimeCore.LESSON_ROOT)) {
            is LessonLoadResult.Failure -> StudioHomeModel(
                recommendation = null,
                resumeCandidate = null,
                coloringResumeCandidate = null,
                contentMessage = "Your drawing studio is ready, but this lesson needs a quick refresh.",
            )

            is LessonLoadResult.Success -> {
                val packageData = loaded.packageData
                val strings = loadStrings(packageData.packageRoot, packageData.lesson.assets.strings["en"])
                val title = strings[packageData.lesson.metadata.titleKey]
                    ?: packageData.lesson.lessonId.toDisplayTitle()
                val summary = strings[packageData.lesson.metadata.summaryKey]
                    ?: "A calm step-by-step drawing lesson."
                val recommendation = StudioRecommendationPolicy.recommend(
                    profile = profile,
                    lesson = packageData.lesson,
                    title = title,
                    summary = summary,
                )
                val resume = when (val persisted = sessionStore.load(LessonLabRuntimeCore.SESSION_ID)) {
                    is AtomicLessonSessionStore.LoadResult.Loaded ->
                        StudioRecommendationPolicy.resumeCandidate(
                            snapshot = persisted.snapshot,
                            lesson = packageData.lesson,
                        )

                    AtomicLessonSessionStore.LoadResult.Missing,
                    is AtomicLessonSessionStore.LoadResult.Corrupt,
                    -> null
                }
                val coloringSessionId = ColoringSessionEngine.sessionIdFor(LessonLabRuntimeCore.DOCUMENT_ID)
                val coloringResume = when (val persisted = coloringStore.load(coloringSessionId)) {
                    is AtomicColoringSessionStore.LoadResult.Loaded -> persisted.snapshot
                        .takeIf { snapshot ->
                            snapshot.phase == ColoringSessionPhase.ACTIVE &&
                                snapshot.lessonId == packageData.lesson.lessonId &&
                                snapshot.lessonRevision == packageData.lesson.revision &&
                                snapshot.childDocumentId == LessonLabRuntimeCore.DOCUMENT_ID
                        }
                        ?.let { snapshot ->
                            ColoringResumeCandidate(
                                sessionId = snapshot.sessionId,
                                lessonId = snapshot.lessonId,
                                lessonRevision = snapshot.lessonRevision,
                                childDocumentId = snapshot.childDocumentId,
                                savedAtEpochMillis = snapshot.savedAtEpochMillis,
                            )
                        }

                    AtomicColoringSessionStore.LoadResult.Missing,
                    is AtomicColoringSessionStore.LoadResult.Corrupt,
                    -> null
                }

                StudioHomeModel(
                    recommendation = recommendation,
                    resumeCandidate = if (coloringResume == null) resume else null,
                    coloringResumeCandidate = coloringResume,
                )
            }
        }
    }

    private fun loadStrings(packageRoot: String, relativePath: String?): Map<String, String> {
        if (relativePath.isNullOrBlank()) return emptyMap()
        val path = "${packageRoot.trimEnd('/')}/${relativePath.trimStart('/')}"
        val text = source.readText(path) ?: return emptyMap()
        return runCatching {
            Json.decodeFromString<Map<String, String>>(text)
        }.getOrDefault(emptyMap())
    }

    private fun String.toDisplayTitle(): String = split('-', '_')
        .filter(String::isNotBlank)
        .joinToString(" ") { token -> token.replaceFirstChar { it.uppercaseChar() } }

    private companion object {
        const val P2_SESSION_DIRECTORY = "lesson-lab-sessions"
    }
}
