package com.evostackr.smscampaignmanager.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.evostackr.smscampaignmanager.data.local.AppDatabase
import com.evostackr.smscampaignmanager.data.repository.SettingsRepository

class MonthlyResetWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(context)
            val settingsRepo = SettingsRepository(db.settingsDao())
            settingsRepo.checkAndResetMonthlyCounterIfNeeded()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

