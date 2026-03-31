package com.privacyalert.domain.service

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.ScoreRecord
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.repository.AlertRepository
import com.privacyalert.domain.repository.ScoreRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.UUID

class ScoreServiceTest {
    private val alertRepository = mockk<AlertRepository>()
    private val scoreRepository = mockk<ScoreRepository>()
    private val service = ScoreService(alertRepository, scoreRepository)

    private val userId = UUID.randomUUID()

    private fun alert(
        category: ThreatCategory = ThreatCategory.DATA_BREACH,
        severity: Severity = Severity.CRITICAL,
    ) = Alert(
        userId = userId,
        category = category,
        severity = severity,
        title = "Test",
        description = "Test",
    )

    @Test
    fun `getCurrent returns latest score when it exists`() {
        val record = ScoreRecord(userId = userId, score = 85)
        every { scoreRepository.findLatestByUserId(userId) } returns record

        val result = service.getCurrent(userId)

        assertEquals(85, result.score)
    }

    @Test
    fun `getCurrent recalculates when no score exists`() {
        every { scoreRepository.findLatestByUserId(userId) } returns null
        every { alertRepository.findAllUnresolvedByUserId(userId) } returns emptyList()
        every { scoreRepository.save(any()) } answers { firstArg() }

        val result = service.getCurrent(userId)

        assertEquals(100, result.score)
        verify { scoreRepository.save(any()) }
    }

    @Test
    fun `getHistory returns paginated score records`() {
        val pageable = PageRequest.of(0, 20)
        val records = listOf(ScoreRecord(userId = userId, score = 90))
        val page = PageImpl(records, pageable, 1)

        every { scoreRepository.findAllByUserId(userId, pageable) } returns page

        val result = service.getHistory(userId, pageable)

        assertEquals(1, result.content.size)
    }

    @Test
    fun `recalculate saves new score based on unresolved alerts`() {
        val alerts =
            listOf(
                alert(ThreatCategory.DATA_BREACH, Severity.CRITICAL),
                alert(ThreatCategory.DATA_BREACH, Severity.CRITICAL),
            )

        every { alertRepository.findAllUnresolvedByUserId(userId) } returns alerts
        every { scoreRepository.save(any()) } answers { firstArg() }

        val result = service.recalculate(userId)

        // 2 data breaches: 15 * 1.0 * (1 + ln(2)) = 15 * 1.693 = 25.4 → score ≈ 75
        assertEquals(75, result.score)
    }

    // --- Pure calculation tests ---

    @Test
    fun `calculate returns 100 when no unresolved alerts`() {
        assertEquals(100, ScoreService.calculate(emptyList()))
    }

    @Test
    fun `calculate deducts correctly for single data breach`() {
        val alerts = listOf(alert(ThreatCategory.DATA_BREACH, Severity.CRITICAL))
        // 15 * 1.0 * (1 + ln(1)) = 15 * 1.0 = 15 → score = 85
        assertEquals(85, ScoreService.calculate(alerts))
    }

    @Test
    fun `calculate applies diminishing returns for multiple alerts in same category`() {
        val alerts =
            listOf(
                alert(ThreatCategory.DATA_BREACH, Severity.CRITICAL),
                alert(ThreatCategory.DATA_BREACH, Severity.CRITICAL),
            )
        // 15 * 1.0 * (1 + ln(2)) = 15 * 1.693 = 25.4 → score = 75
        assertEquals(75, ScoreService.calculate(alerts))
    }

    @Test
    fun `calculate handles multiple categories correctly`() {
        val alerts =
            listOf(
                alert(ThreatCategory.DATA_BREACH, Severity.CRITICAL),
                alert(ThreatCategory.DATA_BREACH, Severity.CRITICAL),
                alert(ThreatCategory.NETWORK_VULNERABILITY, Severity.HIGH),
                alert(ThreatCategory.TRACKER_EXPOSURE, Severity.MEDIUM),
                alert(ThreatCategory.TRACKER_EXPOSURE, Severity.MEDIUM),
                alert(ThreatCategory.TRACKER_EXPOSURE, Severity.MEDIUM),
                alert(ThreatCategory.DEVICE_HYGIENE, Severity.MEDIUM),
            )
        // Data breach:   15 * 1.0 * (1 + ln(2)) = 25.4
        // Network vuln:  10 * 0.9 * (1 + ln(1)) = 9.0
        // Tracker:         5 * 0.6 * (1 + ln(3)) = 6.3
        // Device hygiene:  5 * 0.5 * (1 + ln(1)) = 2.5
        // Total = 43.2 → score ≈ 57
        assertEquals(57, ScoreService.calculate(alerts))
    }

    @Test
    fun `calculate floors score at 0`() {
        // Many critical alerts across multiple categories should drive score to 0
        val alerts =
            (1..10).map { alert(ThreatCategory.DATA_BREACH, Severity.CRITICAL) } +
                (1..10).map { alert(ThreatCategory.NETWORK_VULNERABILITY, Severity.HIGH) } +
                (1..10).map { alert(ThreatCategory.IDENTITY_EXPOSURE, Severity.HIGH) }
        val score = ScoreService.calculate(alerts)
        assertEquals(0, score)
    }

    @Test
    fun `calculate handles low severity alerts with small deductions`() {
        val alerts = listOf(alert(ThreatCategory.SOCIAL_FOOTPRINT, Severity.LOW))
        // 2 * 0.4 * (1 + ln(1)) = 0.8 → score = 99
        assertEquals(99, ScoreService.calculate(alerts))
    }
}
