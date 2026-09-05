package com.evostackr.smscampaignmanager.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.evostackr.smscampaignmanager.data.local.AppDatabase
import com.evostackr.smscampaignmanager.util.EncryptedBackupEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.documentfile.provider.DocumentFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(context)
        val settings = db.settingsDao().getOrInitSettings()

        if (!settings.autoBackupEnabled || settings.backupFolderUri.isNullOrBlank()) {
            return Result.success()
        }

        return try {
            val folderUri = Uri.parse(settings.backupFolderUri)
            val timeStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "sms_campaign_autobackup_$timeStr.enc"

            val createdFileUri = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, folderUri)
                ?.createFile("application/octet-stream", fileName)
                ?.uri ?: return Result.failure()

            val bytesWritten = context.contentResolver.openOutputStream(createdFileUri)?.use { stream ->
                EncryptedBackupEngine.createEncryptedBackup(context, stream)
            } ?: 0L

            db.settingsDao().updateSettings(
                settings.copy(
                    lastBackupTimestamp = System.currentTimeMillis(),
                    lastBackupSize = bytesWritten
                )
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}

