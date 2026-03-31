package com.privacyalert.integration

import com.privacyalert.config.AppProperties
import com.privacyalert.domain.model.PasteFinding
import com.privacyalert.domain.repository.PasteFindingRepository
import com.privacyalert.domain.service.BreachResult
import com.privacyalert.domain.service.BreachScanner
import com.privacyalert.integration.scraping.RateLimitedHttpClient
import com.privacyalert.integration.scraping.RobotsTxtChecker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PasteMonitorClient(
    private val httpClient: RateLimitedHttpClient,
    private val robotsTxtChecker: RobotsTxtChecker,
    private val pasteFindingRepository: PasteFindingRepository,
    private val appProperties: AppProperties,
) : BreachScanner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun scanEmail(email: String): List<BreachResult> {
        if (!appProperties.scanning.pasteMonitorEnabled) return emptyList()
        return searchPasteSites(email)
    }

    override fun scanPhone(phone: String): List<BreachResult> {
        if (!appProperties.scanning.pasteMonitorEnabled || phone.isBlank()) return emptyList()
        return searchPasteSites(phone)
    }

    private fun searchPasteSites(query: String): List<BreachResult> {
        val results = mutableListOf<BreachResult>()

        // Search DuckDuckGo for paste site results
        val searchUrl = "https://html.duckduckgo.com/html/?q=${java.net.URLEncoder.encode("\"$query\" site:pastebin.com", Charsets.UTF_8)}"

        if (!robotsTxtChecker.isAllowed(searchUrl)) return emptyList()

        try {
            val doc = httpClient.fetch(searchUrl) ?: return emptyList()
            val links = doc.select("a.result__a")
                .mapNotNull { it.attr("href") }
                .filter { it.contains("pastebin.com") }
                .take(5)

            for (link in links) {
                if (pasteFindingRepository.existsByPasteUrl(link)) continue

                pasteFindingRepository.save(
                    PasteFinding(
                        source = "pastebin.com",
                        pasteUrl = link,
                        title = "Paste containing personal data",
                    ),
                )

                results.add(
                    BreachResult(
                        name = "Paste exposure",
                        domain = "pastebin.com",
                        breachDate = java.time.LocalDate.now().toString(),
                        dataClasses = listOf("Email addresses"),
                    ),
                )
            }
        } catch (e: Exception) {
            log.warn("Paste monitoring failed: {}", e.message)
        }

        return results
    }
}
