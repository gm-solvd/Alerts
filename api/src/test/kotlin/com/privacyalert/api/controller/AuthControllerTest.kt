package com.privacyalert.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.privacyalert.config.JwtAuthFilter
import com.privacyalert.config.SecurityConfig
import com.privacyalert.api.dto.LoginRequest
import com.privacyalert.api.dto.RefreshTokenRequest
import com.privacyalert.api.dto.RegisterRequest
import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.model.AuthTokens
import com.privacyalert.domain.service.AuthService
import com.privacyalert.domain.service.JwtProvider
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class, JwtAuthFilter::class)
class AuthControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    lateinit var authService: AuthService

    @MockkBean
    lateinit var jwtProvider: JwtProvider

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private val tokens = AuthTokens(accessToken = "access-token", refreshToken = "refresh-token")

    @Test
    fun `POST register returns 201 with tokens`() {
        every { authService.register("new@example.com", "password123") } returns tokens

        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RegisterRequest("new@example.com", "password123"))
        }.andExpect {
            status { isCreated() }
            jsonPath("$.accessToken") { value("access-token") }
            jsonPath("$.refreshToken") { value("refresh-token") }
        }
    }

    @Test
    fun `POST register returns 409 when email already exists`() {
        every { authService.register("existing@example.com", "password123") } throws
            AppException.ConflictException("Email 'existing@example.com' is already registered")

        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RegisterRequest("existing@example.com", "password123"))
        }.andExpect {
            status { isConflict() }
            jsonPath("$.code") { value("CONFLICT") }
        }
    }

    @Test
    fun `POST register returns 400 for invalid email`() {
        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("email" to "not-an-email", "password" to "password123"))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST register returns 400 for short password`() {
        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("email" to "user@example.com", "password" to "short"))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST login returns 200 with tokens`() {
        every { authService.login("test@example.com", "password123") } returns tokens

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(LoginRequest("test@example.com", "password123"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { value("access-token") }
        }
    }

    @Test
    fun `POST login returns 401 for invalid credentials`() {
        every { authService.login("test@example.com", "wrong") } throws
            AppException.UnauthorizedException("Invalid email or password")

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(LoginRequest("test@example.com", "wrong"))
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.code") { value("UNAUTHORIZED") }
        }
    }

    @Test
    fun `POST refresh returns 200 with new tokens`() {
        every { authService.refreshToken("valid-refresh") } returns tokens

        mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest("valid-refresh"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { value("access-token") }
        }
    }

    @Test
    fun `POST refresh returns 401 for invalid refresh token`() {
        every { authService.refreshToken("invalid") } throws
            AppException.UnauthorizedException("Invalid refresh token")

        mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshTokenRequest("invalid"))
        }.andExpect {
            status { isUnauthorized() }
        }
    }
}
