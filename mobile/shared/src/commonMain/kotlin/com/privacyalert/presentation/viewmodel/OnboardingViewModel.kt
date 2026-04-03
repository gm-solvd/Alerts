package com.privacyalert.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.usecase.auth.RegisterUseCase
import com.privacyalert.domain.usecase.scan.FullScanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentStep: Int = 0,
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val registrationComplete: Boolean = false,
    val scanResults: List<Alert>? = null,
    val error: String? = null,
)

class OnboardingViewModel(
    private val registerUseCase: RegisterUseCase,
    private val fullScanUseCase: FullScanUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setEmail(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, error = null) }
    }

    fun setPassword(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, error = null) }
    }

    fun nextStep() {
        val state = _uiState.value
        when (state.currentStep) {
            0 -> if (validateEmail()) _uiState.update { it.copy(currentStep = 1) }
            1 -> if (validatePassword()) submitRegistration()
        }
    }

    fun previousStep() {
        _uiState.update {
            if (it.currentStep > 0) it.copy(currentStep = it.currentStep - 1) else it
        }
    }

    private fun validateEmail(): Boolean {
        val email = _uiState.value.email.trim()
        return if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            _uiState.update { it.copy(emailError = "Please enter a valid email address") }
            false
        } else {
            true
        }
    }

    private fun validatePassword(): Boolean {
        val password = _uiState.value.password
        return if (password.length < 8) {
            _uiState.update { it.copy(passwordError = "Password must be at least 8 characters") }
            false
        } else {
            true
        }
    }

    private fun submitRegistration() {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            registerUseCase(_uiState.value.email.trim(), _uiState.value.password).fold(
                onSuccess = {
                    _uiState.update { it.copy(registrationComplete = true) }
                    runScan()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Registration failed",
                        )
                    }
                },
            )
        }
    }

    private suspend fun runScan() {
        fullScanUseCase(_uiState.value.email.trim()).first().fold(
            onSuccess = { alerts ->
                _uiState.update { it.copy(isLoading = false, scanResults = alerts) }
            },
            onFailure = {
                _uiState.update { it.copy(isLoading = false, scanResults = emptyList()) }
            },
        )
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
