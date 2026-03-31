package com.privacyalert.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val jwt: JwtProperties,
    val hibp: HibpProperties,
    val scanning: ScanningProperties = ScanningProperties(),
    val admin: AdminProperties = AdminProperties(),
) {
    data class AdminProperties(
        val token: String = "changeme",
    )

    data class JwtProperties(
        val secret: String,
        val accessTokenTtl: Duration = Duration.ofMinutes(15),
        val refreshTokenTtl: Duration = Duration.ofDays(30),
    )

    data class HibpProperties(
        val apiKey: String = "",
        val enabled: Boolean = false,
        val baseUrl: String = "https://haveibeenpwned.com/api/v3",
    )

    data class ScanningProperties(
        val pasteMonitorEnabled: Boolean = true,
        val rateLimitRequestsPerSecond: Int = 2,
        val rateLimitPerDomainDelayMs: Long = 3000,
        val socialEnabledPlatforms: List<String> = listOf("github", "stackoverflow", "reddit", "mastodon"),
    )
}
