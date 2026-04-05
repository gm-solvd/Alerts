package com.privacyalert.presentation.ui.screen

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.privacyalert.presentation.ui.component.CredentialExposedBadge
import com.privacyalert.presentation.ui.component.ProgressCard
import com.privacyalert.presentation.ui.component.ScoreGauge
import com.privacyalert.presentation.ui.component.SectionHeader
import com.privacyalert.presentation.ui.component.SeverityBadge
import com.privacyalert.presentation.ui.theme.PrivacyAlertTheme
import com.privacyalert.presentation.ui.theme.Spacing
import com.privacyalert.presentation.ui.navigation.AlertsTab
import com.privacyalert.domain.model.Alert
import com.privacyalert.presentation.viewmodel.AuthViewModel
import com.privacyalert.presentation.ui.theme.AppColors
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
        val uiState by dashboardVm.uiState.collectAsState()

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
fun DashboardContent(
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
            is DashboardUiState.Loading -> LoadingContent()
            is DashboardUiState.SessionExpired -> SessionExpiredContent()
            is DashboardUiState.Error -> ErrorContent(
                message = uiState.message,
                onRetry = onRefresh,
            )
            is DashboardUiState.Success -> SuccessContent(
                uiState = uiState,
                onRefresh = onRefresh,
                onViewAllAlerts = onViewAllAlerts,
                onAlertClick = onAlertClick,
                onScanNow = onScanNow,
                onFixIt = onFixIt,
                onDismissAction = onDismissAction,
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SessionExpiredContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Session expired. Redirecting to login...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessContent(
    uiState: DashboardUiState.Success,
    onRefresh: () -> Unit,
    onViewAllAlerts: () -> Unit,
    onAlertClick: (Alert) -> Unit,
    onScanNow: () -> Unit,
    onFixIt: () -> Unit,
    onDismissAction: () -> Unit,
) {
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

            val actionState = uiState.actionState
            when (actionState) {
                is ActionState.Scanning -> ScanningCard()
                is ActionState.Fixing -> FixingCard()
                is ActionState.ScanComplete -> ScanCompleteCard(
                    newAlerts = actionState.newAlerts,
                    onDismiss = onDismissAction,
                )
                is ActionState.FixComplete -> FixCompleteCard(
                    resolvedCount = actionState.resolvedCount,
                    onDismiss = onDismissAction,
                )
                is ActionState.ActionError -> ActionErrorCard(
                    message = actionState.message,
                    onDismiss = onDismissAction,
                )
                is ActionState.Idle -> IdleActionButtons(
                    onScanNow = onScanNow,
                    onFixIt = onFixIt,
                )
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

@Composable
private fun ScanningCard() {
    ProgressCard(
        icon = Icons.Default.Search,
        title = "Scanning...",
        containerColor = AppColors.BluePrimary,
    )
}

@Composable
private fun FixingCard() {
    ProgressCard(
        icon = Icons.Default.Build,
        title = "Fixing...",
        containerColor = AppColors.FixGreen,
    )
}

@Composable
private fun ScanCompleteCard(newAlerts: Int, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = "Scan complete — $newAlerts new alerts found",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun FixCompleteCard(resolvedCount: Int, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = "Fix complete — $resolvedCount mitigations resolved",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun ActionErrorCard(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Button(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun IdleActionButtons(onScanNow: () -> Unit, onFixIt: () -> Unit) {
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
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                SeverityBadge(severity = alert.severity)
                if ("credential_exposed" in alert.tags) {
                    CredentialExposedBadge()
                }
            }
        }
    }
}
