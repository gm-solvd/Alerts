package com.privacyalert.api.controller

import com.ninjasquad.springmockk.MockkBean
import com.privacyalert.config.JwtAuthFilter
import com.privacyalert.config.SecurityConfig
import com.privacyalert.domain.model.ScoreRecord
import com.privacyalert.domain.service.JwtProvider
import com.privacyalert.domain.service.ScoreService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant
import java.util.UUID

@WebMvcTest(ScoreController::class)
@Import(SecurityConfig::class, JwtAuthFilter::class)
class ScoreControllerTest(
    @Autowired val mockMvc: MockMvc,
) {
    @MockkBean
    lateinit var scoreService: ScoreService

    @MockkBean
    lateinit var jwtProvider: JwtProvider

    private val userId = UUID.randomUUID()

    private fun authenticateAs(userId: UUID) {
        every { jwtProvider.validateAndExtractUserId("test-token") } returns userId
    }

    @Test
    fun `GET score returns 200 with current score`() {
        authenticateAs(userId)
        val record = ScoreRecord(userId = userId, score = 85, recordedAt = Instant.parse("2026-01-01T00:00:00Z"))
        every { scoreService.getCurrent(userId) } returns record

        mockMvc
            .get("/api/v1/score") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.score") { value(85) }
            }
    }

    @Test
    fun `GET score history returns 200 with paginated scores`() {
        authenticateAs(userId)
        val records =
            listOf(
                ScoreRecord(userId = userId, score = 85, recordedAt = Instant.parse("2026-01-02T00:00:00Z")),
                ScoreRecord(userId = userId, score = 90, recordedAt = Instant.parse("2026-01-01T00:00:00Z")),
            )
        val page = PageImpl(records, PageRequest.of(0, 20), 2)
        every { scoreService.getHistory(userId, any()) } returns page

        mockMvc
            .get("/api/v1/score/history") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.content[0].score") { value(85) }
                jsonPath("$.totalElements") { value(2) }
            }
    }

    @Test
    fun `GET score returns 401 without authentication`() {
        every { jwtProvider.validateAndExtractUserId(any()) } returns null

        mockMvc
            .get("/api/v1/score")
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
