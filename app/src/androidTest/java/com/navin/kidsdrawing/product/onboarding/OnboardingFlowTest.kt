package com.navin.kidsdrawing.product.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.navin.kidsdrawing.product.design.StudioTheme
import com.navin.kidsdrawing.product.profile.ChildProfileDraft
import org.junit.Rule
import org.junit.Test

class OnboardingFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun nickname_continue_moves_to_age_decision_with_accessible_text_labels() {
        var draft by mutableStateOf(ChildProfileDraft())

        composeRule.setContent {
            StudioTheme {
                OnboardingFlow(
                    draft = draft,
                    onDraftChange = { draft = it },
                    onComplete = {},
                )
            }
        }

        composeRule.onNodeWithText("What should we call you?").assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).performTextInput("Maya")
        composeRule.onNodeWithText("Continue").performClick()

        composeRule.onNodeWithText("Which studio feels right for you?").assertIsDisplayed()
        composeRule.onNodeWithText("4–5  ·  Little Artist").assertIsDisplayed()
        composeRule.onNodeWithText("10–12  ·  Young Artist").assertIsDisplayed()
    }
}
