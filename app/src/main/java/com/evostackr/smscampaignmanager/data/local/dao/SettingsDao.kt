package com.evostackr.smscampaignmanager.data.local.dao

import androidx.room.*
import com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SettingsEntity)

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettingsDirect(): SettingsEntity?

    @Query("UPDATE settings SET sentThisMonth = sentThisMonth + :count WHERE id = 1")
    suspend fun incrementMonthlySent(count: Int = 1)

    @Query("UPDATE settings SET sentToday = sentToday + :count WHERE id = 1")
    suspend fun incrementDailySent(count: Int = 1)

    @Query("UPDATE settings SET sentThisMonth = 0, lastResetMonthYear = :monthYear WHERE id = 1")
    suspend fun resetMonthlyCounter(monthYear: String)

    @Query("UPDATE settings SET sentToday = 0, lastResetDay = :day WHERE id = 1")
    suspend fun resetDailyCounter(day: String)

    suspend fun getOrInitSettings(): SettingsEntity {
        return getSettingsDirect() ?: SettingsEntity().also { insertOrUpdateSettings(it) }
    }

    suspend fun updateSettings(settings: SettingsEntity) {
        insertOrUpdateSettings(settings)
    }
}

