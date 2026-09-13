package com.navin.kidsdrawing.product.home

import android.content.Context
import com.navin.kidsdrawing.coloring.persistence.AtomicColoringSessionStore
import com.navin.kidsdrawing.coloring.session.ColoringSessionEngine
import com.navin.kidsdrawing.coloring.session.ColoringSessionPhase
import com.navin.kidsdrawing.lesson.content.AndroidAssetLessonSource
import com.navin.kidsdrawing.lesson.content.LessonCatalog
import com.navin.kidsdrawing.lesson.lab.LessonLabRuntimeCore
import com.navin.kidsdrawing.lesson.persistence.AtomicLessonSessionStore
import com.navin.kidsdrawing.product.coloring.ProductColoringRuntime
import com.navin.kidsdrawing.product.profile.ChildProfile
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Read-only product projection over authored catalog content + persisted Lesson/Coloring engine state.
 *
 * Home never owns a second session state machine. Active coloring takes presentation priority over
 * a drawing resume because a successful coloring handoff has already finished the lesson session.
 * P4.1 keeps the P3 fixed recovery IDs for compatibility while removing the hard-coded package root;
 * P4.2 will introduce true multi-lesson product routing over this catalog foundation.
 */
class StudioHomeRepository(context: Context) {
    private val appContext = context.applicationContext
    private val source = AndroidAssetLessonSource(appContext.assets)
    private val catalog = LessonCatalog(source)
    private val sessionStore = AtomicLessonSessionStore(
        File(appContext.filesDir, P2_SESSION_DIRECTORY),
    )
    private val coloringStore = AtomicColoringSessionStore(
        File(appContext.filesDir, ProductColoringRuntime.COLORING_SESSION_DIRECTORY),
    )

    suspend fun load(profile: ChildProfile): StudioHomeModel = withContext(Dispatchers.IO) {
        val catalogSnapshot = catalog.load()
        val entry = catalogSnapshot.entries.firstOrNull()
            ?: return@withContext StudioHomeModel(
                recommendation = null,
                resumeCandidate = null,
                coloringResumeCandidate = null,
                contentMessage = "Your drawing studio is ready, but its lessons need a quick refresh.",
            )
        val packageData = checkNotNull(catalogSnapshot.runtimePackage(entry.identity))

        val recommendation = StudioRecommendationPolicy.recommend(
            profile = profile,
            lesson = packageData.lesson,
            title = entry.title,
            summary = entry.summary,
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

    private companion object {
        const val P2_SESSION_DIRECTORY = "lesson-lab-sessions"
    }
}
