package com.privacyalert.domain.service

import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.model.ScanJob
import com.privacyalert.domain.model.ScanJobStatus
import com.privacyalert.domain.model.UserScanProfile
import com.privacyalert.domain.repository.ScanJobRepository
import com.privacyalert.domain.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class AsyncScanService(
    private val scanService: ScanService,
    private val scanJobRepository: ScanJobRepository,
    private val userRepository: UserRepository,
    private val scanTaskExecutor: AsyncTaskExecutor,
) {
    private val log = LoggerFactory.getLogger(AsyncScanService::class.java)

    fun startScan(userId: UUID): ScanJob {
        val user =
            userRepository.findById(userId)
                ?: throw AppException.ResourceNotFoundException("User", userId)
        val job = scanJobRepository.save(ScanJob(userId = userId))

        val profile =
            UserScanProfile(
                email = user.email,
                phoneNumber = user.phoneNumber,
                fullName = user.fullName,
                homeAddress = user.homeAddress,
                dateOfBirth = user.dateOfBirth,
            )

        scanTaskExecutor.execute { runScan(job.id, userId, profile) }
        return job
    }

    fun getJobStatus(jobId: UUID): ScanJob =
        scanJobRepository.findById(jobId)
            ?: throw AppException.ResourceNotFoundException("ScanJob", jobId)

    private fun runScan(
        jobId: UUID,
        userId: UUID,
        profile: UserScanProfile,
    ) {
        try {
            scanJobRepository.save(
                scanJobRepository.findById(jobId)!!.copy(
                    status = ScanJobStatus.RUNNING,
                    startedAt = Instant.now(),
                    progress = "breach",
                ),
            )

            val alerts = mutableListOf<com.privacyalert.domain.model.Alert>()

            alerts += scanService.breachScan(userId, profile)
            updateProgress(jobId, "identity")

            alerts += scanService.identityScan(userId, profile)
            updateProgress(jobId, "pii")

            alerts += scanService.piiExposureScan(userId, profile)
            updateProgress(jobId, "social")

            alerts += scanService.socialFootprintScan(userId, profile, null)
            updateProgress(jobId, "reputation")

            alerts += scanService.emailReputationScan(userId, profile)

            scanJobRepository.save(
                scanJobRepository.findById(jobId)!!.copy(
                    status = ScanJobStatus.COMPLETED,
                    totalAlerts = alerts.size,
                    completedAt = Instant.now(),
                    progress = null,
                ),
            )
        } catch (e: Exception) {
            log.error("Scan job {} failed", jobId, e)
            scanJobRepository.save(
                scanJobRepository.findById(jobId)!!.copy(
                    status = ScanJobStatus.FAILED,
                    errorMessage = "Scan could not be completed. Please try again.",
                    completedAt = Instant.now(),
                    progress = null,
                ),
            )
        }
    }

    private fun updateProgress(
        jobId: UUID,
        nextScanner: String,
    ) {
        scanJobRepository.save(
            scanJobRepository.findById(jobId)!!.copy(progress = nextScanner),
        )
    }
}
