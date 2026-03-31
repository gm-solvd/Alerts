package com.privacyalert.api.controller

import com.ninjasquad.springmockk.MockkBean
import com.privacyalert.config.JwtAuthFilter
import com.privacyalert.config.SecurityConfig
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.service.AlertService
import com.privacyalert.domain.service.JwtProvider
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import java.time.Instant
import java.util.UUID

@WebMvcTest(AlertController::class)
@Import(SecurityConfig::class, JwtAuthFilter::class)
class AlertControllerTest(
    @Autowired val mockMvc: MockMvc,
) {
    @MockkBean
    lateinit var alertService: AlertService

    @MockkBean
    lateinit var jwtProvider: JwtProvider

    private val userId = UUID.randomUUID()
    private val alertId = UUID.randomUUID()

    private val testAlert =
        Alert(
            id = alertId,
            userId = userId,
            category = ThreatCategory.DATA_BREACH,
            severity = Severity.CRITICAL,
            title = "Data breach: LinkedIn",
            description = "Your email was found in the LinkedIn breach",
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )

    private fun authenticateAs(userId: UUID) {
        every { jwtProvider.validateAndExtractUserId("test-token") } returns userId
    }

    @Test
    fun `GET alerts returns 200 with paginated alert list`() {
        authenticateAs(userId)
        val page = PageImpl(listOf(testAlert), PageRequest.of(0, 20), 1)
        every { alertService.findAll(userId, null, null, any()) } returns page

        mockMvc
            .get("/api/v1/alerts") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.content[0].id") { value(alertId.toString()) }
                jsonPath("$.content[0].severity") { value("CRITICAL") }
                jsonPath("$.totalElements") { value(1) }
            }
    }

    @Test
    fun `GET alerts filters by category and severity`() {
        authenticateAs(userId)
        val page = PageImpl(listOf(testAlert), PageRequest.of(0, 20), 1)
        every { alertService.findAll(userId, ThreatCategory.DATA_BREACH, Severity.CRITICAL, any()) } returns page

        mockMvc
            .get("/api/v1/alerts?category=DATA_BREACH&severity=CRITICAL") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.content[0].category") { value("DATA_BREACH") }
            }
    }

    @Test
    fun `GET alerts returns 403 without authentication`() {
        every { jwtProvider.validateAndExtractUserId(any()) } returns null

        mockMvc
            .get("/api/v1/alerts")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `GET alerts by id returns 200 with alert`() {
        authenticateAs(userId)
        every { alertService.findById(alertId, userId) } returns testAlert

        mockMvc
            .get("/api/v1/alerts/$alertId") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.id") { value(alertId.toString()) }
                jsonPath("$.title") { value("Data breach: LinkedIn") }
            }
    }

    @Test
    fun `GET alerts by id returns 404 when alert not found`() {
        authenticateAs(userId)
        every { alertService.findById(alertId, userId) } throws
            AppException.ResourceNotFoundException("Alert", alertId)

        mockMvc
            .get("/api/v1/alerts/$alertId") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.code") { value("RESOURCE_NOT_FOUND") }
            }
    }

    @Test
    fun `PATCH alerts resolve returns 200 with resolved alert`() {
        authenticateAs(userId)
        val resolvedAlert = testAlert.copy(resolved = true, resolvedAt = Instant.now())
        every { alertService.resolve(alertId, userId) } returns resolvedAlert

        mockMvc
            .patch("/api/v1/alerts/$alertId/resolve") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.resolved") { value(true) }
            }
    }

    @Test
    fun `DELETE alerts returns 204 on success`() {
        authenticateAs(userId)
        every { alertService.delete(alertId, userId) } just Runs

        mockMvc
            .delete("/api/v1/alerts/$alertId") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isNoContent() }
            }
    }
}
