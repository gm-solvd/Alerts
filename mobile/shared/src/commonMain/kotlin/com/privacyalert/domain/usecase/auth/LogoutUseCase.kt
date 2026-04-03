package com.privacyalert.domain.usecase.auth

import com.privacyalert.domain.repository.AuthRepository
import com.privacyalert.domain.util.safeApiCall

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> =
        safeApiCall { authRepository.clearTokens() }
}
