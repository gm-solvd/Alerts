package com.privacyalert.android.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.privacyalert.android.ui.screen.DashboardScreen
import com.privacyalert.android.ui.screen.LoginScreen
import com.privacyalert.android.ui.theme.PrivacyAlertTheme
import com.privacyalert.domain.usecase.auth.CheckAuthUseCase
import org.koin.compose.koinInject

@Composable
fun App() {
    val checkAuth = koinInject<CheckAuthUseCase>()
    val isAuthenticated by checkAuth().collectAsState(initial = false)

    PrivacyAlertTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Crossfade(targetState = isAuthenticated, label = "auth-gate") { authed ->
                if (authed) {
                    Navigator(DashboardScreen()) { SlideTransition(it) }
                } else {
                    Navigator(LoginScreen()) { SlideTransition(it) }
                }
            }
        }
    }
}
