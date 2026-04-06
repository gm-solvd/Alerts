package com.privacyalert.android.ui.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.privacyalert.presentation.ui.screen.RegisterContent
import com.privacyalert.presentation.ui.theme.PrivacyAlertTheme
import com.privacyalert.presentation.viewmodel.AuthUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RegisterScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun register_idle() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                RegisterContent(
                    authState = AuthUiState.Idle,
                    onRegister = { _, _ -> },
                    onNavigateToLogin = {},
                    onClearError = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Register_Idle.png")
    }

    @Test
    fun register_error() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                RegisterContent(
                    authState = AuthUiState.Error("Email already registered"),
                    onRegister = { _, _ -> },
                    onNavigateToLogin = {},
                    onClearError = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Register_Error.png")
    }
}
