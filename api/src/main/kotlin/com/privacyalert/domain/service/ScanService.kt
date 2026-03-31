package com.privacyalert.domain.service

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.repository.AlertRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ScanService(
    private val alertRepository: AlertRepository,
    private val breachScanner: BreachScanner,
    private val scoreService: ScoreService,
) {

    fun breachScan(userId: UUID, email: String): List<Alert> {
        val breaches = breachScanner.scanEmail(email)

        val alerts = breaches.map { breach ->
            alertRepository.save(
                Alert(
                    userId = userId,
                    category = ThreatCategory.DATA_BREACH,
                    severity = Severity.CRITICAL,
                    title = "Data breach: ${breach.name}",
                    description = "Your email was found in the ${breach.name} breach (${breach.breachDate}). " +
                        "Exposed data: ${breach.dataClasses.joinToString(", ")}.",
                ),
            )
        }

        if (alerts.isNotEmpty()) {
            scoreService.recalculate(userId)
        }

        return alerts
    }

    // TODO: Implement social media footprint search
    fun identityScan(userId: UUID, email: String): List<Alert> {
        return emptyList()
    }
}
