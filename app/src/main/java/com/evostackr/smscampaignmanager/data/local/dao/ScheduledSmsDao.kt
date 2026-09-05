package com.evostackr.smscampaignmanager.data.local.dao

import androidx.room.*
import com.evostackr.smscampaignmanager.data.local.entity.ScheduledSmsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledSmsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledSms(scheduledSms: ScheduledSmsEntity): Long

    @Update
    suspend fun updateScheduledSms(scheduledSms: ScheduledSmsEntity)

    @Delete
    suspend fun deleteScheduledSms(scheduledSms: ScheduledSmsEntity)

    @Query("SELECT * FROM scheduled_sms ORDER BY scheduledTime ASC")
    fun getAllScheduledSms(): Flow<List<ScheduledSmsEntity>>

    @Query("SELECT * FROM scheduled_sms WHERE status = 'PENDING' AND scheduledTime <= :currentTime ORDER BY scheduledTime ASC")
    suspend fun getDuePendingScheduledSms(currentTime: Long): List<ScheduledSmsEntity>

    @Query("UPDATE scheduled_sms SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}

