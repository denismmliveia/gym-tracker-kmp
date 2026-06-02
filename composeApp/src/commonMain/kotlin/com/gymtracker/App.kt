package com.gymtracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.ui.theme.GymTrackerTheme
import com.gymtracker.ui.theme.ThemeViewModel

@Composable
fun App(container: AppContainer) {
    val themeVm: ThemeViewModel = viewModel { ThemeViewModel(container.themePreferences) }
    val accent by themeVm.accentColor.collectAsState()

    CompositionLocalProvider(LocalAppContainer provides container) {
        GymTrackerTheme(
            primaryColor   = accent.primary,
            secondaryColor = accent.secondary
        ) {
            // AppNavigation wired in Plan B3
        }
    }
}
