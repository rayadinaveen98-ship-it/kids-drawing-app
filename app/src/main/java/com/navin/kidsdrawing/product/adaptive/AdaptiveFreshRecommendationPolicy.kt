package com.navin.kidsdrawing.product.adaptive

import com.navin.kidsdrawing.product.home.LessonAgeFit
import com.navin.kidsdrawing.product.home.LessonRecommendation
import com.navin.kidsdrawing.product.home.RecommendationReason
import com.navin.kidsdrawing.product.home.StudioRecommendationPolicy
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildProfile
import kotlin.math.abs
import kotlin.math.roundToInt

enum class AdaptiveRecommendationReason {
    CONTINUE_JOURNEY,
    NEW_SKILL,
    INTEREST_MATCH,
    AGE_FIT,
    PREFERRED_MODE,
    REPEAT_FAMILIAR,
}

data class AdaptiveRecommendationDecision(
    val recommendation: LessonRecommendation,
    val reasons: List<AdaptiveRecommendationReason>,
) {
    val primaryReason: AdaptiveRecommendationReason?
        get() = reasons.firstOrNull()

    val reasonCopy: String
        get() = when (primaryReason) {
            AdaptiveRecommendationReason.CONTINUE_JOURNEY -> "Continue your art journey"
            AdaptiveRecommendationReason.NEW_SKILL -> "Try a new art skill"
            AdaptiveRecommendationReason.INTEREST_MATCH -> "Matches something you like"
            AdaptiveRecommendationReason.AGE_FIT -> "A good fit for your studio"
            AdaptiveRecommendationReason.PREFERRED_MODE -> "Matches how you like to learn"
            AdaptiveRecommendationReason.REPEAT_FAMILIAR -> "Try a familiar lesson again"
            null -> "A studio pick for you"
        }
}

/**
 * Deterministic fresh-primary policy. Browse/category/journey discovery must keep using the complete
 * baseline recommendation list; this policy only orders eligible fresh-primary candidates.
 */
object AdaptiveFreshRecommendationPolicy {
    fun rank(
        profile: ChildProfile,
        recommendations: List<LessonRecommendation>,
        state: LocalAdaptiveState,
    ): List<AdaptiveRecommendationDecision> {
        if (recommendations.isEmpty()) return emptyList()

        val completedIds = state.completedLessons.mapTo(mutableSetOf()) { it.lessonId }
        val prerequisiteEligible = recommendations.filter { recommendation ->
            recommendation.prerequisiteLessonIds.all(completedIds::contains)
        }
        if (prerequisiteEligible.isEmpty()) return emptyList()

        val exactEligible = prerequisiteEligible.filter { it.ageFit == LessonAgeFit.EXACT }
        val exactFresh = exactEligible.filterNot { it.lessonId in completedIds }
        val anyFresh = prerequisiteEligible.filterNot { it.lessonId in completedIds }
        val freshPool = when {
            exactFresh.isNotEmpty() -> exactFresh
            anyFresh.isNotEmpty() -> anyFresh
            exactEligible.isNotEmpty() -> exactEligible
            else -> prerequisiteEligible
        }

        val recentIdentities = state.recentCompletions.toSet()
        val recentDifficulty = state.recentCompletions
            .asReversed()
            .firstNotNullOfOrNull { identity ->
                recommendations.firstOrNull {
                    it.lessonId == identity.lessonId && it.lessonRevision == identity.revision
                }?.difficulty
            }
        val difficultyTarget = adaptiveDifficultyTarget(profile.ageBand, recentDifficulty)

        return freshPool
            .map { recommendation ->
                RankedCandidate(
                    decision = AdaptiveRecommendationDecision(
                        recommendation = recommendation,
                        reasons = reasonsFor(profile, recommendation, state, completedIds),
                    ),
                    journeyContinuation = isJourneyContinuation(recommendation, completedIds),
                    skillExposureFloor = skillExposureFloor(recommendation, state),
                    interestMatch = recommendation.reason == RecommendationReason.INTEREST_MATCH,
                    preferredMode = recommendation.defaultMode == profile.teachingMode,
                    difficultyDistance = abs(recommendation.difficulty - difficultyTarget),
                    recentRepeat = AdaptiveLessonIdentity(
                        recommendation.lessonId,
                        recommendation.lessonRevision,
                    ) in recentIdentities,
                )
            }
            .sortedWith(
                compareByDescending<RankedCandidate> { it.journeyContinuation }
                    .thenBy { it.skillExposureFloor }
                    .thenByDescending { it.interestMatch }
                    .thenByDescending { it.preferredMode }
                    .thenBy { it.difficultyDistance }
                    .thenBy { it.recentRepeat }
                    .thenBy { it.decision.recommendation.lessonId }
                    .thenBy { it.decision.recommendation.lessonRevision },
            )
            .map(RankedCandidate::decision)
    }

