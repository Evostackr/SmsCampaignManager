package com.evostackr.smscampaignmanager.data.local.dao

import androidx.room.*
import com.evostackr.smscampaignmanager.data.local.entity.ContactEntity
import com.evostackr.smscampaignmanager.data.local.entity.ContactGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>): LongArray

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    suspend fun getAllContactsDirect(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE groupTag = :groupTag ORDER BY name ASC")
    fun getContactsByGroup(groupTag: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE groupTag = :groupTag ORDER BY name ASC")
    suspend fun getContactsByGroupDirect(groupTag: String): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchContacts(query: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getContactByPhone(phoneNumber: String): ContactEntity?

    @Query("SELECT groupTag FROM contacts WHERE groupTag IS NOT NULL AND groupTag != '' GROUP BY groupTag")
    fun getAllGroupTags(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGroup(group: ContactGroupEntity): Long

    @Query("SELECT * FROM contact_groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<ContactGroupEntity>>
}

