package com.privacyalert.integration

import com.privacyalert.domain.model.DataBrokerSite
import com.privacyalert.domain.model.UserScanProfile
import com.privacyalert.domain.repository.DataBrokerSiteRepository
import com.privacyalert.integration.scraping.RateLimitedHttpClient
import com.privacyalert.integration.scraping.RobotsTxtChecker
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PiiExposureScannerImplTest {
    private val dataBrokerSiteRepository = mockk<DataBrokerSiteRepository>()
    private val httpClient = mockk<RateLimitedHttpClient>()
    private val robotsTxtChecker = mockk<RobotsTxtChecker>()
    private val scanner = PiiExposureScannerImpl(dataBrokerSiteRepository, httpClient, robotsTxtChecker)

    @Test
    fun `scan returns empty when profile has no fullName`() {
        val profile = UserScanProfile(email = "test@example.com", fullName = null)

        every { dataBrokerSiteRepository.findAllActive() } returns emptyList()
        every { robotsTxtChecker.isAllowed(any()) } returns true
        every { httpClient.fetch(any()) } returns null

        val results = scanner.scan(profile)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `scan returns empty when robots txt blocks data broker URL`() {
        val profile =
            UserScanProfile(
                email = "test@example.com",
                fullName = "John Doe",
            )
        val site =
            DataBrokerSite(
                name = "TestBroker",
                baseUrl = "https://testbroker.com",
                searchUrlTemplate = "https://testbroker.com/search?name={name}",
                resultSelector = "div.result",
                piiFields = listOf("name", "phone"),
            )

        every { dataBrokerSiteRepository.findAllActive() } returns listOf(site)
        every { robotsTxtChecker.isAllowed(any()) } returns false

        val results = scanner.scan(profile)

        assertTrue(results.isEmpty())
        verify(exactly = 0) { httpClient.fetch(any()) }
    }

    @Test
    fun `scan returns results when data broker site returns matching content`() {
        val profile =
            UserScanProfile(
                email = "test@example.com",
                fullName = "John Doe",
            )
        val site =
            DataBrokerSite(
                name = "TestBroker",
                baseUrl = "https://testbroker.com",
                searchUrlTemplate = "https://testbroker.com/search?name={name}",
                resultSelector = "div.result",
                piiFields = listOf("name", "phone"),
            )

        val html = "<html><body><div class=\"result\">John Doe, age 35, located in Springfield</div></body></html>"
        val doc = Jsoup.parse(html)

        every { dataBrokerSiteRepository.findAllActive() } returns listOf(site)
        every { robotsTxtChecker.isAllowed(any()) } returns true
        every { httpClient.fetch(any()) } returns doc

        val results = scanner.scan(profile)

        assertEquals(1, results.size)
        assertEquals("TestBroker", results[0].source)
        assertEquals(listOf("name", "phone"), results[0].exposedFields)
    }
}
