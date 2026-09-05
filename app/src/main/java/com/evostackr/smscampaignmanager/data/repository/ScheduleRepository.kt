package com.evostackr.smscampaignmanager.data.repository

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.evostackr.smscampaignmanager.data.local.dao.ScheduledSmsDao
import com.evostackr.smscampaignmanager.data.local.entity.ScheduledSmsEntity
import com.evostackr.smscampaignmanager.worker.ScheduledSmsWorker
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class ScheduleRepository(
    private val scheduledSmsDao: ScheduledSmsDao,
    private val context: Context
) {
    val allScheduledSms: Flow<List<ScheduledSmsEntity>> = scheduledSmsDao.getAllScheduledSms()

    suspend fun scheduleSms(
        recipientPhone: String,
        recipientName: String?,
        message: String,
        scheduledTime: Long,
        repeatInterval: String = ScheduledSmsEntity.REPEAT_ONE_TIME
    ): Long {
        val entity = ScheduledSmsEntity(
            recipientPhone = recipientPhone,
            recipientName = recipientName,
            message = message,
            scheduledTime = scheduledTime,
            repeatInterval = repeatInterval,
            status = ScheduledSmsEntity.STATUS_PENDING
        )
        val id = scheduledSmsDao.insertScheduledSms(entity)

        enqueueWorkManagerTask(id, recipientPhone, message, scheduledTime)
        return id
    }

    private fun enqueueWorkManagerTask(id: Long, recipientPhone: String, message: String, scheduledTime: Long) {
        val delayMillis = (scheduledTime - System.currentTimeMillis()).coerceAtLeast(0)
        val workRequest = OneTimeWorkRequestBuilder<ScheduledSmsWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "SCHEDULED_ID" to id,
                    "PHONE" to recipientPhone,
                    "MESSAGE" to message
                )
            )
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    suspend fun cancelScheduledSms(scheduledSms: ScheduledSmsEntity) {
        scheduledSmsDao.updateStatus(scheduledSms.id, ScheduledSmsEntity.STATUS_CANCELLED)
    }
}

