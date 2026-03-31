package com.privacyalert.api.controller

import com.privacyalert.api.dto.AdminStatsResponse
import com.privacyalert.api.dto.AdminUserDetailResponse
import com.privacyalert.api.dto.AdminUserResponse
import com.privacyalert.api.dto.AlertResponse
import com.privacyalert.api.dto.PageResponse
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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val adminService: AdminService,
) {
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
        oauthProvider = user.oauthProvider,
        alertCount = alertCount,
        score = score,
        createdAt = user.createdAt,
    )

private fun AdminUserDetail.toResponse(): AdminUserDetailResponse =
    AdminUserDetailResponse(
        id = user.id,
        email = user.email,
        oauthProvider = user.oauthProvider,
        alertCount = alertCount,
        score = score,
        recentAlerts = recentAlerts.map { it.toResponse() },
        createdAt = user.createdAt,
    )
