package com.navin.kidsdrawing.product.parent

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navin.kidsdrawing.product.accessibility.AccessibilityPreferences
import com.navin.kidsdrawing.product.design.StudioTheme
import com.navin.kidsdrawing.product.profile.NarrationPreference
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ParentAccessibilityScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun reduceMotionControlRequestsPersistentSettingChange() {
        var requested = false

        composeRule.setContent {
            StudioTheme {
                ParentAccessibilityScreen(
                    narrationPreference = NarrationPreference.TEXT_ONLY,
                    preferences = AccessibilityPreferences(reduceMotion = false),
                    onSetReduceMotion = { enabled ->
                        requested = enabled
                        true
                    },
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Accessibility & Audio").assertIsDisplayed()
        composeRule.onNode(hasStateDescription("Reduce motion off")).performClick()
        composeRule.waitForIdle()
        assertTrue(requested)
    }

    @Test
    fun reducedMotionGateKeepsAccessibleFallbackAndUsesStaticHoldFeedback() {
        val session = ParentAccessSession(ParentElapsedClock { 1_000L })
        var unlocked by mutableStateOf(false)

        composeRule.setContent {
            StudioTheme {
                if (unlocked) {
                    Text("Unlocked parent zone")
                } else {
                    ParentGateScreen(
                        session = session,
                        onUnlocked = { unlocked = true },
                        onCancel = {},
                        reduceMotion = true,
                    )
                }
            }
        }

        composeRule.onNodeWithText("Timed hold ready").assertIsDisplayed()
        composeRule.onNodeWithText("Use accessible confirmation instead").performClick()
        composeRule.onNodeWithText("Continue to adult controls").performClick()
        composeRule.onNodeWithText("Yes, open Parent Zone").performClick()
        composeRule.onNodeWithText("Unlocked parent zone").assertIsDisplayed()
    }
}
