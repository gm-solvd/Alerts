package com.privacyalert.integration

import com.privacyalert.api.dto.LoginRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql

@Sql(
    scripts = ["/sql/cleanup.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
@Sql(
    scripts = ["/sql/cleanup.sql"],
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
)
class SecurityIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `unauthenticated requests to protected endpoints return 403`() {
        val headers = HttpHeaders()

        val alertsResponse =
            restTemplate.exchange(
                "/api/v1/alerts",
                HttpMethod.GET,
                HttpEntity<Void>(headers),
                String::class.java,
            )
        assertThat(alertsResponse.statusCode).isEqualTo(HttpStatus.FORBIDDEN)

        val scoreResponse =
            restTemplate.exchange(
                "/api/v1/score",
                HttpMethod.GET,
                HttpEntity<Void>(headers),
                String::class.java,
            )
        assertThat(scoreResponse.statusCode).isEqualTo(HttpStatus.FORBIDDEN)

        val scanResponse =
            restTemplate.exchange(
                "/api/v1/scan/breach",
                HttpMethod.POST,
                HttpEntity<Void>(headers),
                String::class.java,
            )
        assertThat(scanResponse.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `malformed jwt returns 403`() {
        val headers =
            HttpHeaders().apply {
                setBearerAuth("not-a-valid-jwt")
            }

        val response =
            restTemplate.exchange(
                "/api/v1/alerts",
                HttpMethod.GET,
                HttpEntity<Void>(headers),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `admin token grants access to admin endpoints`() {
        val response =
            restTemplate.exchange(
                "/api/v1/admin/stats",
                HttpMethod.GET,
                HttpEntity<Void>(adminHeaders()),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `regular user jwt cannot access admin endpoints`() {
        val tokens = registerUser("sectest@test.com", "password123")

        val response =
            restTemplate.exchange(
                "/api/v1/admin/users",
                HttpMethod.GET,
                HttpEntity<Void>(userHeaders(tokens.accessToken)),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `auth endpoints are public`() {
        val loginRequest = LoginRequest(email = "nonexistent@test.com", password = "wrongpassword")
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val response =
            restTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                HttpEntity(loginRequest, headers),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}
