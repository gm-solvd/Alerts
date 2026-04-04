package com.privacyalert.domain.repository

/**
 * Domain-level abstraction for user profile storage.
 * Implemented by data layer (e.g., SqlDelightTokenStorage).
 */
interface UserRepository {
    suspend fun saveEmail(email: String)
    suspend fun getEmail(): String?
}
