package com.evostackr.smscampaignmanager.data.local.dao

import androidx.room.*
import com.evostackr.smscampaignmanager.data.local.entity.RecipientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipients(recipients: List<RecipientEntity>): List<Long>

    @Update
    suspend fun updateRecipient(recipient: RecipientEntity)

    @Query("SELECT * FROM recipients WHERE campaignId = :campaignId ORDER BY id ASC")
    fun getRecipientsForCampaign(campaignId: Long): Flow<List<RecipientEntity>>

    @Query("SELECT * FROM recipients WHERE campaignId = :campaignId ORDER BY id ASC")
    suspend fun getRecipientsForCampaignDirect(campaignId: Long): List<RecipientEntity>

    @Query("SELECT * FROM recipients WHERE campaignId = :campaignId AND (status = 'PENDING' OR status = 'SENDING') ORDER BY id ASC LIMIT 1")
    suspend fun getNextPendingRecipient(campaignId: Long): RecipientEntity?

    @Query("SELECT * FROM recipients WHERE campaignId = :campaignId AND status = 'PENDING' ORDER BY id ASC")
    suspend fun getPendingRecipients(campaignId: Long): List<RecipientEntity>

    @Query("SELECT COUNT(*) FROM recipients WHERE campaignId = :campaignId AND status = 'PENDING'")
    fun getPendingCountForCampaign(campaignId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM recipients WHERE status = 'PENDING'")
    fun getTotalPendingQueueCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM recipients WHERE status = 'FAILED'")
    fun getTotalFailedCount(): Flow<Int>

    @Query("UPDATE recipients SET status = :status, errorReason = :errorReason, sentTime = :sentTime WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, errorReason: String? = null, sentTime: Long? = null)

    @Query("UPDATE recipients SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetryCount(id: Long)
}

