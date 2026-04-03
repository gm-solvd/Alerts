package com.privacyalert.android.ui.screen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.privacyalert.android.ui.theme.PrivacyAlertColors
import androidx.compose.ui.text.style.TextDecoration
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.privacyalert.android.ui.theme.Spacing
import com.privacyalert.domain.model.Mitigation
import com.privacyalert.presentation.viewmodel.MitigationsUiState
import com.privacyalert.presentation.viewmodel.MitigationsViewModel

class MitigationsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<MitigationsViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        MitigationsContent(
            uiState = uiState,
            onRefresh = { viewModel.load() },
            onComplete = { id -> viewModel.completeMitigation(id) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MitigationsContent(
    uiState: MitigationsUiState,
    onRefresh: () -> Unit,
    onComplete: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Fix It") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        when (uiState) {
            is MitigationsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is MitigationsUiState.Error -> {
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

            is MitigationsUiState.Success -> {
                if (uiState.incomplete.isEmpty() && uiState.completed.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PrivacyAlertColors.Success,
                                modifier = Modifier.padding(bottom = Spacing.sm),
                            )
                            Text(
                                text = "No mitigations needed",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "Run a scan to find actionable items.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            if (uiState.incomplete.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(Spacing.sm))
                                    Text(
                                        text = "To Do (${uiState.incomplete.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                }
                                items(uiState.incomplete, key = { it.id }) { mitigation ->
                                    MitigationActionCard(
                                        mitigation = mitigation,
                                        onComplete = { onComplete(mitigation.id) },
                                    )
                                }
                            }

                            if (uiState.completed.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(Spacing.md))
                                    Text(
                                        text = "Completed (${uiState.completed.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                items(uiState.completed, key = { it.id }) { mitigation ->
                                    CompletedMitigationCard(mitigation = mitigation)
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

@Composable
private fun MitigationActionCard(
    mitigation: Mitigation,
    onComplete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = mitigation.title,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = mitigation.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            FilledTonalButton(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text("Mark as Done")
            }
        }
    }
}

@Composable
private fun CompletedMitigationCard(mitigation: Mitigation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = PrivacyAlertColors.Success,
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                text = mitigation.title,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
