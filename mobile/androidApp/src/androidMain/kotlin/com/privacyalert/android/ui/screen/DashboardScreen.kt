package com.privacyalert.android.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import androidx.compose.ui.tooling.preview.Preview
import com.privacyalert.android.ui.component.ProgressCard
import com.privacyalert.android.ui.component.ScoreGauge
import com.privacyalert.android.ui.component.SectionHeader
import com.privacyalert.android.ui.component.SeverityBadge
import com.privacyalert.android.ui.preview.PreviewData
import com.privacyalert.android.ui.theme.PrivacyAlertTheme
import com.privacyalert.android.ui.theme.Spacing
import com.privacyalert.android.ui.navigation.AlertsTab
import com.privacyalert.domain.model.Alert
import com.privacyalert.presentation.viewmodel.AuthViewModel
import com.privacyalert.android.ui.theme.AppColors
import com.privacyalert.presentation.viewmodel.ActionState
import com.privacyalert.presentation.viewmodel.DashboardUiState
import com.privacyalert.presentation.viewmodel.DashboardViewModel

class DashboardScreen : Screen {

    @Composable
    override fun Content() {
        val dashboardVm = koinScreenModel<DashboardViewModel>()
        val authVm = koinScreenModel<AuthViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        val tabNavigator = LocalTabNavigator.current
        val uiState by dashboardVm.uiState.collectAsStateWithLifecycle()

        DashboardContent(
            uiState = uiState,
            onRefresh = { dashboardVm.load() },
            onLogout = { authVm.logout() },
            onViewAllAlerts = { tabNavigator.current = AlertsTab },
            onAlertClick = { alert -> navigator.push(AlertDetailScreen(alert.id)) },
            onScanNow = { dashboardVm.startScan() },
            onFixIt = { dashboardVm.startFixAll() },
            onDismissAction = { dashboardVm.dismissAction() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onViewAllAlerts: () -> Unit,
    onAlertClick: (Alert) -> Unit,
    onScanNow: () -> Unit,
    onFixIt: () -> Unit,
    onDismissAction: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Privacy Alert") },
            actions = {
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        when (uiState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is DashboardUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Button(onClick = onRefresh) {
                            Text("Retry")
                        }
                    }
                }
            }

            is DashboardUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = false,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(Spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(Spacing.md))

                        ScoreGauge(score = uiState.score.score)

                        Spacer(modifier = Modifier.height(Spacing.md))

                        when (uiState.actionState) {
                            is ActionState.Scanning -> {
                                ProgressCard(
                                    icon = Icons.Default.Search,
                                    title = "Scanning...",
                                    containerColor = AppColors.BluePrimary,
                                )
                            }

                            is ActionState.Fixing -> {
                                ProgressCard(
                                    icon = Icons.Default.Build,
                                    title = "Fixing...",
                                    containerColor = AppColors.FixGreen,
                                )
                            }

                            is ActionState.ScanComplete -> {
                                val scanResult = uiState.actionState as ActionState.ScanComplete
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    ),
                                ) {
                                    Column(modifier = Modifier.padding(Spacing.md)) {
                                        Text(
                                            text = "Scan complete — ${scanResult.newAlerts} new alerts found",
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        Spacer(modifier = Modifier.height(Spacing.sm))
                                        Button(onClick = onDismissAction) {
                                            Text("Done")
                                        }
                                    }
                                }
                            }

                            is ActionState.FixComplete -> {
                                val fixResult = uiState.actionState as ActionState.FixComplete
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    ),
                                ) {
                                    Column(modifier = Modifier.padding(Spacing.md)) {
                                        Text(
                                            text = "Fix complete — ${fixResult.resolvedCount} mitigations resolved",
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        Spacer(modifier = Modifier.height(Spacing.sm))
                                        Button(onClick = onDismissAction) {
                                            Text("Done")
                                        }
                                    }
                                }
                            }

                            is ActionState.ActionError -> {
                                val actionError = uiState.actionState as ActionState.ActionError
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                    ),
                                ) {
                                    Column(modifier = Modifier.padding(Spacing.md)) {
                                        Text(
                                            text = actionError.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                        Spacer(modifier = Modifier.height(Spacing.sm))
                                        Button(onClick = onDismissAction) {
                                            Text("Dismiss")
                                        }
                                    }
                                }
                            }

                            is ActionState.Idle -> {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                ) {
                                    FilledTonalButton(onClick = onScanNow) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                        )
                                        Spacer(modifier = Modifier.width(Spacing.xs))
                                        Text("Run Scan")
                                    }
                                    FilledTonalButton(onClick = onFixIt) {
                                        Icon(
                                            imageVector = Icons.Default.Build,
                                            contentDescription = null,
                                        )
                                        Spacer(modifier = Modifier.width(Spacing.xs))
                                        Text("Fix It")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xl))

                        if (uiState.topAlerts.isNotEmpty()) {
                            SectionHeader(
                                title = "Recent Alerts",
                                actionText = "View All",
                                onAction = onViewAllAlerts,
                            )
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            uiState.topAlerts.forEach { alert ->
                                AlertSummaryCard(
                                    alert = alert,
                                    onClick = { onAlertClick(alert) },
                                )
                                Spacer(modifier = Modifier.height(Spacing.sm))
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            ) {
                                Text(
                                    text = "No alerts. Your privacy looks good!",
                                    modifier = Modifier.padding(Spacing.md),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.lg))
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertSummaryCard(
    alert: Alert,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = alert.category.name.replace('_', ' '),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(Spacing.sm))
            SeverityBadge(severity = alert.severity)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardContentSuccessPreview() {
    PrivacyAlertTheme {
        DashboardContent(
            uiState = DashboardUiState.Success(
                score = PreviewData.scoreHigh,
                topAlerts = PreviewData.alertList.take(3),
            ),
            onRefresh = {},
            onLogout = {},
            onViewAllAlerts = {},
            onAlertClick = {},
            onScanNow = {},
            onFixIt = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardContentScanningPreview() {
    PrivacyAlertTheme {
        DashboardContent(
            uiState = PreviewData.dashboardScanning,
            onRefresh = {},
            onLogout = {},
            onViewAllAlerts = {},
            onAlertClick = {},
            onScanNow = {},
            onFixIt = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardContentFixingPreview() {
    PrivacyAlertTheme {
        DashboardContent(
            uiState = PreviewData.dashboardFixing,
            onRefresh = {},
            onLogout = {},
            onViewAllAlerts = {},
            onAlertClick = {},
            onScanNow = {},
            onFixIt = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardContentErrorPreview() {
    PrivacyAlertTheme {
        DashboardContent(
            uiState = DashboardUiState.Error("Failed to load dashboard"),
            onRefresh = {},
            onLogout = {},
            onViewAllAlerts = {},
            onAlertClick = {},
            onScanNow = {},
            onFixIt = {},
        )
    }
}
