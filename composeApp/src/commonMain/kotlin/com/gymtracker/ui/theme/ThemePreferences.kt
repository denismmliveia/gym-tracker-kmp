package com.gymtracker.ui.theme

import com.gymtracker.platform.PlatformContext
import kotlinx.coroutines.flow.Flow

expect class ThemePreferences(context: PlatformContext) {
    val accentColorFlow: Flow<AccentColor>
    suspend fun save(color: AccentColor)
}
