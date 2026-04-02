package com.privacyalert.domain.repository

import com.privacyalert.domain.model.AuthTokens
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(email: String, password: String): AuthTokens
    suspend fun login(email: String, password: String): AuthTokens
    suspend fun refreshToken(): AuthTokens
    suspend fun getStoredTokens(): AuthTokens?
    suspend fun clearTokens()
    fun isLoggedIn(): Flow<Boolean>
}
