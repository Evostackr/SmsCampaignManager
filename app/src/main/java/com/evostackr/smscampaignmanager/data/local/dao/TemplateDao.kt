package com.evostackr.smscampaignmanager.data.local.dao

import androidx.room.*
import com.evostackr.smscampaignmanager.data.local.entity.TemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity): Long

    @Update
    suspend fun updateTemplate(template: TemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: TemplateEntity)

    @Query("SELECT * FROM templates ORDER BY isFavorite DESC, lastUsedAt DESC, createdAt DESC")
    fun getAllTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE (:query IS NULL OR :query = '' OR title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND (:category IS NULL OR :category = 'All' OR category = :category) ORDER BY CASE WHEN :sortBy = 'RECENT' THEN lastUsedAt END DESC, CASE WHEN :sortBy = 'ALPHABETICAL' THEN title END ASC, createdAt DESC")
    fun searchAndFilterTemplates(query: String?, category: String?, sortBy: String?): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates ORDER BY id DESC")
    suspend fun getAllTemplatesDirect(): List<TemplateEntity>

    @Query("SELECT COUNT(*) FROM templates WHERE isBuiltIn = 1")
    suspend fun getBuiltInTemplateCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTemplates(templates: List<TemplateEntity>)

    @Query("SELECT * FROM templates WHERE isFavorite = 1 ORDER BY lastUsedAt DESC")
    fun getFavoriteTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates ORDER BY lastUsedAt DESC LIMIT 5")
    fun getRecentlyUsedTemplates(): Flow<List<TemplateEntity>>

    @Query("UPDATE templates SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun markTemplateUsed(id: Long, timestamp: Long = System.currentTimeMillis())
}

