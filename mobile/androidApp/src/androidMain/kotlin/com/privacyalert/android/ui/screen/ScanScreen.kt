package com.privacyalert.android.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.privacyalert.android.ui.component.SeverityBadge
import com.privacyalert.android.ui.theme.ComponentSize
import com.privacyalert.android.ui.theme.Spacing
import com.privacyalert.domain.model.Alert
import com.privacyalert.presentation.viewmodel.ScanUiState
import com.privacyalert.presentation.viewmodel.ScanViewModel

class ScanScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<ScanViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        val uiState by viewModel.uiState.collectAsState()

        ScanContent(
            uiState = uiState,
            onStartScan = { email -> viewModel.startFullScan(email) },
            onReset = { viewModel.reset() },
            onAlertClick = { alert -> navigator.push(AlertDetailScreen(alert.id)) },
            onBack = { navigator.pop() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanContent(
    uiState: ScanUiState,
    onStartScan: (String) -> Unit,
    onReset: () -> Unit,
    onAlertClick: (Alert) -> Unit,
    onBack: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Privacy Scan") },
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
            is ScanUiState.Idle -> {
                ScanInputSection(
                    email = email,
                    onEmailChange = { email = it },
                    onStartScan = { onStartScan(email) },
                )
            }

            is ScanUiState.Scanning -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            text = "Scanning for privacy threats...",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "This may take a moment",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            is ScanUiState.Error -> {
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
                        Button(onClick = onReset) {
                            Text("Try Again")
                        }
                    }
                }
            }

            is ScanUiState.Success -> {
                ScanResultsSection(
                    alerts = uiState.alerts,
                    onAlertClick = onAlertClick,
                    onScanAgain = onReset,
                )
            }
        }
    }
}

@Composable
private fun ScanInputSection(
    email: String,
    onEmailChange: (String) -> Unit,
    onStartScan: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(Spacing.xl))

        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "Full Privacy Scan",
            style = MaterialTheme.typography.headlineSmall,
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = "Check for data breaches, identity exposure, and permission issues.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email address") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Button(
            onClick = onStartScan,
            modifier = Modifier
                .fillMaxWidth()
                .height(ComponentSize.buttonHeight),
            enabled = email.isNotBlank(),
        ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text("Start Scan")
        }
    }
}

@Composable
private fun ScanResultsSection(
    alerts: List<Alert>,
    onAlertClick: (Alert) -> Unit,
    onScanAgain: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.sm))

            AnimatedVisibility(visible = true) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (alerts.isEmpty()) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        },
                    ),
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            text = if (alerts.isEmpty()) {
                                "No threats found"
                            } else {
                                "${alerts.size} threat${if (alerts.size > 1) "s" else ""} found"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = if (alerts.isEmpty()) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            },
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = if (alerts.isEmpty()) {
                                "Your email appears clean. Keep monitoring regularly."
                            } else {
                                "Tap an alert to see details and recommended actions."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (alerts.isEmpty()) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            },
                        )
                    }
                }
            }
        }

        items(alerts, key = { it.id }) { alert ->
            ScanResultCard(alert = alert, onClick = { onAlertClick(alert) })
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.sm))
            TextButton(
                onClick = onScanAgain,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Scan Again")
            }
            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun ScanResultCard(
    alert: Alert,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
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
                    maxLines = 2,
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
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
