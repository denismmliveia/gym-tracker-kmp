package com.gymtracker.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ThemeViewModel(prefs: ThemePreferences) : ViewModel() {
    val accentColor: StateFlow<AccentColor> = prefs.accentColorFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AccentColor.ORANGE)
}
