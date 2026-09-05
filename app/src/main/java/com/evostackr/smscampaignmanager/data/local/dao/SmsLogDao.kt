package com.evostackr.smscampaignmanager.data.local.dao

import androidx.room.*
import com.evostackr.smscampaignmanager.data.local.entity.SmsLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SmsLogEntity): Long

    @Query("SELECT * FROM sms_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SmsLogEntity>>

    @Query("SELECT * FROM sms_logs WHERE campaignId = :campaignId ORDER BY timestamp DESC")
    fun getLogsForCampaign(campaignId: Long): Flow<List<SmsLogEntity>>

    @Query("SELECT COUNT(*) FROM sms_logs WHERE result = 'SUCCESS' AND timestamp >= :startTimestamp")
    fun getSentCountSince(startTimestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM sms_logs WHERE result = 'SUCCESS' AND timestamp >= :startTimestamp")
    suspend fun getSentCountSinceDirect(startTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM sms_logs WHERE result = 'FAILED'")
    fun getFailedCountTotal(): Flow<Int>

    @Query("SELECT * FROM sms_logs WHERE messageType = :type ORDER BY timestamp DESC")
    fun getLogsByType(type: String): Flow<List<SmsLogEntity>>
}
