package com.privacyalert.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Mitigation
import com.privacyalert.domain.usecase.alert.GetAlertDetailUseCase
import com.privacyalert.domain.usecase.alert.ResolveAlertUseCase
import com.privacyalert.domain.usecase.mitigation.GetMitigationsByAlertUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AlertDetailUiState {
    data object Loading : AlertDetailUiState()
    data class Success(
        val alert: Alert,
        val mitigations: List<Mitigation>,
    ) : AlertDetailUiState()
    data class Error(val message: String) : AlertDetailUiState()
}

class AlertDetailViewModel(
    private val alertId: String,
    private val getAlertDetailUseCase: GetAlertDetailUseCase,
    private val resolveAlertUseCase: ResolveAlertUseCase,
    private val getMitigationsByAlertUseCase: GetMitigationsByAlertUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow<AlertDetailUiState>(AlertDetailUiState.Loading)
    val uiState: StateFlow<AlertDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        screenModelScope.launch {
            _uiState.value = AlertDetailUiState.Loading
            getAlertDetailUseCase(alertId).fold(
                onSuccess = { alert ->
                    val mitigations = getMitigationsByAlertUseCase(alertId)
                        .getOrDefault(emptyList())
                    _uiState.value = AlertDetailUiState.Success(alert, mitigations)
                },
                onFailure = {
                    _uiState.value = AlertDetailUiState.Error(
                        it.message ?: "Failed to load alert",
                    )
                },
            )
        }
    }

    fun resolve() {
        screenModelScope.launch {
            resolveAlertUseCase(alertId).fold(
                onSuccess = { load() },
                onFailure = { /* Keep current state, could show snackbar */ },
            )
        }
    }
}
