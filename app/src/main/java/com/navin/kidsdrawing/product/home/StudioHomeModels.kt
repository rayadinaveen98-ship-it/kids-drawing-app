package com.navin.kidsdrawing.product.home

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.AgeBand as LessonAgeBand
import com.navin.kidsdrawing.lesson.model.LessonSource
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import kotlin.math.abs

enum class StudioDestination {
    HOME,
    LESSON_START,
    LESSON_RESUME,
    COLORING_RESUME,
    LESSON_SELECTED,
    CATEGORY,
    JOURNEY,
    ART_JOURNEY,
    FREE_DRAW,
    GALLERY,
    PARENT_ZONE,
}

enum class LessonAgeFit {
    EXACT,
    FALLBACK,
}

enum class RecommendationReason {
    INTEREST_MATCH,
    AGE_MATCH,
    STARTER_PICK,
}

data class LessonRecommendation(
    val lessonId: String,
    val lessonRevision: Int,
    val title: String,
    val summary: String,
    val estimatedMinutes: Int,
    val difficulty: Int,
    val defaultMode: TeachingMode,
    val defaultPace: TeachingPace,
    val ageFit: LessonAgeFit,
    val reason: RecommendationReason,
    val primarySkillIds: List<String>,
    val journeyIds: List<String>,
    val categoryIds: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val prerequisiteLessonIds: List<String> = emptyList(),
    val adaptiveReasonCopy: String? = null,
) {
    val actionLabel: String
        get() = when (defaultMode) {
            TeachingMode.DRAW_WITH_ME -> "Draw together"
            TeachingMode.WATCH_THEN_DRAW -> "Watch, then draw"
            TeachingMode.TRACE_AND_LEARN -> "Trace and learn"
        }
}

data class ResumeLessonCandidate(
    val sessionId: String,
    val lessonId: String,
    val lessonRevision: Int,
    val childDocumentId: String,
    val phase: LessonSnapshotPhase,
    val mode: TeachingMode,
    val pace: TeachingPace,
    val currentStepIndex: Int?,
    val currentStepId: String?,
    val totalSteps: Int,
    val savedAtEpochMillis: Long,
) {
    val progressLabel: String
        get() = when (phase) {
            LessonSnapshotPhase.DRAWING_COMPLETE,
            LessonSnapshotPhase.AWAITING_POST_DRAWING_CHOICE,
            LessonSnapshotPhase.HANDING_OFF_TO_COLORING,
            -> "Drawing complete · choose what comes next"

            else -> currentStepIndex?.let { index ->
                "Step ${(index + 1).coerceAtMost(totalSteps)} of $totalSteps · continue where you left off"
            } ?: "Continue where you left off"
        }
}

data class ColoringResumeCandidate(
    val sessionId: String,
    val lessonId: String,
    val lessonRevision: Int,
    val childDocumentId: String,
    val savedAtEpochMillis: Long,
) {
    val progressLabel: String
        get() = "Your drawing is safe · continue adding color"
}

data class StudioCategory(
    val categoryId: String,
    val title: String,
    val lessons: List<LessonRecommendation>,
) {
    val lessonCount: Int
        get() = lessons.size
}

data class StudioJourney(
    val journeyId: String,
    val title: String,
    val lessons: List<LessonRecommendation>,
    val activeLessonId: String? = null,
) {
    val progressLabel: String
        get() = activeLessonId?.let { active ->
            val index = lessons.indexOfFirst { it.lessonId == active }
            if (index >= 0) "Lesson ${index + 1} of ${lessons.size} in progress"
            else "${lessons.size} lessons"
        } ?: "${lessons.size} lessons"
}

data class StudioHomeModel(
    val recommendation: LessonRecommendation?,
    val resumeCandidate: ResumeLessonCandidate?,
    val coloringResumeCandidate: ColoringResumeCandidate? = null,
    val contentMessage: String? = null,
    val recommendations: List<LessonRecommendation> = listOfNotNull(recommendation),
    val categories: List<StudioCategory> = emptyList(),
    val journeys: List<StudioJourney> = emptyList(),
)

enum class HomeCardDensity {
    SPACIOUS,
    BALANCED,
    COMPACT,
}

