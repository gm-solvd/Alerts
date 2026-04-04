package com.privacyalert.android.ui.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.privacyalert.android.ui.preview.PreviewData
import com.privacyalert.android.ui.screen.DashboardContentForTest
import com.privacyalert.android.ui.theme.PrivacyAlertTheme
import com.privacyalert.presentation.viewmodel.DashboardUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DashboardScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboard_loading() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContentForTest(uiState = DashboardUiState.Loading)
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_Loading.png")
    }

    @Test
    fun dashboard_idle() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContentForTest(uiState = PreviewData.dashboardIdle)
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_Idle.png")
    }

    @Test
    fun dashboard_scanning() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContentForTest(uiState = PreviewData.dashboardScanning)
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_Scanning.png")
    }

    @Test
    fun dashboard_fixing() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContentForTest(uiState = PreviewData.dashboardFixing)
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_Fixing.png")
    }

    @Test
    fun dashboard_scan_complete() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContentForTest(uiState = PreviewData.dashboardScanComplete)
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_ScanComplete.png")
    }

    @Test
    fun dashboard_fix_complete() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContentForTest(uiState = PreviewData.dashboardFixComplete)
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_FixComplete.png")
    }

    @Test
    fun dashboard_error() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContentForTest(
                    uiState = DashboardUiState.Error("Failed to load dashboard"),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_Error.png")
    }

    @Test
    fun dashboard_empty() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContentForTest(uiState = PreviewData.dashboardEmpty)
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_Empty.png")
    }
}
