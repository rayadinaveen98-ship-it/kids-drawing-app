package com.navin.kidsdrawing.product.profile

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChildProfileTest {
    @Test
    fun nickname_is_trimmed_and_collapsed_before_completion() {
        val draft = completeDraft(nickname = "  Navi   Star  ")

        val profile = draft.toCompletedProfileOrNull()

        assertEquals("Navi Star", profile?.nickname)
    }

    @Test
    fun incomplete_draft_cannot_become_profile() {
        val draft = ChildProfileDraft(nickname = "Navi")

        assertNull(draft.toCompletedProfileOrNull())
    }

    @Test
    fun nickname_validation_rejects_blank_and_overlong_values() {
        assertFalse(completeDraft(nickname = "   ").isNicknameValid())
        assertFalse(completeDraft(nickname = "x".repeat(25)).isNicknameValid())
        assertTrue(completeDraft(nickname = "x".repeat(24)).isNicknameValid())
    }

    @Test
    fun onboarding_step_index_is_safely_bounded() {
        assertEquals(OnboardingStep.NICKNAME, OnboardingStep.fromIndex(-10))
        assertEquals(OnboardingStep.NARRATION, OnboardingStep.fromIndex(999))
    }

    @Test
    fun enum_decoder_returns_null_for_unknown_persisted_values() {
        assertNull(enumValueOrNull<AgeBand>("NOT_A_REAL_BAND"))
        assertEquals(AgeBand.GROWING_ARTIST, enumValueOrNull<AgeBand>("GROWING_ARTIST"))
    }

    private fun completeDraft(nickname: String) = ChildProfileDraft(
        nickname = nickname,
        ageBand = AgeBand.CREATIVE_EXPLORER,
        teachingMode = TeachingMode.DRAW_WITH_ME,
        pace = TeachingPace.NORMAL,
        interests = setOf(ChildInterest.ANIMALS),
        handedness = Handedness.RIGHT,
        narrationPreference = NarrationPreference.VOICE_AND_TEXT,
        currentStepIndex = OnboardingStep.NARRATION.ordinal,
    )
}
