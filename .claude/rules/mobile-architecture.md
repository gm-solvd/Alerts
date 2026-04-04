---
paths:
  - "mobile/**/*.kt"
---

# Mobile Architecture — KMP

Clean Architecture with 3 layers. Domain has zero Ktor/SQLDelight/Koin/Compose imports.

```
presentation (ViewModels, MVVM) → domain (UseCases, Models) ← data (Repositories, Sources)
```

## Module Structure (shared/)

```
shared/
├── domain/
│   ├── model/          # Pure Kotlin data classes
│   ├── repository/     # Interfaces only
│   └── usecase/        # Single-responsibility, invoke() operator
├── data/
│   ├── remote/         # Ktor HTTP client, API service
│   ├── local/          # SQLDelight, DataStore
│   └── repository/     # Implements domain interfaces
└── presentation/
    └── viewmodel/      # MVVM, exposes StateFlow<UiState>
```

## Layer Rules

- **Domain**: Pure Kotlin. Use cases have single `operator fun invoke()`. Return `Result<T>`, never throw.
- **Data**: Ktor + SQLDelight. Repositories implement domain interfaces. Map API/DB models to domain models.
- **Presentation**: ViewModels expose `StateFlow<UiState>`. UiState is sealed class (Loading, Success, Error).

## UiState Pattern

```kotlin
sealed class AlertsUiState {
    data object Loading : AlertsUiState()
    data class Success(val alerts: List<Alert>) : AlertsUiState()
    data class Error(val message: String) : AlertsUiState()
}
```

## DI (Koin)

- `factory` for use cases, `single` for stateful objects (HTTP client, DB, repos), `viewModel` for ViewModels
- `expect/actual` for platform-specific deps (SQLDelight driver)
- Never call `get()` service locator in business logic

Full reference: `rules/architecture-mobile/`
