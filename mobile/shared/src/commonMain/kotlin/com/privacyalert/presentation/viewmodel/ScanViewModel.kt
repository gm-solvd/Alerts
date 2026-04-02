package com.privacyalert.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.privacyalert.domain.model.Alert
import com.privacyalert.domain.usecase.scan.FullScanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScanUiState {
    data object Idle : ScanUiState()
    data object Scanning : ScanUiState()
    data class Success(val alerts: List<Alert>) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}

class ScanViewModel(
    private val fullScanUseCase: FullScanUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun startFullScan(email: String) {
        screenModelScope.launch {
            _uiState.value = ScanUiState.Scanning
            fullScanUseCase(email).fold(
                onSuccess = { _uiState.value = ScanUiState.Success(it) },
                onFailure = {
                    _uiState.value = ScanUiState.Error(it.message ?: "Scan failed")
                },
            )
        }
    }

    fun reset() {
        _uiState.value = ScanUiState.Idle
    }
}
