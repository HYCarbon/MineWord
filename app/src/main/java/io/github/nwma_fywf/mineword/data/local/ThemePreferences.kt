package io.github.nwma_fywf.mineword.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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
    MONOSPACE,
    CUSTOM
}

class ThemePreferences(private val context: Context) {

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val USE_DYNAMIC_COLOR_KEY = booleanPreferencesKey("use_dynamic_color")
        private val FONT_STYLE_KEY = stringPreferencesKey("font_style")
        private val CUSTOM_FONT_PATH_KEY = stringPreferencesKey("custom_font_path")
        private val CUSTOM_PRIMARY_KEY = intPreferencesKey("custom_primary")
        private val CUSTOM_SECONDARY_KEY = intPreferencesKey("custom_secondary")
        private val CUSTOM_TERTIARY_KEY = intPreferencesKey("custom_tertiary")
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

    val useDynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_DYNAMIC_COLOR_KEY] ?: true
    }

    suspend fun setUseDynamicColor(use: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLOR_KEY] = use
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

    val customFontPath: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_FONT_PATH_KEY]
    }

    suspend fun setCustomFontPath(path: String?) {
        context.dataStore.edit { preferences ->
            if (path != null) {
                preferences[CUSTOM_FONT_PATH_KEY] = path
            } else {
                preferences.remove(CUSTOM_FONT_PATH_KEY)
            }
        }
    }

    val customPrimaryColor: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_PRIMARY_KEY]
    }

    val customSecondaryColor: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_SECONDARY_KEY]
    }

    val customTertiaryColor: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_TERTIARY_KEY]
    }

    suspend fun setCustomPrimaryColor(color: Int?) {
        context.dataStore.edit { preferences ->
            if (color != null) {
                preferences[CUSTOM_PRIMARY_KEY] = color
            } else {
                preferences.remove(CUSTOM_PRIMARY_KEY)
            }
        }
    }

    suspend fun setCustomSecondaryColor(color: Int?) {
        context.dataStore.edit { preferences ->
            if (color != null) {
                preferences[CUSTOM_SECONDARY_KEY] = color
            } else {
                preferences.remove(CUSTOM_SECONDARY_KEY)
            }
        }
    }

    suspend fun setCustomTertiaryColor(color: Int?) {
        context.dataStore.edit { preferences ->
            if (color != null) {
                preferences[CUSTOM_TERTIARY_KEY] = color
            } else {
                preferences.remove(CUSTOM_TERTIARY_KEY)
            }
        }
    }

    suspend fun setCustomThemeColor(primary: Int, secondary: Int, tertiary: Int) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_PRIMARY_KEY] = primary
            preferences[CUSTOM_SECONDARY_KEY] = secondary
            preferences[CUSTOM_TERTIARY_KEY] = tertiary
        }
    }

    suspend fun clearCustomThemeColor() {
        context.dataStore.edit { preferences ->
            preferences.remove(CUSTOM_PRIMARY_KEY)
            preferences.remove(CUSTOM_SECONDARY_KEY)
            preferences.remove(CUSTOM_TERTIARY_KEY)
        }
    }
}