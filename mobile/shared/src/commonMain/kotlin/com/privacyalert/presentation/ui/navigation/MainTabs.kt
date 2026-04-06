package com.privacyalert.presentation.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition
import com.privacyalert.presentation.ui.screen.AlertsScreen
import com.privacyalert.presentation.ui.screen.DashboardScreen

object DashboardTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember { TabOptions(index = 0u, title = "Dashboard", icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(DashboardScreen()) { SlideTransition(it) }
    }
}

object AlertsTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Notifications)
            return remember { TabOptions(index = 1u, title = "Alerts", icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(AlertsScreen()) { SlideTransition(it) }
    }
}
