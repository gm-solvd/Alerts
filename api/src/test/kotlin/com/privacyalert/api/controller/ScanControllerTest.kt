package com.privacyalert.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.privacyalert.api.dto.ScanProfileRequest
import com.privacyalert.config.JwtAuthFilter
import com.privacyalert.config.SecurityConfig
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.model.UserScanProfile
import com.privacyalert.domain.service.JwtProvider
import com.privacyalert.domain.service.ScanService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.UUID

@WebMvcTest(ScanController::class)
@Import(SecurityConfig::class, JwtAuthFilter::class)
class ScanControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    lateinit var scanService: ScanService

    @MockkBean
    lateinit var jwtProvider: JwtProvider

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private val userId = UUID.randomUUID()

    private fun authenticateAs(userId: UUID) {
        every { jwtProvider.validateAndExtractUserId("test-token") } returns userId
    }

    // ── breach scan ─────────────────────────────────────────────────────

    @Test
    fun `POST breach scan returns 200 with alerts`() {
        authenticateAs(userId)
        val alerts = listOf(
            Alert(
                userId = userId,
                category = ThreatCategory.DATA_BREACH,
                severity = Severity.CRITICAL,
                title = "Data breach: LinkedIn",
                description = "Found in LinkedIn breach",
            ),
        )
        every { scanService.breachScan(userId, any<UserScanProfile>()) } returns alerts

        mockMvc.post("/api/v1/scan/breach") {
            header("Authorization", "Bearer test-token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ScanProfileRequest("user@example.com"))
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].title") { value("Data breach: LinkedIn") }
        }
    }

    @Test
    fun `POST breach scan returns 200 with empty list when no breaches found`() {
        authenticateAs(userId)
        every { scanService.breachScan(userId, any<UserScanProfile>()) } returns emptyList()

        mockMvc.post("/api/v1/scan/breach") {
            header("Authorization", "Bearer test-token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ScanProfileRequest("clean@example.com"))
        }.andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
            jsonPath("$.length()") { value(0) }
        }
    }

    @Test
    fun `POST breach scan returns 403 without authentication`() {
        every { jwtProvider.validateAndExtractUserId(any()) } returns null

        mockMvc.post("/api/v1/scan/breach") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ScanProfileRequest("user@example.com"))
        }.andExpect {
            status { isForbidden() }
        }
    }

    // ── identity scan ───────────────────────────────────────────────────

    @Test
    fun `POST identity scan returns 200 with alerts`() {
        authenticateAs(userId)
        val alerts = listOf(
            Alert(
                userId = userId,
                category = ThreatCategory.IDENTITY_EXPOSURE,
                severity = Severity.HIGH,
                title = "Identity exposed on Spokeo",
                description = "Your profile was found on Spokeo",
            ),
        )
        every { scanService.identityScan(userId, any<UserScanProfile>()) } returns alerts

        mockMvc.post("/api/v1/scan/identity") {
            header("Authorization", "Bearer test-token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ScanProfileRequest("user@example.com", fullName = "John Doe"))
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].title") { value("Identity exposed on Spokeo") }
            jsonPath("$[0].category") { value("IDENTITY_EXPOSURE") }
        }
    }

    @Test
    fun `POST identity scan returns 200 with empty list when no results found`() {
        authenticateAs(userId)
        every { scanService.identityScan(userId, any<UserScanProfile>()) } returns emptyList()

        mockMvc.post("/api/v1/scan/identity") {
            header("Authorization", "Bearer test-token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ScanProfileRequest("user@example.com"))
        }.andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
            jsonPath("$.length()") { value(0) }
        }
    }

    // ── pii scan ────────────────────────────────────────────────────────

    @Test
    fun `POST pii scan returns 200 with alerts`() {
        authenticateAs(userId)
        val alerts = listOf(
            Alert(
                userId = userId,
                category = ThreatCategory.TRACKER_EXPOSURE,
                severity = Severity.HIGH,
                title = "PII found on DataBroker",
                description = "Your personal information was found",
            ),
        )
        every { scanService.piiExposureScan(userId, any<UserScanProfile>()) } returns alerts

        mockMvc.post("/api/v1/scan/pii") {
            header("Authorization", "Bearer test-token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                ScanProfileRequest(
                    email = "user@example.com",
                    phoneNumber = "+1234567890",
                    fullName = "John Doe",
                    homeAddress = "123 Main St",
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].title") { value("PII found on DataBroker") }
            jsonPath("$[0].category") { value("TRACKER_EXPOSURE") }
        }
    }

    @Test
    fun `POST pii scan returns 200 with empty list when no results found`() {
        authenticateAs(userId)
        every { scanService.piiExposureScan(userId, any<UserScanProfile>()) } returns emptyList()

        mockMvc.post("/api/v1/scan/pii") {
            header("Authorization", "Bearer test-token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ScanProfileRequest("user@example.com"))
        }.andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
            jsonPath("$.length()") { value(0) }
        }
    }

    @Test
    fun `POST pii scan returns 403 without authentication`() {
        every { jwtProvider.validateAndExtractUserId(any()) } returns null

        mockMvc.post("/api/v1/scan/pii") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ScanProfileRequest("user@example.com"))
        }.andExpect {
            status { isForbidden() }
        }
    }

    // ── social footprint scan ───────────────────────────────────────────

    @Test
    fun `POST social scan returns 200 with alerts`() {
        authenticateAs(userId)
        val alerts = listOf(
            Alert(
                userId = userId,
                category = ThreatCategory.SOCIAL_FOOTPRINT,
                severity = Severity.MEDIUM,
                title = "Profile found on Twitter",
                description = "Your profile was found on Twitter",
            ),
        )
        every { scanService.socialFootprintScan(userId, any<UserScanProfile>(), "johndoe") } returns alerts

        mockMvc.post("/api/v1/scan/social") {
            header("Authorization", "Bearer test-token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                ScanProfileRequest(email = "user@example.com", fullName = "John Doe", username = "johndoe"),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].title") { value("Profile found on Twitter") }
            jsonPath("$[0].category") { value("SOCIAL_FOOTPRINT") }
        }
    }

    @Test
    fun `POST social scan returns 200 with null username`() {
        authenticateAs(userId)
        every { scanService.socialFootprintScan(userId, any<UserScanProfile>(), null) } returns emptyList()

        mockMvc.post("/api/v1/scan/social") {
            header("Authorization", "Bearer test-token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ScanProfileRequest("user@example.com"))
        }.andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
            jsonPath("$.length()") { value(0) }
        }
    }

    @Test
    fun `POST social scan returns 403 without authentication`() {
        every { jwtProvider.validateAndExtractUserId(any()) } returns null

        mockMvc.post("/api/v1/scan/social") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ScanProfileRequest("user@example.com"))
        }.andExpect {
            status { isForbidden() }
        }
    }

    // ── full scan ───────────────────────────────────────────────────────

    @Test
    fun `POST full scan returns 200 with aggregated alerts`() {
        authenticateAs(userId)
        val alerts = listOf(
            Alert(userId = userId, category = ThreatCategory.DATA_BREACH, severity = Severity.CRITICAL, title = "Data breach: LinkedIn", description = "Breach"),
            Alert(userId = userId, category = ThreatCategory.IDENTITY_EXPOSURE, severity = Severity.HIGH, title = "Identity exposed on Spokeo", description = "Identity"),
            Alert(userId = userId, category = ThreatCategory.TRACKER_EXPOSURE, severity = Severity.HIGH, title = "PII found on DataBroker", description = "PII"),
            Alert(userId = userId, category = ThreatCategory.SOCIAL_FOOTPRINT, severity = Severity.MEDIUM, title = "Profile found on Twitter", description = "Social"),
        )
        every { scanService.fullScan(userId, any<UserScanProfile>(), "johndoe") } returns alerts

        mockMvc.post("/api/v1/scan/full") {
            header("Authorization", "Bearer test-token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                ScanProfileRequest(
                    email = "user@example.com",
                    phoneNumber = "+1234567890",
                    fullName = "John Doe",
                    homeAddress = "123 Main St",
                    username = "johndoe",
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(4) }
            jsonPath("$[0].title") { value("Data breach: LinkedIn") }
            jsonPath("$[3].title") { value("Profile found on Twitter") }
        }
    }

    @Test
    fun `POST full scan returns 200 with empty list when nothing found`() {
        authenticateAs(userId)
        every { scanService.fullScan(userId, any<UserScanProfile>(), null) } returns emptyList()

        mockMvc.post("/api/v1/scan/full") {
            header("Authorization", "Bearer test-token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ScanProfileRequest("clean@example.com"))
        }.andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
            jsonPath("$.length()") { value(0) }
        }
    }

    @Test
    fun `POST full scan returns 403 without authentication`() {
        every { jwtProvider.validateAndExtractUserId(any()) } returns null

        mockMvc.post("/api/v1/scan/full") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ScanProfileRequest("user@example.com"))
        }.andExpect {
            status { isForbidden() }
        }
    }

    // ── validation ──────────────────────────────────────────────────────

    @Test
    fun `POST scan rejects invalid email`() {
        authenticateAs(userId)

        mockMvc.post("/api/v1/scan/breach") {
            header("Authorization", "Bearer test-token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ScanProfileRequest("not-an-email"))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST scan rejects blank email`() {
        authenticateAs(userId)

        mockMvc.post("/api/v1/scan/breach") {
            header("Authorization", "Bearer test-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"email": ""}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
