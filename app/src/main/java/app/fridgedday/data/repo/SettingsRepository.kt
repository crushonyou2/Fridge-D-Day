package app.fridgedday.data.repo

import app.fridgedday.data.pref.AppSettings
import app.fridgedday.data.pref.SettingsDataStore
import app.fridgedday.data.pref.ThemeMode
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val dataStore: SettingsDataStore) {

    val settings: Flow<AppSettings> = dataStore.settingsFlow

    suspend fun updateDailyNotify(enabled: Boolean) {
        dataStore.updateDailyNotify(enabled)
    }

    suspend fun updateNotifyTime(hour: Int, minute: Int) {
        dataStore.updateNotifyTime(hour, minute)
    }

    suspend fun updateDefaultDaysBefore(days: Int) {
        dataStore.updateDefaultDaysBefore(days)
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        dataStore.updateThemeMode(mode)
    }

    suspend fun setFirstLaunchCompleted() {
        dataStore.setFirstLaunchCompleted()
    }
}
