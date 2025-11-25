package app.fridgedday.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fridgedday.data.pref.AppSettings
import app.fridgedday.data.pref.ThemeMode
import app.fridgedday.data.repo.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun toggleDailyNotify(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDailyNotify(enabled)
        }
    }

    fun updateNotifyTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.updateNotifyTime(hour, minute)
        }
    }

    fun updateDefaultDaysBefore(days: Int) {
        viewModelScope.launch {
            repository.updateDefaultDaysBefore(days)
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.updateThemeMode(mode)
        }
    }
}
