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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.privacyalert.android.ui.theme.PrivacyAlertColors
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.privacyalert.android.ui.component.SeverityBadge
import com.privacyalert.android.ui.theme.Spacing
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Mitigation
import com.privacyalert.presentation.viewmodel.AlertDetailUiState
import com.privacyalert.presentation.viewmodel.AlertDetailViewModel
import org.koin.core.parameter.parametersOf

class AlertDetailScreen(private val alertId: String) : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<AlertDetailViewModel> { parametersOf(alertId) }
        val navigator = LocalNavigator.currentOrThrow
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        AlertDetailContent(
            uiState = uiState,
            onResolve = { viewModel.resolve() },
            onRetry = { viewModel.load() },
            onBack = { navigator.pop() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertDetailContent(
    uiState: AlertDetailUiState,
    onResolve: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Alert Detail") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        when (uiState) {
            is AlertDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is AlertDetailUiState.Error -> {
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
                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }
            }

            is AlertDetailUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.md),
                ) {
                    AlertInfoSection(alert = uiState.alert)

                    if (!uiState.alert.resolved) {
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Button(
                            onClick = onResolve,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text("Mark as Resolved")
                        }
                    }

                    if (uiState.mitigations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        Text(
                            text = "Mitigations",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        uiState.mitigations.forEach { mitigation ->
                            MitigationCard(mitigation = mitigation)
                            Spacer(modifier = Modifier.height(Spacing.sm))
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))
                }
            }
        }
    }
}

@Composable
private fun AlertInfoSection(alert: Alert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                SeverityBadge(severity = alert.severity)
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = alert.category.name.replace('_', ' '),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            if (alert.resolved) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PrivacyAlertColors.Success,
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Resolved",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrivacyAlertColors.Success,
                    )
                }
            }
        }
    }
}

@Composable
private fun MitigationCard(mitigation: Mitigation) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (mitigation.completed) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = PrivacyAlertColors.Success,
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                }
                Text(
                    text = mitigation.title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                if (mitigation.actionUrl != null) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Open link",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = mitigation.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
