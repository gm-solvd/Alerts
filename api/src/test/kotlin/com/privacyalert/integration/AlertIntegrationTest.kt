package com.privacyalert.integration

import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql

@Sql(
    scripts = ["/sql/cleanup.sql", "/sql/common-fixtures.sql", "/sql/alerts-fixtures.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
@Sql(
    scripts = ["/sql/cleanup.sql"],
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
)
class AlertIntegrationTest : BaseIntegrationTest() {
    private lateinit var accessToken: String

    @BeforeEach
    fun setUp() {
        val tokens = loginUser("user@test.com", "password123")
        accessToken = tokens.accessToken
    }

    @Test
    fun `list alerts returns paginated results for authenticated user`() {
        val response = get("/api/v1/alerts", userHeaders(accessToken), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body: Map<String, Any> = objectMapper.readValue(response.body!!)
        val content = body["content"] as List<*>
        val totalElements = (body["totalElements"] as Number).toLong()

        assertThat(content).hasSize(6)
        assertThat(totalElements).isEqualTo(6)
    }

    @Test
    fun `list alerts filters by category`() {
        val response = get("/api/v1/alerts?category=DATA_BREACH", userHeaders(accessToken), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body: Map<String, Any> = objectMapper.readValue(response.body!!)
        val content = body["content"] as List<*>

        assertThat(content).hasSize(2)
    }

    @Test
    fun `list alerts filters by severity`() {
        val response = get("/api/v1/alerts?severity=CRITICAL", userHeaders(accessToken), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body: Map<String, Any> = objectMapper.readValue(response.body!!)
        val content = body["content"] as List<*>

        assertThat(content).hasSize(1)
    }

    @Test
    fun `list alerts returns empty for user with no alerts`() {
        val user2Tokens = loginUser("user2@test.com", "password123")

        val response = get("/api/v1/alerts", userHeaders(user2Tokens.accessToken), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body: Map<String, Any> = objectMapper.readValue(response.body!!)
        val content = body["content"] as List<*>

        assertThat(content).isEmpty()
    }

    @Test
    fun `get alert by id returns correct fields`() {
        val response =
            get(
                "/api/v1/alerts/aaaa1111-0000-0000-0000-000000000001",
                userHeaders(accessToken),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body: Map<String, Any> = objectMapper.readValue(response.body!!)

        assertThat(body["category"]).isEqualTo("DATA_BREACH")
        assertThat(body["severity"]).isEqualTo("CRITICAL")
    }

    @Test
    fun `get alert returns 404 for another users alert`() {
        val user2Tokens = loginUser("user2@test.com", "password123")

        val response =
            get(
                "/api/v1/alerts/aaaa1111-0000-0000-0000-000000000001",
                userHeaders(user2Tokens.accessToken),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `resolve alert marks it resolved and updates timestamp`() {
        val response =
            patch(
                "/api/v1/alerts/aaaa1111-0000-0000-0000-000000000001/resolve",
                userHeaders(accessToken),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body: Map<String, Any> = objectMapper.readValue(response.body!!)

        assertThat(body["resolved"]).isEqualTo(true)
        assertThat(body["resolvedAt"]).isNotNull()
    }

    @Test
    fun `delete alert returns 204`() {
        val response =
            delete(
                "/api/v1/alerts/aaaa1111-0000-0000-0000-000000000004",
                userHeaders(accessToken),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    }

    @Test
    fun `delete alert with mitigations cascades`() {
        val deleteResponse =
            delete(
                "/api/v1/alerts/aaaa1111-0000-0000-0000-000000000001",
                userHeaders(accessToken),
            )

        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        val mitigationsResponse =
            get(
                "/api/v1/mitigations/aaaa1111-0000-0000-0000-000000000001",
                userHeaders(accessToken),
                String::class.java,
            )

        val mitigations: List<Any> = objectMapper.readValue(mitigationsResponse.body!!)
        assertThat(mitigations).isEmpty()
    }

    @Test
    fun `list alerts returns 401 without authentication`() {
        val response =
            restTemplate.exchange(
                "/api/v1/alerts",
                HttpMethod.GET,
                HttpEntity<Void>(HttpHeaders()),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}
