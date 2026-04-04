package com.privacyalert.android.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.tooling.preview.Preview
import com.privacyalert.android.ui.component.SeverityBadge
import com.privacyalert.android.ui.preview.PreviewData
import com.privacyalert.android.ui.theme.PrivacyAlertTheme
import com.privacyalert.android.ui.theme.Spacing
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Severity
import com.privacyalert.presentation.viewmodel.AlertsUiState
import com.privacyalert.presentation.viewmodel.AlertsViewModel

class AlertsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<AlertsViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        AlertsContent(
            uiState = uiState,
            onRefresh = { viewModel.loadAlerts() },
            onLoadMore = { viewModel.loadMore() },
            onFilterSeverity = { viewModel.filterBySeverity(it) },
            onAlertClick = { alert -> navigator.push(AlertDetailScreen(alert.id)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertsContent(
    uiState: AlertsUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onFilterSeverity: (Severity?) -> Unit,
    onAlertClick: (Alert) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Alerts") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        when (uiState) {
            is AlertsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is AlertsUiState.Error -> {
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

            is AlertsUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = false,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SeverityFilterChips(
                            selectedSeverity = uiState.selectedSeverity,
                            onFilterSeverity = onFilterSeverity,
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            items(uiState.alerts, key = { it.id }) { alert ->
                                AlertCard(
                                    alert = alert,
                                    onClick = { onAlertClick(alert) },
                                )
                            }

                            if (uiState.hasMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Spacing.md),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        TextButton(onClick = onLoadMore) {
                                            Text("Load more")
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(Spacing.md))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeverityFilterChips(
    selectedSeverity: Severity?,
    onFilterSeverity: (Severity?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        FilterChip(
            selected = selectedSeverity == null,
            onClick = { onFilterSeverity(null) },
            label = { Text("All") },
        )
        Severity.entries.forEach { severity ->
            FilterChip(
                selected = selectedSeverity == severity,
                onClick = {
                    onFilterSeverity(if (selectedSeverity == severity) null else severity)
                },
                label = { Text(severity.name) },
            )
        }
    }
}

@Composable
private fun AlertCard(
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
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = alert.category.name.replace('_', ' '),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (alert.resolved) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "Resolved",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(modifier = Modifier.width(Spacing.sm))
            SeverityBadge(severity = alert.severity)
        }
    }
}

/** Test-only entry point for AlertsContent. */
@Composable
fun AlertsContentForTest(
    uiState: AlertsUiState,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onFilterSeverity: (Severity?) -> Unit = {},
    onAlertClick: (Alert) -> Unit = {},
) {
    AlertsContent(
        uiState = uiState,
        onRefresh = onRefresh,
        onLoadMore = onLoadMore,
        onFilterSeverity = onFilterSeverity,
        onAlertClick = onAlertClick,
    )
}

@Preview(showBackground = true)
@Composable
private fun AlertsContentSuccessPreview() {
    PrivacyAlertTheme {
        AlertsContent(
            uiState = AlertsUiState.Success(
                alerts = PreviewData.alertList,
                hasMore = true,
                selectedSeverity = null,
            ),
            onRefresh = {},
            onLoadMore = {},
            onFilterSeverity = {},
            onAlertClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlertsContentErrorPreview() {
    PrivacyAlertTheme {
        AlertsContent(
            uiState = AlertsUiState.Error("Failed to load alerts"),
            onRefresh = {},
            onLoadMore = {},
            onFilterSeverity = {},
            onAlertClick = {},
        )
    }
}
