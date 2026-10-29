package com.lira.field

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.lira.field.ui.components.AssignmentStatusBadge
import com.lira.field.ui.components.DemoDataBanner
import com.lira.field.ui.theme.LiraTheme
import org.junit.Rule
import org.junit.Test

class LiraFieldUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testDemoBannerDisplaysMockDataIndicator() {
        composeTestRule.setContent {
            LiraTheme {
                DemoDataBanner(
                    isMockMode = true,
                    currentStage = 0,
                    onSelectStage = {},
                    onReset = {}
                )
            }
        }

        composeTestRule.onNodeWithText("DEMO MOCK DATA").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stage 1: Plan R2 Assigned").assertIsDisplayed()
    }

    @Test
    fun testAssignmentStatusBadge() {
        composeTestRule.setContent {
            LiraTheme {
                AssignmentStatusBadge(status = "APPROVED")
            }
        }

        composeTestRule.onNodeWithText("APPROVED").assertIsDisplayed()
    }
}

