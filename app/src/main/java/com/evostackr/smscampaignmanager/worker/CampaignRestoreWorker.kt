package com.evostackr.smscampaignmanager.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.evostackr.smscampaignmanager.data.local.AppDatabase
import com.evostackr.smscampaignmanager.data.local.entity.CampaignEntity
import com.evostackr.smscampaignmanager.service.SmsSendingService

class CampaignRestoreWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(context)
            val campaignDao = db.campaignDao()

            val activeCampaign = campaignDao.getActiveCampaignDirect()
            if (activeCampaign != null && activeCampaign.status == CampaignEntity.STATUS_IN_PROGRESS) {
                // Resume campaign sending service
                SmsSendingService.startCampaign(context, activeCampaign.id)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

