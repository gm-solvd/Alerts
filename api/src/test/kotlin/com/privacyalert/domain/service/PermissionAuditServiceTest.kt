package com.privacyalert.domain.service

import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.repository.AlertRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class PermissionAuditServiceTest {

    private val alertRepository = mockk<AlertRepository>()
    private val scoreService = mockk<ScoreService>()
    private val service = PermissionAuditService(alertRepository, scoreService)

    private val userId = UUID.randomUUID()

    @Test
    fun `submitAudit creates alerts for granted risky permissions`() {
        val permissions = listOf(
            PermissionEntry("android.permission.CAMERA", granted = true),
            PermissionEntry("android.permission.READ_SMS", granted = true),
        )

        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.submitAudit(userId, permissions)

        assertEquals(2, alerts.size)
        assertTrue(alerts.all { it.category == ThreatCategory.APP_OVERPERMISSIONS })
        assertTrue(alerts.all { it.severity == Severity.MEDIUM })
        verify { scoreService.recalculate(userId) }
    }

    @Test
    fun `submitAudit ignores non-risky permissions`() {
        val permissions = listOf(
            PermissionEntry("android.permission.INTERNET", granted = true),
            PermissionEntry("android.permission.VIBRATE", granted = true),
        )

        val alerts = service.submitAudit(userId, permissions)

        assertTrue(alerts.isEmpty())
        verify(exactly = 0) { scoreService.recalculate(any()) }
    }

    @Test
    fun `submitAudit ignores risky permissions that are not granted`() {
        val permissions = listOf(
            PermissionEntry("android.permission.CAMERA", granted = false),
            PermissionEntry("android.permission.READ_SMS", granted = false),
        )

        val alerts = service.submitAudit(userId, permissions)

        assertTrue(alerts.isEmpty())
        verify(exactly = 0) { scoreService.recalculate(any()) }
    }

    @Test
    fun `submitAudit creates alerts only for granted risky permissions in mixed list`() {
        val permissions = listOf(
            PermissionEntry("android.permission.CAMERA", granted = true),
            PermissionEntry("android.permission.INTERNET", granted = true),
            PermissionEntry("android.permission.READ_SMS", granted = false),
            PermissionEntry("android.permission.RECORD_AUDIO", granted = true),
        )

        every { alertRepository.save(any()) } answers { firstArg() }
        every { scoreService.recalculate(userId) } returns mockk()

        val alerts = service.submitAudit(userId, permissions)

        assertEquals(2, alerts.size)
        assertTrue(alerts.any { it.title.contains("CAMERA") })
        assertTrue(alerts.any { it.title.contains("RECORD_AUDIO") })
    }

    @Test
    fun `submitAudit handles empty permissions list`() {
        val alerts = service.submitAudit(userId, emptyList())

        assertTrue(alerts.isEmpty())
        verify(exactly = 0) { scoreService.recalculate(any()) }
    }
}
