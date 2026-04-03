package com.privacyalert.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.privacyalert.domain.model.Mitigation
import com.privacyalert.domain.usecase.mitigation.CompleteMitigationUseCase
import com.privacyalert.domain.usecase.mitigation.GetMitigationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class MitigationsUiState {
    data object Loading : MitigationsUiState()
    data class Success(
        val incomplete: List<Mitigation>,
        val completed: List<Mitigation>,
    ) : MitigationsUiState()
    data class Error(val message: String) : MitigationsUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class MitigationsViewModel(
    private val getMitigationsUseCase: GetMitigationsUseCase,
    private val completeMitigationUseCase: CompleteMitigationUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow<MitigationsUiState>(MitigationsUiState.Loading)
    val uiState: StateFlow<MitigationsUiState> = _uiState.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)

    init {
        _refreshTrigger
            .flatMapLatest { getMitigationsUseCase() }
            .onEach { result ->
                _uiState.value = result.fold(
                    onSuccess = { mitigations ->
                        MitigationsUiState.Success(
                            incomplete = mitigations.filter { !it.completed },
                            completed = mitigations.filter { it.completed },
                        )
                    },
                    onFailure = {
                        MitigationsUiState.Error(it.message ?: "Failed to load mitigations")
                    },
                )
            }
            .launchIn(screenModelScope)
    }

    fun load() {
        _uiState.value = MitigationsUiState.Loading
        _refreshTrigger.value++
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
