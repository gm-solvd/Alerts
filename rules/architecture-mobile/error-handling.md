# Error Handling — Mobile (KMP)

## Strategy
Use Kotlin's `Result<T>` or a custom sealed `Either`-style wrapper in use cases. ViewModels catch errors and update `UiState` to an `Error` state — Composables never handle errors directly.

---

## Error Types (shared/domain)

```kotlin
sealed class AppError : Exception() {
    data class NetworkError(override val message: String) : AppError()
    data class NotFound(val resource: String) : AppError()
    data class Unauthorized(override val message: String = "Session expired") : AppError()
    data class ServerError(val code: Int, override val message: String) : AppError()
    object UnknownError : AppError()
}
```

---

## Use Case Pattern

```kotlin
class ScanBreachUseCase(private val scanRepository: ScanRepository) {

    suspend operator fun invoke(email: String): Result<List<Breach>> =
        runCatching { scanRepository.scanBreach(email) }
            .mapFailure { it.toAppError() }
}

// Extension to map generic exceptions to AppError
fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this
    is IOException -> AppError.NetworkError(message ?: "Network failure")
    else -> AppError.UnknownError
}
```

---

## ViewModel Error Handling

```kotlin
viewModelScope.launch {
    _uiState.value = UiState.Loading
    getAlerts()
        .onSuccess { alerts -> _uiState.value = UiState.Success(alerts) }
        .onFailure { error ->
            _uiState.value = when (error) {
                is AppError.Unauthorized -> UiState.SessionExpired
                else -> UiState.Error(error.message ?: "Something went wrong")
            }
        }
}
```

---

## Composable Error Display

- Show a non-blocking `Snackbar` for recoverable errors (network timeout, etc.)
- Show a full-screen error state with retry for critical load failures
- Never crash the UI — always have a fallback state

```kotlin
when (val state = uiState) {
    is UiState.Loading -> LoadingIndicator()
    is UiState.Success -> DashboardContent(state)
    is UiState.Error -> ErrorScreen(message = state.message, onRetry = viewModel::load)
    is UiState.SessionExpired -> navigateToLogin()
}
```

---

## Rules
- Use cases return `Result<T>` — never throw to the ViewModel
- ViewModels never propagate exceptions to Composables
- Network errors are always wrapped before leaving the data layer
- Show user-friendly messages — never expose raw exception messages in UI
