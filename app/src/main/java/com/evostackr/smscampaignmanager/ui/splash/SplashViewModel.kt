package com.evostackr.smscampaignmanager.ui.splash

import androidx.lifecycle.ViewModel
import com.evostackr.smscampaignmanager.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    suspend fun isOnboardingCompleted(): Boolean {
        return settingsRepository.isOnboardingCompleted()
    }
}
