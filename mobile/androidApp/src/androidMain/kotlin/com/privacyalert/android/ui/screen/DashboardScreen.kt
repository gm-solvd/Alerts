package com.privacyalert.android.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.privacyalert.android.ui.theme.Spacing
import com.privacyalert.domain.model.Alert
import com.privacyalert.presentation.viewmodel.DashboardUiState
import com.privacyalert.presentation.viewmodel.DashboardViewModel

class DashboardScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<DashboardViewModel>()
        val uiState by viewModel.uiState.collectAsState()

        DashboardContent(
            uiState = uiState,
            onRetry = { viewModel.load() },
        )
    }
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onRetry: () -> Unit,
) {
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
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }

        is DashboardUiState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.Top,
            ) {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                Text(
                    text = "Privacy Score",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${uiState.score.score}",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                if (uiState.topAlerts.isNotEmpty()) {
                    Text(
                        text = "Recent Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    uiState.topAlerts.forEach { alert ->
                        AlertCard(alert = alert)
                        Spacer(modifier = Modifier.height(Spacing.sm))
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertCard(alert: Alert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = alert.title,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = alert.severity.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