    private fun reasonsFor(
        profile: ChildProfile,
        recommendation: LessonRecommendation,
        state: LocalAdaptiveState,
        completedIds: Set<String>,
    ): List<AdaptiveRecommendationReason> = buildList {
        if (isJourneyContinuation(recommendation, completedIds)) {
            add(AdaptiveRecommendationReason.CONTINUE_JOURNEY)
        }
        if (skillExposureFloor(recommendation, state) == 0) {
            add(AdaptiveRecommendationReason.NEW_SKILL)
        }
        if (recommendation.reason == RecommendationReason.INTEREST_MATCH) {
            add(AdaptiveRecommendationReason.INTEREST_MATCH)
        }
        if (recommendation.ageFit == LessonAgeFit.EXACT) {
            add(AdaptiveRecommendationReason.AGE_FIT)
        }
        if (recommendation.defaultMode == profile.teachingMode) {
            add(AdaptiveRecommendationReason.PREFERRED_MODE)
        }
        if (recommendation.lessonId in completedIds) {
            add(AdaptiveRecommendationReason.REPEAT_FAMILIAR)
        }
    }

    private fun isJourneyContinuation(
        recommendation: LessonRecommendation,
        completedIds: Set<String>,
    ): Boolean = recommendation.journeyIds.isNotEmpty() &&
        recommendation.prerequisiteLessonIds.isNotEmpty() &&
        recommendation.prerequisiteLessonIds.all(completedIds::contains)

    private fun skillExposureFloor(
        recommendation: LessonRecommendation,
        state: LocalAdaptiveState,
    ): Int = recommendation.primarySkillIds
        .map { state.skillExposureCounts[it] ?: 0 }
        .minOrNull()
        ?: LocalAdaptiveState.MAX_COUNTER_VALUE

    private fun adaptiveDifficultyTarget(ageBand: AgeBand, recentDifficulty: Int?): Int {
        val ageTarget = when (ageBand) {
            AgeBand.LITTLE_ARTIST -> 1
            AgeBand.CREATIVE_EXPLORER -> 2
            AgeBand.GROWING_ARTIST -> 3
            AgeBand.YOUNG_ARTIST -> 4
        }
        if (recentDifficulty == null) return ageTarget
        return ((ageTarget + recentDifficulty) / 2.0).roundToInt().coerceIn(1, 5)
    }

    private data class RankedCandidate(
        val decision: AdaptiveRecommendationDecision,
        val journeyContinuation: Boolean,
        val skillExposureFloor: Int,
        val interestMatch: Boolean,
        val preferredMode: Boolean,
        val difficultyDistance: Int,
        val recentRepeat: Boolean,
    )
}

/** Keeps StudioRecommendationPolicy as the product-facing recommendation surface. */
fun StudioRecommendationPolicy.adaptiveFreshDecisions(
    profile: ChildProfile,
    recommendations: List<LessonRecommendation>,
    state: LocalAdaptiveState,
): List<AdaptiveRecommendationDecision> = AdaptiveFreshRecommendationPolicy.rank(
    profile = profile,
    recommendations = recommendations,
    state = state,
)
