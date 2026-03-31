package com.privacyalert.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.privacyalert.api.dto.PermissionAuditRequest
import com.privacyalert.api.dto.PermissionEntryDto
import com.privacyalert.config.JwtAuthFilter
import com.privacyalert.config.SecurityConfig
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.service.JwtProvider
import com.privacyalert.domain.service.PermissionAuditService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.UUID

@WebMvcTest(PermissionAuditController::class)
@Import(SecurityConfig::class, JwtAuthFilter::class)
class PermissionAuditControllerTest(
    @Autowired val mockMvc: MockMvc,
) {
    @MockkBean
    lateinit var permissionAuditService: PermissionAuditService

    @MockkBean
    lateinit var jwtProvider: JwtProvider

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private val userId = UUID.randomUUID()

    private fun authenticateAs(userId: UUID) {
        every { jwtProvider.validateAndExtractUserId("test-token") } returns userId
    }

    @Test
    fun `POST audit permissions returns 200 with alerts`() {
        authenticateAs(userId)
        val alert =
            Alert(
                userId = userId,
                category = ThreatCategory.APP_OVERPERMISSIONS,
                severity = Severity.MEDIUM,
                title = "Risky permission granted: CAMERA",
                description = "The permission 'android.permission.CAMERA' is granted.",
            )
        every { permissionAuditService.submitAudit(userId, any()) } returns listOf(alert)

        val request =
            PermissionAuditRequest(
                permissions =
                    listOf(
                        PermissionEntryDto("android.permission.CAMERA", true),
                    ),
            )

        mockMvc
            .post("/api/v1/audit/permissions") {
                header("Authorization", "Bearer test-token")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$[0].title") { value("Risky permission granted: CAMERA") }
            }
    }

    @Test
    fun `POST audit permissions returns 200 with empty list for safe permissions`() {
        authenticateAs(userId)
        every { permissionAuditService.submitAudit(userId, any()) } returns emptyList()

        val request =
            PermissionAuditRequest(
                permissions =
                    listOf(
                        PermissionEntryDto("android.permission.INTERNET", true),
                    ),
            )

        mockMvc
            .post("/api/v1/audit/permissions") {
                header("Authorization", "Bearer test-token")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(0) }
            }
    }

    @Test
    fun `POST audit permissions returns 403 without authentication`() {
        every { jwtProvider.validateAndExtractUserId(any()) } returns null

        mockMvc
            .post("/api/v1/audit/permissions") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    objectMapper.writeValueAsString(
                        PermissionAuditRequest(
                            permissions = listOf(PermissionEntryDto("android.permission.CAMERA", true)),
                        ),
                    )
            }.andExpect {
                status { isForbidden() }
            }
    }
}
