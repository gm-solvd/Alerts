package com.privacyalert.domain.service

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.ScanResult
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.model.UserScanProfile
import com.privacyalert.domain.repository.AlertRepository
import com.privacyalert.domain.repository.ScanResultRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ScanService(
    private val alertRepository: AlertRepository,
    private val breachScanner: BreachScanner,
    private val piiExposureScanner: PiiExposureScanner,
    private val identityExposureScanner: IdentityExposureScanner,
    private val socialFootprintScanner: SocialFootprintScanner,
    private val scoreService: ScoreService,
    private val dataTypeNormalizer: DataTypeNormalizer,
    private val breachRiskClassifier: BreachRiskClassifier,
    private val scanResultRepository: ScanResultRepository,
) {
    fun breachScan(
        userId: UUID,
        profile: UserScanProfile,
    ): List<Alert> {
        val emailBreaches = breachScanner.scanEmail(profile.email)
        val phoneBreaches = profile.phoneNumber?.let { breachScanner.scanPhone(it) } ?: emptyList()

        val allBreaches = (emailBreaches + phoneBreaches).distinctBy { it.name }

        val alerts =
            allBreaches.map { breach ->
                val normalized = dataTypeNormalizer.normalize(breach.dataClasses)
                val severity = breachRiskClassifier.classify(normalized)
                alertRepository.save(
                    Alert(
                        userId = userId,
                        category = ThreatCategory.DATA_BREACH,
                        severity = severity,
                        title = "Data breach: ${breach.name}",
                        description =
                            "Your data was found in the ${breach.name} breach (${breach.breachDate}). " +
                                "Exposed data: ${normalized.joinToString(", ")}.",
                    ),
                )
            }

        scanResultRepository.save(
            ScanResult(
                userId = userId,
                scanType = "breach",
                scanInput = profile.email,
                findings =
                    allBreaches
                        .joinToString("; ") {
                            "${it.name} (${it.domain}, ${it.breachDate}) - ${it.dataClasses.joinToString(", ")}"
                        }.ifEmpty { "No breaches found" },
            ),
        )

        if (alerts.isNotEmpty()) {
            scoreService.recalculate(userId)
        }

        return alerts
    }

    fun identityScan(
        userId: UUID,
        profile: UserScanProfile,
    ): List<Alert> {
        val results = identityExposureScanner.scan(profile.email, profile.fullName)

        val alerts =
            results.map { result ->
                alertRepository.save(
                    Alert(
                        userId = userId,
                        category = ThreatCategory.IDENTITY_EXPOSURE,
                        severity = if (result.exposedFields.size >= 3) Severity.HIGH else Severity.MEDIUM,
                        title = "Identity exposed on ${result.source}",
                        description =
                            "Your profile was found on ${result.source} (${result.profileUrl}). " +
                                "Exposed info: ${result.exposedFields.joinToString(", ")}.",
                    ),
                )
            }

        scanResultRepository.save(
            ScanResult(
                userId = userId,
                scanType = "identity",
                scanInput = profile.email,
                findings =
                    results
                        .joinToString("; ") {
                            "${it.source}: ${it.profileUrl} - ${it.exposedFields.joinToString(", ")}"
                        }.ifEmpty { "No identity exposures found" },
            ),
        )

        if (alerts.isNotEmpty()) {
            scoreService.recalculate(userId)
        }

        return alerts
    }

    fun piiExposureScan(
        userId: UUID,
        profile: UserScanProfile,
    ): List<Alert> {
        val results = piiExposureScanner.scan(profile)

        val alerts =
            results.map { result ->
                val severity =
                    when {
                        result.exposedFields.containsAll(listOf("phone", "address")) -> Severity.HIGH
                        result.exposedFields.containsAll(listOf("name", "address")) -> Severity.MEDIUM
                        else -> Severity.LOW
                    }
                alertRepository.save(
                    Alert(
                        userId = userId,
                        category = ThreatCategory.TRACKER_EXPOSURE,
                        severity = severity,
                        title = "PII found on ${result.source}",
                        description =
                            "Your personal information was found on ${result.source} (${result.sourceUrl}). " +
                                "Exposed fields: ${result.exposedFields.joinToString(", ")}.",
                    ),
                )
            }

        scanResultRepository.save(
            ScanResult(
                userId = userId,
                scanType = "pii",
                scanInput = profile.email,
                findings =
                    results
                        .joinToString("; ") {
                            "${it.source} (${it.sourceUrl}) - fields: ${it.exposedFields.joinToString(", ")}"
                        }.ifEmpty { "No PII exposures found" },
            ),
        )

        if (alerts.isNotEmpty()) {
            scoreService.recalculate(userId)
        }

        return alerts
    }

    fun socialFootprintScan(
        userId: UUID,
        profile: UserScanProfile,
        username: String?,
    ): List<Alert> {
        val results = socialFootprintScanner.scan(profile.email, profile.fullName, username)

        val alerts =
            results.map { result ->
                alertRepository.save(
                    Alert(
                        userId = userId,
                        category = ThreatCategory.SOCIAL_FOOTPRINT,
                        severity = if (result.publicInfoFound.size >= 3) Severity.MEDIUM else Severity.LOW,
                        title = "Profile found on ${result.platform}",
                        description =
                            "Your profile '${result.username}' was found on ${result.platform} (${result.profileUrl}). " +
                                "Public info: ${result.publicInfoFound.joinToString(", ")}.",
                    ),
                )
            }

        scanResultRepository.save(
            ScanResult(
                userId = userId,
                scanType = "social",
                scanInput = profile.email,
                findings =
                    results
                        .joinToString("; ") {
                            "${it.platform}: ${it.username} (${it.profileUrl}) - ${it.publicInfoFound.joinToString(", ")}"
                        }.ifEmpty { "No social profiles found" },
            ),
        )

        if (alerts.isNotEmpty()) {
            scoreService.recalculate(userId)
        }

        return alerts
    }

    fun fullScan(
        userId: UUID,
        profile: UserScanProfile,
        username: String?,
    ): List<Alert> {
        val alerts = mutableListOf<Alert>()
        alerts += breachScan(userId, profile)
        alerts += identityScan(userId, profile)
        alerts += piiExposureScan(userId, profile)
        alerts += socialFootprintScan(userId, profile, username)
        return alerts
    }
}
