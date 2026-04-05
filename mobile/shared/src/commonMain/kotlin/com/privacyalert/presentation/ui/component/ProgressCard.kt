package com.privacyalert.presentation.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.privacyalert.presentation.ui.theme.IconSize
import com.privacyalert.presentation.ui.theme.Spacing

@Composable
fun ProgressCard(
    icon: ImageVector,
    title: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = Color.White,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(IconSize.lg),
                    tint = Color.White,
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.9f),
                trackColor = Color.White.copy(alpha = 0.3f),
            )
        }
    }
}
