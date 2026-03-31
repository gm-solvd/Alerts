# Clean Architecture — Mobile (KMP)

## Layer Overview

```
┌──────────────────────────────────┐
│     presentation (ViewModels)    │  ← MVVM, Compose observes UiState
├──────────────────────────────────┤
│     domain (UseCases, Models)    │  ← Pure Kotlin, no framework deps
├──────────────────────────────────┤
│     data (Repositories, Sources) │  ← Ktor, SQLDelight, platform APIs
└──────────────────────────────────┘
```

**Dependency rule**: Presentation → Domain ← Data. Domain knows nothing about Ktor, SQLDelight, or Compose.

---

## Module Structure (shared/)

```
shared/
├── domain/
│   ├── model/          # Pure Kotlin data classes
│   │   ├── Alert.kt
│   │   ├── ThreatCategory.kt     # enum
│   │   ├── Severity.kt           # enum: INFO, MEDIUM, HIGH, CRITICAL
│   │   ├── ExposureScore.kt
│   │   └── User.kt
│   ├── repository/     # Interfaces only
│   │   ├── AlertRepository.kt
│   │   ├── UserRepository.kt
│   │   └── ScanRepository.kt
│   └── usecase/        # One class, one public function
│       ├── GetAlertsUseCase.kt
│       ├── GetExposureScoreUseCase.kt
│       ├── ScanBreachUseCase.kt
│       ├── AuditPermissionsUseCase.kt
│       ├── ResolveAlertUseCase.kt
│       └── GetMitigationsUseCase.kt
├── data/
│   ├── remote/
│   │   ├── ApiClient.kt          # Ktor HttpClient config
│   │   ├── AlertApi.kt
│   │   ├── AuthApi.kt
│   │   └── dto/
│   ├── local/
│   │   ├── PrivacyAlert.sq       # SQLDelight queries
│   │   └── LocalAlertDataSource.kt
│   └── repository/               # Implements domain interfaces
│       ├── AlertRepositoryImpl.kt
│       ├── UserRepositoryImpl.kt
│       └── ScanRepositoryImpl.kt
└── presentation/
    └── viewmodel/
        ├── DashboardViewModel.kt
        ├── AlertsViewModel.kt
        ├── ScanViewModel.kt
        └── MitigationsViewModel.kt
```

---

## Rules

### Domain layer
- No Ktor, SQLDelight, Koin, or Compose imports
- Use cases have a single `invoke` operator or named `execute()` function
- Repository interfaces return domain models, not DTOs or DB types

### Data layer
- DTOs are mapped to domain models before leaving this layer
- SQLDelight-generated types stay inside `local/`
- Repository implementations are the only classes that know about both local and remote sources

### Presentation layer
- ViewModels expose a single `uiState: StateFlow<UiState>` per screen
- No business logic in ViewModels — delegate to use cases
- No repository calls from ViewModels — always go through a use case

---

## UiState Pattern

```kotlin
sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val score: ExposureScore,
        val topAlerts: List<Alert>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(
    private val getExposureScore: GetExposureScoreUseCase,
    private val getAlerts: GetAlertsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = Loading
            runCatching {
                val score = getExposureScore()
                val alerts = getAlerts(limit = 3)
                Success(score, alerts)
            }.fold(
                onSuccess = { _uiState.value = it },
                onFailure = { _uiState.value = Error(it.message ?: "Unknown error") }
            )
        }
    }
}
```
