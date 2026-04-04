package com.privacyalert.data.local

import com.privacyalert.domain.model.AuthTokens
import kotlinx.coroutines.flow.Flow

interface TokenStorage {
    suspend fun saveTokens(tokens: AuthTokens)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
    fun hasTokens(): Flow<Boolean>
    suspend fun saveEmail(email: String)
    suspend fun getEmail(): String?
}
