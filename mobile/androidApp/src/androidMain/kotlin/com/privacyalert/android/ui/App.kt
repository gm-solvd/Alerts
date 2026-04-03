package com.privacyalert.android.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.privacyalert.android.preferences.OnboardingPreferences
import com.privacyalert.android.ui.navigation.AlertsTab
import com.privacyalert.android.ui.navigation.DashboardTab
import com.privacyalert.android.ui.navigation.FixItTab
import com.privacyalert.android.ui.navigation.ScanTab
import com.privacyalert.android.ui.screen.LoginScreen
import com.privacyalert.android.ui.screen.OnboardingScreen
import com.privacyalert.android.ui.screen.RegisterScreen
import com.privacyalert.android.ui.screen.SplashScreen
import com.privacyalert.android.ui.theme.PrivacyAlertTheme
import com.privacyalert.domain.usecase.auth.CheckAuthUseCase
import org.koin.compose.koinInject

private sealed interface AppPhase {
    data object Splash : AppPhase
    data object Onboarding : AppPhase
    data class AuthGate(val startOnRegister: Boolean = false) : AppPhase
}

@Composable
fun App() {
    val checkAuth = koinInject<CheckAuthUseCase>()
    val onboardingPrefs = koinInject<OnboardingPreferences>()
    val isAuthenticated by checkAuth().collectAsStateWithLifecycle(initialValue = false)

    var phase by remember { mutableStateOf<AppPhase>(AppPhase.Splash) }

    PrivacyAlertTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            AnimatedContent(
                targetState = phase,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "app-phase",
            ) { currentPhase ->
                when (currentPhase) {
                    is AppPhase.Splash -> SplashScreen(
                        onFinished = {
                            phase = if (onboardingPrefs.hasCompletedOnboarding()) {
                                AppPhase.AuthGate()
                            } else {
                                AppPhase.Onboarding
                            }
                        },
                    )

                    is AppPhase.Onboarding -> OnboardingScreen(
                        onLoginClick = {
                            onboardingPrefs.setOnboardingCompleted()
                            phase = AppPhase.AuthGate()
                        },
                        onRegisterClick = {
                            onboardingPrefs.setOnboardingCompleted()
                            phase = AppPhase.AuthGate(startOnRegister = true)
                        },
                    )

                    is AppPhase.AuthGate -> {
                        Crossfade(
                            targetState = isAuthenticated,
                            label = "auth-gate",
                        ) { authed ->
                            if (authed) {
                                MainContent()
                            } else {
                                val initialScreen = if (currentPhase.startOnRegister) {
                                    RegisterScreen()
                                } else {
                                    LoginScreen()
                                }
                                Navigator(initialScreen) { SlideTransition(it) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainContent() {
    TabNavigator(DashboardTab) {
        Scaffold(
            bottomBar = { BottomNavBar() },
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                CurrentTab()
            }
        }
    }
}

private val tabs = listOf(DashboardTab, AlertsTab, ScanTab, FixItTab)

@Composable
private fun BottomNavBar() {
    val tabNavigator = LocalTabNavigator.current

    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = tabNavigator.current == tab,
                onClick = { tabNavigator.current = tab },
                icon = {
                    tab.options.icon?.let { painter ->
                        Icon(painter = painter, contentDescription = tab.options.title)
                    }
                },
                label = { Text(text = tab.options.title) },
            )
        }
    }
}
