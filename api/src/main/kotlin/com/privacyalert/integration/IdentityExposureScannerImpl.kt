package com.privacyalert.integration

import com.privacyalert.domain.service.IdentityExposureResult
import com.privacyalert.domain.service.IdentityExposureScanner
import com.privacyalert.integration.scraping.RateLimitedHttpClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.net.URLEncoder
import java.security.MessageDigest

@Component
class IdentityExposureScannerImpl(
    private val httpClient: RateLimitedHttpClient,
) : IdentityExposureScanner {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    override fun scan(
        email: String,
        fullName: String?,
    ): List<IdentityExposureResult> {
        val results = mutableListOf<IdentityExposureResult>()

        checkGravatar(email)?.let { results.add(it) }
        checkGitHub(email)?.let { results.add(it) }
        checkKeybase(email)?.let { results.add(it) }
        searchWeb(email, fullName)?.let { results.addAll(it) }

        return results
    }

    private fun checkGravatar(email: String): IdentityExposureResult? =
        try {
            val hash = md5(email.lowercase().trim())
            val url = "https://gravatar.com/$hash.json"
            val response =
                restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(String::class.java)
            if (response != null) {
                val exposedFields = mutableListOf("email", "photo")
                if (response.contains("\"currentLocation\"")) exposedFields.add("location")
                if (response.contains("\"displayName\"")) exposedFields.add("name")

                IdentityExposureResult(
                    source = "Gravatar",
                    profileUrl = "https://gravatar.com/$hash",
                    displayName = extractJsonField(response, "displayName"),
                    exposedFields = exposedFields,
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

    private fun checkGitHub(email: String): IdentityExposureResult? =
        try {
            val url = "https://api.github.com/search/users?q=${URLEncoder.encode("$email in:email", Charsets.UTF_8)}"
            val response =
                restClient
                    .get()
                    .uri(url)
                    .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .body(String::class.java)

            if (response != null && response.contains("\"total_count\":") && !response.contains("\"total_count\":0")) {
                val exposedFields = mutableListOf("email", "username")
                val login = extractJsonField(response, "login")
                val profileUrl = extractJsonField(response, "html_url") ?: "https://github.com/$login"

                IdentityExposureResult(
                    source = "GitHub",
                    profileUrl = profileUrl,
                    displayName = login,
                    exposedFields = exposedFields,
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

    private fun checkKeybase(email: String): IdentityExposureResult? =
        try {
            val url = "https://keybase.io/_/api/1.0/user/lookup.json?email=${URLEncoder.encode(email, Charsets.UTF_8)}"
            val response =
                restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(String::class.java)

            if (response != null && response.contains("\"them\"") && !response.contains("\"them\":[]")) {
                val username = extractJsonField(response, "username")
                IdentityExposureResult(
                    source = "Keybase",
                    profileUrl = "https://keybase.io/$username",
                    displayName = username,
                    exposedFields = listOf("email", "identity_proofs"),
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

    private fun searchWeb(
        email: String,
        fullName: String?,
    ): List<IdentityExposureResult>? {
        return try {
            val query =
                if (fullName != null) {
                    "\"$fullName\" \"$email\""
                } else {
                    "\"$email\""
                }
            val searchUrl = "https://html.duckduckgo.com/html/?q=${URLEncoder.encode(query, Charsets.UTF_8)}"
            val doc = httpClient.fetch(searchUrl) ?: return null

            doc
                .select("a.result__a")
                .take(5)
                .mapNotNull { link ->
                    val href = link.attr("href")
                    val title = link.text()
                    if (href.isNotBlank() && !href.contains("duckduckgo")) {
                        IdentityExposureResult(
                            source = "Web",
                            profileUrl = href,
                            displayName = title.take(100),
                            exposedFields = listOf("email", "name"),
                        )
                    } else {
                        null
                    }
                }.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            log.debug("Web identity search failed: {}", e.message)
            null
        }
    }

    private fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun extractJsonField(
        json: String,
        field: String,
    ): String? {
        val pattern = "\"$field\"\\s*:\\s*\"([^\"]*)\""
        return Regex(pattern).find(json)?.groupValues?.get(1)
    }
}
