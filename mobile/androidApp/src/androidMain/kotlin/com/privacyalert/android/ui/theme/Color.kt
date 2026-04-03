package com.privacyalert.android.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Core palette — Ruuster-inspired deep navy + warm gradient accents
object PrivacyAlertColors {
    // Backgrounds
    val DeepNavy = Color(0xFF0F1729)
    val NavySurface = Color(0xFF1A2332)
    val NavySurfaceVariant = Color(0xFF243044)

    // Primary — Orange
    val Primary = Color(0xFFF97316)
    val PrimaryContainer = Color(0xFF3D1D00)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnPrimaryContainer = Color(0xFFFFB86C)

    // Secondary — Indigo
    val Secondary = Color(0xFF5B6ABF)
    val SecondaryContainer = Color(0xFF1E2254)
    val OnSecondary = Color(0xFFFFFFFF)
    val OnSecondaryContainer = Color(0xFFB8C1FF)

    // Tertiary — Coral
    val Tertiary = Color(0xFFE84B6A)
    val TertiaryContainer = Color(0xFF3D0F1B)
    val OnTertiary = Color(0xFFFFFFFF)
    val OnTertiaryContainer = Color(0xFFFFB3C1)

    // Accent
    val AccentWarm = Color(0xFFFBBF24)

    // Error
    val Error = Color(0xFFF44336)
    val ErrorContainer = Color(0xFF3D0E0E)
    val OnError = Color(0xFFFFFFFF)
    val OnErrorContainer = Color(0xFFFFB4AB)

    // Neutrals — Dark
    val OnBackground = Color(0xFFE8ECF2)
    val OnSurface = Color(0xFFE8ECF2)
    val OnSurfaceVariant = Color(0xFF8E95A3)
    val Outline = Color(0xFF3A4255)
    val OutlineVariant = Color(0xFF2A3140)
    val InverseSurface = Color(0xFFE8ECF2)
    val InverseOnSurface = Color(0xFF0F1729)
    val InversePrimary = Color(0xFFBB5A0F)
    val Scrim = Color(0xFF000000)

    // Neutrals — Light
    val LightBackground = Color(0xFFFAFBFE)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceVariant = Color(0xFFE8ECF2)
    val LightOnBackground = Color(0xFF0F1729)
    val LightOnSurface = Color(0xFF0F1729)
    val LightOnSurfaceVariant = Color(0xFF4A5568)
    val LightOutline = Color(0xFFCBD2DE)
    val LightOutlineVariant = Color(0xFFE2E8F0)
}

val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = PrivacyAlertColors.Primary,
    onPrimary = PrivacyAlertColors.OnPrimary,
    primaryContainer = PrivacyAlertColors.PrimaryContainer,
    onPrimaryContainer = PrivacyAlertColors.OnPrimaryContainer,
    secondary = PrivacyAlertColors.Secondary,
    onSecondary = PrivacyAlertColors.OnSecondary,
    secondaryContainer = PrivacyAlertColors.SecondaryContainer,
    onSecondaryContainer = PrivacyAlertColors.OnSecondaryContainer,
    tertiary = PrivacyAlertColors.Tertiary,
    onTertiary = PrivacyAlertColors.OnTertiary,
    tertiaryContainer = PrivacyAlertColors.TertiaryContainer,
    onTertiaryContainer = PrivacyAlertColors.OnTertiaryContainer,
    error = PrivacyAlertColors.Error,
    onError = PrivacyAlertColors.OnError,
    errorContainer = PrivacyAlertColors.ErrorContainer,
    onErrorContainer = PrivacyAlertColors.OnErrorContainer,
    background = PrivacyAlertColors.DeepNavy,
    onBackground = PrivacyAlertColors.OnBackground,
    surface = PrivacyAlertColors.NavySurface,
    onSurface = PrivacyAlertColors.OnSurface,
    surfaceVariant = PrivacyAlertColors.NavySurfaceVariant,
    onSurfaceVariant = PrivacyAlertColors.OnSurfaceVariant,
    outline = PrivacyAlertColors.Outline,
    outlineVariant = PrivacyAlertColors.OutlineVariant,
    inverseSurface = PrivacyAlertColors.InverseSurface,
    inverseOnSurface = PrivacyAlertColors.InverseOnSurface,
    inversePrimary = PrivacyAlertColors.InversePrimary,
    scrim = PrivacyAlertColors.Scrim,
)

val LightColorScheme: ColorScheme = lightColorScheme(
    primary = PrivacyAlertColors.Primary,
    onPrimary = PrivacyAlertColors.OnPrimary,
    primaryContainer = Color(0xFFFFDCC2),
    onPrimaryContainer = Color(0xFF3D1D00),
    secondary = PrivacyAlertColors.Secondary,
    onSecondary = PrivacyAlertColors.OnSecondary,
    secondaryContainer = Color(0xFFDDE1FF),
    onSecondaryContainer = Color(0xFF1E2254),
    tertiary = PrivacyAlertColors.Tertiary,
    onTertiary = PrivacyAlertColors.OnTertiary,
    tertiaryContainer = Color(0xFFFFD9DF),
    onTertiaryContainer = Color(0xFF3D0F1B),
    error = PrivacyAlertColors.Error,
    onError = PrivacyAlertColors.OnError,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = PrivacyAlertColors.LightBackground,
    onBackground = PrivacyAlertColors.LightOnBackground,
    surface = PrivacyAlertColors.LightSurface,
    onSurface = PrivacyAlertColors.LightOnSurface,
    surfaceVariant = PrivacyAlertColors.LightSurfaceVariant,
    onSurfaceVariant = PrivacyAlertColors.LightOnSurfaceVariant,
    outline = PrivacyAlertColors.LightOutline,
    outlineVariant = PrivacyAlertColors.LightOutlineVariant,
    inverseSurface = PrivacyAlertColors.DeepNavy,
    inverseOnSurface = PrivacyAlertColors.OnBackground,
    inversePrimary = PrivacyAlertColors.OnPrimaryContainer,
    scrim = PrivacyAlertColors.Scrim,
)
