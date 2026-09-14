package com.navin.kidsdrawing.product.adaptive

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import com.navin.kidsdrawing.product.home.ColoringResumeCandidate
import com.navin.kidsdrawing.product.home.LessonAgeFit
import com.navin.kidsdrawing.product.home.LessonRecommendation
import com.navin.kidsdrawing.product.home.RecommendationReason
import com.navin.kidsdrawing.product.home.ResumeLessonCandidate
import com.navin.kidsdrawing.product.home.StudioPrimarySelectionPolicy
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.Handedness
import com.navin.kidsdrawing.product.profile.NarrationPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveFreshRecommendationPolicyTest {
    @Test
    fun unmetPrerequisiteCannotBecomeFreshPrimary() {
        val locked = recommendation(
            id = "create-character",
            prerequisiteIds = listOf("body-pose"),
            journeyIds = listOf("journey.character_creator"),
        )
        val open = recommendation(id = "face")

        val ranked = AdaptiveFreshRecommendationPolicy.rank(profile(), listOf(locked, open), LocalAdaptiveState.empty())

        assertEquals(listOf("face"), ranked.map { it.recommendation.lessonId })
    }

    @Test
    fun exactAgeFreshPoolDominatesFreshFallbackPool() {
        val exact = recommendation(id = "exact", ageFit = LessonAgeFit.EXACT, difficulty = 3)
        val fallback = recommendation(
            id = "fallback",
            ageFit = LessonAgeFit.FALLBACK,
            reason = RecommendationReason.INTEREST_MATCH,
            difficulty = 3,
        )

        val ranked = AdaptiveFreshRecommendationPolicy.rank(profile(), listOf(fallback, exact), LocalAdaptiveState.empty())

        assertEquals(listOf("exact"), ranked.map { it.recommendation.lessonId })
    }

    @Test
    fun uncompletedFallbackBeatsCompletedExactAgeRepeat() {
        val completedExact = recommendation(id = "completed-exact", ageFit = LessonAgeFit.EXACT)
        val freshFallback = recommendation(id = "fresh-fallback", ageFit = LessonAgeFit.FALLBACK)
        val state = LocalAdaptiveState(
            completedLessons = listOf(AdaptiveLessonIdentity("completed-exact", 1)),
        )

        val ranked = AdaptiveFreshRecommendationPolicy.rank(
            profile(),
            listOf(completedExact, freshFallback),
            state,
        )

        assertEquals(listOf("fresh-fallback"), ranked.map { it.recommendation.lessonId })
    }

    @Test
    fun journeyContinuationWinsBeforeNewSkillAndInterest() {
        val continuation = recommendation(
            id = "body-pose",
            prerequisiteIds = listOf("face"),
            journeyIds = listOf("journey.character_creator"),
            skills = listOf("pose.basic"),
        )
        val interest = recommendation(
            id = "animal",
            reason = RecommendationReason.INTEREST_MATCH,
            skills = listOf("brand.new.skill"),
        )
        val state = LocalAdaptiveState(
            completedLessons = listOf(AdaptiveLessonIdentity("face", 1)),
            skillExposureCounts = mapOf("pose.basic" to 3),
        )

        val ranked = AdaptiveFreshRecommendationPolicy.rank(profile(), listOf(interest, continuation), state)

        assertEquals("body-pose", ranked.first().recommendation.lessonId)
        assertEquals(AdaptiveRecommendationReason.CONTINUE_JOURNEY, ranked.first().primaryReason)
    }

    @Test
    fun underexposedSkillWinsWhenJourneyDimensionTies() {
        val familiarSkill = recommendation(id = "familiar", skills = listOf("line.curve"))
        val newSkill = recommendation(id = "new", skills = listOf("perspective.one_point"))
        val state = LocalAdaptiveState(skillExposureCounts = mapOf("line.curve" to 4))

        val ranked = AdaptiveFreshRecommendationPolicy.rank(profile(), listOf(familiarSkill, newSkill), state)

        assertEquals("new", ranked.first().recommendation.lessonId)
        assertEquals(AdaptiveRecommendationReason.NEW_SKILL, ranked.first().primaryReason)
    }

    @Test
    fun interestWinsWhenHigherPolicyDimensionsTie() {
        val neutral = recommendation(
            id = "neutral",
            reason = RecommendationReason.AGE_MATCH,
            skills = listOf("same.skill"),
        )
        val interest = recommendation(
            id = "interest",
            reason = RecommendationReason.INTEREST_MATCH,
            skills = listOf("same.skill"),
        )

        val ranked = AdaptiveFreshRecommendationPolicy.rank(profile(), listOf(neutral, interest), LocalAdaptiveState.empty())

        assertEquals("interest", ranked.first().recommendation.lessonId)
        assertTrue(ranked.first().reasons.contains(AdaptiveRecommendationReason.INTEREST_MATCH))
    }

    @Test
    fun recentRepeatIsAvoidedWhenEarlierDimensionsTie() {
        val recent = recommendation(id = "recent", skills = listOf("same.skill"))
        val notRecent = recommendation(id = "not-recent", skills = listOf("same.skill"))
        val state = LocalAdaptiveState(
            recentCompletions = listOf(AdaptiveLessonIdentity("recent", 1)),
        )

        val ranked = AdaptiveFreshRecommendationPolicy.rank(profile(), listOf(recent, notRecent), state)

        assertEquals("not-recent", ranked.first().recommendation.lessonId)
    }

    @Test
    fun uncompletedLessonsSuppressCompletedLessonsUntilFreshPoolIsExhausted() {
        val completed = recommendation(id = "completed")
        val fresh = recommendation(id = "fresh")
        val state = LocalAdaptiveState(
            completedLessons = listOf(AdaptiveLessonIdentity("completed", 1)),
        )

        val ranked = AdaptiveFreshRecommendationPolicy.rank(profile(), listOf(completed, fresh), state)

        assertEquals(listOf("fresh"), ranked.map { it.recommendation.lessonId })
        assertFalse(ranked.first().reasons.contains(AdaptiveRecommendationReason.REPEAT_FAMILIAR))
    }

    @Test
    fun completedLessonMayReturnOnlyAsFamiliarFallback() {
        val completed = recommendation(id = "completed")
        val state = LocalAdaptiveState(
            completedLessons = listOf(AdaptiveLessonIdentity("completed", 1)),
        )

        val ranked = AdaptiveFreshRecommendationPolicy.rank(profile(), listOf(completed), state)

        assertEquals("completed", ranked.single().recommendation.lessonId)
        assertTrue(ranked.single().reasons.contains(AdaptiveRecommendationReason.REPEAT_FAMILIAR))
    }

    @Test
    fun reasonCopyNeverUsesAbilityOrScoringLanguage() {
        AdaptiveRecommendationReason.entries.forEach { reason ->
            val decision = AdaptiveRecommendationDecision(
                recommendation = recommendation(id = "copy"),
                reasons = listOf(reason),
            )
            val copy = decision.reasonCopy.lowercase()
            listOf("weak", "advanced", "failed", "score", "rank", "grade", "talent", "ability").forEach { forbidden ->
                assertFalse("Adaptive reason copy must not contain '$forbidden'", copy.contains(forbidden))
            }
        }
    }

    @Test
    fun identicalInputsProduceIdenticalOrderingAndReasons() {
        val recommendations = listOf(
            recommendation(id = "b", skills = listOf("skill.b")),
            recommendation(id = "a", skills = listOf("skill.a")),
        )
        val state = LocalAdaptiveState(skillExposureCounts = mapOf("skill.a" to 1))

        assertEquals(
            AdaptiveFreshRecommendationPolicy.rank(profile(), recommendations, state),
            AdaptiveFreshRecommendationPolicy.rank(profile(), recommendations, state),
        )
    }

    @Test
    fun existingPrimarySelectionStillPrefersColoringThenDrawingOverAdaptiveFresh() {
        val fresh = recommendation(id = "fresh")
        val drawing = ResumeLessonCandidate(
            sessionId = "draw-session",
            lessonId = "drawing-resume",
            lessonRevision = 1,
            childDocumentId = "draw-doc",
            phase = LessonSnapshotPhase.AWAITING_CHILD,
            mode = TeachingMode.DRAW_WITH_ME,
            pace = TeachingPace.NORMAL,
            currentStepIndex = 1,
            currentStepId = "step",
            totalSteps = 3,
            savedAtEpochMillis = 10L,
        )
        val coloring = ColoringResumeCandidate(
            sessionId = "color-session",
            lessonId = "color-resume",
            lessonRevision = 1,
            childDocumentId = "color-doc",
            savedAtEpochMillis = 5L,
        )

        val withBoth = StudioPrimarySelectionPolicy.select(listOf(fresh), listOf(drawing), listOf(coloring))
        val drawingOnly = StudioPrimarySelectionPolicy.select(listOf(fresh), listOf(drawing), emptyList())

        assertEquals("color-resume", withBoth.lessonId)
        assertEquals("drawing-resume", drawingOnly.lessonId)
    }

    private fun profile() = ChildProfile(
        nickname = "Maya",
        ageBand = AgeBand.GROWING_ARTIST,
        teachingMode = TeachingMode.DRAW_WITH_ME,
        pace = TeachingPace.NORMAL,
        interests = setOf(ChildInterest.ANIMALS),
        handedness = Handedness.RIGHT,
        narrationPreference = NarrationPreference.VOICE_AND_TEXT,
    )

    private fun recommendation(
        id: String,
        ageFit: LessonAgeFit = LessonAgeFit.EXACT,
        reason: RecommendationReason = RecommendationReason.AGE_MATCH,
        difficulty: Int = 3,
        skills: List<String> = listOf("shape.combine"),
        prerequisiteIds: List<String> = emptyList(),
        journeyIds: List<String> = emptyList(),
    ) = LessonRecommendation(
        lessonId = id,
        lessonRevision = 1,
        title = id,
        summary = id,
        estimatedMinutes = 8,
        difficulty = difficulty,
        defaultMode = TeachingMode.DRAW_WITH_ME,
        defaultPace = TeachingPace.NORMAL,
        ageFit = ageFit,
        reason = reason,
        primarySkillIds = skills,
        journeyIds = journeyIds,
        prerequisiteLessonIds = prerequisiteIds,
    )
}
