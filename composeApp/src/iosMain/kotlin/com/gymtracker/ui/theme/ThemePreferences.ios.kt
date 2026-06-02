package com.gymtracker.ui.theme

import com.gymtracker.platform.PlatformContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

actual class ThemePreferences actual constructor(context: PlatformContext) {
    private val _flow = MutableStateFlow(AccentColor.ORANGE)

    actual val accentColorFlow: Flow<AccentColor> = _flow

    actual suspend fun save(color: AccentColor) {
        _flow.value = color
    }
}
