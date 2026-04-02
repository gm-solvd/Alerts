package com.privacyalert.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.ScoreRecord
import com.privacyalert.domain.usecase.alert.GetAlertsUseCase
import com.privacyalert.domain.usecase.score.GetScoreUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(
        val score: ScoreRecord,
        val topAlerts: List<Alert>,
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(
    private val getScoreUseCase: GetScoreUseCase,
    private val getAlertsUseCase: GetAlertsUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        screenModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            val scoreResult = getScoreUseCase()
            val alertsResult = getAlertsUseCase(page = 0, size = 3)

            scoreResult.fold(
                onSuccess = { score ->
                    val alerts = alertsResult.getOrNull()?.content ?: emptyList()
                    _uiState.value = DashboardUiState.Success(score, alerts)
                },
                onFailure = {
                    _uiState.value = DashboardUiState.Error(it.message ?: "Failed to load dashboard")
                },
            )
        }
    }
}
