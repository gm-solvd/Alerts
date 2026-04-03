package com.privacyalert.android.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.privacyalert.android.ui.theme.ComponentSize

@Composable
fun ScoreGauge(
    score: Int,
    modifier: Modifier = Modifier,
) {
    val fraction = (score.coerceIn(0, 100)) / 100f
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 800),
        label = "score-animation",
    )
    val scoreColor = scoreColor(score)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(ComponentSize.scoreGaugeSize),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(ComponentSize.scoreGaugeSize)) {
            val strokeWidth = 12.dp.toPx()
            val sweepAngle = 270f

            // Background track
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Score arc
            drawArc(
                color = scoreColor,
                startAngle = 135f,
                sweepAngle = sweepAngle * animatedFraction,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.displayLarge,
                color = scoreColor,
            )
            Text(
                text = scoreLabel(score),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun scoreColor(score: Int): Color = when {
    score >= 80 -> Color(0xFF4CAF50) // Green
    score >= 60 -> Color(0xFFFFC107) // Amber
    score >= 40 -> MaterialTheme.colorScheme.primary // Orange (theme primary)
    else -> MaterialTheme.colorScheme.error // Red
}

private fun scoreLabel(score: Int): String = when {
    score >= 80 -> "Good"
    score >= 60 -> "Fair"
    score >= 40 -> "At Risk"
    else -> "Critical"
}
