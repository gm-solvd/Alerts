package com.privacyalert.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.model.Severity
import com.privacyalert.domain.usecase.alert.GetAlertsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

sealed class AlertsUiState {
    data object Loading : AlertsUiState()
    data class Success(
        val alerts: List<Alert>,
        val hasMore: Boolean,
        val selectedSeverity: Severity? = null,
    ) : AlertsUiState()
    data class Error(val message: String) : AlertsUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class AlertsViewModel(
    private val getAlertsUseCase: GetAlertsUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow<AlertsUiState>(AlertsUiState.Loading)
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    private val _selectedSeverity = MutableStateFlow<Severity?>(null)
    private val _refreshTrigger = MutableStateFlow(0)
    private val _currentPage = MutableStateFlow(0)
    private var accumulatedAlerts = mutableListOf<Alert>()

    init {
        combine(_selectedSeverity, _refreshTrigger) { severity, _ -> severity }
            .flatMapLatest { severity ->
                accumulatedAlerts = mutableListOf()
                _currentPage.value = 0
                _uiState.value = AlertsUiState.Loading
                _currentPage.flatMapLatest { page ->
                    getAlertsUseCase(severity = severity, page = page)
                }
            }
            .onEach { result ->
                result.fold(
                    onSuccess = { pageResult ->
                        if (_currentPage.value > 0 && accumulatedAlerts.isNotEmpty()) {
                            accumulatedAlerts.addAll(pageResult.content)
                        } else {
                            accumulatedAlerts = pageResult.content.toMutableList()
                        }
                        _uiState.value = AlertsUiState.Success(
                            alerts = accumulatedAlerts.toList(),
                            hasMore = _currentPage.value < pageResult.totalPages - 1,
                            selectedSeverity = _selectedSeverity.value,
                        )
                    },
                    onFailure = {
                        _uiState.value = AlertsUiState.Error(it.message ?: "Failed to load alerts")
                    },
                )
            }
            .launchIn(screenModelScope)
    }

    fun loadAlerts() {
        accumulatedAlerts = mutableListOf()
        _currentPage.value = 0
        _refreshTrigger.value++
    }

    fun loadMore() {
        _currentPage.value++
    }

    fun filterBySeverity(severity: Severity?) {
        _selectedSeverity.value = severity
    }
}
