package com.privacyalert.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val jwt: JwtProperties,
    val hibp: HibpProperties,
) {
    data class JwtProperties(
        val secret: String,
        val accessTokenTtl: Duration = Duration.ofMinutes(15),
        val refreshTokenTtl: Duration = Duration.ofDays(30),
    )

    data class HibpProperties(
        val apiKey: String = "",
        val baseUrl: String = "https://haveibeenpwned.com/api/v3",
    )
}
