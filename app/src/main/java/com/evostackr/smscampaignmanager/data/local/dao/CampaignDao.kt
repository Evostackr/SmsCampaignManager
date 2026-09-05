package com.evostackr.smscampaignmanager.data.local.dao

import androidx.room.*
import com.evostackr.smscampaignmanager.data.local.entity.CampaignEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaignDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: CampaignEntity): Long

    @Update
    suspend fun updateCampaign(campaign: CampaignEntity)

    @Delete
    suspend fun deleteCampaign(campaign: CampaignEntity)

    @Query("SELECT * FROM campaigns WHERE id = :id")
    suspend fun getCampaignById(id: Long): CampaignEntity?

    @Query("SELECT * FROM campaigns WHERE id = :id")
    fun getCampaignByIdFlow(id: Long): Flow<CampaignEntity?>

    @Query("SELECT * FROM campaigns ORDER BY createdAt DESC")
    fun getAllCampaigns(): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM campaigns ORDER BY createdAt DESC")
    suspend fun getAllCampaignsDirect(): List<CampaignEntity>

    @Query("SELECT * FROM campaigns WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getCampaignsByStatusDirect(status: String): List<CampaignEntity>

    @Query("""
        SELECT * FROM campaigns 
        WHERE (:query IS NULL OR :query = '' OR name LIKE '%' || :query || '%')
        AND (:statusFilter IS NULL OR :statusFilter = '' OR :statusFilter = 'ALL' OR status = :statusFilter)
        ORDER BY createdAt DESC
    """)
    fun searchAndFilterCampaigns(query: String?, statusFilter: String?): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM campaigns WHERE status IN ('IN_PROGRESS', 'PAUSED') ORDER BY startedAt DESC LIMIT 1")
    fun getActiveCampaign(): Flow<CampaignEntity?>

    @Query("SELECT * FROM campaigns WHERE status IN ('IN_PROGRESS', 'PAUSED') ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveCampaignDirect(): CampaignEntity?

    @Query("UPDATE campaigns SET nextAllowedSendTime = :time WHERE id = :campaignId")
    suspend fun updateNextAllowedSendTime(campaignId: Long, time: Long)

    @Query("UPDATE campaigns SET status = :status WHERE id = :campaignId")
    suspend fun updateCampaignStatus(campaignId: Long, status: String)

    @Query("UPDATE campaigns SET sentCount = sentCount + 1 WHERE id = :campaignId")
    suspend fun incrementSentCount(campaignId: Long)

    @Query("UPDATE campaigns SET failedCount = failedCount + 1 WHERE id = :campaignId")
    suspend fun incrementFailedCount(campaignId: Long)
}

