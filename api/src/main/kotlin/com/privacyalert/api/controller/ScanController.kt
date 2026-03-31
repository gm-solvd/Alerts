package com.privacyalert.api.controller

import com.privacyalert.api.dto.AlertResponse
import com.privacyalert.api.dto.BreachScanRequest
import com.privacyalert.api.dto.IdentityScanRequest
import com.privacyalert.api.dto.toResponse
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
    fun breachScan(@Valid @RequestBody request: BreachScanRequest): ResponseEntity<List<AlertResponse>> {
        val alerts = scanService.breachScan(authenticatedUserId(), request.email)
        return ResponseEntity.ok(alerts.map { it.toResponse() })
    }

    @PostMapping("/identity")
    fun identityScan(@Valid @RequestBody request: IdentityScanRequest): ResponseEntity<List<AlertResponse>> {
        val alerts = scanService.identityScan(authenticatedUserId(), request.email)
        return ResponseEntity.ok(alerts.map { it.toResponse() })
    }
}
