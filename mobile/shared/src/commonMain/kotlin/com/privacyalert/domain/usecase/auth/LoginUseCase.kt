package com.privacyalert.domain.usecase.auth

import com.privacyalert.domain.model.AuthTokens
import com.privacyalert.domain.repository.AuthRepository
import com.privacyalert.domain.util.safeApiCall

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<AuthTokens> =
        safeApiCall { authRepository.login(email, password) }
}
