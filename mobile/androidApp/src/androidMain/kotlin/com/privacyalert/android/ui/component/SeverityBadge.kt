package com.privacyalert.android.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.privacyalert.android.ui.theme.ComponentSize
import com.privacyalert.android.ui.theme.PrivacyAlertColors
import com.privacyalert.android.ui.theme.PrivacyAlertTheme
import com.privacyalert.android.ui.theme.Spacing
import com.privacyalert.domain.model.Severity

@Composable
fun SeverityBadge(
    severity: Severity,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) = severityColors(severity)
    Box(
        modifier = modifier
            .height(ComponentSize.severityBadgeHeight)
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = Spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = severity.name,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
    }
}

@Composable
private fun severityColors(severity: Severity): Pair<Color, Color> = when (severity) {
    Severity.CRITICAL -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    Severity.HIGH -> PrivacyAlertColors.Primary to Color.White
    Severity.MEDIUM -> PrivacyAlertColors.AccentWarm to Color.Black
    Severity.LOW -> PrivacyAlertColors.Success to Color.White
}

@Preview(showBackground = true)
@Composable
private fun SeverityBadgePreview() {
    PrivacyAlertTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.padding(Spacing.md),
        ) {
            Severity.entries.forEach { severity ->
                SeverityBadge(severity = severity)
            }
        }
    }
}
