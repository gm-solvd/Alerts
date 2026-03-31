# Styling — Mobile (Material Expressive + Compose)

## Design System
**Material3 Expressive** — the evolution of Material You with more expressive motion, shapes, and color roles.

---

## Theme Setup

```kotlin
// shared or androidApp/theme/Theme.kt
@Composable
fun PrivacyAlertTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme(...) else lightColorScheme(...)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = PrivacyAlertTypography,
        shapes = PrivacyAlertShapes,
        content = content
    )
}
```

---

## Color Roles (Severity Mapping)

Map threat severity to Material color roles:

| Severity | Color Role | Usage |
|---|---|---|
| CRITICAL | `error` / `errorContainer` | Breach alerts, critical badges |
| HIGH | Custom `warning` (orange) | High-risk warnings |
| MEDIUM | `tertiary` | Medium-risk items |
| INFO | `secondary` | Informational alerts |
| Resolved | `surfaceVariant` + muted | Resolved/dismissed items |

```kotlin
fun Severity.toContainerColor(colorScheme: ColorScheme) = when (this) {
    Severity.CRITICAL -> colorScheme.errorContainer
    Severity.HIGH     -> Color(0xFFFF6D00)   // custom warning orange
    Severity.MEDIUM   -> colorScheme.tertiaryContainer
    Severity.INFO     -> colorScheme.secondaryContainer
}
```

---

## Typography
Use Material3's type scale — don't create custom text styles unless necessary:

| Use case | Token |
|---|---|
| Screen title | `titleLarge` |
| Card title | `titleMedium` |
| Body / description | `bodyMedium` |
| Badge / label | `labelSmall` |
| Score number | `displayLarge` (bold) |

---

## Shapes (Material Expressive)
Material Expressive uses more rounded, expressive shapes:

```kotlin
val PrivacyAlertShapes = Shapes(
    small  = RoundedCornerShape(12.dp),   // chips, badges
    medium = RoundedCornerShape(16.dp),   // cards
    large  = RoundedCornerShape(24.dp),   // bottom sheets, dialogs
)
```

---

## Motion
Use Material3 motion specs for transitions:
- Screen transitions: `SharedAxisTransition` (horizontal for nav, vertical for drill-down)
- Alert appear: `FadeThrough`
- Score gauge: animate value changes with `animateFloatAsState`

---

## Rules
- Never hardcode colors — always use `MaterialTheme.colorScheme.*`
- Never hardcode `sp` for text sizes — use `MaterialTheme.typography.*`
- Support dark mode from day one — test every screen in both modes
- Use `dp` for spacing, never `px`
- Define spacing constants to avoid magic numbers:
  ```kotlin
  object Spacing {
      val xs = 4.dp
      val sm = 8.dp
      val md = 16.dp
      val lg = 24.dp
      val xl = 32.dp
  }
  ```
