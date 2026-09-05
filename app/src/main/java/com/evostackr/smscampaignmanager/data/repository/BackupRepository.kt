package com.evostackr.smscampaignmanager.data.repository

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.evostackr.smscampaignmanager.data.local.dao.SettingsDao
import com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity
import com.evostackr.smscampaignmanager.util.EncryptedBackupEngine
import com.evostackr.smscampaignmanager.util.RecoveryStats
import com.evostackr.smscampaignmanager.util.SmsHistoryRecoveryManager
import com.evostackr.smscampaignmanager.worker.AutoBackupWorker
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class BackupRepository(
    private val settingsDao: SettingsDao,
    private val context: Context
) {
    suspend fun createEncryptedBackup(outputStream: OutputStream): Long {
        val size = EncryptedBackupEngine.createEncryptedBackup(context, outputStream)
        val current = settingsDao.getOrInitSettings()
        settingsDao.updateSettings(
            current.copy(
                lastBackupTimestamp = System.currentTimeMillis(),
                lastBackupSize = size
            )
        )
        return size
    }

    suspend fun restoreEncryptedBackup(inputStream: InputStream): Boolean {
        val success = EncryptedBackupEngine.restoreEncryptedBackup(context, inputStream)
        if (success) {
            val current = settingsDao.getOrInitSettings()
            settingsDao.updateSettings(
                current.copy(
                    recoverySource = SettingsEntity.SOURCE_BACKUP_RESTORE,
                    isRecoveryWizardPending = false
                )
            )
        }
        return success
    }

    suspend fun recoverFromDeviceSmsHistory(): RecoveryStats {
        val stats = SmsHistoryRecoveryManager.recoverMonthlyStatsFromDeviceSms(context)
        val current = settingsDao.getOrInitSettings()
        settingsDao.updateSettings(
            current.copy(
                sentThisMonth = stats.sentThisMonth,
                recoverySource = stats.recoverySource,
                isRecoveryWizardPending = false
            )
        )
        return stats
    }

    suspend fun scheduleAutoBackup(frequency: String, folderUri: String) {
        val current = settingsDao.getOrInitSettings()
        settingsDao.updateSettings(
            current.copy(
                autoBackupEnabled = true,
                backupFrequency = frequency,
                backupFolderUri = folderUri
            )
        )

        val intervalDays = if (frequency == SettingsEntity.FREQUENCY_WEEKLY) 7L else 1L
        val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(intervalDays, TimeUnit.DAYS).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "AutoBackupWorkerTask",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
    }
}

