package com.privacyalert.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.privacyalert.domain.model.Mitigation
import com.privacyalert.domain.usecase.mitigation.CompleteMitigationUseCase
import com.privacyalert.domain.usecase.mitigation.GetMitigationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MitigationsUiState {
    data object Loading : MitigationsUiState()
    data class Success(
        val incomplete: List<Mitigation>,
        val completed: List<Mitigation>,
    ) : MitigationsUiState()
    data class Error(val message: String) : MitigationsUiState()
}

class MitigationsViewModel(
    private val getMitigationsUseCase: GetMitigationsUseCase,
    private val completeMitigationUseCase: CompleteMitigationUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow<MitigationsUiState>(MitigationsUiState.Loading)
    val uiState: StateFlow<MitigationsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        screenModelScope.launch {
            _uiState.value = MitigationsUiState.Loading
            getMitigationsUseCase().fold(
                onSuccess = { mitigations ->
                    _uiState.value = MitigationsUiState.Success(
                        incomplete = mitigations.filter { !it.completed },
                        completed = mitigations.filter { it.completed },
                    )
                },
                onFailure = {
                    _uiState.value = MitigationsUiState.Error(
                        it.message ?: "Failed to load mitigations",
                    )
                },
            )
        }
    }

    fun completeMitigation(id: String) {
        screenModelScope.launch {
            completeMitigationUseCase(id).fold(
                onSuccess = { load() },
                onFailure = { /* Keep current state */ },
            )
        }
    }
}
