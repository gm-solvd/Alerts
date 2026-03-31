package com.privacyalert.api.controller

import com.privacyalert.api.dto.MitigationResponse
import com.privacyalert.api.dto.toResponse
import com.privacyalert.domain.service.MitigationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/mitigations")
class MitigationController(
    private val mitigationService: MitigationService,
) {
    @GetMapping
    fun list(): ResponseEntity<List<MitigationResponse>> {
        val mitigations = mitigationService.findAllByUserId(authenticatedUserId())
        return ResponseEntity.ok(mitigations.map { it.toResponse() })
    }

    @GetMapping("/{alertId}")
    fun getByAlertId(
        @PathVariable alertId: UUID,
    ): ResponseEntity<List<MitigationResponse>> {
        val mitigations = mitigationService.findByAlertId(alertId)
        return ResponseEntity.ok(mitigations.map { it.toResponse() })
    }

    @PatchMapping("/{id}/complete")
    fun complete(
        @PathVariable id: UUID,
    ): ResponseEntity<MitigationResponse> {
        val mitigation = mitigationService.complete(id)
        return ResponseEntity.ok(mitigation.toResponse())
    }
}
