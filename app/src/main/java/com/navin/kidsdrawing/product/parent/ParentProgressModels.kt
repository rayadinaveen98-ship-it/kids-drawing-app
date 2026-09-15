package com.navin.kidsdrawing.product.parent

import com.navin.kidsdrawing.gallery.domain.GalleryArtworkCardModel
import com.navin.kidsdrawing.gallery.domain.GalleryArtworkSource
import com.navin.kidsdrawing.gallery.domain.GalleryCompletionKind
import com.navin.kidsdrawing.gallery.domain.GalleryListResult
import com.navin.kidsdrawing.product.adaptive.AdaptiveLessonIdentity
import com.navin.kidsdrawing.product.adaptive.LocalAdaptiveReadResult
import com.navin.kidsdrawing.product.adaptive.LocalAdaptiveState
import com.navin.kidsdrawing.product.home.ColoringResumeCandidate
import com.navin.kidsdrawing.product.home.LessonRecommendation
import com.navin.kidsdrawing.product.home.ResumeLessonCandidate
import com.navin.kidsdrawing.product.home.StudioHomeModel
import com.navin.kidsdrawing.product.home.StudioJourney

/** Parent-facing projection only. It is descriptive and never mutates learning/artwork truth. */
data class ParentProgressModel(
    val completedLessonCount: Int,
    val totalCurriculumLessons: Int,
    val savedArtworkCount: Int,
    val recentCompletions: List<ParentProgressLesson>,
    val exploredCategories: List<String>,
    val practicedSkills: List<String>,
    val journeys: List<ParentProgressJourney>,
    val recentArtwork: List<ParentProgressArtwork>,
    val inProgress: List<ParentProgressInProgress>,
    val notices: List<String>,
) {
    val hasLearningHistory: Boolean
        get() = completedLessonCount > 0 || savedArtworkCount > 0 || inProgress.isNotEmpty()
}

data class ParentProgressLesson(
    val lessonId: String,
    val revision: Int,
    val title: String,
    val categories: List<String>,
    val skills: List<String>,
    val journeys: List<String>,
)

data class ParentProgressJourney(
    val journeyId: String,
    val title: String,
    val completedLessons: Int,
    val totalLessons: Int,
    val nextAvailableLessonTitle: String?,
)

data class ParentProgressArtwork(
    val entryId: String,
    val title: String,
    val sourceLabel: String,
    val completionLabel: String,
    val completedAtEpochMillis: Long,
    val lessonId: String?,
)

data class ParentProgressInProgress(
    val lessonId: String,
    val title: String,
    val kindLabel: String,
    val savedAtEpochMillis: Long,
)

object ParentProgressProjection {
    private const val MAX_RECENT_ARTWORK = 8
    private const val MAX_VISIBLE_SKILLS = 12

    fun project(
        home: StudioHomeModel,
        adaptiveRead: LocalAdaptiveReadResult,
        galleryResult: GalleryListResult,
    ): ParentProgressModel {
        val adaptiveState = (adaptiveRead as? LocalAdaptiveReadResult.Loaded)?.state
        val recommendationsByIdentity = home.recommendations.associateBy {
            AdaptiveLessonIdentity(it.lessonId, it.lessonRevision)
        }
        val recommendationsByLessonId = home.recommendations.associateBy { it.lessonId }
        val completedIdentities = adaptiveState?.completedLessons.orEmpty()
        val completedIds = completedIdentities.mapTo(linkedSetOf()) { it.lessonId }

        val recentCompletions = adaptiveState?.recentCompletions
            .orEmpty()
            .asReversed()
            .mapNotNull(recommendationsByIdentity::get)
            .map(::lessonModel)

        val completedRecommendations = completedIdentities.mapNotNull(recommendationsByIdentity::get)
        val exploredCategories = completedRecommendations
            .flatMap(LessonRecommendation::categoryIds)
            .distinct()
            .sorted()
            .map(::taxonomyLabel)

        val practicedSkills = adaptiveState?.skillExposureCounts
            .orEmpty()
            .filterValues { it > 0 }
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { taxonomyLabel(it.key) }
            .distinct()
            .take(MAX_VISIBLE_SKILLS)

        val journeys = home.journeys.map { journey ->
            journeyModel(journey, completedIds)
        }

        val galleryCards = when (galleryResult) {
            is GalleryListResult.Ready -> galleryResult.cards
            GalleryListResult.Empty,
            is GalleryListResult.Unavailable,
            -> emptyList()
        }
        val recentArtwork = galleryCards
            .take(MAX_RECENT_ARTWORK)
            .map(::artworkModel)

        val inProgress = buildList {
            home.coloringResumeCandidate?.let { candidate ->
                add(inProgressModel(candidate, recommendationsByLessonId[candidate.lessonId]))
            }
            home.resumeCandidate?.let { candidate ->
                if (none { it.lessonId == candidate.lessonId && it.kindLabel == "Coloring in progress" }) {
                    add(inProgressModel(candidate, recommendationsByLessonId[candidate.lessonId]))
                }
            }
        }

        val notices = buildList {
            when (adaptiveRead) {
                is LocalAdaptiveReadResult.Corrupt -> add(
                    "Some saved learning history could not be read. Artwork and current curriculum are still shown when available.",
                )
                is LocalAdaptiveReadResult.Incompatible -> add(
                    "Saved learning history was created by a newer app format and is not shown here.",
                )
                is LocalAdaptiveReadResult.Unavailable -> add(
                    "Saved learning history is temporarily unavailable. Nothing was changed or deleted.",
                )
                LocalAdaptiveReadResult.Missing,
                is LocalAdaptiveReadResult.Loaded,
                -> Unit
            }
            if (galleryResult is GalleryListResult.Unavailable) {
                add("Saved artwork activity is temporarily unavailable. Artwork files were left untouched.")
            }
            home.contentMessage?.let {
                add("Some curriculum content is unavailable right now, so this view may be partial.")
            }
            val unknownCompleted = completedIdentities.count { it !in recommendationsByIdentity }
            if (unknownCompleted > 0) {
                add("$unknownCompleted completed lesson${if (unknownCompleted == 1) "" else "s"} are no longer in the current curriculum catalog.")
            }
        }

        return ParentProgressModel(
            completedLessonCount = completedIdentities.size,
            totalCurriculumLessons = home.recommendations.distinctBy { it.lessonId to it.lessonRevision }.size,
            savedArtworkCount = galleryCards.size,
            recentCompletions = recentCompletions,
            exploredCategories = exploredCategories,
            practicedSkills = practicedSkills,
            journeys = journeys,
            recentArtwork = recentArtwork,
            inProgress = inProgress,
            notices = notices.distinct(),
        )
    }

