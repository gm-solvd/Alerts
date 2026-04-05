package com.privacyalert.presentation.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Swiss-clean design palette derived from the Pencil design system.
 *
 * Light mode: white backgrounds, blue (#2563EB) primary.
 * Dark mode: slate backgrounds, lighter blue (#3B82F6) primary.
 */
object AppColors {
    // Primary — Blue
    val BluePrimary = Color(0xFF2563EB)
    val BluePrimaryDark = Color(0xFF3B82F6)
    val BluePrimaryContainer = Color(0xFFDBEAFE)
    val BluePrimaryContainerDark = Color(0xFF1E3A5F)
    val OnBluePrimary = Color(0xFFFFFFFF)
    val OnBluePrimaryContainer = Color(0xFF1E40AF)
    val OnBluePrimaryContainerDark = Color(0xFFBFDBFE)

    // Secondary — Slate
    val Secondary = Color(0xFF64748B)
    val SecondaryDark = Color(0xFF94A3B8)
    val SecondaryContainer = Color(0xFFF1F5F9)
    val SecondaryContainerDark = Color(0xFF334155)
    val OnSecondary = Color(0xFFFFFFFF)
    val OnSecondaryContainer = Color(0xFF1E293B)
    val OnSecondaryContainerDark = Color(0xFFE2E8F0)

    // Tertiary — Violet
    val Tertiary = Color(0xFF7C3AED)
    val TertiaryDark = Color(0xFFA78BFA)
    val TertiaryContainer = Color(0xFFEDE9FE)
    val TertiaryContainerDark = Color(0xFF3B1D6E)
    val OnTertiary = Color(0xFFFFFFFF)
    val OnTertiaryContainer = Color(0xFF4C1D95)
    val OnTertiaryContainerDark = Color(0xFFDDD6FE)

    // Error — Red
    val Error = Color(0xFFEF4444)
    val ErrorDark = Color(0xFFFCA5A5)
    val ErrorContainer = Color(0xFFFEE2E2)
    val ErrorContainerDark = Color(0xFF7F1D1D)
    val OnError = Color(0xFFFFFFFF)
    val OnErrorContainer = Color(0xFF991B1B)
    val OnErrorContainerDark = Color(0xFFFECACA)

    // Semantic — used directly in components
    val Success = Color(0xFF16A34A)
    val SuccessDark = Color(0xFF4ADE80)
    val Warning = Color(0xFFF59E0B)
    val WarningDark = Color(0xFFFBBF24)
    val FixGreen = Color(0xFF16A34A)

    // Severity mapping
    val SeverityCritical = Error
    val SeverityHigh = Color(0xFFF97316)  // orange
    val SeverityMedium = Warning
    val SeverityLow = Color(0xFF3B82F6)   // blue
    val SeverityInfo = Secondary

    // Light neutrals
    val LightBackground = Color(0xFFFFFFFF)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceVariant = Color(0xFFF1F5F9)
    val LightOnBackground = Color(0xFF0F172A)
    val LightOnSurface = Color(0xFF0F172A)
    val LightOnSurfaceVariant = Color(0xFF64748B)
    val LightOutline = Color(0xFFCBD5E1)
    val LightOutlineVariant = Color(0xFFE2E8F0)
    val LightInverseSurface = Color(0xFF1E293B)
    val LightInverseOnSurface = Color(0xFFF1F5F9)

    // Dark neutrals
    val DarkBackground = Color(0xFF0F172A)
    val DarkSurface = Color(0xFF1E293B)
    val DarkSurfaceVariant = Color(0xFF334155)
    val DarkOnBackground = Color(0xFFF1F5F9)
    val DarkOnSurface = Color(0xFFF1F5F9)
    val DarkOnSurfaceVariant = Color(0xFF94A3B8)
    val DarkOutline = Color(0xFF475569)
    val DarkOutlineVariant = Color(0xFF334155)
    val DarkInverseSurface = Color(0xFFF1F5F9)
    val DarkInverseOnSurface = Color(0xFF0F172A)

    val Scrim = Color(0xFF000000)
}

val LightColorScheme: ColorScheme = lightColorScheme(
    primary = AppColors.BluePrimary,
    onPrimary = AppColors.OnBluePrimary,
    primaryContainer = AppColors.BluePrimaryContainer,
    onPrimaryContainer = AppColors.OnBluePrimaryContainer,
    secondary = AppColors.Secondary,
    onSecondary = AppColors.OnSecondary,
    secondaryContainer = AppColors.SecondaryContainer,
    onSecondaryContainer = AppColors.OnSecondaryContainer,
    tertiary = AppColors.Tertiary,
    onTertiary = AppColors.OnTertiary,
    tertiaryContainer = AppColors.TertiaryContainer,
    onTertiaryContainer = AppColors.OnTertiaryContainer,
    error = AppColors.Error,
    onError = AppColors.OnError,
    errorContainer = AppColors.ErrorContainer,
    onErrorContainer = AppColors.OnErrorContainer,
    background = AppColors.LightBackground,
    onBackground = AppColors.LightOnBackground,
    surface = AppColors.LightSurface,
    onSurface = AppColors.LightOnSurface,
    surfaceVariant = AppColors.LightSurfaceVariant,
    onSurfaceVariant = AppColors.LightOnSurfaceVariant,
    outline = AppColors.LightOutline,
    outlineVariant = AppColors.LightOutlineVariant,
    inverseSurface = AppColors.LightInverseSurface,
    inverseOnSurface = AppColors.LightInverseOnSurface,
    inversePrimary = AppColors.BluePrimaryDark,
    scrim = AppColors.Scrim,
)

val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = AppColors.BluePrimaryDark,
    onPrimary = AppColors.OnBluePrimary,
    primaryContainer = AppColors.BluePrimaryContainerDark,
    onPrimaryContainer = AppColors.OnBluePrimaryContainerDark,
    secondary = AppColors.SecondaryDark,
    onSecondary = AppColors.OnSecondary,
    secondaryContainer = AppColors.SecondaryContainerDark,
    onSecondaryContainer = AppColors.OnSecondaryContainerDark,
    tertiary = AppColors.TertiaryDark,
    onTertiary = AppColors.OnTertiary,
    tertiaryContainer = AppColors.TertiaryContainerDark,
    onTertiaryContainer = AppColors.OnTertiaryContainerDark,
    error = AppColors.ErrorDark,
    onError = AppColors.OnError,
    errorContainer = AppColors.ErrorContainerDark,
    onErrorContainer = AppColors.OnErrorContainerDark,
    background = AppColors.DarkBackground,
    onBackground = AppColors.DarkOnBackground,
    surface = AppColors.DarkSurface,
    onSurface = AppColors.DarkOnSurface,
    surfaceVariant = AppColors.DarkSurfaceVariant,
    onSurfaceVariant = AppColors.DarkOnSurfaceVariant,
    outline = AppColors.DarkOutline,
    outlineVariant = AppColors.DarkOutlineVariant,
    inverseSurface = AppColors.DarkInverseSurface,
    inverseOnSurface = AppColors.DarkInverseOnSurface,
    inversePrimary = AppColors.BluePrimary,
    scrim = AppColors.Scrim,
)
