package com.privacyalert.android.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.privacyalert.android.ui.theme.AppColors
import com.privacyalert.android.ui.theme.PrivacyAlertTheme
import com.privacyalert.android.ui.theme.Spacing
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val headline: String,
    val body: String,
    val illustration: @Composable () -> Unit,
)

@Composable
fun OnboardingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    val pages = listOf(
        OnboardingPage(
            headline = "Your Email",
            body = "We use your email to scan for data breaches " +
                "and alert you when your information appears " +
                "in leaked databases.",
            illustration = { EmailIllustration() },
        ),
        OnboardingPage(
            headline = "Secure Password",
            body = "Your password is encrypted and never stored " +
                "in plain text. We use it to keep your account " +
                "and privacy data safe.",
            illustration = { PasswordIllustration() },
        ),
        OnboardingPage(
            headline = "You're All Set",
            body = "Start monitoring your digital footprint. " +
                "We'll alert you to threats and guide you " +
                "through fixing them.",
            illustration = { GetStartedIllustration() },
        ),
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Skip button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (!isLastPage) {
                    TextButton(onClick = onLoginClick) {
                        Text("Skip")
                    }
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            // Page indicator dots
            PageIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Bottom CTAs
            if (isLastPage) {
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text("Log In")
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                TextButton(onClick = onRegisterClick) {
                    Text("Don't have an account? Create now")
                }
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text("Next")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        page.illustration()

        Spacer(modifier = Modifier.height(Spacing.xl))

        Text(
            text = page.headline,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = page.body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentPage) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        },
                    ),
            )
        }
    }
}

// --- Canvas Illustrations ---

@Composable
private fun EmailIllustration() {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = AppColors.Secondary
    val surface = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = Modifier.size(160.dp)) {
        drawEmailIllustration(primary, secondary, surface)
    }
}

private fun DrawScope.drawEmailIllustration(
    primary: Color,
    secondary: Color,
    surface: Color,
) {
    val w = size.width
    val h = size.height

    // Background circle
    drawCircle(color = surface.copy(alpha = 0.3f), radius = w * 0.45f)

    // Envelope body
    val envLeft = w * 0.15f
    val envTop = h * 0.3f
    val envRight = w * 0.85f
    val envBottom = h * 0.7f
    drawRoundRect(
        color = primary,
        topLeft = Offset(envLeft, envTop),
        size = Size(envRight - envLeft, envBottom - envTop),
        cornerRadius = CornerRadius(w * 0.03f),
    )

    // Envelope flap
    val flapPath = Path().apply {
        moveTo(envLeft, envTop)
        lineTo(w * 0.5f, h * 0.5f)
        lineTo(envRight, envTop)
    }
    drawPath(
        path = flapPath,
        color = primary.copy(alpha = 0.7f),
        style = Stroke(width = w * 0.03f, cap = StrokeCap.Round),
    )

    // @ symbol
    drawCircle(
        color = Color.White,
        radius = w * 0.1f,
        center = Offset(w * 0.5f, h * 0.52f),
        style = Stroke(width = w * 0.025f),
    )
    // Inner a of @
    drawCircle(
        color = Color.White,
        radius = w * 0.05f,
        center = Offset(w * 0.5f, h * 0.52f),
        style = Stroke(width = w * 0.02f),
    )

    // Small notification badge
    drawCircle(
        color = secondary,
        radius = w * 0.06f,
        center = Offset(w * 0.78f, h * 0.28f),
    )
}

@Composable
private fun PasswordIllustration() {
    val primary = MaterialTheme.colorScheme.primary
    val accent = AppColors.Warning
    val surface = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = Modifier.size(160.dp)) {
        drawPasswordIllustration(primary, accent, surface)
    }
}