    private fun lessonModel(recommendation: LessonRecommendation): ParentProgressLesson =
        ParentProgressLesson(
            lessonId = recommendation.lessonId,
            revision = recommendation.lessonRevision,
            title = recommendation.title,
            categories = recommendation.categoryIds.map(::taxonomyLabel),
            skills = recommendation.primarySkillIds.map(::taxonomyLabel),
            journeys = recommendation.journeyIds.map(::taxonomyLabel),
        )

    private fun journeyModel(
        journey: StudioJourney,
        completedIds: Set<String>,
    ): ParentProgressJourney {
        val completedCount = journey.lessons.count { it.lessonId in completedIds }
        val nextAvailable = journey.lessons.firstOrNull { lesson ->
            lesson.lessonId !in completedIds &&
                lesson.prerequisiteLessonIds.all(completedIds::contains)
        }
        return ParentProgressJourney(
            journeyId = journey.journeyId,
            title = journey.title,
            completedLessons = completedCount,
            totalLessons = journey.lessons.size,
            nextAvailableLessonTitle = nextAvailable?.title,
        )
    }

    private fun artworkModel(card: GalleryArtworkCardModel): ParentProgressArtwork {
        val record = card.record
        return ParentProgressArtwork(
            entryId = record.entryId,
            title = record.title,
            sourceLabel = when (record.source) {
                GalleryArtworkSource.LESSON -> "Lesson artwork"
                GalleryArtworkSource.FREE_DRAW -> "Free Draw artwork"
            },
            completionLabel = when (record.completionKind) {
                GalleryCompletionKind.DRAWING_ONLY -> "Drawing saved"
                GalleryCompletionKind.COLORED -> "Colored artwork saved"
            },
            completedAtEpochMillis = record.completedAtEpochMillis,
            lessonId = record.lessonId,
        )
    }

    private fun inProgressModel(
        candidate: ColoringResumeCandidate,
        recommendation: LessonRecommendation?,
    ): ParentProgressInProgress = ParentProgressInProgress(
        lessonId = candidate.lessonId,
        title = recommendation?.title ?: "Saved lesson",
        kindLabel = "Coloring in progress",
        savedAtEpochMillis = candidate.savedAtEpochMillis,
    )

    private fun inProgressModel(
        candidate: ResumeLessonCandidate,
        recommendation: LessonRecommendation?,
    ): ParentProgressInProgress = ParentProgressInProgress(
        lessonId = candidate.lessonId,
        title = recommendation?.title ?: "Saved lesson",
        kindLabel = "Drawing lesson in progress",
        savedAtEpochMillis = candidate.savedAtEpochMillis,
    )

    internal fun taxonomyLabel(value: String): String = value
        .split('-', '_', '.')
        .filter(String::isNotBlank)
        .joinToString(" ") { token -> token.replaceFirstChar { it.uppercaseChar() } }
}
