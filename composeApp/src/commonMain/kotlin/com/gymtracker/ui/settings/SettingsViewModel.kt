package com.gymtracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.ui.theme.AccentColor
import com.gymtracker.ui.theme.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val prefs: ThemePreferences) : ViewModel() {
    val selected: StateFlow<AccentColor> = prefs.accentColorFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AccentColor.ORANGE)

    fun select(color: AccentColor) {
        viewModelScope.launch { prefs.save(color) }
    }
}
