package com.privacyalert.domain.usecase.user

import com.privacyalert.domain.repository.UserRepository

class GetUserEmailUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): String? = userRepository.getEmail()
}
