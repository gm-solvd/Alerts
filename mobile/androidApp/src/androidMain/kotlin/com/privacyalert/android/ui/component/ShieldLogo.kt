package com.privacyalert.android.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.privacyalert.android.ui.theme.AppColors
import com.privacyalert.android.ui.theme.PrivacyAlertTheme

@Composable
fun ShieldLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val shieldPath = Path().apply {
            moveTo(w * 0.5f, h * 0.05f)
            cubicTo(w * 0.35f, h * 0.05f, w * 0.08f, h * 0.12f, w * 0.08f, h * 0.12f)
            lineTo(w * 0.08f, h * 0.5f)
            cubicTo(w * 0.08f, h * 0.72f, w * 0.25f, h * 0.88f, w * 0.5f, h * 0.97f)
            cubicTo(w * 0.75f, h * 0.88f, w * 0.92f, h * 0.72f, w * 0.92f, h * 0.5f)
            lineTo(w * 0.92f, h * 0.12f)
            cubicTo(w * 0.92f, h * 0.12f, w * 0.65f, h * 0.05f, w * 0.5f, h * 0.05f)
            close()
        }

        // Shield fill with gradient
        drawPath(
            path = shieldPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor, primaryColor.copy(alpha = 0.8f)),
            ),
        )

        // Shield outline
        drawPath(
            path = shieldPath,
            color = primaryColor.copy(alpha = 0.3f),
            style = Stroke(width = w * 0.02f, join = StrokeJoin.Round),
        )

        // Keyhole — circle
        val keyholeRadius = w * 0.1f
        val keyholeCenterY = h * 0.4f
        drawCircle(
            color = onPrimaryColor,
            radius = keyholeRadius,
            center = Offset(w * 0.5f, keyholeCenterY),
        )

        // Keyhole — bottom trapezoid shape
        val keyPath = Path().apply {
            moveTo(w * 0.5f - keyholeRadius * 0.5f, keyholeCenterY + keyholeRadius * 0.6f)
            lineTo(w * 0.5f - keyholeRadius * 0.8f, h * 0.65f)
            lineTo(w * 0.5f + keyholeRadius * 0.8f, h * 0.65f)
            lineTo(w * 0.5f + keyholeRadius * 0.5f, keyholeCenterY + keyholeRadius * 0.6f)
            close()
        }
        drawPath(path = keyPath, color = onPrimaryColor)

        // Inner highlight arc for depth
        val highlightPath = Path().apply {
            moveTo(w * 0.25f, h * 0.2f)
            cubicTo(w * 0.35f, h * 0.15f, w * 0.5f, h * 0.13f, w * 0.5f, h * 0.13f)
        }
        drawPath(
            path = highlightPath,
            color = Color.White.copy(alpha = 0.3f),
            style = Stroke(width = w * 0.025f, cap = StrokeCap.Round),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ShieldLogoPreview() {
    PrivacyAlertTheme {
        ShieldLogo(size = 120.dp)
    }
}
