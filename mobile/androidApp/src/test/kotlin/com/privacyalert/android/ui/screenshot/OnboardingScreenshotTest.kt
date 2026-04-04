package com.privacyalert.android.ui.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.privacyalert.android.ui.screen.OnboardingScreen
import com.privacyalert.android.ui.theme.PrivacyAlertTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OnboardingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun onboarding_page1() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                OnboardingScreen(
                    onLoginClick = {},
                    onRegisterClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Onboarding_Page1.png")
    }
}
