package com.navin.kidsdrawing.product.design

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class StudioChoiceCardAccessibilityTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun singleChoiceExposesSelectedStateAndVisibleCue() {
        composeRule.setContent {
            StudioTheme {
                StudioChoiceCard(
                    title = "Left hand",
                    selected = true,
                    onClick = {},
                    selectionMode = StudioChoiceSelectionMode.SINGLE,
                )
            }
        }

        composeRule.onNode(hasStateDescription("Selected")).assertIsDisplayed()
        composeRule.onNodeWithText("✓ Selected").assertIsDisplayed()
    }

    @Test
    fun singleChoiceExposesNotSelectedState() {
        composeRule.setContent {
            StudioTheme {
                StudioChoiceCard(
                    title = "Right hand",
                    selected = false,
                    onClick = {},
                    selectionMode = StudioChoiceSelectionMode.SINGLE,
                )
            }
        }

        composeRule.onNode(hasStateDescription("Not selected")).assertIsDisplayed()
    }

    @Test
    fun multiChoiceExposesSelectedState() {
        composeRule.setContent {
            StudioTheme {
                StudioChoiceCard(
                    title = "Animals",
                    selected = true,
                    onClick = {},
                    selectionMode = StudioChoiceSelectionMode.MULTIPLE,
                )
            }
        }

        composeRule.onNode(hasStateDescription("Selected")).assertIsDisplayed()
        composeRule.onNodeWithText("✓ Selected").assertIsDisplayed()
    }

    @Test
    fun navigationCardIsClickableWithoutFakeSelectionCue() {
        composeRule.setContent {
            StudioTheme {
                StudioChoiceCard(
                    title = "Learning",
                    selected = false,
                    onClick = {},
                    selectionMode = StudioChoiceSelectionMode.NAVIGATION,
                )
            }
        }

        composeRule.onNodeWithText("Learning").assertHasClickAction()
        composeRule.onNodeWithText("✓ Selected").assertDoesNotExist()
    }
}
