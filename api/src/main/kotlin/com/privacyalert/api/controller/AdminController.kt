package com.privacyalert.api.controller

import com.privacyalert.api.dto.AdminStatsResponse
import com.privacyalert.api.dto.AdminUserDetailResponse
import com.privacyalert.api.dto.AdminUserResponse
import com.privacyalert.api.dto.AlertResponse
import com.privacyalert.api.dto.CreateUserRequest
import com.privacyalert.api.dto.PageResponse
import com.privacyalert.api.dto.ScanExecutionResponse
import com.privacyalert.api.dto.ScanResultResponse
import com.privacyalert.api.dto.ScannerResultResponse
import com.privacyalert.api.dto.toPageResponse
import com.privacyalert.api.dto.toResponse
import com.privacyalert.domain.service.AdminService
import com.privacyalert.domain.service.AdminUserDetail
import com.privacyalert.domain.service.AdminUserView
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val adminService: AdminService,
) {
    @PostMapping("/users")
    fun createUser(
        @RequestBody request: CreateUserRequest,
    ): ResponseEntity<AdminUserResponse> {
        val user =
            adminService.createUser(
                email = request.email,
                fullName = request.fullName,
                phoneNumber = request.phoneNumber,
                homeAddress = request.homeAddress,
                dateOfBirth = request.dateOfBirth,
            )
        val response =
            AdminUserResponse(
                id = user.id,
                email = user.email,
                fullName = user.fullName,
                oauthProvider = user.oauthProvider,
                alertCount = 0,
                score = null,
                createdAt = user.createdAt,
            )
        return ResponseEntity.created(URI.create("/api/v1/admin/users/${user.id}")).body(response)
    }

    @GetMapping("/users")
    fun listUsers(
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<PageResponse<AdminUserResponse>> {
        val page = adminService.findAllUsers(pageable)
        return ResponseEntity.ok(page.toPageResponse { it.toResponse() })
    }

    @GetMapping("/users/{id}")
    fun getUser(
        @PathVariable id: UUID,
    ): ResponseEntity<AdminUserDetailResponse> {
        val detail = adminService.findUserById(id)
        return ResponseEntity.ok(detail.toResponse())
    }

    @DeleteMapping("/users/{id}")
    fun deleteUser(
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        adminService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/users/{id}/alerts")
    fun getUserAlerts(
        @PathVariable id: UUID,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<PageResponse<AlertResponse>> {
        val page = adminService.findAlertsByUserId(id, pageable)
        return ResponseEntity.ok(page.toPageResponse { it.toResponse() })
    }

    @PostMapping("/users/{id}/scan")
    fun triggerScan(
        @PathVariable id: UUID,
    ): ResponseEntity<ScanExecutionResponse> {
        val execution = adminService.triggerScan(id)
        return ResponseEntity.ok(
            ScanExecutionResponse(
                totalAlerts = execution.totalAlerts,
                scanners =
                    execution.scanners.map { scanner ->
                        ScannerResultResponse(
                            scannerName = scanner.scannerName,
                            findingsCount = scanner.findingsCount,
                            alerts = scanner.alerts.map { it.toResponse() },
                        )
                    },
            ),
        )
    }

    @GetMapping("/users/{id}/scans")
    fun getScanHistory(
        @PathVariable id: UUID,
        @PageableDefault(size = 50) pageable: Pageable,
    ): ResponseEntity<PageResponse<ScanResultResponse>> {
        val page = adminService.getScanHistory(id, pageable)
        return ResponseEntity.ok(
            page.toPageResponse { result ->
                ScanResultResponse(
                    id = result.id,
                    scanType = result.scanType,
                    findings = result.findings,
                    createdAt = result.createdAt,
                )
            },
        )
    }

    @GetMapping("/stats")
    fun getStats(): ResponseEntity<AdminStatsResponse> {
        val stats = adminService.getStats()
        return ResponseEntity.ok(
            AdminStatsResponse(
                totalUsers = stats.totalUsers,
                totalAlerts = stats.totalAlerts,
                alertsByCategory = stats.alertsByCategory,
                alertsBySeverity = stats.alertsBySeverity,
            ),
        )
    }
}

private fun AdminUserView.toResponse(): AdminUserResponse =
    AdminUserResponse(
        id = user.id,
        email = user.email,
        fullName = user.fullName,
        oauthProvider = user.oauthProvider,
        alertCount = alertCount,
        score = score,
        createdAt = user.createdAt,
    )

private fun AdminUserDetail.toResponse(): AdminUserDetailResponse =
    AdminUserDetailResponse(
        id = user.id,
        email = user.email,
        fullName = user.fullName,
        phoneNumber = user.phoneNumber,
        homeAddress = user.homeAddress,
        dateOfBirth = user.dateOfBirth,
        oauthProvider = user.oauthProvider,
        alertCount = alertCount,
        score = score,
        recentAlerts = recentAlerts.map { it.toResponse() },
        createdAt = user.createdAt,
    )
