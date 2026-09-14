package com.navin.kidsdrawing.product.adaptive

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.product.home.LessonAgeFit
import com.navin.kidsdrawing.product.home.LessonRecommendation
import com.navin.kidsdrawing.product.home.RecommendationReason
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.Handedness
import com.navin.kidsdrawing.product.profile.NarrationPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class P5_7CrossAgeAdaptiveGateTest {
    @Test
    fun all_age_bands_have_deterministic_sensible_difficulty_context() {
        val targets = mapOf(
            AgeBand.LITTLE_ARTIST to 1,
            AgeBand.CREATIVE_EXPLORER to 2,
            AgeBand.GROWING_ARTIST to 3,
            AgeBand.YOUNG_ARTIST to 4,
        )

        targets.forEach { (ageBand, expectedDifficulty) ->
            val recommendations = (1..5).map { difficulty ->
                recommendation(
                    id = "lesson-$difficulty",
                    difficulty = difficulty,
                    skill = "shared.skill",
                )
            }
            val first = AdaptiveFreshRecommendationPolicy.rank(
                profile = profile(ageBand),
                recommendations = recommendations,
                state = LocalAdaptiveState(
                    skillExposureCounts = mapOf("shared.skill" to 1),
                ),
            )
            val second = AdaptiveFreshRecommendationPolicy.rank(
                profile = profile(ageBand),
                recommendations = recommendations,
                state = LocalAdaptiveState(
                    skillExposureCounts = mapOf("shared.skill" to 1),
                ),
            )

            assertEquals(first, second)
            assertEquals(expectedDifficulty, first.first().recommendation.difficulty)
        }
    }

    @Test
    fun cross_age_reason_copy_remains_non_judgmental() {
        AgeBand.entries.forEach { ageBand ->
            val decision = AdaptiveFreshRecommendationPolicy.rank(
                profile = profile(ageBand),
                recommendations = listOf(recommendation("lesson-${ageBand.name}", 2, "new.skill")),
                state = LocalAdaptiveState.empty(),
            ).single()
            val copy = decision.reasonCopy.lowercase()

            listOf("weak", "failed", "score", "rank", "grade", "talent", "ability").forEach { forbidden ->
                assertFalse(copy.contains(forbidden))
            }
            assertTrue(copy.isNotBlank())
        }
    }

    private fun profile(ageBand: AgeBand) = ChildProfile(
        nickname = "Maya",
        ageBand = ageBand,
        teachingMode = TeachingMode.DRAW_WITH_ME,
        pace = TeachingPace.NORMAL,
        interests = setOf(ChildInterest.ANIMALS),
        handedness = Handedness.RIGHT,
        narrationPreference = NarrationPreference.VOICE_AND_TEXT,
    )

    private fun recommendation(
        id: String,
        difficulty: Int,
        skill: String,
    ) = LessonRecommendation(
        lessonId = id,
        lessonRevision = 1,
        title = id,
        summary = id,
        estimatedMinutes = 8,
        difficulty = difficulty,
        defaultMode = TeachingMode.DRAW_WITH_ME,
        defaultPace = TeachingPace.NORMAL,
        ageFit = LessonAgeFit.EXACT,
        reason = RecommendationReason.AGE_MATCH,
        primarySkillIds = listOf(skill),
        journeyIds = emptyList(),
    )
}
