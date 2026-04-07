package com.privacyalert.integration

import com.privacyalert.domain.model.DataBrokerCategory
import com.privacyalert.domain.model.DataBrokerSite
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.UserScanProfile
import com.privacyalert.domain.repository.DataBrokerSiteRepository
import com.privacyalert.integration.scraping.RateLimitedHttpClient
import com.privacyalert.integration.scraping.RobotsTxtChecker
import io.mockk.every
import io.mockk.mockk
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DataBrokerScannerImplTest {
    private val dataBrokerSiteRepository = mockk<DataBrokerSiteRepository>()
    private val httpClient = mockk<RateLimitedHttpClient>()
    private val robotsTxtChecker = mockk<RobotsTxtChecker>()

    private val scanner =
        DataBrokerScannerImpl(
            dataBrokerSiteRepository,
            httpClient,
            robotsTxtChecker,
        )

    private val profile =
        UserScanProfile(
            email = "user@example.com",
            phoneNumber = "+1234567890",
            fullName = "John Doe",
            homeAddress = "123 Main St, New York",
        )

    // ── Credit Bureau heuristic ─────────────────────────────────────────

    @Test
    fun `credit bureau returns HIGH severity for user with financial PII`() {
        val site = creditBureauSite()
        every { dataBrokerSiteRepository.findAllActive() } returns listOf(site)

        val results = scanner.scan(profile)

        assertEquals(1, results.size)
        assertEquals(Severity.HIGH, results[0].severity)
        assertEquals(DataBrokerCategory.CREDIT_BUREAU, results[0].category)
        assertEquals("heuristic_credit_bureau", results[0].detectionMethod)
    }

    @Test
    fun `credit bureau returns null for user without name or address`() {
        val site = creditBureauSite()
        val minimalProfile = UserScanProfile(email = "user@example.com")
        every { dataBrokerSiteRepository.findAllActive() } returns listOf(site)

        val results = scanner.scan(minimalProfile)

        assertTrue(results.isEmpty())
    }

    // ── People Search web scrape ────────────────────────────────────────

    @Test
    fun `people search returns result when name match found in HTML`() {
        val site = peopleSearchSite()
        every { dataBrokerSiteRepository.findAllActive() } returns listOf(site)
        every { robotsTxtChecker.isAllowed(any()) } returns true
        every { httpClient.fetch(any()) } returns Jsoup.parse("<div class='card'>John Doe, age 35, 123 Main St</div>")

        val results = scanner.scan(profile)

        assertEquals(1, results.size)
        assertEquals("web_scrape", results[0].detectionMethod)
        assertTrue(results[0].exposedFields.contains("name"))
    }

    @Test
    fun `people search returns HIGH severity when phone and address exposed`() {
        val site = peopleSearchSite(piiFields = listOf("name", "phone", "address"))
        every { dataBrokerSiteRepository.findAllActive() } returns listOf(site)
        every { robotsTxtChecker.isAllowed(any()) } returns true
        every { httpClient.fetch(any()) } returns Jsoup.parse("<div class='card'>John Doe, 555-1234, 123 Main St</div>")

        val results = scanner.scan(profile)

        assertEquals(1, results.size)
        assertEquals(Severity.HIGH, results[0].severity)
    }

    @Test
    fun `people search returns null when no name match in HTML`() {
        val site = peopleSearchSite()
        every { dataBrokerSiteRepository.findAllActive() } returns listOf(site)
        every { robotsTxtChecker.isAllowed(any()) } returns true
        every { httpClient.fetch(any()) } returns Jsoup.parse("<div class='card'>No matching records found</div>")

        val results = scanner.scan(profile)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `people search skips when robots txt disallows`() {
        val site = peopleSearchSite()
        every { dataBrokerSiteRepository.findAllActive() } returns listOf(site)
        every { robotsTxtChecker.isAllowed(any()) } returns false

        val results = scanner.scan(profile)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `people search skips when fullName is null`() {
        val site = peopleSearchSite()
        val noNameProfile = UserScanProfile(email = "user@example.com")
        every { dataBrokerSiteRepository.findAllActive() } returns listOf(site)

        val results = scanner.scan(noNameProfile)

        assertTrue(results.isEmpty())
    }

    // ── Marketing Data heuristic ────────────────────────────────────────

    @Test
    fun `marketing data returns MEDIUM severity for any user with email`() {
        val site = marketingDataSite()
        every { dataBrokerSiteRepository.findAllActive() } returns listOf(site)

        val results = scanner.scan(profile)

        assertEquals(1, results.size)
        assertEquals(Severity.MEDIUM, results[0].severity)
        assertEquals(DataBrokerCategory.MARKETING_DATA, results[0].category)
        assertEquals("heuristic_marketing", results[0].detectionMethod)
    }

    // ── Data Aggregator heuristic ───────────────────────────────────────

    @Test
    fun `data aggregator returns HIGH severity for user with substantial PII`() {
        val site = dataAggregatorSite()
        every { dataBrokerSiteRepository.findAllActive() } returns listOf(site)

        val results = scanner.scan(profile)

        assertEquals(1, results.size)
        assertEquals(Severity.HIGH, results[0].severity)
        assertEquals(DataBrokerCategory.DATA_AGGREGATOR, results[0].category)
        assertEquals("heuristic_aggregator", results[0].detectionMethod)
    }

    @Test
    fun `data aggregator returns null for user without substantial PII`() {
        val site = dataAggregatorSite()
        val minimalProfile = UserScanProfile(email = "user@example.com", fullName = "John Doe")
        every { dataBrokerSiteRepository.findAllActive() } returns listOf(site)

        val results = scanner.scan(minimalProfile)

        assertTrue(results.isEmpty())
    }

    // ── Mixed broker types ──────────────────────────────────────────────

    @Test
    fun `scan returns results from multiple broker categories`() {
        val sites =
            listOf(
                creditBureauSite(),
                marketingDataSite(),
                dataAggregatorSite(),
            )
        every { dataBrokerSiteRepository.findAllActive() } returns sites

        val results = scanner.scan(profile)

        assertEquals(3, results.size)
        assertTrue(results.any { it.category == DataBrokerCategory.CREDIT_BUREAU })
        assertTrue(results.any { it.category == DataBrokerCategory.MARKETING_DATA })
        assertTrue(results.any { it.category == DataBrokerCategory.DATA_AGGREGATOR })
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun creditBureauSite() =
        DataBrokerSite(
            name = "Experian",
            baseUrl = "https://www.experian.com",
            piiFields = listOf("name", "address", "phone", "email", "ssn", "credit_history"),
            category = DataBrokerCategory.CREDIT_BUREAU,
            privacyPolicyUrl = "https://www.experian.com/privacy/",
            dataAccessUrl = "https://www.experian.com/consumer-products/free-credit-report",
        )

    private fun peopleSearchSite(piiFields: List<String> = listOf("name", "phone", "address", "email")) =
        DataBrokerSite(
            name = "Spokeo",
            baseUrl = "https://www.spokeo.com",
            searchUrlTemplate = "https://www.spokeo.com/{name}",
            resultSelector = "div.card",
            piiFields = piiFields,
            category = DataBrokerCategory.PEOPLE_SEARCH,
        )

    private fun marketingDataSite() =
        DataBrokerSite(
            name = "Acxiom",
            baseUrl = "https://www.acxiom.com",
            piiFields = listOf("name", "address", "phone", "email", "demographics"),
            category = DataBrokerCategory.MARKETING_DATA,
        )

    private fun dataAggregatorSite() =
        DataBrokerSite(
            name = "LexisNexis",
            baseUrl = "https://www.lexisnexis.com",
            piiFields = listOf("name", "address", "phone", "email", "court_records"),
            category = DataBrokerCategory.DATA_AGGREGATOR,
        )
}
