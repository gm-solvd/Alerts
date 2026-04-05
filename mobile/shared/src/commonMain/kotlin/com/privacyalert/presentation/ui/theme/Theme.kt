package com.privacyalert.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
expect fun PlatformThemeEffect(darkTheme: Boolean)

@Composable
fun PrivacyAlertTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    PlatformThemeEffect(darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PrivacyAlertTypography,
        shapes = PrivacyAlertShapes,
        content = content,
    )
}
