package io.github.nwma_fywf.mineword.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class FontStyle {
    DEFAULT,
    SERIF,
    SANS_SERIF,
    MONOSPACE
}

class ThemePreferences(private val context: Context) {

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val FONT_STYLE_KEY = stringPreferencesKey("font_style")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val modeString = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeString)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    val fontStyle: Flow<FontStyle> = context.dataStore.data.map { preferences ->
        val styleString = preferences[FONT_STYLE_KEY] ?: FontStyle.DEFAULT.name
        try {
            FontStyle.valueOf(styleString)
        } catch (e: IllegalArgumentException) {
            FontStyle.DEFAULT
        }
    }

    suspend fun setFontStyle(style: FontStyle) {
        context.dataStore.edit { preferences ->
            preferences[FONT_STYLE_KEY] = style.name
        }
    }
}