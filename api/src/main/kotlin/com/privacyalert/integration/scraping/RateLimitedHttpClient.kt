package com.privacyalert.integration.scraping

import com.privacyalert.config.AppProperties
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class RateLimitedHttpClient(
    private val appProperties: AppProperties,
) {
    private val lastRequestTime = ConcurrentHashMap<String, Long>()

    fun fetch(url: String): Document? {
        val domain = extractDomain(url)
        throttle(domain)
        return try {
            Jsoup
                .connect(url)
                .userAgent("PrivacyAlertBot/1.0")
                .timeout(10_000)
                .get()
        } catch (e: Exception) {
            null
        }
    }

    fun fetchText(url: String): String? = fetch(url)?.text()

    private fun throttle(domain: String) {
        val delayMs = appProperties.scanning.rateLimitPerDomainDelayMs
        val lastTime = lastRequestTime[domain] ?: 0L
        val waitTime = delayMs - (System.currentTimeMillis() - lastTime)
        if (waitTime > 0) {
            Thread.sleep(waitTime)
        }
        lastRequestTime[domain] = System.currentTimeMillis()
    }

    private fun extractDomain(url: String): String =
        try {
            java.net.URI(url).host ?: url
        } catch (e: Exception) {
            url
        }
}
