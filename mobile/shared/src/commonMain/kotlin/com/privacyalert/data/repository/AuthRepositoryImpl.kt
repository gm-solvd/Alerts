package com.privacyalert.data.repository

import com.privacyalert.data.local.TokenStorage
import com.privacyalert.data.remote.api.AuthApi
import com.privacyalert.data.remote.dto.LoginRequestDto
import com.privacyalert.data.remote.dto.RefreshTokenRequestDto
import com.privacyalert.data.remote.dto.RegisterRequestDto
import com.privacyalert.domain.model.AuthTokens
import com.privacyalert.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override suspend fun register(email: String, password: String): AuthTokens {
        val response = authApi.register(RegisterRequestDto(email, password))
        val tokens = response.toDomain()
        tokenStorage.saveTokens(tokens)
        return tokens
    }

    override suspend fun login(email: String, password: String): AuthTokens {
        val response = authApi.login(LoginRequestDto(email, password))
        val tokens = response.toDomain()
        tokenStorage.saveTokens(tokens)
        return tokens
    }

    override suspend fun refreshToken(): AuthTokens {
        val refresh = tokenStorage.getRefreshToken()
            ?: throw IllegalStateException("No refresh token available")
        val response = authApi.refresh(RefreshTokenRequestDto(refresh))
        val tokens = response.toDomain()
        tokenStorage.saveTokens(tokens)
        return tokens
    }

    override suspend fun getStoredTokens(): AuthTokens? {
        val access = tokenStorage.getAccessToken() ?: return null
        val refresh = tokenStorage.getRefreshToken() ?: return null
        return AuthTokens(access, refresh)
    }

    override suspend fun clearTokens() {
        tokenStorage.clearTokens()
    }

    override fun isLoggedIn(): Flow<Boolean> = tokenStorage.hasTokens()
}
