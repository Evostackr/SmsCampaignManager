package com.evostackr.smscampaignmanager.data.local.dao

import androidx.room.*
import com.evostackr.smscampaignmanager.data.local.entity.ImportedContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportedContactDao {

    @Query("SELECT * FROM imported_contacts ORDER BY name ASC")
    fun getAllImportedContactsFlow(): Flow<List<ImportedContactEntity>>

    @Query("SELECT * FROM imported_contacts WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' OR phoneNumber LIKE '%' || :query || '%'")
    suspend fun searchImportedContacts(query: String): List<ImportedContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<ImportedContactEntity>)

    @Query("DELETE FROM imported_contacts")
    suspend fun deleteAll()
}

