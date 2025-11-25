package app.fridgedday.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val DAILY_NOTIFY = booleanPreferencesKey("daily_notify")
        val NOTIFY_HOUR = intPreferencesKey("notify_hour")
        val NOTIFY_MINUTE = intPreferencesKey("notify_minute")
        val DEFAULT_DAYS_BEFORE = intPreferencesKey("default_days_before")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            dailyNotify = prefs[Keys.DAILY_NOTIFY] ?: true,
            notifyTime = LocalTime.of(
                prefs[Keys.NOTIFY_HOUR] ?: 9,
                prefs[Keys.NOTIFY_MINUTE] ?: 0
            ),
            defaultDaysBefore = prefs[Keys.DEFAULT_DAYS_BEFORE] ?: 3,
            themeMode = try {
                ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: "SYSTEM")
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            },
            isFirstLaunch = prefs[Keys.IS_FIRST_LAUNCH] ?: true
        )
    }

    suspend fun updateDailyNotify(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DAILY_NOTIFY] = enabled
        }
    }

    suspend fun updateNotifyTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFY_HOUR] = hour
            prefs[Keys.NOTIFY_MINUTE] = minute
        }
    }

    suspend fun updateDefaultDaysBefore(days: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_DAYS_BEFORE] = days
        }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_FIRST_LAUNCH] = false
        }
    }
}
