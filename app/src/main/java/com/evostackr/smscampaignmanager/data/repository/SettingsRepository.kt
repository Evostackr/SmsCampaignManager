package com.evostackr.smscampaignmanager.data.repository

import com.evostackr.smscampaignmanager.data.local.dao.SettingsDao
import com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class SettingsRepository(
    private val settingsDao: SettingsDao
) {
    val settingsFlow: Flow<SettingsEntity?> = settingsDao.getSettings()

    private fun getCurrentMonthYearKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        return sdf.format(Date())
    }

    private fun getCurrentDayKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    suspend fun getOrInitSettings(): SettingsEntity {
        var current = settingsDao.getSettingsDirect()
        val currentMonthYear = getCurrentMonthYearKey()
        val currentDay = getCurrentDayKey()

        if (current == null) {
            current = SettingsEntity(
                monthlyLimit = 100,
                defaultDelaySeconds = 30,
                maxRetryCount = 3,
                sentThisMonth = 0,
                lastResetMonthYear = currentMonthYear,
                dailyLimit = 100,
                sentToday = 0,
                lastResetDay = currentDay,
                autoResumeEnabled = true,
                delayMode = SettingsEntity.MODE_FIXED,
                minDelaySeconds = 30,
                maxDelaySeconds = 90
            )
            settingsDao.insertOrUpdateSettings(current)
        } else {
            var needsUpdate = false
            if (current.lastResetMonthYear != currentMonthYear) {
                current = current.copy(
                    sentThisMonth = 0,
                    lastResetMonthYear = currentMonthYear
                )
                needsUpdate = true
            }
            if (current.lastResetDay != currentDay) {
                current = current.copy(
                    sentToday = 0,
                    lastResetDay = currentDay
                )
                needsUpdate = true
            }
            if (needsUpdate) {
                settingsDao.insertOrUpdateSettings(current)
            }
        }
        return current
    }

    suspend fun updateSettings(settings: SettingsEntity) {
        settingsDao.insertOrUpdateSettings(settings)
    }

    suspend fun incrementMonthlySent(count: Int = 1) {
        getOrInitSettings()
        settingsDao.incrementMonthlySent(count)
    }
    
    suspend fun incrementDailySent(count: Int = 1) {
        getOrInitSettings()
        settingsDao.incrementDailySent(count)
    }

    suspend fun checkAndResetMonthlyCounterIfNeeded() {
        val currentMonthYear = getCurrentMonthYearKey()
        val currentDay = getCurrentDayKey()
        val current = settingsDao.getSettingsDirect()
        if (current != null) {
            if (current.lastResetMonthYear != currentMonthYear) {
                settingsDao.resetMonthlyCounter(currentMonthYear)
            }
            if (current.lastResetDay != currentDay) {
                settingsDao.resetDailyCounter(currentDay)
            }
        }
    }

    /** Update onboarding completion status. */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        val current = getOrInitSettings()
        settingsDao.insertOrUpdateSettings(current.copy(isOnboardingCompleted = completed))
    }

    /** Check whether onboarding has been completed. */
    suspend fun isOnboardingCompleted(): Boolean {
        val current = settingsDao.getSettingsDirect()
        return current?.isOnboardingCompleted ?: false
    }
}
