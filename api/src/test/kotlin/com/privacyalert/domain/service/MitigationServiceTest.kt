package com.privacyalert.domain.service

import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.model.Mitigation
import com.privacyalert.domain.repository.MitigationRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class MitigationServiceTest {
    private val mitigationRepository = mockk<MitigationRepository>()
    private val service = MitigationService(mitigationRepository)

    private val userId = UUID.randomUUID()
    private val alertId = UUID.randomUUID()

    private fun mitigation(
        id: UUID = UUID.randomUUID(),
        completed: Boolean = false,
    ) = Mitigation(
        id = id,
        alertId = alertId,
        title = "Change password",
        description = "Change your password immediately",
        actionUrl = "https://example.com/reset",
        completed = completed,
    )

    @Test
    fun `findAllByUserId returns mitigations for user`() {
        val mitigations = listOf(mitigation(), mitigation())
        every { mitigationRepository.findAllByUserId(userId) } returns mitigations

        val result = service.findAllByUserId(userId)

        assertEquals(2, result.size)
    }

    @Test
    fun `findByAlertId returns mitigations for alert`() {
        val mitigations = listOf(mitigation())
        every { mitigationRepository.findAllByAlertId(alertId) } returns mitigations

        val result = service.findByAlertId(alertId)

        assertEquals(1, result.size)
    }

    @Test
    fun `complete marks mitigation as completed`() {
        val mitigationId = UUID.randomUUID()
        val existing = mitigation(id = mitigationId, completed = false)

        every { mitigationRepository.findById(mitigationId) } returns existing
        every { mitigationRepository.save(any()) } answers { firstArg() }

        val result = service.complete(mitigationId)

        assertTrue(result.completed)
        assertNotNull(result.completedAt)
    }

    @Test
    fun `complete throws ResourceNotFoundException when mitigation does not exist`() {
        val mitigationId = UUID.randomUUID()

        every { mitigationRepository.findById(mitigationId) } returns null

        assertThrows<AppException.ResourceNotFoundException> {
            service.complete(mitigationId)
        }
    }
}
