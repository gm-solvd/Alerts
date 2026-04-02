package com.privacyalert.integration

import com.privacyalert.api.dto.AuthTokensResponse
import com.privacyalert.api.dto.LoginRequest
import com.privacyalert.api.dto.RefreshTokenRequest
import com.privacyalert.api.dto.RegisterRequest
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
class AuthIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `register returns 201 with access and refresh tokens`() {
        val request = RegisterRequest(email = "newuser@example.com", password = "securePass123")
        val response =
            restTemplate.postForEntity(
                "/api/v1/auth/register",
                request,
                AuthTokensResponse::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.accessToken).isNotBlank()
        assertThat(response.body!!.refreshToken).isNotBlank()
    }

    @Test
    fun `register persists user and allows login`() {
        val email = "persist@example.com"
        val password = "securePass123"

        val registerRequest = RegisterRequest(email = email, password = password)
        val registerResponse =
            restTemplate.postForEntity(
                "/api/v1/auth/register",
                registerRequest,
                AuthTokensResponse::class.java,
            )
        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.CREATED)

        val loginRequest = LoginRequest(email = email, password = password)
        val loginResponse =
            restTemplate.postForEntity(
                "/api/v1/auth/login",
                loginRequest,
                AuthTokensResponse::class.java,
            )
        assertThat(loginResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(loginResponse.body).isNotNull
        assertThat(loginResponse.body!!.accessToken).isNotBlank()
        assertThat(loginResponse.body!!.refreshToken).isNotBlank()
    }

    @Test
    fun `register returns 409 for duplicate email`() {
        val request = RegisterRequest(email = "duplicate@example.com", password = "securePass123")
        restTemplate.postForEntity(
            "/api/v1/auth/register",
            request,
            AuthTokensResponse::class.java,
        )

        val duplicateResponse =
            restTemplate.postForEntity(
                "/api/v1/auth/register",
                request,
                String::class.java,
            )
        assertThat(duplicateResponse.statusCode).isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `register returns 400 for invalid email format`() {
        val request = RegisterRequest(email = "not-an-email", password = "securePass123")
        val response =
            restTemplate.postForEntity(
                "/api/v1/auth/register",
                request,
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `register returns 400 for password shorter than 8 chars`() {
        val request = RegisterRequest(email = "short@example.com", password = "short")
        val response =
            restTemplate.postForEntity(
                "/api/v1/auth/register",
                request,
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `login returns 200 with tokens for valid credentials`() {
        val email = "login@example.com"
        val password = "securePass123"

        val registerRequest = RegisterRequest(email = email, password = password)
        restTemplate.postForEntity(
            "/api/v1/auth/register",
            registerRequest,
            AuthTokensResponse::class.java,
        )

        val loginRequest = LoginRequest(email = email, password = password)
        val loginResponse =
            restTemplate.postForEntity(
                "/api/v1/auth/login",
                loginRequest,
                AuthTokensResponse::class.java,
            )

        assertThat(loginResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(loginResponse.body).isNotNull
        assertThat(loginResponse.body!!.accessToken).isNotBlank()
        assertThat(loginResponse.body!!.refreshToken).isNotBlank()
    }

    @Test
    fun `login returns 401 for wrong password`() {
        val email = "wrongpass@example.com"
        val password = "securePass123"

        val registerRequest = RegisterRequest(email = email, password = password)
        restTemplate.postForEntity(
            "/api/v1/auth/register",
            registerRequest,
            AuthTokensResponse::class.java,
        )

        val loginRequest = LoginRequest(email = email, password = "wrongPassword99")
        val loginResponse =
            restTemplate.postForEntity(
                "/api/v1/auth/login",
                loginRequest,
                String::class.java,
            )

        assertThat(loginResponse.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `login returns 401 for nonexistent email`() {
        val loginRequest = LoginRequest(email = "noone@example.com", password = "securePass123")
        val loginResponse =
            restTemplate.postForEntity(
                "/api/v1/auth/login",
                loginRequest,
                String::class.java,
            )

        assertThat(loginResponse.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `refresh returns 200 with new token pair`() {
        val registerRequest = RegisterRequest(email = "refresh@example.com", password = "securePass123")
        val registerResponse =
            restTemplate.postForEntity(
                "/api/v1/auth/register",
                registerRequest,
                AuthTokensResponse::class.java,
            )
        val refreshToken = registerResponse.body!!.refreshToken

        val refreshRequest = RefreshTokenRequest(refreshToken = refreshToken)
        val refreshResponse =
            restTemplate.postForEntity(
                "/api/v1/auth/refresh",
                refreshRequest,
                AuthTokensResponse::class.java,
            )

        assertThat(refreshResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(refreshResponse.body).isNotNull
        assertThat(refreshResponse.body!!.accessToken).isNotBlank()
        assertThat(refreshResponse.body!!.refreshToken).isNotBlank()
    }

    @Test
    fun `refresh returns 401 for invalid refresh token`() {
        val refreshRequest = RefreshTokenRequest(refreshToken = "invalid-token-value")
        val refreshResponse =
            restTemplate.postForEntity(
                "/api/v1/auth/refresh",
                refreshRequest,
                String::class.java,
            )

        assertThat(refreshResponse.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `full auth flow register then access protected endpoint`() {
        val registerRequest = RegisterRequest(email = "fullflow@example.com", password = "securePass123")
        val registerResponse =
            restTemplate.postForEntity(
                "/api/v1/auth/register",
                registerRequest,
                AuthTokensResponse::class.java,
            )

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.CREATED)
        val accessToken = registerResponse.body!!.accessToken

        val headers =
            HttpHeaders().apply {
                setBearerAuth(accessToken)
                contentType = MediaType.APPLICATION_JSON
            }
        val scoreResponse =
            restTemplate.exchange(
                "/api/v1/score",
                HttpMethod.GET,
                HttpEntity<Void>(headers),
                String::class.java,
            )

        assertThat(scoreResponse.statusCode).isEqualTo(HttpStatus.OK)
    }
}
