package com.privacyalert.domain.usecase.auth

import com.privacyalert.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class CheckAuthUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<Boolean> = authRepository.isLoggedIn()
}
