package com.privacyalert.api.controller

import com.ninjasquad.springmockk.MockkBean
import com.privacyalert.config.JwtAuthFilter
import com.privacyalert.config.SecurityConfig
import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.model.Mitigation
import com.privacyalert.domain.service.JwtProvider
import com.privacyalert.domain.service.MitigationService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import java.time.Instant
import java.util.UUID

@WebMvcTest(MitigationController::class)
@Import(SecurityConfig::class, JwtAuthFilter::class)
class MitigationControllerTest(
    @Autowired val mockMvc: MockMvc,
) {
    @MockkBean
    lateinit var mitigationService: MitigationService

    @MockkBean
    lateinit var jwtProvider: JwtProvider

    private val userId = UUID.randomUUID()
    private val alertId = UUID.randomUUID()
    private val mitigationId = UUID.randomUUID()

    private val testMitigation =
        Mitigation(
            id = mitigationId,
            alertId = alertId,
            title = "Change password",
            description = "Change your password immediately",
            actionUrl = "https://example.com/reset",
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )

    private fun authenticateAs(userId: UUID) {
        every { jwtProvider.validateAndExtractUserId("test-token") } returns userId
    }

    @Test
    fun `GET mitigations returns 200 with list`() {
        authenticateAs(userId)
        every { mitigationService.findAllByUserId(userId) } returns listOf(testMitigation)

        mockMvc
            .get("/api/v1/mitigations") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isOk() }
                jsonPath("$[0].title") { value("Change password") }
            }
    }

    @Test
    fun `GET mitigations by alertId returns 200 with list`() {
        authenticateAs(userId)
        every { mitigationService.findByAlertId(alertId) } returns listOf(testMitigation)

        mockMvc
            .get("/api/v1/mitigations/$alertId") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isOk() }
                jsonPath("$[0].alertId") { value(alertId.toString()) }
            }
    }

    @Test
    fun `PATCH mitigations complete returns 200 with completed mitigation`() {
        authenticateAs(userId)
        val completed = testMitigation.copy(completed = true, completedAt = Instant.now())
        every { mitigationService.complete(mitigationId) } returns completed

        mockMvc
            .patch("/api/v1/mitigations/$mitigationId/complete") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.completed") { value(true) }
            }
    }

    @Test
    fun `PATCH mitigations complete returns 404 when not found`() {
        authenticateAs(userId)
        every { mitigationService.complete(mitigationId) } throws
            AppException.ResourceNotFoundException("Mitigation", mitigationId)

        mockMvc
            .patch("/api/v1/mitigations/$mitigationId/complete") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.code") { value("RESOURCE_NOT_FOUND") }
            }
    }

    @Test
    fun `GET mitigations returns 403 without authentication`() {
        every { jwtProvider.validateAndExtractUserId(any()) } returns null

        mockMvc
            .get("/api/v1/mitigations")
            .andExpect {
                status { isForbidden() }
            }
    }
}
