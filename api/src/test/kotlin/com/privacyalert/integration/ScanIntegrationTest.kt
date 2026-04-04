package com.privacyalert.integration

import com.privacyalert.api.dto.ScanProfileRequest
import com.privacyalert.domain.service.BreachResult
import com.privacyalert.domain.service.IdentityExposureResult
import com.privacyalert.domain.service.PiiExposureResult
import com.privacyalert.domain.service.SocialFootprintResult
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql

@Sql(
    scripts = ["/sql/cleanup.sql", "/sql/common-fixtures.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
@Sql(
    scripts = ["/sql/cleanup.sql"],
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
)
class ScanIntegrationTest : BaseIntegrationTest() {
    private lateinit var accessToken: String

    @BeforeEach
    fun setUp() {
        val tokens = loginUser("user@test.com", "password123")
        accessToken = tokens.accessToken

        every { breachScanner.scanEmail(any()) } returns
            listOf(
                BreachResult("TestBreach", "test.com", "2023-01-01", listOf("email", "password")),
            )
        every { breachScanner.scanPhone(any()) } returns emptyList()
        every { identityExposureScanner.scan(any(), any()) } returns emptyList()
        every { piiExposureScanner.scan(any()) } returns emptyList()
        every { socialFootprintScanner.scan(any(), any(), any()) } returns emptyList()
    }

    @Test
    fun `breach scan creates alerts from scanner results`() {
        val request = ScanProfileRequest(email = "user@test.com")
        val response =
            post(
                "/api/v1/scan/breach",
                request,
                userHeaders(accessToken),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        assertThat(body.isArray).isTrue()
        assertThat(body.size()).isGreaterThanOrEqualTo(1)

        val alert = body[0]
        assertThat(alert.get("category").asText()).isEqualTo("DATA_BREACH")
        assertThat(alert.get("resolved").asBoolean()).isFalse()
    }

    @Test
    fun `breach scan persists scan result with jsonb findings`() {
        val request = ScanProfileRequest(email = "user@test.com")
        post(
            "/api/v1/scan/breach",
            request,
            userHeaders(accessToken),
            String::class.java,
        )

        val scansResponse =
            get(
                "/api/v1/admin/users/11111111-1111-1111-1111-111111111111/scans",
                adminHeaders(),
                String::class.java,
            )

        assertThat(scansResponse.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(scansResponse.body)
        val content = body.get("content")
        assertThat(content.isArray).isTrue()
        assertThat(content.size()).isGreaterThanOrEqualTo(1)

        val scanResult = content[0]
        val details = scanResult.get("details")
        assertThat(details.isArray).isTrue()
        assertThat(details.size()).isGreaterThan(0)
    }

    @Test
    fun `identity scan creates identity exposure alerts`() {
        every { identityExposureScanner.scan(any(), any()) } returns
            listOf(
                IdentityExposureResult("WhitePages", "https://wp.com", "Test", listOf("name")),
            )

        val request = ScanProfileRequest(email = "user@test.com")
        val response =
            post(
                "/api/v1/scan/identity",
                request,
                userHeaders(accessToken),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        assertThat(body.isArray).isTrue()
        assertThat(body.size()).isGreaterThanOrEqualTo(1)

        val hasIdentityExposure = body.any { it.get("category").asText() == "IDENTITY_EXPOSURE" }
        assertThat(hasIdentityExposure).isTrue()
    }

    @Test
    fun `pii scan creates tracker exposure alerts`() {
        every { piiExposureScanner.scan(any()) } returns
            listOf(
                PiiExposureResult("DataBroker", "https://db.com", listOf("email"), "snippet"),
            )

        val request = ScanProfileRequest(email = "user@test.com")
        val response =
            post(
                "/api/v1/scan/pii",
                request,
                userHeaders(accessToken),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        assertThat(body.isArray).isTrue()
        assertThat(body.size()).isGreaterThanOrEqualTo(1)

        val hasTrackerExposure = body.any { it.get("category").asText() == "TRACKER_EXPOSURE" }
        assertThat(hasTrackerExposure).isTrue()
    }

    @Test
    fun `social scan creates social footprint alerts`() {
        every { socialFootprintScanner.scan(any(), any(), any()) } returns
            listOf(
                SocialFootprintResult("github", "https://github.com/test", "test", listOf("repos")),
            )

        val request = ScanProfileRequest(email = "user@test.com", username = "test")
        val response =
            post(
                "/api/v1/scan/social",
                request,
                userHeaders(accessToken),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        assertThat(body.isArray).isTrue()
        assertThat(body.size()).isGreaterThanOrEqualTo(1)

        val hasSocialFootprint = body.any { it.get("category").asText() == "SOCIAL_FOOTPRINT" }
        assertThat(hasSocialFootprint).isTrue()
    }

    @Test
    fun `full scan aggregates all four scan types`() {
        every { breachScanner.scanEmail(any()) } returns
            listOf(
                BreachResult("TestBreach", "test.com", "2023-01-01", listOf("email", "password")),
            )
        every { identityExposureScanner.scan(any(), any()) } returns
            listOf(
                IdentityExposureResult("WhitePages", "https://wp.com", "Test", listOf("name")),
            )
        every { piiExposureScanner.scan(any()) } returns
            listOf(
                PiiExposureResult("DataBroker", "https://db.com", listOf("email"), "snippet"),
            )
        every { socialFootprintScanner.scan(any(), any(), any()) } returns
            listOf(
                SocialFootprintResult("github", "https://github.com/test", "test", listOf("repos")),
            )

        val request = ScanProfileRequest(email = "user@test.com", username = "test")
        val response =
            post(
                "/api/v1/scan/full",
                request,
                userHeaders(accessToken),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        assertThat(body.isArray).isTrue()
        assertThat(body.size()).isGreaterThanOrEqualTo(4)
    }

    @Test
    fun `scan recalculates score after creating alerts`() {
        val scoreBefore =
            get(
                "/api/v1/score",
                userHeaders(accessToken),
                String::class.java,
            )
        assertThat(scoreBefore.statusCode).isEqualTo(HttpStatus.OK)
        val scoreValueBefore = objectMapper.readTree(scoreBefore.body).get("score").asInt()
        assertThat(scoreValueBefore).isEqualTo(100)

        val request = ScanProfileRequest(email = "user@test.com")
        post(
            "/api/v1/scan/breach",
            request,
            userHeaders(accessToken),
            String::class.java,
        )

        val scoreAfter =
            get(
                "/api/v1/score",
                userHeaders(accessToken),
                String::class.java,
            )
        assertThat(scoreAfter.statusCode).isEqualTo(HttpStatus.OK)
        val scoreValueAfter = objectMapper.readTree(scoreAfter.body).get("score").asInt()
        assertThat(scoreValueAfter).isLessThan(100)
    }

    @Test
    fun `breach scan returns 401 without auth`() {
        val request = ScanProfileRequest(email = "user@test.com")
        val response =
            post(
                "/api/v1/scan/breach",
                request,
                org.springframework.http.HttpHeaders().apply {
                    contentType = org.springframework.http.MediaType.APPLICATION_JSON
                },
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}
