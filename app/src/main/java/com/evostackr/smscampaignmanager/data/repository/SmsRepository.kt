package com.evostackr.smscampaignmanager.data.repository

import com.evostackr.smscampaignmanager.data.local.dao.SmsLogDao
import com.evostackr.smscampaignmanager.data.local.entity.SmsLogEntity
import kotlinx.coroutines.flow.Flow

class SmsRepository(
    private val smsLogDao: SmsLogDao
) {
    val allLogs: Flow<List<SmsLogEntity>> = smsLogDao.getAllLogs()

    fun getLogsForCampaign(campaignId: Long): Flow<List<SmsLogEntity>> {
        return smsLogDao.getLogsForCampaign(campaignId)
    }

    /** All logs filtered by type (e.g. TYPE_SMS) */
    fun getLogsByType(type: String): Flow<List<SmsLogEntity>> = smsLogDao.getLogsByType(type)

    /**
     * Log an SMS send event.
     *
     * @param phoneNumber   Destination phone number.
     * @param message       Message text that was sent.
     * @param result        One of SmsLogEntity.RESULT_SUCCESS / RESULT_FAILED / RESULT_CANCELLED.
     * @param campaignId    Campaign this message belongs to, or null for standalone sends.
     * @param errorMessage  Error description on failure, null on success.
     * @param messageType   SmsLogEntity.TYPE_SMS.
     * @param simUsed       SIM subscription ID used, or null if unknown.
     * @param retryCount    Number of retry attempts made.
     */
    suspend fun logMessage(
        phoneNumber: String,
        message: String,
        result: String,
        campaignId: Long? = null,
        errorMessage: String? = null,
        messageType: String = SmsLogEntity.TYPE_SMS,
        simUsed: Int? = null,
        retryCount: Int = 0
    ): Long {
        val log = SmsLogEntity(
            phoneNumber = phoneNumber,
            message = message,
            timestamp = System.currentTimeMillis(),
            result = result,
            campaignId = campaignId,
            errorMessage = errorMessage,
            messageType = messageType,
            simUsed = simUsed,
            retryCount = retryCount
        )
        return smsLogDao.insertLog(log)
    }

    suspend fun logSms(
        phoneNumber: String,
        message: String,
        result: String,
        campaignId: Long? = null,
        errorMessage: String? = null
    ): Long = logMessage(
        phoneNumber = phoneNumber,
        message = message,
        result = result,
        campaignId = campaignId,
        errorMessage = errorMessage,
        messageType = SmsLogEntity.TYPE_SMS
    )

    fun getSentCountSince(startTimestamp: Long): Flow<Int> {
        return smsLogDao.getSentCountSince(startTimestamp)
    }

    suspend fun getSentCountSinceDirect(startTimestamp: Long): Int {
        return smsLogDao.getSentCountSinceDirect(startTimestamp)
    }
}
