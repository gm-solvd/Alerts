package com.privacyalert.domain.service

import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.model.ScanJob
import com.privacyalert.domain.model.ScanJobStatus
import com.privacyalert.domain.model.User
import com.privacyalert.domain.repository.ScanJobRepository
import com.privacyalert.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.task.AsyncTaskExecutor
import java.util.UUID

class AsyncScanServiceTest {
    private val scanService = mockk<ScanService>()
    private val scanJobRepository = mockk<ScanJobRepository>()
    private val userRepository = mockk<UserRepository>()
    private val scanTaskExecutor = mockk<AsyncTaskExecutor>(relaxed = true)
    private val service =
        AsyncScanService(
            scanService,
            scanJobRepository,
            userRepository,
            scanTaskExecutor,
        )

    private val userId = UUID.randomUUID()
    private val user =
        User(
            id = userId,
            email = "user@example.com",
            fullName = "Test User",
        )

    @Test
    fun `startScan creates a ScanJob and returns it`() {
        val jobSlot = slot<ScanJob>()
        every { userRepository.findById(userId) } returns user
        every { scanJobRepository.save(capture(jobSlot)) } answers { jobSlot.captured }

        val result = service.startScan(userId)

        assertEquals(userId, result.userId)
        assertEquals(ScanJobStatus.PENDING, result.status)
        assertNotNull(result.id)
        verify { scanTaskExecutor.execute(any()) }
    }

    @Test
    fun `startScan throws ResourceNotFoundException for unknown user`() {
        every { userRepository.findById(userId) } returns null

        val exception =
            assertThrows<AppException.ResourceNotFoundException> {
                service.startScan(userId)
            }

        assertEquals("User", exception.resource)
        assertEquals(userId, exception.id)
    }

    @Test
    fun `getJobStatus returns the job for known ID`() {
        val jobId = UUID.randomUUID()
        val job = ScanJob(id = jobId, userId = userId, status = ScanJobStatus.RUNNING)
        every { scanJobRepository.findById(jobId) } returns job

        val result = service.getJobStatus(jobId)

        assertEquals(jobId, result.id)
        assertEquals(ScanJobStatus.RUNNING, result.status)
    }

    @Test
    fun `getJobStatus throws ResourceNotFoundException for unknown ID`() {
        val jobId = UUID.randomUUID()
        every { scanJobRepository.findById(jobId) } returns null

        val exception =
            assertThrows<AppException.ResourceNotFoundException> {
                service.getJobStatus(jobId)
            }

        assertEquals("ScanJob", exception.resource)
        assertEquals(jobId, exception.id)
    }
}
