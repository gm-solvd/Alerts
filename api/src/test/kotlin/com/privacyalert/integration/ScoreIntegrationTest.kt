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
class ScoreIntegrationTest : BaseIntegrationTest() {
    private lateinit var accessToken: String

    @BeforeEach
    fun setUp() {
        val tokens = loginUser("user@test.com", "password123")
        accessToken = tokens.accessToken
    }

    @Test
    fun `get score returns calculated score for user with alerts`() {
        val response = get("/api/v1/score", userHeaders(accessToken), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body: Map<String, Any> = objectMapper.readValue(response.body!!)
        val score = (body["score"] as Number).toInt()

        assertThat(score).isBetween(0, 100)
    }

    @Test
    fun `get score returns 100 for user with no unresolved alerts`() {
        val user2Tokens = loginUser("user2@test.com", "password123")

        val response = get("/api/v1/score", userHeaders(user2Tokens.accessToken), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body: Map<String, Any> = objectMapper.readValue(response.body!!)
        val score = (body["score"] as Number).toInt()

        assertThat(score).isEqualTo(100)
    }

    @Test
    fun `score history returns paginated records`() {
        val response = get("/api/v1/score/history", userHeaders(accessToken), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val body: Map<String, Any> = objectMapper.readValue(response.body!!)
        val content = body["content"] as List<*>

        assertThat(content).isNotEmpty()
    }

    @Test
    fun `score recalculates after alert resolution`() {
        val scoreBefore = get("/api/v1/score", userHeaders(accessToken), String::class.java)
        val bodyBefore: Map<String, Any> = objectMapper.readValue(scoreBefore.body!!)
        val firstScore = (bodyBefore["score"] as Number).toInt()

        patch(
            "/api/v1/alerts/aaaa1111-0000-0000-0000-000000000001/resolve",
            userHeaders(accessToken),
            String::class.java,
        )

        val scoreAfter = get("/api/v1/score", userHeaders(accessToken), String::class.java)
        val bodyAfter: Map<String, Any> = objectMapper.readValue(scoreAfter.body!!)
        val secondScore = (bodyAfter["score"] as Number).toInt()

        assertThat(secondScore).isGreaterThanOrEqualTo(firstScore)
    }

    @Test
    fun `get score returns 403 without authentication`() {
        val response =
            restTemplate.exchange(
                "/api/v1/score",
                HttpMethod.GET,
                HttpEntity<Void>(HttpHeaders()),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
}
