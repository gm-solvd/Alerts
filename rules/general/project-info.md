# Project Info

## What It Is
A mobile-first app that monitors the user's digital exposure, scores their privacy health, and sends categorized threat alerts with actionable mitigation steps. Targets the general public.

---

## Monorepo Structure

```
privacy-alert-system/
├── mobile/                   # KMP + CMP application
│   ├── shared/               # Shared Kotlin (domain + data + viewmodels)
│   ├── androidApp/           # Android entry point
│   └── iosApp/               # iOS entry point
├── backend/                  # Kotlin + Spring Boot API
└── rules/                    # Project documentation & conventions
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Mobile UI | Compose Multiplatform + Material3 Expressive |
| Mobile shared logic | Kotlin Multiplatform (KMP) |
| Mobile architecture | MVVM + Clean Architecture |
| Mobile DI | Koin |
| Mobile networking | Ktor Client |
| Mobile local DB | SQLDelight |
| Mobile async | Kotlinx Coroutines + Flow |
| Backend | Kotlin + Spring Boot 3.x |
| Backend DB | PostgreSQL |
| Backend ORM | Spring Data JPA + Hibernate |
| Backend auth | Spring Security — JWT + OAuth2 (Google/Apple) |

---

## Key Patterns
- **MVVM** on the presentation layer (shared ViewModels via KMP)
- **Clean Architecture** on both mobile and backend — domain has zero framework dependencies
- **SOLID** principles throughout
- **Repository pattern** to abstract data sources
- Unidirectional data flow: ViewModel exposes `UiState`, Composables observe it

---

## Threat Categories

| Category | Severity |
|---|---|
| Data breach | Critical |
| Network vulnerability | High |
| Identity exposure | High |
| App overpermissions | Medium |
| Tracker exposure | Medium |
| Social footprint | Low–Medium |
| Device hygiene | Medium |

---

## MVP Scope
1. Dashboard — exposure score + top active risks
2. Alerts feed — categorized, severity-badged
3. Breach scan — via HaveIBeenPwned
4. Permission audit — device-side
5. Fix It list — prioritized mitigations
6. Auth — email/password + Google/Apple

> Push notifications: deferred to post-MVP.
