package com.privacyalert.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.usecase.alert.GetAlertsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AlertsUiState {
    data object Loading : AlertsUiState()
    data class Success(
        val alerts: List<Alert>,
        val hasMore: Boolean,
        val selectedSeverity: Severity? = null,
    ) : AlertsUiState()
    data class Error(val message: String) : AlertsUiState()
}

class AlertsViewModel(
    private val getAlertsUseCase: GetAlertsUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow<AlertsUiState>(AlertsUiState.Loading)
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private var currentAlerts = mutableListOf<Alert>()
    private var selectedSeverity: Severity? = null

    init {
        loadAlerts()
    }

    fun loadAlerts() {
        currentPage = 0
        currentAlerts.clear()
        fetchAlerts()
    }

    fun loadMore() {
        currentPage++
        fetchAlerts(append = true)
    }

    fun filterBySeverity(severity: Severity?) {
        selectedSeverity = severity
        loadAlerts()
    }

    private fun fetchAlerts(append: Boolean = false) {
        screenModelScope.launch {
            if (!append) _uiState.value = AlertsUiState.Loading

            getAlertsUseCase(severity = selectedSeverity, page = currentPage).fold(
                onSuccess = { result ->
                    if (append) {
                        currentAlerts.addAll(result.content)
                    } else {
                        currentAlerts = result.content.toMutableList()
                    }
                    _uiState.value = AlertsUiState.Success(
                        alerts = currentAlerts.toList(),
                        hasMore = currentPage < result.totalPages - 1,
                        selectedSeverity = selectedSeverity,
                    )
                },
                onFailure = {
                    _uiState.value = AlertsUiState.Error(it.message ?: "Failed to load alerts")
                },
            )
        }
    }
}
