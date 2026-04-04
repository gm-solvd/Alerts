package com.privacyalert.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.privacyalert.data.local.TokenStorage
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.ScoreRecord
import com.privacyalert.domain.usecase.alert.GetAlertsUseCase
import com.privacyalert.domain.usecase.mitigation.CompleteMitigationUseCase
import com.privacyalert.domain.usecase.mitigation.GetMitigationsUseCase
import com.privacyalert.domain.usecase.scan.FullScanUseCase
import com.privacyalert.domain.usecase.score.GetScoreUseCase
import com.privacyalert.presentation.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class ActionState {
    data object Idle : ActionState()
    data object Scanning : ActionState()
    data object Fixing : ActionState()
    data class ScanComplete(val newAlerts: Int) : ActionState()
    data class FixComplete(val resolvedCount: Int) : ActionState()
    data class ActionError(val message: String) : ActionState()
}

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(
        val score: ScoreRecord,
        val topAlerts: List<Alert>,
        val actionState: ActionState = ActionState.Idle,
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(
    private val getScoreUseCase: GetScoreUseCase,
    private val getAlertsUseCase: GetAlertsUseCase,
    private val fullScanUseCase: FullScanUseCase,
    private val getMitigationsUseCase: GetMitigationsUseCase,
    private val completeMitigationUseCase: CompleteMitigationUseCase,
    private val tokenStorage: TokenStorage,
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

    fun startScan() {
        val current = _uiState.value
        if (current !is DashboardUiState.Success) return

        _uiState.value = current.copy(actionState = ActionState.Scanning)

        screenModelScope.launch {
            val email = tokenStorage.getEmail()
            if (email == null) {
                _uiState.value = current.copy(
                    actionState = ActionState.ActionError("No email found. Please log in again."),
                )
                return@launch
            }

            fullScanUseCase(email).first().fold(
                onSuccess = { alerts ->
                    _uiState.value = current.copy(
                        actionState = ActionState.ScanComplete(newAlerts = alerts.size),
                    )
                },
                onFailure = {
                    _uiState.value = current.copy(
                        actionState = ActionState.ActionError(it.toUserMessage()),
                    )
                },
            )
        }
    }

    fun startFixAll() {
        val current = _uiState.value
        if (current !is DashboardUiState.Success) return

        _uiState.value = current.copy(actionState = ActionState.Fixing)

        screenModelScope.launch {
            getMitigationsUseCase().first().fold(
                onSuccess = { mitigations ->
                    val incomplete = mitigations.filter { !it.completed }
                    var resolvedCount = 0
                    for (mitigation in incomplete) {
                        completeMitigationUseCase(mitigation.id).fold(
                            onSuccess = { resolvedCount++ },
                            onFailure = { /* continue with next */ },
                        )
                    }
                    _uiState.value = current.copy(
                        actionState = ActionState.FixComplete(resolvedCount = resolvedCount),
                    )
                },
                onFailure = {
                    _uiState.value = current.copy(
                        actionState = ActionState.ActionError(it.toUserMessage()),
                    )
                },
            )
        }
    }

    fun dismissAction() {
        val current = _uiState.value
        if (current is DashboardUiState.Success) {
            _uiState.value = current.copy(actionState = ActionState.Idle)
        }
        load()
    }
}
