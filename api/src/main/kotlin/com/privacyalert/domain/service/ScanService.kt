package com.privacyalert.domain.service

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.DataBrokerCategory
import com.privacyalert.domain.model.Mitigation
import com.privacyalert.domain.model.ScanResult
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.StructuredFinding
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.model.UserScanProfile
import com.privacyalert.domain.repository.AlertRepository
import com.privacyalert.domain.repository.MitigationRepository
import com.privacyalert.domain.repository.ScanResultRepository
import org.springframework.stereotype.Service
import java.util.Optional
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
    private val emailReputationScanner: Optional<EmailReputationScanner>,
    private val dataBrokerScanner: DataBrokerScanner,
    private val mitigationRepository: MitigationRepository,
) {
    fun breachScan(
        userId: UUID,
        profile: UserScanProfile,
    ): List<Alert> {
        val emailBreaches = breachScanner.scanEmail(profile.email)
        val phoneBreaches = profile.phoneNumber?.let { breachScanner.scanPhone(it) } ?: emptyList()

        val allBreaches = (emailBreaches + phoneBreaches).distinctBy { it.name }

        val structuredFindings = mutableListOf<StructuredFinding>()

        val alerts =
            allBreaches.map { breach ->
                val normalized = dataTypeNormalizer.normalize(breach.dataClasses)
                val severity = breachRiskClassifier.classify(normalized)
                val hasPassword = normalized.any { it.equals("Passwords", ignoreCase = true) }
                structuredFindings +=
                    StructuredFinding(
                        type = "breach",
                        name = breach.name,
                        sourceUrl = null,
                        date = breach.breachDate,
                        dataClasses = normalized,
                        severity = severity.name,
                        credentialExposed = hasPassword,
                    )
                val tags = if (hasPassword) listOf("credential_exposed") else emptyList()
                alertRepository.save(
                    Alert(
                        userId = userId,
                        category = ThreatCategory.DATA_BREACH,
                        severity = severity,
                        title = "Data breach: ${breach.name}",
                        description =
                            "Your data was found in the ${breach.name} breach (${breach.breachDate}). " +
                                "Exposed data: ${normalized.joinToString(", ")}.",
                        tags = tags,
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
                findingsJson = structuredFindings,
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

        val structuredFindings = mutableListOf<StructuredFinding>()

        val alerts =
            results.map { result ->
                val severity = if (result.exposedFields.size >= 3) Severity.HIGH else Severity.MEDIUM
                structuredFindings +=
                    StructuredFinding(
                        type = "identity",
                        name = result.source,
                        sourceUrl = result.profileUrl,
                        exposedFields = result.exposedFields,
                        severity = severity.name,
                    )
                alertRepository.save(
                    Alert(
                        userId = userId,
                        category = ThreatCategory.IDENTITY_EXPOSURE,
                        severity = severity,
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
                findingsJson = structuredFindings,
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

        val structuredFindings = mutableListOf<StructuredFinding>()

        val alerts =
            results.map { result ->
                val severity =
                    when {
                        result.exposedFields.containsAll(listOf("phone", "address")) -> Severity.HIGH
                        result.exposedFields.containsAll(listOf("name", "address")) -> Severity.MEDIUM
                        else -> Severity.LOW
                    }
                structuredFindings +=
                    StructuredFinding(
                        type = "pii",
                        name = result.source,
                        sourceUrl = result.sourceUrl,
                        exposedFields = result.exposedFields,
                        severity = severity.name,
                    )
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
                findingsJson = structuredFindings,
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

        val structuredFindings = mutableListOf<StructuredFinding>()

        val alerts =
            results.map { result ->
                val severity = if (result.publicInfoFound.size >= 3) Severity.MEDIUM else Severity.LOW
                structuredFindings +=
                    StructuredFinding(
                        type = "social",
                        name = result.platform,
                        sourceUrl = result.profileUrl,
                        exposedFields = result.publicInfoFound,
                        severity = severity.name,
                    )
                alertRepository.save(
                    Alert(
                        userId = userId,
                        category = ThreatCategory.SOCIAL_FOOTPRINT,
                        severity = severity,
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
                findingsJson = structuredFindings,
            ),
        )

        if (alerts.isNotEmpty()) {
            scoreService.recalculate(userId)
        }

        return alerts
    }

    fun emailReputationScan(
        userId: UUID,
        profile: UserScanProfile,
    ): List<Alert> {
        val scanner = emailReputationScanner.orElse(null) ?: return emptyList()
        val result = scanner.scan(profile.email) ?: return emptyList()

        val severity =
            when {
                result.reputation == "none" || result.suspicious -> Severity.HIGH
                result.reputation == "low" -> Severity.MEDIUM
                result.reputation == "medium" -> Severity.LOW
                else -> return emptyList()
            }

        val tags = mutableListOf("email_reputation")
        if (result.credentialsLeaked) tags += "credential_exposed"

        val finding =
            StructuredFinding(
                type = "reputation",
                name = "EmailRep",
                severity = severity.name,
                credentialExposed = result.credentialsLeaked,
                exposedFields =
                    buildList {
                        if (result.credentialsLeaked) add("Credentials leaked")
                        if (result.darkWebAppearances > 0) add("Dark web appearances: ${result.darkWebAppearances}")
                        if (result.dataBreachCount > 0) add("Data breaches: ${result.dataBreachCount}")
                        if (result.profilesFound > 0) add("Profiles found: ${result.profilesFound}")
                    },
            )

        val description =
            buildString {
                append("Email reputation: ${result.reputation}.")
                if (result.suspicious) append(" Flagged as suspicious.")
                if (result.credentialsLeaked) append(" Credentials found in leaked databases.")
                if (result.darkWebAppearances > 0) append(" ${result.darkWebAppearances} dark web appearances.")
                if (result.dataBreachCount > 0) append(" Found in ${result.dataBreachCount} data breaches.")
            }

        val alert =
            alertRepository.save(
                Alert(
                    userId = userId,
                    category = ThreatCategory.IDENTITY_EXPOSURE,
                    severity = severity,
                    title = "Email reputation: ${result.reputation}",
                    description = description,
                    tags = tags,
                ),
            )

        scanResultRepository.save(
            ScanResult(
                userId = userId,
                scanType = "reputation",
                scanInput = profile.email,
                findings = description,
                findingsJson = listOf(finding),
            ),
        )

        if (severity != Severity.LOW) {
            scoreService.recalculate(userId)
        }

        return listOf(alert)
    }

    fun dataBrokerScan(
        userId: UUID,
        profile: UserScanProfile,
    ): List<Alert> {
        val results = dataBrokerScanner.scan(profile)

        val structuredFindings = mutableListOf<StructuredFinding>()

        val alerts =
            results.map { result ->
                structuredFindings +=
                    StructuredFinding(
                        type = "data_broker",
                        name = result.brokerName,
                        severity = result.severity.name,
                        exposedFields = result.exposedFields,
                    )
                val alert =
                    alertRepository.save(
                        Alert(
                            userId = userId,
                            category = ThreatCategory.DATA_BROKER_EXPOSURE,
                            severity = result.severity,
                            title = "Data held by ${result.brokerName}",
                            description =
                                "${result.brokerName} (${result.category.name.lowercase().replace('_', ' ')}) " +
                                    "likely holds your data. Exposed fields: ${result.exposedFields.joinToString(", ")}.",
                            tags = listOf("data_broker", result.category.name.lowercase()),
                        ),
                    )

                val mitigationTitle =
                    when (result.category) {
                        DataBrokerCategory.CREDIT_BUREAU ->
                            "Request your credit report from ${result.brokerName}"
                        else -> "Request data removal from ${result.brokerName}"
                    }
                mitigationRepository.save(
                    Mitigation(
                        alertId = alert.id,
                        title = mitigationTitle,
                        description = "Visit ${result.brokerName}'s data access portal to review and request removal of your data.",
                        actionUrl = result.dataAccessUrl,
                    ),
                )

                alert
            }

        scanResultRepository.save(
            ScanResult(
                userId = userId,
                scanType = "data_broker",
                scanInput = profile.email,
                findings =
                    results
                        .joinToString("; ") {
                            "${it.brokerName} (${it.category}) - ${it.exposedFields.joinToString(", ")}"
                        }.ifEmpty { "No data broker exposures found" },
                findingsJson = structuredFindings,
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
        alerts += emailReputationScan(userId, profile)
        alerts += dataBrokerScan(userId, profile)
        return alerts
    }
}
