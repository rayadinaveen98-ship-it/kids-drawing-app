package com.navin.kidsdrawing.product.home

import android.content.Context
import com.navin.kidsdrawing.coloring.persistence.AtomicColoringSessionStore
import com.navin.kidsdrawing.coloring.session.ColoringSessionEngine
import com.navin.kidsdrawing.coloring.session.ColoringSessionPhase
import com.navin.kidsdrawing.lesson.content.AndroidAssetLessonSource
import com.navin.kidsdrawing.lesson.content.CatalogIndexV2Entry
import com.navin.kidsdrawing.lesson.content.CatalogIndexV2LoadResult
import com.navin.kidsdrawing.lesson.content.CatalogIndexV2Loader
import com.navin.kidsdrawing.lesson.persistence.AtomicLessonSessionStore
import com.navin.kidsdrawing.product.adaptive.LocalAdaptiveStateRepository
import com.navin.kidsdrawing.product.adaptive.adaptiveFreshDecisions
import com.navin.kidsdrawing.product.coloring.ProductColoringRuntime
import com.navin.kidsdrawing.product.lesson.ProductLessonRuntime
import com.navin.kidsdrawing.product.profile.ChildProfile
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Read-only product projection over the metadata-only Catalog Index V2 + persisted session state.
 *
 * Full lesson teaching packages are intentionally not loaded by Home. They are loaded lazily only
 * after a child selects a lesson. Resume UI uses the index's generated drawingStepCount instead.
 */
class StudioHomeRepository(context: Context) {
    private val appContext = context.applicationContext
    private val source = AndroidAssetLessonSource(appContext.assets)
    private val catalog = CatalogIndexV2Loader(source)
    private val sessionStore = AtomicLessonSessionStore(
        File(appContext.filesDir, ProductLessonRuntime.SESSION_DIRECTORY),
    )
    private val coloringStore = AtomicColoringSessionStore(
        File(appContext.filesDir, ProductColoringRuntime.COLORING_SESSION_DIRECTORY),
    )
    private val adaptiveRepository = LocalAdaptiveStateRepository(
        File(appContext.filesDir, LocalAdaptiveStateRepository.DIRECTORY_NAME),
    )

    suspend fun load(profile: ChildProfile): StudioHomeModel = withContext(Dispatchers.IO) {
        val catalogResult = catalog.load()
        val catalogSnapshot = (catalogResult as? CatalogIndexV2LoadResult.Success)?.snapshot
            ?: return@withContext unavailableModel()
        if (catalogSnapshot.entries.isEmpty()) return@withContext unavailableModel()

        val projected = catalogSnapshot.entries.map { entry ->
            ProjectedLesson(
                entry = entry,
                recommendation = entry.toStudioRecommendation(profile),
                runtimeIdentity = ProductLessonRuntime.runtimeIdentityFor(entry.identity),
            )
        }
        val ranked = StudioRecommendationPolicy.rank(
            profile = profile,
            recommendations = projected.map(ProjectedLesson::recommendation),
        )
        val adaptiveState = adaptiveRepository.loadForPolicy()
        val adaptiveDecisions = adaptiveState?.let { state ->
            StudioRecommendationPolicy.adaptiveFreshDecisions(
                profile = profile,
                recommendations = ranked,
                state = state,
            )
        }
        val freshRanked = adaptiveDecisions?.map { it.recommendation } ?: ranked
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
                        lesson.entry.resumeCandidate(snapshot)?.let(drawingCandidates::add)
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
                            snapshot.lessonId == lesson.entry.identity.lessonId &&
                            snapshot.lessonRevision == lesson.entry.identity.revision &&
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
            rankedRecommendations = freshRanked,
            drawingCandidates = drawingCandidates,
            coloringCandidates = coloringCandidates,
        )
        val primaryKey = primary.lessonId?.let { lessonId ->
            primary.lessonRevision?.let { revision -> lessonId to revision }
        }
        val freshPrimaryDecision = if (primary.drawingResume == null && primary.coloringResume == null) {
            primaryKey?.let { key ->
                adaptiveDecisions?.firstOrNull { decision ->
                    decision.recommendation.lessonId == key.first &&
                        decision.recommendation.lessonRevision == key.second
                }
            }
        } else {
            null
        }
        val primaryRecommendation = primaryKey?.let { key ->
            projectedByKey[key]?.recommendation?.let { baseline ->
                freshPrimaryDecision?.let { decision ->
                    baseline.copy(adaptiveReasonCopy = decision.reasonCopy)
                } ?: baseline
            }
        } ?: if (adaptiveState == null) {
            ranked.firstOrNull()
        } else {
            adaptiveDecisions?.firstOrNull()?.let { decision ->
                decision.recommendation.copy(adaptiveReasonCopy = decision.reasonCopy)
            }
        }
        val activeLessonId = primary.coloringResume?.lessonId ?: primary.drawingResume?.lessonId

        StudioHomeModel(
            recommendation = primaryRecommendation,
            resumeCandidate = primary.drawingResume,
            coloringResumeCandidate = primary.coloringResume,
            contentMessage = null,
            recommendations = ranked,
            categories = catalogCategories(ranked),
            journeys = catalogJourneys(ranked, activeLessonId),
        )
    }

    private fun unavailableModel() = StudioHomeModel(
        recommendation = null,
        resumeCandidate = null,
        coloringResumeCandidate = null,
        contentMessage = "Your drawing studio is ready, but its lessons need a quick refresh.",
        recommendations = emptyList(),
    )

    private data class ProjectedLesson(
        val entry: CatalogIndexV2Entry,
        val recommendation: LessonRecommendation,
        val runtimeIdentity: com.navin.kidsdrawing.lesson.lab.LessonRuntimeIdentity,
    )
}
