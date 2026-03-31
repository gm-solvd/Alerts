package com.privacyalert.api.controller

import com.privacyalert.api.dto.PageResponse
import com.privacyalert.api.dto.ScoreResponse
import com.privacyalert.api.dto.toPageResponse
import com.privacyalert.api.dto.toResponse
import com.privacyalert.domain.service.ScoreService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/score")
class ScoreController(
    private val scoreService: ScoreService,
) {
    @GetMapping
    fun getCurrent(): ResponseEntity<ScoreResponse> {
        val score = scoreService.getCurrent(authenticatedUserId())
        return ResponseEntity.ok(score.toResponse())
    }

    @GetMapping("/history")
    fun getHistory(
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<PageResponse<ScoreResponse>> {
        val page = scoreService.getHistory(authenticatedUserId(), pageable)
        return ResponseEntity.ok(page.toPageResponse { it.toResponse() })
    }
}
