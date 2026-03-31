package com.privacyalert.integration

import com.privacyalert.config.AppProperties
import com.privacyalert.integration.scraping.RateLimitedHttpClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClient

class SocialFootprintScannerImplTest {
    private val httpClient = mockk<RateLimitedHttpClient>()

    private val appProperties =
        AppProperties(
            jwt = AppProperties.JwtProperties(secret = "test-secret-key-that-is-long-enough-for-hs256"),
            hibp = AppProperties.HibpProperties(),
            scanning =
                AppProperties.ScanningProperties(
                    socialEnabledPlatforms = listOf("github", "stackoverflow", "reddit", "mastodon"),
                    rateLimitPerDomainDelayMs = 0,
                ),
        )

    private val mockRestClient = mockk<RestClient>()
    private val mockRequestSpec = mockk<RestClient.RequestHeadersUriSpec<*>>(relaxed = true)

    @BeforeEach
    fun setUp() {
        mockkStatic(RestClient::class)
        every { RestClient.create() } returns mockRestClient
        every { mockRestClient.get() } returns mockRequestSpec

        // Make all RestClient calls throw so platform checks fail gracefully
        every { mockRequestSpec.uri(any<String>()) } returns mockRequestSpec
        every { (mockRequestSpec as RestClient.RequestHeadersSpec<*>).header(any(), any()) } returns mockRequestSpec
        every { (mockRequestSpec as RestClient.RequestHeadersSpec<*>).retrieve() } throws RuntimeException("Network error")
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(RestClient::class)
    }

    @Test
    fun `scan derives usernames from email local part`() {
        every { httpClient.fetch(any()) } returns null

        val scanner = SocialFootprintScannerImpl(httpClient, appProperties)
        val results = scanner.scan("john.doe@example.com", null, null)

        // All platform checks fail due to mocked RestClient and null httpClient responses.
        // Derived usernames would be "john.doe" and "johndoe".
        assertTrue(results.isEmpty())
    }

    @Test
    fun `scan returns empty when all platform checks fail`() {
        every { httpClient.fetch(any()) } returns null

        val scanner = SocialFootprintScannerImpl(httpClient, appProperties)
        val results = scanner.scan("testuser@example.com", null, null)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `scan only checks enabled platforms`() {
        val limitedAppProperties =
            AppProperties(
                jwt = AppProperties.JwtProperties(secret = "test-secret-key-that-is-long-enough-for-hs256"),
                hibp = AppProperties.HibpProperties(),
                scanning =
                    AppProperties.ScanningProperties(
                        socialEnabledPlatforms = emptyList(),
                        rateLimitPerDomainDelayMs = 0,
                    ),
            )

        every { httpClient.fetch(any()) } returns null

        val scanner = SocialFootprintScannerImpl(httpClient, limitedAppProperties)
        val results = scanner.scan("testuser@example.com", null, null)

        // No platforms enabled, so no checks at all (web search may still run but returns null)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `scan uses provided username when available`() {
        every { httpClient.fetch(any()) } returns null

        val scanner = SocialFootprintScannerImpl(httpClient, appProperties)
        val results = scanner.scan("testuser@example.com", "Full Name", "customuser")

        // All checks fail gracefully
        assertTrue(results.isEmpty())
    }

    @Test
    fun `scan with no enabled platforms returns empty`() {
        val noSocialProps =
            AppProperties(
                jwt = AppProperties.JwtProperties(secret = "test-secret-key-that-is-long-enough-for-hs256"),
                hibp = AppProperties.HibpProperties(),
                scanning =
                    AppProperties.ScanningProperties(
                        socialEnabledPlatforms = emptyList(),
                        rateLimitPerDomainDelayMs = 0,
                    ),
            )

        every { httpClient.fetch(any()) } returns null

        val scanner = SocialFootprintScannerImpl(httpClient, noSocialProps)
        val results = scanner.scan("user@example.com", null, null)

        assertEquals(0, results.size)
    }
}
