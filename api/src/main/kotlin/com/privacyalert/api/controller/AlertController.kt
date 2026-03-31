package com.privacyalert.api.controller

import com.privacyalert.api.dto.AlertResponse
import com.privacyalert.api.dto.PageResponse
import com.privacyalert.api.dto.toPageResponse
import com.privacyalert.api.dto.toResponse
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.model.ThreatCategory
import com.privacyalert.domain.service.AlertService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/alerts")
class AlertController(
    private val alertService: AlertService,
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) category: ThreatCategory?,
        @RequestParam(required = false) severity: Severity?,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<PageResponse<AlertResponse>> {
        val page = alertService.findAll(authenticatedUserId(), category, severity, pageable)
        return ResponseEntity.ok(page.toPageResponse { it.toResponse() })
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<AlertResponse> {
        val alert = alertService.findById(id, authenticatedUserId())
        return ResponseEntity.ok(alert.toResponse())
    }

    @PatchMapping("/{id}/resolve")
    fun resolve(@PathVariable id: UUID): ResponseEntity<AlertResponse> {
        val alert = alertService.resolve(id, authenticatedUserId())
        return ResponseEntity.ok(alert.toResponse())
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        alertService.delete(id, authenticatedUserId())
        return ResponseEntity.noContent().build()
    }
}
