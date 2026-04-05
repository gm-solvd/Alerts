package com.privacyalert.android.ui.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.privacyalert.android.ui.preview.PreviewData
import com.privacyalert.presentation.ui.screen.AlertDetailContent
import com.privacyalert.presentation.ui.theme.PrivacyAlertTheme
import com.privacyalert.presentation.viewmodel.AlertDetailUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AlertDetailScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun alertDetail_loading() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                AlertDetailContent(
                    uiState = AlertDetailUiState.Loading,
                    onResolve = {},
                    onRetry = {},
                    onBack = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/AlertDetail_Loading.png")
    }

    @Test
    fun alertDetail_success_unresolved() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                AlertDetailContent(
                    uiState = AlertDetailUiState.Success(
                        alert = PreviewData.alertCritical,
                        mitigations = listOf(
                            PreviewData.mitigationIncomplete,
                            PreviewData.mitigationIncomplete2,
                        ),
                    ),
                    onResolve = {},
                    onRetry = {},
                    onBack = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/AlertDetail_Unresolved.png")
    }

    @Test
    fun alertDetail_success_resolved() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                AlertDetailContent(
                    uiState = AlertDetailUiState.Success(
                        alert = PreviewData.alertLowResolved,
                        mitigations = listOf(PreviewData.mitigationCompleted),
                    ),
                    onResolve = {},
                    onRetry = {},
                    onBack = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/AlertDetail_Resolved.png")
    }

    @Test
    fun alertDetail_error() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                AlertDetailContent(
                    uiState = AlertDetailUiState.Error("Alert not found"),
                    onResolve = {},
                    onRetry = {},
                    onBack = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/AlertDetail_Error.png")
    }

    @Test
    fun alertDetail_session_expired() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                AlertDetailContent(
                    uiState = AlertDetailUiState.SessionExpired,
                    onResolve = {},
                    onRetry = {},
                    onBack = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/AlertDetail_SessionExpired.png")
    }
}
