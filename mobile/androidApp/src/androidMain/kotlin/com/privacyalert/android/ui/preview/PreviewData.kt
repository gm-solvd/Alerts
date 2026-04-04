package com.privacyalert.android.ui.preview

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Mitigation
import com.privacyalert.domain.model.ScoreRecord
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.presentation.viewmodel.ActionState
import com.privacyalert.presentation.viewmodel.DashboardUiState
import kotlinx.datetime.Instant

object PreviewData {

    private val now = Instant.parse("2026-04-03T12:00:00Z")

    val alertCritical = Alert(
        id = "alert-1",
        category = ThreatCategory.DATA_BREACH,
        severity = Severity.CRITICAL,
        title = "Email found in major data breach",
        description = "Your email was found in a breach affecting 10M+ accounts. Passwords and personal data may be exposed.",
        resolved = false,
        resolvedAt = null,
        createdAt = now,
    )

    val alertHigh = Alert(
        id = "alert-2",
        category = ThreatCategory.IDENTITY_EXPOSURE,
        severity = Severity.HIGH,
        title = "Personal information exposed online",
        description = "Your full name and address were found on a public data broker site.",
        resolved = false,
        resolvedAt = null,
        createdAt = now,
    )

    val alertMedium = Alert(
        id = "alert-3",
        category = ThreatCategory.APP_OVERPERMISSIONS,
        severity = Severity.MEDIUM,
        title = "App requesting excessive permissions",
        description = "A social media app is requesting camera and microphone access unnecessarily.",
        resolved = false,
        resolvedAt = null,
        createdAt = now,
    )

    val alertLowResolved = Alert(
        id = "alert-4",
        category = ThreatCategory.TRACKER_EXPOSURE,
        severity = Severity.LOW,
        title = "Minor tracking cookie detected",
        description = "A low-risk advertising tracker was found on a visited site.",
        resolved = true,
        resolvedAt = now,
        createdAt = now,
    )

    val alertList = listOf(alertCritical, alertHigh, alertMedium, alertLowResolved)

    val mitigationIncomplete = Mitigation(
        id = "mit-1",
        alertId = "alert-1",
        title = "Change your password immediately",
        description = "Update your password on the affected service and any other accounts using the same credentials.",
        actionUrl = "https://example.com/reset",
        completed = false,
        completedAt = null,
        createdAt = now,
    )

    val mitigationIncomplete2 = Mitigation(
        id = "mit-2",
        alertId = "alert-1",
        title = "Enable two-factor authentication",
        description = "Add an extra layer of security to prevent unauthorized access.",
        actionUrl = null,
        completed = false,
        completedAt = null,
        createdAt = now,
    )

    val mitigationCompleted = Mitigation(
        id = "mit-3",
        alertId = "alert-2",
        title = "Request data removal from broker",
        description = "Submit a removal request to have your personal information deleted.",
        actionUrl = "https://example.com/remove",
        completed = true,
        completedAt = now,
        createdAt = now,
    )

    val scoreHigh = ScoreRecord(
        id = "score-1",
        score = 85,
        recordedAt = now,
    )

    val scoreLow = ScoreRecord(
        id = "score-2",
        score = 32,
        recordedAt = now,
    )

    // Dashboard state previews
    val dashboardIdle = DashboardUiState.Success(
        score = scoreHigh,
        topAlerts = alertList.take(3),
        actionState = ActionState.Idle,
    )

    val dashboardScanning = DashboardUiState.Success(
        score = scoreHigh,
        topAlerts = alertList.take(3),
        actionState = ActionState.Scanning,
    )

    val dashboardFixing = DashboardUiState.Success(
        score = scoreHigh,
        topAlerts = alertList.take(3),
        actionState = ActionState.Fixing,
    )

    val dashboardScanComplete = DashboardUiState.Success(
        score = scoreHigh,
        topAlerts = alertList.take(3),
        actionState = ActionState.ScanComplete(newAlerts = 3),
    )

    val dashboardFixComplete = DashboardUiState.Success(
        score = scoreHigh,
        topAlerts = alertList.take(3),
        actionState = ActionState.FixComplete(resolvedCount = 5),
    )

    val dashboardEmpty = DashboardUiState.Success(
        score = ScoreRecord(id = "score-perfect", score = 100, recordedAt = now),
        topAlerts = emptyList(),
        actionState = ActionState.Idle,
    )
}
