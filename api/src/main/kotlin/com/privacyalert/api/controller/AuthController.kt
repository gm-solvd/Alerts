package com.privacyalert.api.controller

import com.privacyalert.api.dto.AuthTokensResponse
import com.privacyalert.api.dto.LoginRequest
import com.privacyalert.api.dto.OAuthCallbackRequest
import com.privacyalert.api.dto.RefreshTokenRequest
import com.privacyalert.api.dto.RegisterRequest
import com.privacyalert.domain.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthTokensResponse> {
        val tokens = authService.register(request.email, request.password)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            AuthTokensResponse(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken),
        )
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthTokensResponse> {
        val tokens = authService.login(request.email, request.password)
        return ResponseEntity.ok(
            AuthTokensResponse(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken),
        )
    }

    @PostMapping("/oauth2/callback")
    fun oauthCallback(@Valid @RequestBody request: OAuthCallbackRequest): ResponseEntity<AuthTokensResponse> {
        val tokens = authService.oauthCallback(request.provider, request.idToken)
        return ResponseEntity.ok(
            AuthTokensResponse(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken),
        )
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthTokensResponse> {
        val tokens = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(
            AuthTokensResponse(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken),
        )
    }
}
