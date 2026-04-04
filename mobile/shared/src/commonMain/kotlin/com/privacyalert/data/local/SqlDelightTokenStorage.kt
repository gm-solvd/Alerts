package com.privacyalert.data.local

import com.privacyalert.db.PrivacyAlertDb
import com.privacyalert.domain.model.AuthTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SqlDelightTokenStorage(
    private val db: PrivacyAlertDb,
) : TokenStorage {

    private val _hasTokens = MutableStateFlow(false)

    init {
        _hasTokens.value = db.privacyAlertQueries.hasTokens().executeAsOne() > 0
    }

    override suspend fun saveTokens(tokens: AuthTokens) {
        db.privacyAlertQueries.upsertTokens(
            access_token = tokens.accessToken,
            refresh_token = tokens.refreshToken,
        )
        _hasTokens.value = true
    }

    override suspend fun getAccessToken(): String? =
        db.privacyAlertQueries.getTokens().executeAsOneOrNull()?.access_token

    override suspend fun getRefreshToken(): String? =
        db.privacyAlertQueries.getTokens().executeAsOneOrNull()?.refresh_token

    override suspend fun clearTokens() {
        db.privacyAlertQueries.deleteTokens()
        _hasTokens.value = false
    }

    override fun hasTokens(): Flow<Boolean> = _hasTokens.asStateFlow()

    override suspend fun saveEmail(email: String) {
        db.privacyAlertQueries.upsertEmail(email)
    }

    override suspend fun getEmail(): String? =
        db.privacyAlertQueries.getEmail().executeAsOneOrNull()?.email
}
