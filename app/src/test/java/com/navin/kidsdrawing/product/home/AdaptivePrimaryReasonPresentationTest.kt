package com.navin.kidsdrawing.product.home

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.Handedness
import com.navin.kidsdrawing.product.profile.NarrationPreference
import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptivePrimaryReasonPresentationTest {
    @Test
    fun adaptive_reason_copy_is_visible_for_primary_recommendation() {
        val recommendation = recommendation().copy(
            adaptiveReasonCopy = "Continue your art journey",
        )

        assertEquals(
            "Continue your art journey",
            recommendationReason(profile(), recommendation),
        )
    }

    @Test
    fun baseline_recommendation_keeps_existing_generic_copy() {
        assertEquals(
            "Picked because it matches what you like",
            recommendationReason(profile(), recommendation()),
        )
    }

    @Test
    fun blank_adaptive_reason_cannot_hide_baseline_explanation() {
        val recommendation = recommendation().copy(adaptiveReasonCopy = "   ")

        assertEquals(
            "Picked because it matches what you like",
            recommendationReason(profile(), recommendation),
        )
    }

    private fun recommendation() = LessonRecommendation(
        lessonId = "friendly-alien",
        lessonRevision = 1,
        title = "Friendly Alien",
        summary = "Create an alien character.",
        estimatedMinutes = 10,
        difficulty = 3,
        defaultMode = TeachingMode.DRAW_WITH_ME,
        defaultPace = TeachingPace.NORMAL,
        ageFit = LessonAgeFit.EXACT,
        reason = RecommendationReason.INTEREST_MATCH,
        primarySkillIds = listOf("character.silhouette"),
        journeyIds = listOf("journey.space_artist"),
        categoryIds = listOf("space"),
    )

    private fun profile() = ChildProfile(
        nickname = "Maya",
        ageBand = AgeBand.CREATIVE_EXPLORER,
        teachingMode = TeachingMode.DRAW_WITH_ME,
        pace = TeachingPace.NORMAL,
        interests = setOf(ChildInterest.SPACE),
        handedness = Handedness.RIGHT,
        narrationPreference = NarrationPreference.VOICE_AND_TEXT,
    )
}
