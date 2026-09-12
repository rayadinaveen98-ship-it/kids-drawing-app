package com.navin.kidsdrawing.product.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import com.navin.kidsdrawing.product.design.StudioTheme
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.Handedness
import com.navin.kidsdrawing.product.profile.NarrationPreference
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StudioHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun resumable_session_becomes_primary_home_action() {
        var selectedDestination: StudioDestination? = null
        val recommendation = LessonRecommendation(
            lessonId = "cute-cat",
            lessonRevision = 1,
            title = "Cute Cat",
            summary = "Draw a friendly cat.",
            estimatedMinutes = 8,
            difficulty = 1,
            defaultMode = TeachingMode.DRAW_WITH_ME,
            defaultPace = TeachingPace.NORMAL,
            ageFit = LessonAgeFit.EXACT,
            reason = RecommendationReason.INTEREST_MATCH,
            primarySkillIds = listOf("curves", "shape_construction"),
            journeyIds = listOf("animal_artist"),
        )
        val resume = ResumeLessonCandidate(
            sessionId = "lesson-lab-cute-cat-session",
            lessonId = "cute-cat",
            lessonRevision = 1,
            childDocumentId = "lesson-lab-cute-cat-document",
            phase = LessonSnapshotPhase.AWAITING_CHILD,
            mode = TeachingMode.DRAW_WITH_ME,
            pace = TeachingPace.NORMAL,
            currentStepIndex = 1,
            currentStepId = "ears",
            totalSteps = 4,
            savedAtEpochMillis = 1234L,
        )

        composeRule.setContent {
            StudioTheme {
                StudioHomeScreen(
                    profile = profile(),
                    model = StudioHomeModel(
                        recommendation = recommendation,
                        resumeCandidate = resume,
                    ),
                    onPrimaryLessonAction = { selectedDestination = it },
                    onOpenRecommendation = {},
                    onOpenDestination = {},
                )
            }
        }

        composeRule.onNodeWithText("CONTINUE DRAWING").assertIsDisplayed()
        composeRule.onNodeWithText("Step 2 of 4 · continue where you left off").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Continue Cute Cat drawing").assertIsDisplayed().performClick()
        assertEquals(StudioDestination.LESSON_RESUME, selectedDestination)
        composeRule.onNodeWithText("Picked for you").assertIsDisplayed()
    }

    @Test
    fun fresh_home_exposes_large_recommendation_and_quiet_parent_entry() {
        val recommendation = LessonRecommendation(
            lessonId = "cute-cat",
            lessonRevision = 1,
            title = "Cute Cat",
            summary = "Draw a friendly cat.",
            estimatedMinutes = 8,
            difficulty = 1,
            defaultMode = TeachingMode.WATCH_THEN_DRAW,
            defaultPace = TeachingPace.SLOW,
            ageFit = LessonAgeFit.EXACT,
            reason = RecommendationReason.INTEREST_MATCH,
            primarySkillIds = listOf("curves"),
            journeyIds = listOf("animal_artist"),
        )

        composeRule.setContent {
            StudioTheme {
                StudioHomeScreen(
                    profile = profile(teachingMode = TeachingMode.WATCH_THEN_DRAW),
                    model = StudioHomeModel(
                        recommendation = recommendation,
                        resumeCandidate = null,
                    ),
                    onPrimaryLessonAction = {},
                    onOpenRecommendation = {},
                    onOpenDestination = {},
                )
            }
        }

        composeRule.onNodeWithText("DRAW TOGETHER").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Start Cute Cat lesson").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Open grown-ups area").assertIsDisplayed()
    }

    private fun profile(
        teachingMode: TeachingMode = TeachingMode.DRAW_WITH_ME,
    ) = ChildProfile(
        nickname = "Maya",
        ageBand = AgeBand.CREATIVE_EXPLORER,
        teachingMode = teachingMode,
        pace = TeachingPace.NORMAL,
        interests = setOf(ChildInterest.ANIMALS),
        handedness = Handedness.RIGHT,
        narrationPreference = NarrationPreference.VOICE_AND_TEXT,
    )
}
