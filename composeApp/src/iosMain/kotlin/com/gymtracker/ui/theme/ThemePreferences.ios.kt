package com.gymtracker.ui.theme

import com.gymtracker.platform.PlatformContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

actual class ThemePreferences actual constructor(context: PlatformContext) {

    private val defaults = NSUserDefaults.standardUserDefaults
    private val key = "accent_color"

    private val _flow = MutableStateFlow(
        AccentColor.fromKey(defaults.stringForKey(key) ?: AccentColor.ORANGE.key)
    )

    actual val accentColorFlow: Flow<AccentColor> = _flow

    actual suspend fun save(color: AccentColor) {
        defaults.setObject(color.key, forKey = key)
        _flow.value = color
    }
}
