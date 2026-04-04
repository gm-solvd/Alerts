---
paths:
  - "mobile/**/*Test.kt"
  - "mobile/**/test/**"
---

# Testing — Mobile (KMP)

## Test Pyramid

- **Repository tests**: Ktor MockEngine for HTTP, mock responses and status codes
- **Use case tests**: Pure Kotlin, MockK, `runTest` coroutine scope
- **ViewModel tests**: `MainCoroutineRule`, Turbine for Flow assertions, test UiState transitions
- **UI tests**: Compose UI Test, fake ViewModel with pre-set state

## Tools

| Tool | Purpose |
|------|---------|
| MockK | Mocking |
| Turbine | Flow testing |
| kotlinx-coroutines-test | Coroutine test scope |
| Ktor MockEngine | HTTP mock |
| Compose UI Test | UI assertions |

## Naming

`` `<method> <condition> <expected outcome>` `` using backtick-quoted method names.

## Coverage

- Test success + failure paths for every use case
- Test UiState transitions (Loading → Success, Loading → Error)
- At least one unhappy path per ViewModel

Full reference: `rules/architecture-mobile/testing.md`
