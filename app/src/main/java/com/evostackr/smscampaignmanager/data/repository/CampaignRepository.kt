package com.evostackr.smscampaignmanager.data.repository

import com.evostackr.smscampaignmanager.data.local.dao.CampaignDao
import com.evostackr.smscampaignmanager.data.local.dao.RecipientDao
import com.evostackr.smscampaignmanager.data.local.entity.CampaignEntity
import com.evostackr.smscampaignmanager.data.local.entity.RecipientEntity
import kotlinx.coroutines.flow.Flow
import java.lang.Exception

class CampaignRepository(
    private val campaignDao: CampaignDao,
    private val recipientDao: RecipientDao
) {
    val allCampaigns: Flow<List<CampaignEntity>> = campaignDao.getAllCampaigns()
    val activeCampaign: Flow<CampaignEntity?> = campaignDao.getActiveCampaign()
    val totalPendingQueueCount: Flow<Int> = recipientDao.getTotalPendingQueueCount()
    val totalFailedCount: Flow<Int> = recipientDao.getTotalFailedCount()

    fun searchAndFilterCampaigns(query: String?, statusFilter: String?): Flow<List<CampaignEntity>> {
        return campaignDao.searchAndFilterCampaigns(query, statusFilter)
    }

    /**
     * Create an SMS campaign and bulk-insert its recipients.
     *
     * @param name            Display name of the campaign.
     * @param messageTemplate Message body template (supports {name} and {number} placeholders).
     * @param delaySeconds    Delay between messages in seconds.
     * @param recipients      List of (name, phone) pairs.
     */
    suspend fun createCampaignWithRecipients(
        name: String,
        messageTemplate: String,
        delaySeconds: Int,
        recipients: List<com.evostackr.smscampaignmanager.util.ParsedContact>
    ): Long {
        val campaign = CampaignEntity(
            name = name,
            messageTemplate = messageTemplate,
            delaySeconds = delaySeconds,
            status = CampaignEntity.STATUS_QUEUED,
            createdAt = System.currentTimeMillis(),
            totalRecipients = recipients.size
        )
        val campaignId = campaignDao.insertCampaign(campaign)

        // Chunked insertion (1,000 at a time) for 100k+ scalability
        recipients.chunked(1000).forEach { chunk ->
            val recipientEntities = chunk.map { contact ->
                RecipientEntity(
                    campaignId = campaignId,
                    name = contact.name,
                    phoneNumber = contact.phoneNumber,
                    company = contact.company,
                    date = contact.date,
                    time = contact.time,
                    amount = contact.amount,
                    city = contact.city,
                    custom = contact.custom,
                    status = RecipientEntity.STATUS_PENDING
                )
            }
            recipientDao.insertRecipients(recipientEntities)
        }
        return campaignId
    }

    suspend fun getCampaignById(id: Long): CampaignEntity? {
        return campaignDao.getCampaignById(id)
    }

    suspend fun getCampaignsByStatusDirect(status: String): List<CampaignEntity> {
        return campaignDao.getCampaignsByStatusDirect(status)
    }

    fun getCampaignFlow(id: Long): Flow<CampaignEntity?> {
        return campaignDao.getCampaignByIdFlow(id)
    }

    fun getRecipientsForCampaign(campaignId: Long): Flow<List<RecipientEntity>> {
        return recipientDao.getRecipientsForCampaign(campaignId)
    }

    suspend fun updateCampaignStatus(campaignId: Long, status: String) {
        val campaign = campaignDao.getCampaignById(campaignId)
        if (campaign != null) {
            val updated = campaign.copy(
                status = status,
                startedAt = if (status == CampaignEntity.STATUS_IN_PROGRESS && campaign.startedAt == null) System.currentTimeMillis() else campaign.startedAt,
                finishedAt = if (status == CampaignEntity.STATUS_COMPLETED || status == CampaignEntity.STATUS_CANCELLED) System.currentTimeMillis() else campaign.finishedAt
            )
            campaignDao.updateCampaign(updated)
        }
    }

    suspend fun updateNextAllowedSendTime(campaignId: Long, time: Long) {
        campaignDao.updateNextAllowedSendTime(campaignId, time)
    }

    suspend fun deleteCampaign(campaign: CampaignEntity) {
        campaignDao.deleteCampaign(campaign)
    }

    suspend fun getNextPendingRecipient(campaignId: Long): RecipientEntity? {
        return recipientDao.getNextPendingRecipient(campaignId)
    }

    suspend fun markRecipientSending(recipientId: Long) {
        recipientDao.updateStatus(recipientId, RecipientEntity.STATUS_SENDING)
    }

    suspend fun markRecipientSent(campaignId: Long, recipientId: Long) {
        recipientDao.updateStatus(recipientId, RecipientEntity.STATUS_SENT, sentTime = System.currentTimeMillis())
        campaignDao.incrementSentCount(campaignId)
    }

    suspend fun markRecipientFailed(campaignId: Long, recipientId: Long, reason: String, retryCount: Int) {
        recipientDao.incrementRetryCount(recipientId)
        recipientDao.updateStatus(recipientId, RecipientEntity.STATUS_FAILED, errorReason = reason)
        campaignDao.incrementFailedCount(campaignId)
    }

    suspend fun retryFailedRecipients(campaignId: Long) {
        val recipients = recipientDao.getRecipientsForCampaignDirect(campaignId)
        recipients.filter { it.status == RecipientEntity.STATUS_FAILED }.forEach { rec ->
            recipientDao.updateStatus(rec.id, RecipientEntity.STATUS_PENDING, errorReason = null)
        }
        updateCampaignStatus(campaignId, CampaignEntity.STATUS_QUEUED)
    }

    suspend fun sendSingleSmsDirect(phone: String, text: String): Boolean {
        android.util.Log.d("SmsPipeline", "Sending SMS: recipient=$phone, text=\"$text\"")
        return try {
            val smsManager = android.telephony.SmsManager.getDefault()
            val parts = smsManager.divideMessage(text)
            android.util.Log.d("SmsPipeline", "SmsManager invoked: partsCount=${parts.size}")
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phone, null, text, null, null)
            }
            android.util.Log.d("SmsPipeline", "SMS send success to $phone")
            true
        } catch (e: Exception) {
            android.util.Log.e("SmsPipeline", "SMS send failed to $phone: ${e.message}", e)
            false
        }
    }
}
