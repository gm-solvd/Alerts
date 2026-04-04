---
paths:
  - "mobile/**/error/**"
  - "mobile/**/*Error*"
  - "mobile/**/*Exception*"
  - "mobile/**/Result*"
---

# Error Handling — Mobile

## Error Types

`AppError` sealed class:
- `NetworkError` — no internet, timeout
- `NotFound` — 404 from API
- `Unauthorized` — 401, session expired
- `ServerError` — 5xx from API
- `UnknownError` — catch-all

## Rules

- Use cases return `Result<T>` — never throw to ViewModel
- ViewModels catch errors and map to `UiState.Error`
- Handle `Unauthorized` as session expiration → navigate to login
- Composables never handle errors directly — only display UiState.Error
- Snackbar for recoverable errors, full-screen error with retry for critical failures
- Wrap all network exceptions in data layer, map to `AppError`

Full reference: `rules/architecture-mobile/error-handling.md`
