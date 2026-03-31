package com.privacyalert.api.controller

import com.privacyalert.api.dto.AlertResponse
import com.privacyalert.api.dto.PermissionAuditRequest
import com.privacyalert.api.dto.toResponse
import com.privacyalert.domain.service.PermissionAuditService
import com.privacyalert.domain.service.PermissionEntry
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/audit")
class PermissionAuditController(
    private val permissionAuditService: PermissionAuditService,
) {

    @PostMapping("/permissions")
    fun submitAudit(@Valid @RequestBody request: PermissionAuditRequest): ResponseEntity<List<AlertResponse>> {
        val permissions = request.permissions.map {
            PermissionEntry(name = it.name, granted = it.granted)
        }
        val alerts = permissionAuditService.submitAudit(authenticatedUserId(), permissions)
        return ResponseEntity.ok(alerts.map { it.toResponse() })
    }
}
