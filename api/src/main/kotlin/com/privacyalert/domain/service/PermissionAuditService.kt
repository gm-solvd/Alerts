package com.privacyalert.domain.service

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.repository.AlertRepository
import org.springframework.stereotype.Service
import java.util.UUID

data class PermissionEntry(
    val name: String,
    val granted: Boolean,
)

@Service
class PermissionAuditService(
    private val alertRepository: AlertRepository,
    private val scoreService: ScoreService,
) {

    private val riskyPermissions = setOf(
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.READ_CONTACTS",
        "android.permission.READ_CALL_LOG",
        "android.permission.READ_SMS",
        "android.permission.BODY_SENSORS",
    )

    fun submitAudit(userId: UUID, permissions: List<PermissionEntry>): List<Alert> {
        val risky = permissions.filter { it.granted && it.name in riskyPermissions }

        val alerts = risky.map { perm ->
            alertRepository.save(
                Alert(
                    userId = userId,
                    category = ThreatCategory.APP_OVERPERMISSIONS,
                    severity = Severity.MEDIUM,
                    title = "Risky permission granted: ${perm.name.substringAfterLast(".")}",
                    description = "The permission '${perm.name}' is granted. " +
                        "Review whether all apps using this permission genuinely need it.",
                ),
            )
        }

        if (alerts.isNotEmpty()) {
            scoreService.recalculate(userId)
        }

        return alerts
    }
}
