package com.privacyalert.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.privacyalert.domain.model.AppError
import com.privacyalert.presentation.util.AppLogger
import com.privacyalert.domain.usecase.auth.CheckAuthUseCase
import com.privacyalert.domain.usecase.auth.LoginUseCase
import com.privacyalert.domain.usecase.auth.LogoutUseCase
import com.privacyalert.domain.usecase.auth.RegisterUseCase
import com.privacyalert.domain.usecase.user.SaveUserEmailUseCase
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

private const val TAG = "AuthViewModel"

class AuthViewModel(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    checkAuthUseCase: CheckAuthUseCase,
    private val saveUserEmailUseCase: SaveUserEmailUseCase,
) : ScreenModel {

    val isAuthenticated: StateFlow<Boolean> = checkAuthUseCase()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    fun register(email: String, password: String) {
        screenModelScope.launch {
            _authState.value = AuthUiState.Loading
            registerUseCase(email, password).fold(
                onSuccess = {
                    saveUserEmailUseCase(email)
                    _authState.value = AuthUiState.Success
                },
                onFailure = {
                    AppLogger.e(TAG, "register() failed", it)
                    _authState.value = AuthUiState.Error(it.toRegisterMessage())
                },
            )
        }
    }

    fun login(email: String, password: String) {
        screenModelScope.launch {
            _authState.value = AuthUiState.Loading
            loginUseCase(email, password).fold(
                onSuccess = {
                    saveUserEmailUseCase(email)
                    _authState.value = AuthUiState.Success
                },
                onFailure = {
                    AppLogger.e(TAG, "login() failed", it)
                    _authState.value = AuthUiState.Error(it.toLoginMessage())
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

private fun Throwable.toRegisterMessage(): String = when (this) {
    is AppError.NetworkError -> "Unable to connect. Please check your internet connection."
    is AppError.Conflict -> "An account with this email already exists."
    is AppError.ValidationError -> "Please check your details and try again."
    is AppError.ServerError -> "Something went wrong on our end. Please try again."
    else -> "Registration failed. Please try again."
}

private fun Throwable.toLoginMessage(): String = when (this) {
    is AppError.NetworkError -> "Unable to connect. Please check your internet connection."
    is AppError.Unauthorized -> "Incorrect email or password."
    is AppError.ValidationError -> "Please check your email and password."
    is AppError.ServerError -> "Something went wrong on our end. Please try again."
    else -> "Login failed. Please try again."
}