private fun DrawScope.drawPasswordIllustration(
    primary: Color,
    accent: Color,
    surface: Color,
) {
    val w = size.width
    val h = size.height

    // Background circle
    drawCircle(color = surface.copy(alpha = 0.3f), radius = w * 0.45f)

    // Lock body
    val lockLeft = w * 0.3f
    val lockTop = h * 0.45f
    val lockRight = w * 0.7f
    val lockBottom = h * 0.78f
    drawRoundRect(
        color = primary,
        topLeft = Offset(lockLeft, lockTop),
        size = Size(lockRight - lockLeft, lockBottom - lockTop),
        cornerRadius = CornerRadius(w * 0.04f),
    )

    // Lock shackle
    val shacklePath = Path().apply {
        moveTo(w * 0.35f, lockTop)
        lineTo(w * 0.35f, h * 0.32f)
        cubicTo(
            w * 0.35f, h * 0.2f,
            w * 0.65f, h * 0.2f,
            w * 0.65f, h * 0.32f,
        )
        lineTo(w * 0.65f, lockTop)
    }
    drawPath(
        path = shacklePath,
        color = primary.copy(alpha = 0.8f),
        style = Stroke(width = w * 0.05f, cap = StrokeCap.Round),
    )

    // Keyhole circle
    drawCircle(
        color = Color.White,
        radius = w * 0.05f,
        center = Offset(w * 0.5f, h * 0.57f),
    )
    // Keyhole drop
    val dropPath = Path().apply {
        moveTo(w * 0.48f, h * 0.6f)
        lineTo(w * 0.46f, h * 0.68f)
        lineTo(w * 0.54f, h * 0.68f)
        lineTo(w * 0.52f, h * 0.6f)
        close()
    }
    drawPath(path = dropPath, color = Color.White)

    // Accent star/sparkle
    drawCircle(
        color = accent,
        radius = w * 0.04f,
        center = Offset(w * 0.78f, h * 0.25f),
    )
    drawCircle(
        color = accent.copy(alpha = 0.6f),
        radius = w * 0.025f,
        center = Offset(w * 0.85f, h * 0.35f),
    )
}

@Composable
private fun GetStartedIllustration() {
    val primary = MaterialTheme.colorScheme.primary
    val success = AppColors.Success
    val surface = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = Modifier.size(160.dp)) {
        drawGetStartedIllustration(primary, success, surface)
    }
}

private fun DrawScope.drawGetStartedIllustration(
    primary: Color,
    success: Color,
    surface: Color,
) {
    val w = size.width
    val h = size.height

    // Background circle
    drawCircle(color = surface.copy(alpha = 0.3f), radius = w * 0.45f)

    // Shield shape
    val shieldPath = Path().apply {
        moveTo(w * 0.5f, h * 0.12f)
        cubicTo(w * 0.35f, h * 0.12f, w * 0.15f, h * 0.18f, w * 0.15f, h * 0.18f)
        lineTo(w * 0.15f, h * 0.52f)
        cubicTo(w * 0.15f, h * 0.7f, w * 0.3f, h * 0.82f, w * 0.5f, h * 0.9f)
        cubicTo(w * 0.7f, h * 0.82f, w * 0.85f, h * 0.7f, w * 0.85f, h * 0.52f)
        lineTo(w * 0.85f, h * 0.18f)
        cubicTo(w * 0.85f, h * 0.18f, w * 0.65f, h * 0.12f, w * 0.5f, h * 0.12f)
        close()
    }
    drawPath(path = shieldPath, color = primary)

    // Checkmark
    val checkPath = Path().apply {
        moveTo(w * 0.35f, h * 0.5f)
        lineTo(w * 0.46f, h * 0.62f)
        lineTo(w * 0.65f, h * 0.38f)
    }
    drawPath(
        path = checkPath,
        color = Color.White,
        style = Stroke(width = w * 0.05f, cap = StrokeCap.Round),
    )

    // Success glow dots
    drawCircle(
        color = success,
        radius = w * 0.035f,
        center = Offset(w * 0.82f, h * 0.18f),
    )
    drawCircle(
        color = success.copy(alpha = 0.6f),
        radius = w * 0.025f,
        center = Offset(w * 0.15f, h * 0.25f),
    )
    drawCircle(
        color = success.copy(alpha = 0.4f),
        radius = w * 0.02f,
        center = Offset(w * 0.88f, h * 0.4f),
    )
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    PrivacyAlertTheme {
        OnboardingScreen(
            onLoginClick = {},
            onRegisterClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailIllustrationPreview() {
    PrivacyAlertTheme {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center,
        ) {
            EmailIllustration()
        }
    }
}
