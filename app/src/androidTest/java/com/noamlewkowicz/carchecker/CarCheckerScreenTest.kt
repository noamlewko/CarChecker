package com.noamlewkowicz.carchecker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.noamlewkowicz.carchecker.ui.screen.CarCheckerScreen
import com.noamlewkowicz.carchecker.viewmodel.CarCheckerUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Verifies that CarCheckerScreen renders the correct content for the error
 * state, and that the retry button triggers the callback passed in from the
 * caller instead of the screen handling the retry itself.
 */
class CarCheckerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun errorState_showsMessageAndTriggersRetryOnClick() {
        var retryCount = 0

        composeTestRule.setContent {
            CarCheckerScreen(
                licenseNumber = "1234567",
                uiState = CarCheckerUiState.Error(
                    message = "Unable to connect to the server."
                ),
                onLicenseNumberChange = {},
                onRetry = { retryCount++ }
            )
        }

        composeTestRule
            .onNodeWithText("Unable to connect to the server.")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Try again")
            .performClick()

        assertEquals(1, retryCount)
    }
}
