package com.privacyalert.android.ui.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.privacyalert.android.ui.preview.PreviewData
import com.privacyalert.android.ui.screen.AlertsContentForTest
import com.privacyalert.android.ui.theme.PrivacyAlertTheme
import com.privacyalert.domain.model.Severity
import com.privacyalert.presentation.viewmodel.AlertsUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AlertsScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun alerts_loading() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                AlertsContentForTest(uiState = AlertsUiState.Loading)
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Alerts_Loading.png")
    }

    @Test
    fun alerts_success() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                AlertsContentForTest(
                    uiState = AlertsUiState.Success(
                        alerts = PreviewData.alertList,
                        hasMore = true,
                        selectedSeverity = null,
                    ),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Alerts_Success.png")
    }

    @Test
    fun alerts_filtered_critical() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                AlertsContentForTest(
                    uiState = AlertsUiState.Success(
                        alerts = listOf(PreviewData.alertCritical),
                        hasMore = false,
                        selectedSeverity = Severity.CRITICAL,
                    ),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Alerts_FilteredCritical.png")
    }

    @Test
    fun alerts_empty() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                AlertsContentForTest(
                    uiState = AlertsUiState.Success(
                        alerts = emptyList(),
                        hasMore = false,
                        selectedSeverity = null,
                    ),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Alerts_Empty.png")
    }

    @Test
    fun alerts_error() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                AlertsContentForTest(
                    uiState = AlertsUiState.Error("Failed to load alerts"),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Alerts_Error.png")
    }
}
