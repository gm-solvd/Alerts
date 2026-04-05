package com.privacyalert.android.ui.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.privacyalert.android.ui.preview.PreviewData
import com.privacyalert.presentation.ui.screen.DashboardContent
import com.privacyalert.presentation.ui.theme.PrivacyAlertTheme
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
                DashboardContent(
                    uiState = DashboardUiState.Loading,
                    onRefresh = {},
                    onLogout = {},
                    onViewAllAlerts = {},
                    onAlertClick = { _ -> },
                    onScanNow = {},
                    onFixIt = {},
                    onDismissAction = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_Loading.png")
    }

    @Test
    fun dashboard_idle() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContent(
                    uiState = PreviewData.dashboardIdle,
                    onRefresh = {},
                    onLogout = {},
                    onViewAllAlerts = {},
                    onAlertClick = { _ -> },
                    onScanNow = {},
                    onFixIt = {},
                    onDismissAction = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_Idle.png")
    }

    @Test
    fun dashboard_scanning() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContent(
                    uiState = PreviewData.dashboardScanning,
                    onRefresh = {},
                    onLogout = {},
                    onViewAllAlerts = {},
                    onAlertClick = { _ -> },
                    onScanNow = {},
                    onFixIt = {},
                    onDismissAction = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_Scanning.png")
    }

    @Test
    fun dashboard_fixing() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContent(
                    uiState = PreviewData.dashboardFixing,
                    onRefresh = {},
                    onLogout = {},
                    onViewAllAlerts = {},
                    onAlertClick = { _ -> },
                    onScanNow = {},
                    onFixIt = {},
                    onDismissAction = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_Fixing.png")
    }

    @Test
    fun dashboard_scan_complete() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContent(
                    uiState = PreviewData.dashboardScanComplete,
                    onRefresh = {},
                    onLogout = {},
                    onViewAllAlerts = {},
                    onAlertClick = { _ -> },
                    onScanNow = {},
                    onFixIt = {},
                    onDismissAction = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_ScanComplete.png")
    }

    @Test
    fun dashboard_fix_complete() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContent(
                    uiState = PreviewData.dashboardFixComplete,
                    onRefresh = {},
                    onLogout = {},
                    onViewAllAlerts = {},
                    onAlertClick = { _ -> },
                    onScanNow = {},
                    onFixIt = {},
                    onDismissAction = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_FixComplete.png")
    }

    @Test
    fun dashboard_error() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContent(
                    uiState = DashboardUiState.Error("Failed to load dashboard"),
                    onRefresh = {},
                    onLogout = {},
                    onViewAllAlerts = {},
                    onAlertClick = { _ -> },
                    onScanNow = {},
                    onFixIt = {},
                    onDismissAction = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_Error.png")
    }

    @Test
    fun dashboard_empty() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContent(
                    uiState = PreviewData.dashboardEmpty,
                    onRefresh = {},
                    onLogout = {},
                    onViewAllAlerts = {},
                    onAlertClick = { _ -> },
                    onScanNow = {},
                    onFixIt = {},
                    onDismissAction = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_Empty.png")
    }

    @Test
    fun dashboard_session_expired() {
        composeTestRule.setContent {
            PrivacyAlertTheme {
                DashboardContent(
                    uiState = PreviewData.dashboardSessionExpired,
                    onRefresh = {},
                    onLogout = {},
                    onViewAllAlerts = {},
                    onAlertClick = { _ -> },
                    onScanNow = {},
                    onFixIt = {},
                    onDismissAction = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/Dashboard_SessionExpired.png")
    }
}
