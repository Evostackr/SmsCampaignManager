package com.evostackr.smscampaignmanager

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.evostackr.smscampaignmanager.data.local.AppDatabase
import com.evostackr.smscampaignmanager.data.repository.SettingsRepository
import com.evostackr.smscampaignmanager.worker.MonthlyResetWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class SmsApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    
    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            try {
                settingsRepository.getOrInitSettings()
                settingsRepository.checkAndResetMonthlyCounterIfNeeded()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        try {
            schedulePeriodicMonthlyCheck()
            com.evostackr.smscampaignmanager.util.AlarmManagerHelper.scheduleMidnightReset(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun schedulePeriodicMonthlyCheck() {
        try {
            val periodicResetRequest = PeriodicWorkRequestBuilder<MonthlyResetWorker>(12, TimeUnit.HOURS).build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "MonthlyResetPeriodicWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicResetRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

