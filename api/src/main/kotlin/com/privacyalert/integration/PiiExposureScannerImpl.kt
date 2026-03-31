package com.privacyalert.integration

import com.privacyalert.domain.model.DataBrokerSite
import com.privacyalert.domain.model.UserScanProfile
import com.privacyalert.domain.repository.DataBrokerSiteRepository
import com.privacyalert.domain.service.PiiExposureResult
import com.privacyalert.domain.service.PiiExposureScanner
import com.privacyalert.integration.scraping.RateLimitedHttpClient
import com.privacyalert.integration.scraping.RobotsTxtChecker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URLEncoder

@Component
class PiiExposureScannerImpl(
    private val dataBrokerSiteRepository: DataBrokerSiteRepository,
    private val httpClient: RateLimitedHttpClient,
    private val robotsTxtChecker: RobotsTxtChecker,
) : PiiExposureScanner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun scan(profile: UserScanProfile): List<PiiExposureResult> {
        val results = mutableListOf<PiiExposureResult>()

        results.addAll(scanDataBrokerSites(profile))
        results.addAll(searchWebForPii(profile))

        return results.distinctBy { it.sourceUrl }
    }

    private fun scanDataBrokerSites(profile: UserScanProfile): List<PiiExposureResult> {
        if (profile.fullName.isNullOrBlank()) return emptyList()

        val sites = dataBrokerSiteRepository.findAllActive()
        val results = mutableListOf<PiiExposureResult>()

        for (site in sites) {
            try {
                val url = buildSearchUrl(site, profile) ?: continue
                if (!robotsTxtChecker.isAllowed(url)) continue

                val doc = httpClient.fetch(url) ?: continue
                val selector = site.resultSelector ?: continue
                val resultElements = doc.select(selector)

                if (resultElements.isNotEmpty()) {
                    val text = resultElements.first()?.text()?.lowercase() ?: ""
                    val nameParts = profile.fullName.lowercase().split(" ")
                    val nameMatches = nameParts.any { it.length > 2 && text.contains(it) }

                    if (nameMatches) {
                        results.add(
                            PiiExposureResult(
                                source = site.name,
                                sourceUrl = url,
                                exposedFields = site.piiFields,
                                snippet = resultElements.first()?.text()?.take(200),
                            ),
                        )
                    }
                }
            } catch (e: Exception) {
                log.debug("Data broker scan failed for {}: {}", site.name, e.message)
            }
        }

        return results
    }

    private fun searchWebForPii(profile: UserScanProfile): List<PiiExposureResult> {
        val results = mutableListOf<PiiExposureResult>()
        val queries = buildSearchQueries(profile)

        for (query in queries) {
            try {
                val searchUrl = "https://html.duckduckgo.com/html/?q=${URLEncoder.encode(query, Charsets.UTF_8)}"
                if (!robotsTxtChecker.isAllowed(searchUrl)) continue

                val doc = httpClient.fetch(searchUrl) ?: continue
                val links = doc.select("a.result__a")

                for (link in links.take(5)) {
                    val href = link.attr("href")
                    val title = link.text()
                    if (href.isNotBlank() && looksLikeDataBroker(href)) {
                        val exposedFields = mutableListOf<String>()
                        if (!profile.fullName.isNullOrBlank()) exposedFields.add("name")
                        if (!profile.phoneNumber.isNullOrBlank() && title.contains(profile.phoneNumber.takeLast(4))) {
                            exposedFields.add("phone")
                        }
                        if (!profile.homeAddress.isNullOrBlank()) exposedFields.add("address")

                        results.add(
                            PiiExposureResult(
                                source = "Web search result",
                                sourceUrl = href,
                                exposedFields = exposedFields,
                                snippet = title.take(200),
                            ),
                        )
                    }
                }
            } catch (e: Exception) {
                log.debug("Web PII search failed for query: {}", e.message)
            }
        }

        return results
    }

    private fun buildSearchUrl(site: DataBrokerSite, profile: UserScanProfile): String? {
        val template = site.searchUrlTemplate ?: return null
        val name = profile.fullName ?: return null
        val location = profile.homeAddress?.split(",")?.lastOrNull()?.trim() ?: ""

        return template
            .replace("{name}", URLEncoder.encode(name.replace(" ", "-"), Charsets.UTF_8))
            .replace("{phone}", URLEncoder.encode(profile.phoneNumber ?: "", Charsets.UTF_8))
            .replace("{location}", URLEncoder.encode(location, Charsets.UTF_8))
    }

    private fun buildSearchQueries(profile: UserScanProfile): List<String> {
        val queries = mutableListOf<String>()

        if (!profile.phoneNumber.isNullOrBlank()) {
            queries.add("\"${profile.phoneNumber}\"")
        }
        if (!profile.fullName.isNullOrBlank()) {
            val city = profile.homeAddress?.split(",")?.firstOrNull()?.trim() ?: ""
            if (city.isNotBlank()) {
                queries.add("\"${profile.fullName}\" \"$city\"")
            }
        }
        if (!profile.fullName.isNullOrBlank() && !profile.phoneNumber.isNullOrBlank()) {
            queries.add("\"${profile.fullName}\" \"${profile.phoneNumber}\"")
        }

        return queries.take(3)
    }

    private fun looksLikeDataBroker(url: String): Boolean {
        val brokerDomains = listOf(
            "whitepages", "spokeo", "beenverified", "truepeoplesearch", "fastpeoplesearch",
            "thatsthem", "usphonebook", "radaris", "peoplefinder", "numlookup",
            "intelius", "pipl", "zabasearch", "anywho", "addresses.com",
        )
        return brokerDomains.any { url.contains(it, ignoreCase = true) }
    }
}