data class HomePresentationPolicy(
    val density: HomeCardDensity,
    val showDifficulty: Boolean,
    val showSkills: Boolean,
    val twoColumnSecondaryCards: Boolean,
    val recommendationLimit: Int = 3,
)

object StudioRecommendationPolicy {
    fun recommend(
        profile: ChildProfile,
        lesson: LessonSource,
        title: String,
        summary: String,
    ): LessonRecommendation {
        require(lesson.supportedModes.isNotEmpty()) { "Lesson requires at least one supported mode." }

        val expectedAgeBand = profile.ageBand.toLessonAgeBand()
        val ageFit = if (expectedAgeBand in lesson.metadata.ageBands) {
            LessonAgeFit.EXACT
        } else {
            LessonAgeFit.FALLBACK
        }
        val interestMatched = profile.interests.any { interest ->
            val keys = interest.matchKeys()
            lesson.metadata.categoryIds.any(keys::contains) || lesson.metadata.tags.any(keys::contains)
        }
        val reason = when {
            interestMatched -> RecommendationReason.INTEREST_MATCH
            ageFit == LessonAgeFit.EXACT -> RecommendationReason.AGE_MATCH
            else -> RecommendationReason.STARTER_PICK
        }
        val mode = profile.teachingMode.takeIf { it in lesson.supportedModes }
            ?: lesson.supportedModes.first()

        return LessonRecommendation(
            lessonId = lesson.lessonId,
            lessonRevision = lesson.revision,
            title = title,
            summary = summary,
            estimatedMinutes = lesson.metadata.estimatedMinutes,
            difficulty = lesson.metadata.difficulty,
            defaultMode = mode,
            defaultPace = profile.pace,
            ageFit = ageFit,
            reason = reason,
            primarySkillIds = lesson.metadata.skillIds.take(3),
            journeyIds = lesson.metadata.journeyIds,
            categoryIds = lesson.metadata.categoryIds,
            tags = lesson.metadata.tags,
            prerequisiteLessonIds = lesson.metadata.prerequisiteLessonIds,
        )
    }

    fun rank(
        profile: ChildProfile,
        recommendations: List<LessonRecommendation>,
    ): List<LessonRecommendation> {
        val targetDifficulty = targetDifficulty(profile.ageBand)
        return recommendations.sortedWith(
            compareByDescending<LessonRecommendation> { it.ageFit == LessonAgeFit.EXACT }
                .thenByDescending { it.reason == RecommendationReason.INTEREST_MATCH }
                .thenByDescending { it.defaultMode == profile.teachingMode }
                .thenBy { abs(it.difficulty - targetDifficulty) }
                .thenBy { it.lessonId }
                .thenBy { it.lessonRevision },
        )
    }

