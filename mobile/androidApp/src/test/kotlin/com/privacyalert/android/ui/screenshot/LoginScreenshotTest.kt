package com.privacyalert.android.ui.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.privacyalert.android.ui.screen.LoginContentForTest
import com.privacyalert.android.ui.theme.PrivacyAlertTheme
import com.privacyalert.presentation.viewmodel.AuthUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LoginScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun login_idle() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                LoginContentForTest(authState = AuthUiState.Idle)
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Login_Idle.png")
    }

    @Test
    fun login_loading() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                LoginContentForTest(authState = AuthUiState.Loading)
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Login_Loading.png")
    }

    @Test
    fun login_error() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                LoginContentForTest(
                    authState = AuthUiState.Error("Invalid email or password"),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Login_Error.png")
    }
}
