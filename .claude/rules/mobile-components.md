---
paths:
  - "mobile/**/presentation/**"
  - "mobile/**/ui/**"
  - "mobile/**/*Screen*"
  - "mobile/**/*Content*"
---

# Compose Components — Mobile

## Component Hierarchy

- **Screen** (`AlertsScreen`): Injects ViewModel via Koin, collects UiState, delegates to Content
- **Content** (`AlertsContent`): Stateless, receives data + callbacks as params
- **Component** (`AlertCard`): Pure display, reusable

## Rules

- One responsibility per Composable (~100 lines max)
- No ViewModel calls in nested Composables — only Screen level
- No business logic in Composables — delegate to ViewModel
- State hoisting: state lives in ViewModel (via StateFlow), Composables are stateless receivers
- Use `collectAsStateWithLifecycle()` (not `collectAsState()`)

## Preview Support

- All Content Composables must have `@Preview`
- Domain models have `.preview()` factory functions for preview data

## Naming

- Screen: `AlertsScreen`, Content: `AlertsContent`, Component: `AlertCard`, UiState: `AlertsUiState`

## Navigation (Voyager)

- Each screen is a Voyager `Screen` object with `Content()` composable
- Pass IDs only in navigation, load full data in destination ViewModel
- Auth guard checks token at startup, navigates to AuthFlow if missing
- MainFlow has 4 tabs: Dashboard, Alerts, Scan, Fix It

## Styling (Material 3 Expressive)

- Color mapping: Critical→error, High→warning (orange), Medium→tertiary, Info→secondary
- Typography: Material3 scale (titleLarge, bodyMedium, labelSmall)
- Shapes: small=12.dp, medium=16.dp, large=24.dp rounded corners
- No hardcoded colors/sizes/fonts. Support dark mode from day one. Use `dp` not `px`.

Full reference: `rules/architecture-mobile/components.md`, `styling.md`, `navigation.md`
