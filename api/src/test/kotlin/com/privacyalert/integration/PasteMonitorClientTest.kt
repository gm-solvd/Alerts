package com.privacyalert.integration

import com.privacyalert.config.AppProperties
import com.privacyalert.domain.repository.PasteFindingRepository
import com.privacyalert.integration.scraping.RateLimitedHttpClient
import com.privacyalert.integration.scraping.RobotsTxtChecker
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PasteMonitorClientTest {
    private val httpClient = mockk<RateLimitedHttpClient>()
    private val robotsTxtChecker = mockk<RobotsTxtChecker>()
    private val pasteFindingRepository = mockk<PasteFindingRepository>()

    private fun buildClient(pasteMonitorEnabled: Boolean = true): PasteMonitorClient {
        val appProperties =
            AppProperties(
                jwt = AppProperties.JwtProperties(secret = "test-secret-key-that-is-long-enough-for-hs256"),
                hibp = AppProperties.HibpProperties(),
                scanning = AppProperties.ScanningProperties(pasteMonitorEnabled = pasteMonitorEnabled),
            )
        return PasteMonitorClient(httpClient, robotsTxtChecker, pasteFindingRepository, appProperties)
    }

    @Test
    fun `scanEmail returns empty when paste monitor is disabled`() {
        val client = buildClient(pasteMonitorEnabled = false)

        val results = client.scanEmail("test@example.com")

        assertTrue(results.isEmpty())
        verify(exactly = 0) { httpClient.fetch(any()) }
    }

    @Test
    fun `scanPhone returns empty when paste monitor is disabled`() {
        val client = buildClient(pasteMonitorEnabled = false)

        val results = client.scanPhone("+1234567890")

        assertTrue(results.isEmpty())
        verify(exactly = 0) { httpClient.fetch(any()) }
    }

    @Test
    fun `scanEmail returns empty when robots txt blocks the URL`() {
        val client = buildClient(pasteMonitorEnabled = true)

        every { robotsTxtChecker.isAllowed(any()) } returns false

        val results = client.scanEmail("test@example.com")

        assertTrue(results.isEmpty())
        verify(exactly = 0) { httpClient.fetch(any()) }
    }

    @Test
    fun `scanPhone returns empty for blank phone`() {
        val client = buildClient(pasteMonitorEnabled = true)

        val results = client.scanPhone("   ")

        assertTrue(results.isEmpty())
        verify(exactly = 0) { robotsTxtChecker.isAllowed(any()) }
    }
}
