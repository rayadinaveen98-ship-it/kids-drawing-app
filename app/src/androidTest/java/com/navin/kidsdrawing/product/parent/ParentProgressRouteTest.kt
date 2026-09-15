package com.navin.kidsdrawing.product.parent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.product.design.StudioTheme
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.product.profile.Handedness
import com.navin.kidsdrawing.product.profile.NarrationPreference
import org.junit.Rule
import org.junit.Test

class ParentProgressRouteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun learning_section_opens_real_descriptive_local_progress_surface() {
        val profile = ChildProfile(
            nickname = "Ari",
            ageBand = AgeBand.LITTLE_ARTIST,
            teachingMode = TeachingMode.DRAW_WITH_ME,
            pace = TeachingPace.NORMAL,
            interests = setOf(ChildInterest.ANIMALS),
            handedness = Handedness.RIGHT,
            narrationPreference = NarrationPreference.TEXT_ONLY,
        )

        composeRule.setContent {
            StudioTheme {
                ParentZoneScreen(
                    profile = profile,
                    appVersion = "test",
                    onSaveProfile = { true },
                    onReturnToChild = {},
                )
            }
        }

        composeRule.onNodeWithText("Learning").assertIsDisplayed().performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText("Local and descriptive")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithText("Local and descriptive").assertIsDisplayed()
        composeRule.onNodeWithText("Lessons completed").assertIsDisplayed()
        composeRule.onNodeWithText("Artworks saved").assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.onNodeWithText("Parent Zone").assertIsDisplayed()
    }
}
