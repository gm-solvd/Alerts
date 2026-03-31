package com.privacyalert.api.controller

import com.privacyalert.api.dto.AlertResponse
import com.privacyalert.api.dto.ScanProfileRequest
import com.privacyalert.api.dto.toResponse
import com.privacyalert.domain.model.UserScanProfile
import com.privacyalert.domain.service.ScanService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/scan")
class ScanController(
    private val scanService: ScanService,
) {
    @PostMapping("/breach")
    fun breachScan(
        @Valid @RequestBody request: ScanProfileRequest,
    ): ResponseEntity<List<AlertResponse>> {
        val alerts = scanService.breachScan(authenticatedUserId(), request.toProfile())
        return ResponseEntity.ok(alerts.map { it.toResponse() })
    }

    @PostMapping("/identity")
    fun identityScan(
        @Valid @RequestBody request: ScanProfileRequest,
    ): ResponseEntity<List<AlertResponse>> {
        val alerts = scanService.identityScan(authenticatedUserId(), request.toProfile())
        return ResponseEntity.ok(alerts.map { it.toResponse() })
    }

    @PostMapping("/pii")
    fun piiExposureScan(
        @Valid @RequestBody request: ScanProfileRequest,
    ): ResponseEntity<List<AlertResponse>> {
        val alerts = scanService.piiExposureScan(authenticatedUserId(), request.toProfile())
        return ResponseEntity.ok(alerts.map { it.toResponse() })
    }

    @PostMapping("/social")
    fun socialFootprintScan(
        @Valid @RequestBody request: ScanProfileRequest,
    ): ResponseEntity<List<AlertResponse>> {
        val alerts = scanService.socialFootprintScan(authenticatedUserId(), request.toProfile(), request.username)
        return ResponseEntity.ok(alerts.map { it.toResponse() })
    }

    @PostMapping("/full")
    fun fullScan(
        @Valid @RequestBody request: ScanProfileRequest,
    ): ResponseEntity<List<AlertResponse>> {
        val alerts = scanService.fullScan(authenticatedUserId(), request.toProfile(), request.username)
        return ResponseEntity.ok(alerts.map { it.toResponse() })
    }

    private fun ScanProfileRequest.toProfile() =
        UserScanProfile(
            email = email,
            phoneNumber = phoneNumber,
            fullName = fullName,
            homeAddress = homeAddress,
            dateOfBirth = dateOfBirth,
        )
}
