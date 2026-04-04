package com.privacyalert.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.ScoreRecord
import com.privacyalert.domain.usecase.alert.GetAlertsUseCase
import com.privacyalert.domain.usecase.score.GetScoreUseCase
import com.privacyalert.presentation.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
        _uiState.value = DashboardUiState.Loading
        combine(
            getScoreUseCase(),
            getAlertsUseCase(page = 0, size = 3),
        ) { scoreResult, alertsResult ->
            scoreResult.fold(
                onSuccess = { score ->
                    DashboardUiState.Success(
                        score = score,
                        topAlerts = alertsResult.getOrNull()?.content ?: emptyList(),
                    )
                },
                onFailure = {
                    DashboardUiState.Error(it.toUserMessage())
                },
            )
        }
            .onEach { _uiState.value = it }
            .launchIn(screenModelScope)
    }
}
