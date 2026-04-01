package com.privacyalert.domain.service

import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.AppException
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.model.User
import com.privacyalert.domain.model.UserScanProfile
import com.privacyalert.domain.repository.AlertRepository
import com.privacyalert.domain.repository.ScanResultRepository
import com.privacyalert.domain.repository.ScoreRepository
import com.privacyalert.domain.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

data class AdminUserView(
    val user: User,
    val alertCount: Long,
    val score: Int?,
)

data class AdminUserDetail(
    val user: User,
    val alertCount: Long,
    val score: Int?,
    val recentAlerts: List<Alert>,
)

data class AdminStats(
    val totalUsers: Long,
    val totalAlerts: Long,
    val alertsByCategory: Map<ThreatCategory, Long>,
    val alertsBySeverity: Map<Severity, Long>,
)

data class ScanExecution(
    val totalAlerts: Int,
    val scanners: List<ScannerResult>,
)

data class ScannerResult(
    val scannerName: String,
    val findingsCount: Int,
    val alerts: List<Alert>,
)

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val alertRepository: AlertRepository,
    private val scoreRepository: ScoreRepository,
    private val scanService: ScanService,
    private val scanResultRepository: ScanResultRepository,
) {
    fun createUser(
        email: String,
        fullName: String,
        phoneNumber: String?,
        homeAddress: String?,
        dateOfBirth: LocalDate?,
    ): User {
        if (userRepository.existsByEmail(email)) {
            throw AppException.ConflictException("User with email $email already exists")
        }
        return userRepository.save(
            User(
                email = email,
                fullName = fullName,
                phoneNumber = phoneNumber,
                homeAddress = homeAddress,
                dateOfBirth = dateOfBirth,
            ),
        )
    }

    fun findAllUsers(pageable: Pageable): Page<AdminUserView> =
        userRepository.findAll(pageable).map { user ->
            AdminUserView(
                user = user,
                alertCount = alertRepository.countByUserId(user.id),
                score = scoreRepository.findLatestByUserId(user.id)?.score,
            )
        }

    fun findUserById(id: UUID): AdminUserDetail {
        val user = userRepository.findById(id) ?: throw AppException.ResourceNotFoundException("User", id)
        val alertCount = alertRepository.countByUserId(id)
        val score = scoreRepository.findLatestByUserId(id)?.score
        val recentAlerts =
            alertRepository
                .findAllByUserId(
                    id,
                    Pageable.ofSize(5),
                ).content
        return AdminUserDetail(
            user = user,
            alertCount = alertCount,
            score = score,
            recentAlerts = recentAlerts,
        )
    }

    fun deleteUser(id: UUID) {
        userRepository.findById(id) ?: throw AppException.ResourceNotFoundException("User", id)
        userRepository.deleteById(id)
    }

    fun findAlertsByUserId(
        userId: UUID,
        pageable: Pageable,
    ): Page<Alert> {
        userRepository.findById(userId) ?: throw AppException.ResourceNotFoundException("User", userId)
        return alertRepository.findAllByUserId(userId, pageable)
    }

    fun getStats(): AdminStats =
        AdminStats(
            totalUsers = userRepository.count(),
            totalAlerts = alertRepository.count(),
            alertsByCategory = alertRepository.countByCategory(),
            alertsBySeverity = alertRepository.countBySeverity(),
        )

    fun triggerScan(userId: UUID): ScanExecution {
        val user = userRepository.findById(userId) ?: throw AppException.ResourceNotFoundException("User", userId)
        val profile =
            UserScanProfile(
                email = user.email,
                phoneNumber = user.phoneNumber,
                fullName = user.fullName,
                homeAddress = user.homeAddress,
                dateOfBirth = user.dateOfBirth,
            )

        val scanners = mutableListOf<ScannerResult>()

        val breachAlerts = scanService.breachScan(userId, profile)
        scanners += ScannerResult("breach", breachAlerts.size, breachAlerts)

        val identityAlerts = scanService.identityScan(userId, profile)
        scanners += ScannerResult("identity", identityAlerts.size, identityAlerts)

        val piiAlerts = scanService.piiExposureScan(userId, profile)
        scanners += ScannerResult("pii", piiAlerts.size, piiAlerts)

        val socialAlerts = scanService.socialFootprintScan(userId, profile, null)
        scanners += ScannerResult("social", socialAlerts.size, socialAlerts)

        return ScanExecution(
            totalAlerts = scanners.sumOf { it.findingsCount },
            scanners = scanners,
        )
    }

    fun getScanHistory(
        userId: UUID,
        pageable: Pageable,
    ): Page<com.privacyalert.domain.model.ScanResult> {
        userRepository.findById(userId) ?: throw AppException.ResourceNotFoundException("User", userId)
        return scanResultRepository.findAllByUserId(userId, pageable)
    }
}
