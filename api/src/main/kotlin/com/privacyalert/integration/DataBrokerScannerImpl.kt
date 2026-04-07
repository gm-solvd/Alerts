package com.privacyalert.integration

import com.privacyalert.domain.model.DataBrokerCategory
import com.privacyalert.domain.model.DataBrokerSite
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.UserScanProfile
import com.privacyalert.domain.repository.DataBrokerSiteRepository
import com.privacyalert.domain.service.DataBrokerExposureResult
import com.privacyalert.domain.service.DataBrokerScanner
import com.privacyalert.integration.scraping.RateLimitedHttpClient
import com.privacyalert.integration.scraping.RobotsTxtChecker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URLEncoder

@Component
class DataBrokerScannerImpl(
    private val dataBrokerSiteRepository: DataBrokerSiteRepository,
    private val httpClient: RateLimitedHttpClient,
    private val robotsTxtChecker: RobotsTxtChecker,
) : DataBrokerScanner {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun scan(profile: UserScanProfile): List<DataBrokerExposureResult> {
        val sites = dataBrokerSiteRepository.findAllActive()
        return sites.mapNotNull { site -> evaluateBroker(site, profile) }
    }

    private fun evaluateBroker(
        site: DataBrokerSite,
        profile: UserScanProfile,
    ): DataBrokerExposureResult? =
        when (site.category) {
            DataBrokerCategory.CREDIT_BUREAU -> evaluateCreditBureau(site, profile)
            DataBrokerCategory.PEOPLE_SEARCH -> evaluatePeopleSearch(site, profile)
            DataBrokerCategory.MARKETING_DATA -> evaluateMarketingData(site, profile)
            DataBrokerCategory.DATA_AGGREGATOR -> evaluateDataAggregator(site, profile)
        }

    private fun evaluateCreditBureau(
        site: DataBrokerSite,
        profile: UserScanProfile,
    ): DataBrokerExposureResult? {
        val hasFinancialPii = profile.fullName != null || profile.homeAddress != null
        if (!hasFinancialPii) return null

        return DataBrokerExposureResult(
            brokerId = site.id,
            brokerName = site.name,
            category = site.category,
            severity = Severity.HIGH,
            exposedFields =
                site.piiFields.filter { field ->
                    when (field) {
                        "name" -> profile.fullName != null
                        "address" -> profile.homeAddress != null
                        "phone" -> profile.phoneNumber != null
                        "email" -> true
                        else -> true
                    }
                },
            detectionMethod = "heuristic_credit_bureau",
        )
    }

    private fun evaluatePeopleSearch(
        site: DataBrokerSite,
        profile: UserScanProfile,
    ): DataBrokerExposureResult? {
        if (profile.fullName.isNullOrBlank()) return null

        val searchUrl = buildSearchUrl(site, profile) ?: return null

        return try {
            if (!robotsTxtChecker.isAllowed(searchUrl)) return null
            val doc = httpClient.fetch(searchUrl) ?: return null
            val selector = site.resultSelector ?: return null
            val resultElements = doc.select(selector)

            if (resultElements.isEmpty()) return null

            val text = resultElements.first()?.text()?.lowercase() ?: ""
            val nameParts = profile.fullName.lowercase().split(" ")
            val nameMatches = nameParts.any { it.length > 2 && text.contains(it) }

            if (!nameMatches) return null

            val exposedFields =
                site.piiFields.filter { field ->
                    when (field) {
                        "name" -> true
                        "phone" -> profile.phoneNumber != null
                        "address" -> profile.homeAddress != null
                        "email" -> true
                        "age" -> profile.dateOfBirth != null
                        else -> false
                    }
                }

            val severity =
                when {
                    exposedFields.containsAll(listOf("phone", "address")) -> Severity.HIGH
                    exposedFields.containsAll(listOf("name", "address")) -> Severity.MEDIUM
                    else -> Severity.LOW
                }

            DataBrokerExposureResult(
                brokerId = site.id,
                brokerName = site.name,
                category = site.category,
                severity = severity,
                exposedFields = exposedFields,
                detectionMethod = "web_scrape",
            )
        } catch (e: Exception) {
            log.debug("Data broker scan failed for {}: {}", site.name, e.message)
            null
        }
    }

    private fun evaluateMarketingData(
        site: DataBrokerSite,
        profile: UserScanProfile,
    ): DataBrokerExposureResult? {
        if (profile.email.isBlank()) return null

        return DataBrokerExposureResult(
            brokerId = site.id,
            brokerName = site.name,
            category = site.category,
            severity = Severity.MEDIUM,
            exposedFields =
                site.piiFields.filter { field ->
                    when (field) {
                        "name" -> profile.fullName != null
                        "address" -> profile.homeAddress != null
                        "phone" -> profile.phoneNumber != null
                        "email" -> true
                        else -> true
                    }
                },
            detectionMethod = "heuristic_marketing",
        )
    }

    private fun evaluateDataAggregator(
        site: DataBrokerSite,
        profile: UserScanProfile,
    ): DataBrokerExposureResult? {
        val hasSubstantialPii = profile.fullName != null && (profile.homeAddress != null || profile.phoneNumber != null)
        if (!hasSubstantialPii) return null

        return DataBrokerExposureResult(
            brokerId = site.id,
            brokerName = site.name,
            category = site.category,
            severity = Severity.HIGH,
            exposedFields =
                site.piiFields.filter { field ->
                    when (field) {
                        "name" -> true
                        "address" -> profile.homeAddress != null
                        "phone" -> profile.phoneNumber != null
                        "email" -> true
                        else -> true
                    }
                },
            detectionMethod = "heuristic_aggregator",
        )
    }

    private fun buildSearchUrl(
        site: DataBrokerSite,
        profile: UserScanProfile,
    ): String? {
        val template = site.searchUrlTemplate ?: return null
        val name = profile.fullName ?: return null
        val location =
            profile.homeAddress
                ?.split(",")
                ?.lastOrNull()
                ?.trim() ?: ""

        return template
            .replace("{name}", URLEncoder.encode(name.replace(" ", "-"), Charsets.UTF_8))
            .replace("{phone}", URLEncoder.encode(profile.phoneNumber ?: "", Charsets.UTF_8))
            .replace("{location}", URLEncoder.encode(location, Charsets.UTF_8))
    }
}
