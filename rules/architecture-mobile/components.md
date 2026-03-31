# Components & State Management — Mobile (Compose)

## Component Rules

- **One responsibility per Composable** — if it needs more than ~100 lines, split it
- **Stateless by default** — receive state and callbacks as parameters; hoist state up
- **No ViewModel calls inside nested Composables** — only screen-level Composables inject ViewModels
- **No business logic in Composables** — pass lambdas for actions, let the ViewModel decide

---

## Component Hierarchy

```
Screen (injects ViewModel, owns UiState)
  └── Content (stateless, receives data + callbacks)
        ├── Card / ListItem (pure display)
        └── ActionButton (passes event up via lambda)
```

```kotlin
// Screen-level — only this knows about ViewModel
@Composable
fun AlertsScreen(viewModel: AlertsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AlertsContent(
        uiState = uiState,
        onResolve = viewModel::resolveAlert,
        onDismiss = viewModel::dismissAlert
    )
}

// Content — stateless, testable in isolation
@Composable
fun AlertsContent(
    uiState: AlertsUiState,
    onResolve: (UUID) -> Unit,
    onDismiss: (UUID) -> Unit
) { ... }
```

---

## State Hoisting
State lives in the ViewModel. Composables receive it as parameters and emit events via lambdas — never modify state internally unless it's purely UI state (e.g., expanded/collapsed, focus).

```kotlin
// ✅ UI-only state — OK to keep local
var expanded by remember { mutableStateOf(false) }

// ❌ Business state — must live in ViewModel
var alerts by remember { mutableStateOf(emptyList<Alert>()) }
```

---

## Preview Support
All content Composables must have a `@Preview`:

```kotlin
@Preview(showBackground = true)
@Composable
fun AlertCardPreview() {
    PrivacyAlertTheme {
        AlertCard(
            alert = Alert.preview(),
            onResolve = {},
            onDismiss = {}
        )
    }
}
```

Add a `preview()` factory to domain models for convenience:
```kotlin
fun Alert.Companion.preview() = Alert(
    id = UUID.randomUUID(),
    category = ThreatCategory.BREACH,
    severity = Severity.CRITICAL,
    title = "Adobe breach",
    description = "Your email was found in the 2013 Adobe data breach.",
    resolved = false,
    createdAt = Instant.now()
)
```

---

## Naming Conventions

| Type | Naming |
|---|---|
| Screen Composable | `AlertsScreen`, `DashboardScreen` |
| Content Composable | `AlertsContent`, `DashboardContent` |
| Reusable component | `AlertCard`, `SeverityBadge`, `ScoreGauge` |
| Preview | `AlertCardPreview` |
| UiState | `AlertsUiState` (sealed class) |
