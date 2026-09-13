package com.navin.kidsdrawing.product.home

import android.content.Context
import com.navin.kidsdrawing.coloring.persistence.AtomicColoringSessionStore
import com.navin.kidsdrawing.coloring.session.ColoringSessionEngine
import com.navin.kidsdrawing.coloring.session.ColoringSessionPhase
import com.navin.kidsdrawing.lesson.content.AndroidAssetLessonSource
import com.navin.kidsdrawing.lesson.content.LessonCatalog
import com.navin.kidsdrawing.lesson.persistence.AtomicLessonSessionStore
import com.navin.kidsdrawing.product.coloring.ProductColoringRuntime
import com.navin.kidsdrawing.product.lesson.ProductLessonRuntime
import com.navin.kidsdrawing.product.profile.ChildProfile
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Read-only product projection over authored catalog content + persisted Lesson/Coloring state.
 *
 * P4.2 scans every installed release lesson using its deterministic runtime identity. One corrupt or
 * missing lesson state is isolated; active coloring wins, otherwise newest drawing resume wins,
 * otherwise the highest deterministic fresh recommendation becomes primary.
 */
class StudioHomeRepository(context: Context) {
    private val appContext = context.applicationContext
    private val source = AndroidAssetLessonSource(appContext.assets)
    private val catalog = LessonCatalog(source)
    private val sessionStore = AtomicLessonSessionStore(
        File(appContext.filesDir, ProductLessonRuntime.SESSION_DIRECTORY),
    )
    private val coloringStore = AtomicColoringSessionStore(
        File(appContext.filesDir, ProductColoringRuntime.COLORING_SESSION_DIRECTORY),
    )

    suspend fun load(profile: ChildProfile): StudioHomeModel = withContext(Dispatchers.IO) {
        val catalogSnapshot = catalog.load()
        if (catalogSnapshot.entries.isEmpty()) {
            return@withContext StudioHomeModel(
                recommendation = null,
                resumeCandidate = null,
                coloringResumeCandidate = null,
                contentMessage = "Your drawing studio is ready, but its lessons need a quick refresh.",
                recommendations = emptyList(),
            )
        }

        val projected = catalogSnapshot.entries.mapNotNull { entry ->
            val packageData = catalogSnapshot.runtimePackage(entry.identity) ?: return@mapNotNull null
            val recommendation = StudioRecommendationPolicy.recommend(
                profile = profile,
                lesson = packageData.lesson,
                title = entry.title,
                summary = entry.summary,
            )
            ProjectedLesson(
                recommendation = recommendation,
                packageData = packageData,
                runtimeIdentity = ProductLessonRuntime.runtimeIdentityFor(entry.identity),
            )
        }
        val ranked = StudioRecommendationPolicy.rank(
            profile = profile,
            recommendations = projected.map(ProjectedLesson::recommendation),
        )
        val projectedByKey = projected.associateBy {
            it.recommendation.lessonId to it.recommendation.lessonRevision
        }

        val drawingCandidates = mutableListOf<ResumeLessonCandidate>()
        val coloringCandidates = mutableListOf<ColoringResumeCandidate>()

        projected.forEach { lesson ->
            when (val persisted = sessionStore.load(lesson.runtimeIdentity.sessionId)) {
                is AtomicLessonSessionStore.LoadResult.Loaded -> {
                    val snapshot = persisted.snapshot
                    if (snapshot.sessionId == lesson.runtimeIdentity.sessionId &&
                        snapshot.childDocumentId == lesson.runtimeIdentity.documentId
                    ) {
                        StudioRecommendationPolicy.resumeCandidate(
                            snapshot = snapshot,
                            lesson = lesson.packageData.lesson,
                        )?.let(drawingCandidates::add)
                    }
                }

                AtomicLessonSessionStore.LoadResult.Missing,
                is AtomicLessonSessionStore.LoadResult.Corrupt,
                -> Unit
            }

            val coloringSessionId = ColoringSessionEngine.sessionIdFor(lesson.runtimeIdentity.documentId)
            when (val persisted = coloringStore.load(coloringSessionId)) {
                is AtomicColoringSessionStore.LoadResult.Loaded -> persisted.snapshot
                    .takeIf { snapshot ->
                        snapshot.phase == ColoringSessionPhase.ACTIVE &&
                            snapshot.lessonId == lesson.packageData.lesson.lessonId &&
                            snapshot.lessonRevision == lesson.packageData.lesson.revision &&
                            snapshot.childDocumentId == lesson.runtimeIdentity.documentId
                    }
                    ?.let { snapshot ->
                        coloringCandidates += ColoringResumeCandidate(
                            sessionId = snapshot.sessionId,
                            lessonId = snapshot.lessonId,
                            lessonRevision = snapshot.lessonRevision,
                            childDocumentId = snapshot.childDocumentId,
                            savedAtEpochMillis = snapshot.savedAtEpochMillis,
                        )
                    }

                AtomicColoringSessionStore.LoadResult.Missing,
                is AtomicColoringSessionStore.LoadResult.Corrupt,
                -> Unit
            }
        }

        val primary = StudioPrimarySelectionPolicy.select(
            rankedRecommendations = ranked,
            drawingCandidates = drawingCandidates,
            coloringCandidates = coloringCandidates,
        )
        val primaryKey = primary.lessonId?.let { lessonId ->
            primary.lessonRevision?.let { revision -> lessonId to revision }
        }
        val primaryRecommendation = primaryKey?.let { key ->
            projectedByKey[key]?.recommendation
        } ?: ranked.firstOrNull()
        val activeLessonId = primary.coloringResume?.lessonId ?: primary.drawingResume?.lessonId

        StudioHomeModel(
            recommendation = primaryRecommendation,
            resumeCandidate = primary.drawingResume,
            coloringResumeCandidate = primary.coloringResume,
            contentMessage = if (catalogSnapshot.diagnostics.isNotEmpty()) {
                "A few studio lessons are resting, but the ready ones are safe to use."
            } else {
                null
            },
            recommendations = ranked,
            categories = StudioRecommendationPolicy.categories(ranked),
            journeys = StudioRecommendationPolicy.journeys(ranked, activeLessonId),
        )
    }

    private data class ProjectedLesson(
        val recommendation: LessonRecommendation,
        val packageData: com.navin.kidsdrawing.lesson.model.LessonRuntimePackage,
        val runtimeIdentity: com.navin.kidsdrawing.lesson.lab.LessonRuntimeIdentity,
    )
}
