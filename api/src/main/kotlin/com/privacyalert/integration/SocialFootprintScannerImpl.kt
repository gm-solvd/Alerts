package com.privacyalert.integration

import com.privacyalert.config.AppProperties
import com.privacyalert.domain.service.SocialFootprintResult
import com.privacyalert.domain.service.SocialFootprintScanner
import com.privacyalert.integration.scraping.RateLimitedHttpClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.net.URLEncoder

@Component
class SocialFootprintScannerImpl(
    private val httpClient: RateLimitedHttpClient,
    private val appProperties: AppProperties,
) : SocialFootprintScanner {

    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    override fun scan(email: String, fullName: String?, username: String?): List<SocialFootprintResult> {
        val usernames = deriveUsernames(email, username)
        val results = mutableListOf<SocialFootprintResult>()
        val enabledPlatforms = appProperties.scanning.socialEnabledPlatforms

        for (candidate in usernames) {
            if ("github" in enabledPlatforms) {
                checkGitHub(candidate)?.let { results.add(it) }
            }
            if ("reddit" in enabledPlatforms) {
                checkReddit(candidate)?.let { results.add(it) }
            }
            if ("stackoverflow" in enabledPlatforms) {
                checkStackOverflow(candidate)?.let { results.add(it) }
            }
            if ("mastodon" in enabledPlatforms) {
                checkMastodon(candidate)?.let { results.add(it) }
            }
        }

        searchWebForProfiles(fullName, usernames.firstOrNull())?.let { results.addAll(it) }

        return results.distinctBy { it.profileUrl }
    }

    private fun deriveUsernames(email: String, providedUsername: String?): List<String> {
        val usernames = mutableListOf<String>()
        if (!providedUsername.isNullOrBlank()) {
            usernames.add(providedUsername)
        }
        val localPart = email.substringBefore("@")
        usernames.add(localPart)
        if (localPart.contains(".")) {
            usernames.add(localPart.replace(".", ""))
        }
        return usernames.distinct().take(3)
    }

    private fun checkGitHub(username: String): SocialFootprintResult? {
        return try {
            val url = "https://api.github.com/users/$username"
            val response = restClient.get().uri(url)
                .header("Accept", "application/vnd.github.v3+json")
                .retrieve()
                .body(String::class.java) ?: return null

            val publicInfo = mutableListOf<String>("username")
            if (response.contains("\"bio\"") && !response.contains("\"bio\":null")) publicInfo.add("bio")
            if (response.contains("\"location\"") && !response.contains("\"location\":null")) publicInfo.add("location")
            if (response.contains("\"name\"") && !response.contains("\"name\":null")) publicInfo.add("name")

            SocialFootprintResult(
                platform = "GitHub",
                profileUrl = "https://github.com/$username",
                username = username,
                publicInfoFound = publicInfo,
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun checkReddit(username: String): SocialFootprintResult? {
        return try {
            val url = "https://www.reddit.com/user/$username/about.json"
            val response = restClient.get().uri(url)
                .header("User-Agent", "PrivacyAlertBot/1.0")
                .retrieve()
                .body(String::class.java) ?: return null

            if (response.contains("\"name\"")) {
                SocialFootprintResult(
                    platform = "Reddit",
                    profileUrl = "https://www.reddit.com/user/$username",
                    username = username,
                    publicInfoFound = listOf("username", "posts", "karma"),
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun checkStackOverflow(username: String): SocialFootprintResult? {
        return try {
            val url = "https://stackoverflow.com/users?q=${URLEncoder.encode(username, Charsets.UTF_8)}"
            val doc = httpClient.fetch(url) ?: return null
            val userCards = doc.select("div.user-info")

            if (userCards.isNotEmpty()) {
                val firstCard = userCards.first()
                val profileLink = firstCard?.select("a")?.first()?.attr("href") ?: ""
                SocialFootprintResult(
                    platform = "StackOverflow",
                    profileUrl = "https://stackoverflow.com$profileLink",
                    username = username,
                    publicInfoFound = listOf("username", "reputation", "posts"),
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun checkMastodon(username: String): SocialFootprintResult? {
        val instances = listOf("mastodon.social", "hachyderm.io", "fosstodon.org")
        for (instance in instances) {
            try {
                val url = "https://$instance/@$username"
                val doc = httpClient.fetch(url) ?: continue
                if (doc.select("div.account__header").isNotEmpty() ||
                    doc.title().contains(username, ignoreCase = true)
                ) {
                    return SocialFootprintResult(
                        platform = "Mastodon ($instance)",
                        profileUrl = url,
                        username = username,
                        publicInfoFound = listOf("username", "bio", "posts"),
                    )
                }
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    private fun searchWebForProfiles(fullName: String?, username: String?): List<SocialFootprintResult>? {
        val searchTerm = fullName ?: username ?: return null
        return try {
            val query = "\"$searchTerm\" site:linkedin.com OR site:twitter.com OR site:facebook.com"
            val searchUrl = "https://html.duckduckgo.com/html/?q=${URLEncoder.encode(query, Charsets.UTF_8)}"
            val doc = httpClient.fetch(searchUrl) ?: return null

            doc.select("a.result__a")
                .take(5)
                .mapNotNull { link ->
                    val href = link.attr("href")
                    val platform = when {
                        href.contains("linkedin.com") -> "LinkedIn"
                        href.contains("twitter.com") || href.contains("x.com") -> "Twitter/X"
                        href.contains("facebook.com") -> "Facebook"
                        else -> null
                    }
                    if (platform != null) {
                        SocialFootprintResult(
                            platform = platform,
                            profileUrl = href,
                            username = searchTerm,
                            publicInfoFound = listOf("profile", "name"),
                        )
                    } else {
                        null
                    }
                }
                .takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            log.debug("Web profile search failed: {}", e.message)
            null
        }
    }
}
