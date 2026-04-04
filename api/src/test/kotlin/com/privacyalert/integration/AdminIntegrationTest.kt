package com.privacyalert.integration

import com.privacyalert.api.dto.CreateUserRequest
import com.privacyalert.domain.service.BreachResult
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import java.util.UUID
import java.util.concurrent.TimeUnit

@Sql(
    scripts = [
        "/sql/cleanup.sql",
        "/sql/common-fixtures.sql",
        "/sql/alerts-fixtures.sql",
        "/sql/scan-fixtures.sql",
        "/sql/admin-fixtures.sql",
    ],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
@Sql(scripts = ["/sql/cleanup.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AdminIntegrationTest : BaseIntegrationTest() {
    private val user1Id = "11111111-1111-1111-1111-111111111111"

    @Test
    fun `create user returns 201`() {
        val request =
            CreateUserRequest(
                email = "newuser@test.com",
                fullName = "New User",
                phoneNumber = "+1234567890",
                homeAddress = "123 Main St",
            )
        val response = post("/api/v1/admin/users", request, adminHeaders(), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        val body = objectMapper.readTree(response.body)
        assertThat(body.get("id").asText()).isNotNull()
        assertThat(body.get("email").asText()).isEqualTo("newuser@test.com")
    }

    @Test
    fun `create user returns 409 for duplicate email`() {
        val request =
            CreateUserRequest(
                email = "user@test.com",
                fullName = "Duplicate User",
            )
        val response = post("/api/v1/admin/users", request, adminHeaders(), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `list users returns paginated results with scores`() {
        val response = get("/api/v1/admin/users", adminHeaders(), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        assertThat(body.get("totalElements").asLong()).isEqualTo(5)
        assertThat(body.get("content").isArray).isTrue()
    }

    @Test
    fun `get user detail returns user with alert count and recent alerts`() {
        val response = get("/api/v1/admin/users/$user1Id", adminHeaders(), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        assertThat(body.get("alertCount").asLong()).isEqualTo(6)
        assertThat(body.get("recentAlerts").isArray).isTrue()
        assertThat(body.get("recentAlerts").size()).isGreaterThan(0)
    }

    @Test
    fun `get user returns 404 for unknown id`() {
        val unknownId = UUID.randomUUID()
        val response = get("/api/v1/admin/users/$unknownId", adminHeaders(), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `delete user cascades all related data`() {
        val deleteResponse = delete("/api/v1/admin/users/$user1Id", adminHeaders())
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        val getResponse = get("/api/v1/admin/users/$user1Id", adminHeaders(), String::class.java)
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `get user alerts returns paginated list`() {
        val response = get("/api/v1/admin/users/$user1Id/alerts", adminHeaders(), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        assertThat(body.get("totalElements").asLong()).isEqualTo(6)
    }

    @Test
    fun `trigger scan returns 202 with job id`() {
        every { breachScanner.scanEmail(any()) } returns emptyList()
        every { breachScanner.scanPhone(any()) } returns emptyList()
        every { identityExposureScanner.scan(any(), any()) } returns emptyList()
        every { piiExposureScanner.scan(any()) } returns emptyList()
        every { socialFootprintScanner.scan(any(), any(), any()) } returns emptyList()

        val response = post("/api/v1/admin/users/$user1Id/scan", null, adminHeaders(), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        val body = objectMapper.readTree(response.body)
        assertThat(body.get("jobId").asText()).isNotNull()
    }

    @Test
    fun `poll scan job until completed`() {
        every { breachScanner.scanEmail(any()) } returns
            listOf(
                BreachResult("Test", "test.com", "2023-01-01", listOf("email")),
            )
        every { breachScanner.scanPhone(any()) } returns emptyList()
        every { identityExposureScanner.scan(any(), any()) } returns emptyList()
        every { piiExposureScanner.scan(any()) } returns emptyList()
        every { socialFootprintScanner.scan(any(), any(), any()) } returns emptyList()

        val scanResponse = post("/api/v1/admin/users/$user1Id/scan", null, adminHeaders(), String::class.java)
        val jobId = objectMapper.readTree(scanResponse.body).get("jobId").asText()

        await.atMost(15, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS).untilAsserted {
            val status = get("/api/v1/admin/users/$user1Id/scan-jobs/$jobId", adminHeaders(), String::class.java)
            val json = objectMapper.readTree(status.body)
            assertThat(json.get("status").asText()).isEqualTo("COMPLETED")
        }
    }

    @Test
    fun `failed scan records friendly error message`() {
        every { breachScanner.scanEmail(any()) } throws RuntimeException("connection failed")
        every { breachScanner.scanPhone(any()) } returns emptyList()
        every { identityExposureScanner.scan(any(), any()) } returns emptyList()
        every { piiExposureScanner.scan(any()) } returns emptyList()
        every { socialFootprintScanner.scan(any(), any(), any()) } returns emptyList()

        val scanResponse = post("/api/v1/admin/users/$user1Id/scan", null, adminHeaders(), String::class.java)
        val jobId = objectMapper.readTree(scanResponse.body).get("jobId").asText()

        await.atMost(15, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS).untilAsserted {
            val status = get("/api/v1/admin/users/$user1Id/scan-jobs/$jobId", adminHeaders(), String::class.java)
            val json = objectMapper.readTree(status.body)
            assertThat(json.get("status").asText()).isEqualTo("FAILED")
            assertThat(json.get("errorMessage").asText()).isEqualTo("Scan could not be completed. Please try again.")
        }
    }

    @Test
    fun `scan history returns results with structured findings`() {
        val response = get("/api/v1/admin/users/$user1Id/scans", adminHeaders(), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        val content = body.get("content")
        assertThat(content.isArray).isTrue()
        assertThat(content.size()).isGreaterThan(0)

        val firstItem = content[0]
        assertThat(firstItem.get("details").isArray).isTrue()
    }

    @Test
    fun `get stats returns system wide counts`() {
        val response = get("/api/v1/admin/stats", adminHeaders(), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body = objectMapper.readTree(response.body)
        assertThat(body.get("totalUsers").asLong()).isEqualTo(5)
        assertThat(body.get("totalAlerts").asLong()).isEqualTo(8)
    }

    @Test
    fun `admin endpoints return 403 with regular user jwt`() {
        val tokens = registerUser("regularuser@test.com", "password123")
        val response = get("/api/v1/admin/users", userHeaders(tokens.accessToken), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `admin endpoints return 401 with no token`() {
        val emptyHeaders =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val response = get("/api/v1/admin/users", emptyHeaders, String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}
