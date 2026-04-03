package com.privacyalert.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.privacyalert.domain.usecase.auth.CheckAuthUseCase
import com.privacyalert.domain.usecase.auth.LoginUseCase
import com.privacyalert.domain.usecase.auth.LogoutUseCase
import com.privacyalert.domain.usecase.auth.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    checkAuthUseCase: CheckAuthUseCase,
) : ScreenModel {

    val isAuthenticated: StateFlow<Boolean> = checkAuthUseCase()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    fun register(email: String, password: String) {
        screenModelScope.launch {
            _authState.value = AuthUiState.Loading
            registerUseCase(email, password).fold(
                onSuccess = { _authState.value = AuthUiState.Success },
                onFailure = {
                    _authState.value = AuthUiState.Error(it.message ?: "Registration failed")
                },
            )
        }
    }

    fun login(email: String, password: String) {
        screenModelScope.launch {
            _authState.value = AuthUiState.Loading
            loginUseCase(email, password).fold(
                onSuccess = { _authState.value = AuthUiState.Success },
                onFailure = {
                    _authState.value = AuthUiState.Error(it.message ?: "Login failed")
                },
            )
        }
    }

    fun logout() {
        screenModelScope.launch {
            logoutUseCase()
            _authState.value = AuthUiState.Idle
        }
    }

    fun clearError() {
        _authState.value = AuthUiState.Idle
    }
}
