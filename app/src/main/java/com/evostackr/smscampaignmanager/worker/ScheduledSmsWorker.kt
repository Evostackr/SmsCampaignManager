package com.evostackr.smscampaignmanager.worker

import android.content.Context
import android.telephony.SmsManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.evostackr.smscampaignmanager.data.local.AppDatabase
import com.evostackr.smscampaignmanager.data.local.entity.ScheduledSmsEntity
import com.evostackr.smscampaignmanager.data.local.entity.SmsLogEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * ScheduledSmsWorker
 *
 * WorkManager worker that fires at the scheduled time and sends the SMS.
 */
@HiltWorker
class ScheduledSmsWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_SCHEDULED_ID = "SCHEDULED_ID"
        const val KEY_PHONE = "PHONE"
        const val KEY_MESSAGE = "MESSAGE"
    }

    override suspend fun doWork(): Result {
        val scheduledId = inputData.getLong(KEY_SCHEDULED_ID, -1L)
        val phone = inputData.getString(KEY_PHONE) ?: return Result.failure()
        val message = inputData.getString(KEY_MESSAGE) ?: return Result.failure()

        val db = AppDatabase.getInstance(context)
        return sendSms(db, scheduledId, phone, message)
    }

    private suspend fun sendSms(
        db: AppDatabase,
        scheduledId: Long,
        phone: String,
        message: String
    ): Result {
        return try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phone, null, parts, null, null)

            updateStatus(db, scheduledId, ScheduledSmsEntity.STATUS_EXECUTED)
            logMessage(db, phone, message, SmsLogEntity.RESULT_SUCCESS, null)
            Result.success()
        } catch (e: Exception) {
            updateStatus(db, scheduledId, ScheduledSmsEntity.STATUS_FAILED)
            logMessage(db, phone, message, SmsLogEntity.RESULT_FAILED, e.message)
            Result.failure()
        }
    }

    private suspend fun updateStatus(db: AppDatabase, scheduledId: Long, status: String) {
        if (scheduledId != -1L) {
            db.scheduledSmsDao().updateStatus(scheduledId, status)
        }
    }

    private suspend fun logMessage(
        db: AppDatabase,
        phone: String,
        message: String,
        result: String,
        errorMessage: String?
    ) {
        db.smsLogDao().insertLog(
            SmsLogEntity(
                phoneNumber = phone,
                message = message,
                result = result,
                timestamp = System.currentTimeMillis(),
                errorMessage = errorMessage,
                messageType = SmsLogEntity.TYPE_SMS
            )
        )
    }
}