    fun categories(recommendations: List<LessonRecommendation>): List<StudioCategory> =
        recommendations
            .flatMap { recommendation -> recommendation.categoryIds.map { it to recommendation } }
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })
            .toSortedMap()
            .map { (categoryId, lessons) ->
                StudioCategory(
                    categoryId = categoryId,
                    title = categoryId.toTaxonomyTitle(),
                    lessons = orderByPrerequisites(
                        lessons.distinctBy { it.lessonId to it.lessonRevision },
                    ),
                )
            }

    fun journeys(
        recommendations: List<LessonRecommendation>,
        activeLessonId: String? = null,
    ): List<StudioJourney> = recommendations
        .flatMap { recommendation -> recommendation.journeyIds.map { it to recommendation } }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })
        .toSortedMap()
        .map { (journeyId, lessons) ->
            StudioJourney(
                journeyId = journeyId,
                title = journeyId.toTaxonomyTitle(),
                lessons = orderByPrerequisites(lessons.distinctBy { it.lessonId to it.lessonRevision }),
                activeLessonId = activeLessonId?.takeIf { active ->
                    lessons.any { it.lessonId == active }
                },
            )
        }

    fun presentationFor(ageBand: AgeBand): HomePresentationPolicy = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> HomePresentationPolicy(
            density = HomeCardDensity.SPACIOUS,
            showDifficulty = false,
            showSkills = false,
            twoColumnSecondaryCards = false,
            recommendationLimit = 2,
        )

        AgeBand.CREATIVE_EXPLORER -> HomePresentationPolicy(
            density = HomeCardDensity.SPACIOUS,
            showDifficulty = true,
            showSkills = false,
            twoColumnSecondaryCards = false,
            recommendationLimit = 3,
        )

        AgeBand.GROWING_ARTIST -> HomePresentationPolicy(
            density = HomeCardDensity.BALANCED,
            showDifficulty = true,
            showSkills = true,
            twoColumnSecondaryCards = true,
            recommendationLimit = 4,
        )

        AgeBand.YOUNG_ARTIST -> HomePresentationPolicy(
            density = HomeCardDensity.COMPACT,
            showDifficulty = true,
            showSkills = true,
            twoColumnSecondaryCards = true,
            recommendationLimit = 4,
        )
    }

    fun resumeCandidate(
        snapshot: LessonSessionSnapshot,
        lesson: LessonSource,
    ): ResumeLessonCandidate? {
        if (snapshot.lessonId != lesson.lessonId || snapshot.lessonRevision != lesson.revision) return null
        if (snapshot.phase == LessonSnapshotPhase.READY || snapshot.phase == LessonSnapshotPhase.FINISHED) return null
        val mode = snapshot.mode ?: return null
        val pace = snapshot.pace ?: return null

        return ResumeLessonCandidate(
            sessionId = snapshot.sessionId,
            lessonId = snapshot.lessonId,
            lessonRevision = snapshot.lessonRevision,
            childDocumentId = snapshot.childDocumentId,
            phase = snapshot.phase,
            mode = mode,
            pace = pace,
            currentStepIndex = snapshot.currentStepIndex,
            currentStepId = snapshot.currentStepId,
            totalSteps = lesson.drawing.steps.size,
            savedAtEpochMillis = snapshot.savedAtEpochMillis,
        )
    }

    private fun orderByPrerequisites(
        lessons: List<LessonRecommendation>,
    ): List<LessonRecommendation> {
        val remaining = lessons.associateBy { it.lessonId }.toMutableMap()
        val ordered = mutableListOf<LessonRecommendation>()
        while (remaining.isNotEmpty()) {
            val remainingIds = remaining.keys
            val ready = remaining.values
                .filter { lesson -> lesson.prerequisiteLessonIds.none { it in remainingIds } }
                .sortedWith(compareBy({ it.lessonId }, { it.lessonRevision }))
            if (ready.isEmpty()) {
                ordered += remaining.values.sortedWith(compareBy({ it.lessonId }, { it.lessonRevision }))
                break
            }
            ready.forEach { lesson ->
                ordered += lesson
                remaining.remove(lesson.lessonId)
            }
        }
        return ordered
    }

    private fun targetDifficulty(ageBand: AgeBand): Int = when (ageBand) {
        AgeBand.LITTLE_ARTIST -> 1
        AgeBand.CREATIVE_EXPLORER -> 2
        AgeBand.GROWING_ARTIST -> 3
        AgeBand.YOUNG_ARTIST -> 4
    }

    private fun AgeBand.toLessonAgeBand(): LessonAgeBand = when (this) {
        AgeBand.LITTLE_ARTIST -> LessonAgeBand.LITTLE_ARTISTS
        AgeBand.CREATIVE_EXPLORER -> LessonAgeBand.CREATIVE_EXPLORERS
        AgeBand.GROWING_ARTIST -> LessonAgeBand.GROWING_ARTISTS
        AgeBand.YOUNG_ARTIST -> LessonAgeBand.YOUNG_ARTISTS
    }

    private fun ChildInterest.matchKeys(): Set<String> = when (this) {
        ChildInterest.ANIMALS -> setOf("animals", "pets", "cat")
        ChildInterest.VEHICLES -> setOf("vehicles", "transport", "cars")
        ChildInterest.NATURE -> setOf("nature", "plants", "landscape")
        ChildInterest.CHARACTERS -> setOf("characters", "people", "faces")
        ChildInterest.SPACE -> setOf("space", "astronomy")
        ChildInterest.FANTASY -> setOf("fantasy", "magic")
    }

    private fun String.toTaxonomyTitle(): String = split('-', '_', '.')
        .filter(String::isNotBlank)
        .joinToString(" ") { token -> token.replaceFirstChar { it.uppercaseChar() } }
}
