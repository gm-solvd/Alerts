package com.privacyalert.integration.scraping

import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class RobotsTxtChecker {
    private data class RobotsEntry(
        val disallowedPaths: List<String>,
        val fetchedAt: Long,
    )

    private val cache = ConcurrentHashMap<String, RobotsEntry>()
    private val cacheTtlMs = 3_600_000L // 1 hour

    fun isAllowed(url: String): Boolean {
        val domain =
            try {
                val uri = java.net.URI(url)
                "${uri.scheme}://${uri.host}"
            } catch (e: Exception) {
                return true
            }

        val path =
            try {
                java.net.URI(url).path ?: "/"
            } catch (e: Exception) {
                return true
            }

        val entry = getOrFetch(domain)
        return entry.disallowedPaths.none { path.startsWith(it) }
    }

    private fun getOrFetch(domain: String): RobotsEntry {
        val cached = cache[domain]
        if (cached != null && System.currentTimeMillis() - cached.fetchedAt < cacheTtlMs) {
            return cached
        }

        val entry =
            try {
                val robotsUrl = "$domain/robots.txt"
                val body =
                    Jsoup
                        .connect(robotsUrl)
                        .userAgent("PrivacyAlertBot/1.0")
                        .timeout(5_000)
                        .ignoreContentType(true)
                        .execute()
                        .body()

                val disallowed = parseRobotsTxt(body)
                RobotsEntry(disallowed, System.currentTimeMillis())
            } catch (e: Exception) {
                RobotsEntry(emptyList(), System.currentTimeMillis())
            }

        cache[domain] = entry
        return entry
    }

    private fun parseRobotsTxt(content: String): List<String> {
        var inOurSection = false
        val disallowed = mutableListOf<String>()

        for (line in content.lines()) {
            val trimmed = line.trim().lowercase()
            when {
                trimmed.startsWith("user-agent:") -> {
                    val agent = trimmed.substringAfter("user-agent:").trim()
                    inOurSection = agent == "*" || agent == "privacyalertbot"
                }
                inOurSection && trimmed.startsWith("disallow:") -> {
                    val path =
                        line
                            .trim()
                            .substringAfter("Disallow:")
                            .trim()
                            .ifEmpty { line.trim().substringAfter("disallow:").trim() }
                    if (path.isNotBlank()) {
                        disallowed.add(path)
                    }
                }
            }
        }

        return disallowed
    }
}
