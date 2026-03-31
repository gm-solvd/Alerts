package com.privacyalert.domain.service

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.repository.AlertRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.UUID

class AlertServiceTest {

    private val alertRepository = mockk<AlertRepository>()
    private val scoreService = mockk<ScoreService>()
    private val service = AlertService(alertRepository, scoreService)

    private val userId = UUID.randomUUID()

    private fun alert(
        id: UUID = UUID.randomUUID(),
        resolved: Boolean = false,
    ) = Alert(
        id = id,
        userId = userId,
        category = ThreatCategory.DATA_BREACH,
        severity = Severity.CRITICAL,
        title = "Test alert",
        description = "Test description",
        resolved = resolved,
    )

    @Test
    fun `findAll returns paginated alerts for user`() {
        val pageable = PageRequest.of(0, 20)
        val alerts = listOf(alert(), alert())
        val page = PageImpl(alerts, pageable, alerts.size.toLong())

        every { alertRepository.findAllByUserIdAndCategoryAndSeverity(userId, null, null, pageable) } returns page

        val result = service.findAll(userId, null, null, pageable)

        assertEquals(2, result.content.size)
    }

    @Test
    fun `findAll filters by category and severity`() {
        val pageable = PageRequest.of(0, 20)
        val page = PageImpl(listOf(alert()), pageable, 1)

        every {
            alertRepository.findAllByUserIdAndCategoryAndSeverity(
                userId,
                ThreatCategory.DATA_BREACH,
                Severity.CRITICAL,
                pageable,
            )
        } returns page

        val result = service.findAll(userId, ThreatCategory.DATA_BREACH, Severity.CRITICAL, pageable)

        assertEquals(1, result.content.size)
    }

    @Test
    fun `findById returns alert when it belongs to user`() {
        val alertId = UUID.randomUUID()
        val expected = alert(id = alertId)

        every { alertRepository.findById(alertId) } returns expected

        val result = service.findById(alertId, userId)

        assertEquals(expected, result)
    }

    @Test
    fun `findById throws ResourceNotFoundException when alert does not exist`() {
        val alertId = UUID.randomUUID()

        every { alertRepository.findById(alertId) } returns null

        assertThrows<AppException.ResourceNotFoundException> {
            service.findById(alertId, userId)
        }
    }

    @Test
    fun `findById throws ResourceNotFoundException when alert belongs to different user`() {
        val alertId = UUID.randomUUID()
        val otherUserId = UUID.randomUUID()
        val otherAlert = Alert(
            id = alertId,
            userId = otherUserId,
            category = ThreatCategory.DATA_BREACH,
            severity = Severity.CRITICAL,
            title = "Other",
            description = "Other",
        )

        every { alertRepository.findById(alertId) } returns otherAlert

        assertThrows<AppException.ResourceNotFoundException> {
            service.findById(alertId, userId)
        }
    }

    @Test
    fun `resolve updates resolved flag and recalculates score`() {
        val alertId = UUID.randomUUID()
        val unresolvedAlert = alert(id = alertId, resolved = false)

        every { alertRepository.findById(alertId) } returns unresolvedAlert
        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val result = service.resolve(alertId, userId)

        assertTrue(result.resolved)
        assertNotNull(result.resolvedAt)
        verify { scoreService.recalculate(userId) }
    }

    @Test
    fun `resolve throws ResourceNotFoundException when alert does not exist`() {
        val alertId = UUID.randomUUID()

        every { alertRepository.findById(alertId) } returns null

        assertThrows<AppException.ResourceNotFoundException> {
            service.resolve(alertId, userId)
        }
    }

    @Test
    fun `delete removes alert and recalculates score`() {
        val alertId = UUID.randomUUID()
        val existing = alert(id = alertId)

        every { alertRepository.findById(alertId) } returns existing
        every { alertRepository.deleteById(alertId) } just Runs
        every { scoreService.recalculate(userId) } returns mockk()

        service.delete(alertId, userId)

        verify { alertRepository.deleteById(alertId) }
        verify { scoreService.recalculate(userId) }
    }

    @Test
    fun `delete throws ResourceNotFoundException when alert does not exist`() {
        val alertId = UUID.randomUUID()

        every { alertRepository.findById(alertId) } returns null

        assertThrows<AppException.ResourceNotFoundException> {
            service.delete(alertId, userId)
        }
    }
}
