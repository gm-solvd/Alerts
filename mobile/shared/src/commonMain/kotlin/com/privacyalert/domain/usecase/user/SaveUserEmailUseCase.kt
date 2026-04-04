package com.privacyalert.domain.usecase.user

import com.privacyalert.domain.repository.UserRepository

class SaveUserEmailUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(email: String) {
        userRepository.saveEmail(email)
    }
}
