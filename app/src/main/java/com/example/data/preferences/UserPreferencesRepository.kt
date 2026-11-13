package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.DarkThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qiraat_prefs")

data class UserPrefs(
    val selectedRiwayah: String = "uthmani",
    val fontSizeSp: Float = 28f,
    val compareMode: Boolean = false,
    val darkThemeMode: DarkThemeMode = DarkThemeMode.SYSTEM,
    val lastSurahId: Int = 1,
    val lastSurahName: String = "الفاتحة"
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val SELECTED_RIWAYAH = stringPreferencesKey("selected_riwayah")
        val FONT_SIZE_SP = floatPreferencesKey("font_size_sp")
        val COMPARE_MODE = booleanPreferencesKey("compare_mode")
        val DARK_THEME_MODE = stringPreferencesKey("dark_theme_mode")
        val LAST_SURAH_ID = intPreferencesKey("last_surah_id")
        val LAST_SURAH_NAME = stringPreferencesKey("last_surah_name")
    }

    val userPrefsFlow: Flow<UserPrefs> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[Keys.DARK_THEME_MODE] ?: DarkThemeMode.SYSTEM.name
        val themeMode = runCatching { DarkThemeMode.valueOf(themeStr) }.getOrDefault(DarkThemeMode.SYSTEM)
        UserPrefs(
            selectedRiwayah = prefs[Keys.SELECTED_RIWAYAH] ?: "uthmani",
            fontSizeSp = (prefs[Keys.FONT_SIZE_SP] ?: 28f).coerceIn(20f, 44f),
            compareMode = prefs[Keys.COMPARE_MODE] ?: false,
            darkThemeMode = themeMode,
            lastSurahId = prefs[Keys.LAST_SURAH_ID] ?: 1,
            lastSurahName = prefs[Keys.LAST_SURAH_NAME] ?: "الفاتحة"
        )
    }

    suspend fun setSelectedRiwayah(riwayahCode: String) {
        context.dataStore.edit { it[Keys.SELECTED_RIWAYAH] = riwayahCode }
    }

    suspend fun setFontSize(sizeSp: Float) {
        context.dataStore.edit { it[Keys.FONT_SIZE_SP] = sizeSp.coerceIn(20f, 44f) }
    }

    suspend fun setCompareMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.COMPARE_MODE] = enabled }
    }

    suspend fun setDarkThemeMode(mode: DarkThemeMode) {
        context.dataStore.edit { it[Keys.DARK_THEME_MODE] = mode.name }
    }

    suspend fun setLastSurah(surahId: Int, surahName: String) {
        context.dataStore.edit {
            it[Keys.LAST_SURAH_ID] = surahId
            it[Keys.LAST_SURAH_NAME] = surahName
        }
    }
}
