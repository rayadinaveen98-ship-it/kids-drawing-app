package com.navin.kidsdrawing.product.parent

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navin.kidsdrawing.product.design.StudioTheme
import org.junit.Rule
import org.junit.Test

class ParentGateScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun accessible_confirmation_requires_two_explicit_steps_then_unlocks() {
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
                    )
                }
            }
        }

        composeRule.onNodeWithText("Grown-ups area").assertIsDisplayed()
        composeRule.onNodeWithText("Use accessible confirmation instead").performClick()
        composeRule.onNodeWithText("Continue to adult controls").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Yes, open Parent Zone").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Unlocked parent zone").assertIsDisplayed()
    }

    @Test
    fun cancel_returns_control_without_unlocking_session() {
        val session = ParentAccessSession(ParentElapsedClock { 1_000L })
        var cancelled by mutableStateOf(false)

        composeRule.setContent {
            StudioTheme {
                if (cancelled) {
                    Text("Returned to studio")
                } else {
                    ParentGateScreen(
                        session = session,
                        onUnlocked = {},
                        onCancel = { cancelled = true },
                    )
                }
            }
        }

        composeRule.onNodeWithText("Back to the art studio").performClick()
        composeRule.onNodeWithText("Returned to studio").assertIsDisplayed()
    }
}
