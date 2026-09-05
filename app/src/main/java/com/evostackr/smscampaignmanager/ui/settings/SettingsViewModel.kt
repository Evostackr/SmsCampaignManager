package com.evostackr.smscampaignmanager.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity
import com.evostackr.smscampaignmanager.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    val settingsState: StateFlow<SettingsEntity> = settingsRepository.settingsFlow
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsEntity()
        )

    fun updateMonthlyLimit(limit: Int) {
        viewModelScope.launch {
            val current = settingsRepository.getOrInitSettings()
            settingsRepository.updateSettings(current.copy(monthlyLimit = limit.coerceAtLeast(1)))
        }
    }

    fun updateDefaultDelay(delaySeconds: Int) {
        viewModelScope.launch {
            val current = settingsRepository.getOrInitSettings()
            settingsRepository.updateSettings(current.copy(defaultDelaySeconds = delaySeconds.coerceIn(5, 600)))
        }
    }

    fun updateMaxRetries(retries: Int) {
        viewModelScope.launch {
            val current = settingsRepository.getOrInitSettings()
            settingsRepository.updateSettings(current.copy(maxRetryCount = retries.coerceIn(0, 5)))
        }
    }

    fun toggleDarkTheme(isDark: Boolean?) {
        viewModelScope.launch {
            val current = settingsRepository.getOrInitSettings()
            settingsRepository.updateSettings(current.copy(darkTheme = isDark))
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsRepository.getOrInitSettings()
            settingsRepository.updateSettings(current.copy(notificationsEnabled = enabled))
        }
    }

    fun resetMonthlyCounter() {
        viewModelScope.launch {
            val current = settingsRepository.getOrInitSettings()
            settingsRepository.updateSettings(current.copy(sentThisMonth = 0))
        }
    }

    fun toggleVoiceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsRepository.getOrInitSettings()
            settingsRepository.updateSettings(current.copy(voiceEnabled = enabled))
        }
    }

    fun updateDailyLimit(limit: Int) {
        viewModelScope.launch {
            val current = settingsRepository.getOrInitSettings()
            settingsRepository.updateSettings(current.copy(dailyLimit = limit.coerceAtLeast(1)))
        }
    }

    fun toggleAutoResume(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsRepository.getOrInitSettings()
            settingsRepository.updateSettings(current.copy(autoResumeEnabled = enabled))
        }
    }

    fun updateDelayMode(mode: String) {
        viewModelScope.launch {
            val current = settingsRepository.getOrInitSettings()
            settingsRepository.updateSettings(current.copy(delayMode = mode))
        }
    }

    fun updateMinDelay(delaySeconds: Int) {
        viewModelScope.launch {
            val current = settingsRepository.getOrInitSettings()
            settingsRepository.updateSettings(current.copy(minDelaySeconds = delaySeconds.coerceIn(5, current.maxDelaySeconds)))
        }
    }

    fun updateMaxDelay(delaySeconds: Int) {
        viewModelScope.launch {
            val current = settingsRepository.getOrInitSettings()
            settingsRepository.updateSettings(current.copy(maxDelaySeconds = delaySeconds.coerceAtLeast(current.minDelaySeconds)))
        }
    }
}

