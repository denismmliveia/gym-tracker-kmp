package com.gymtracker.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gymtracker.platform.PlatformContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "theme_prefs")

actual class ThemePreferences actual constructor(private val context: PlatformContext) {
    private val accentKey = stringPreferencesKey("accent_color")

    actual val accentColorFlow: Flow<AccentColor> =
        context.androidContext.dataStore.data
            .map { prefs -> AccentColor.fromKey(prefs[accentKey] ?: AccentColor.ORANGE.key) }

    actual suspend fun save(color: AccentColor) {
        context.androidContext.dataStore.edit { prefs -> prefs[accentKey] = color.key }
    }
}
