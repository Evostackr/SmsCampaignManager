package com.evostackr.smscampaignmanager.data.repository

import com.evostackr.smscampaignmanager.data.local.dao.ContactDao
import com.evostackr.smscampaignmanager.data.local.entity.ContactEntity
import com.evostackr.smscampaignmanager.data.local.entity.ContactGroupEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(
    private val contactDao: ContactDao
) {
    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
    val favoriteContacts: Flow<List<ContactEntity>> = contactDao.getFavoriteContacts()
    val allGroupTags: Flow<List<String>> = contactDao.getAllGroupTags()
    val allGroups: Flow<List<ContactGroupEntity>> = contactDao.getAllGroups()

    suspend fun insertContact(contact: ContactEntity): Long {
        return contactDao.insertContact(contact)
    }

    suspend fun insertContacts(contacts: List<ContactEntity>): LongArray {
        return contactDao.insertContacts(contacts)
    }

    suspend fun updateContact(contact: ContactEntity) {
        contactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: ContactEntity) {
        contactDao.deleteContact(contact)
    }

    fun getContactsByGroup(groupTag: String): Flow<List<ContactEntity>> {
        return contactDao.getContactsByGroup(groupTag)
    }

    suspend fun getContactsByGroupDirect(groupTag: String): List<ContactEntity> {
        return contactDao.getContactsByGroupDirect(groupTag)
    }

    fun searchContacts(query: String): Flow<List<ContactEntity>> {
        return contactDao.searchContacts(query)
    }

    suspend fun getAllContactsDirect(): List<ContactEntity> {
        return contactDao.getAllContactsDirect()
    }

    suspend fun insertGroup(name: String, description: String? = null): Long {
        return contactDao.insertGroup(ContactGroupEntity(name = name, description = description))
    }

    suspend fun mergeDuplicates(): Int {
        val contacts = contactDao.getAllContactsDirect()
        val seenPhones = mutableSetOf<String>()
        var mergedCount = 0

        for (c in contacts) {
            val phone = c.phoneNumber.replace("\\s+".toRegex(), "")
            if (seenPhones.contains(phone)) {
                contactDao.deleteContact(c)
                mergedCount++
            } else {
                seenPhones.add(phone)
            }
        }
        return mergedCount
    }

    suspend fun importDeviceContacts(context: android.content.Context): Int {
        val deviceContacts = com.evostackr.smscampaignmanager.util.ContactsManager.getDeviceContacts(context)
        if (deviceContacts.isEmpty()) return 0

        val existing = contactDao.getAllContactsDirect()
        val existingPhones = existing.map { it.phoneNumber.replace("\\s+".toRegex(), "") }.toSet()

        val newEntities = deviceContacts.filter {
            !existingPhones.contains(it.phoneNumber.replace("\\s+".toRegex(), ""))
        }.map {
            ContactEntity(
                name = it.name ?: "Unknown Contact",
                phoneNumber = it.phoneNumber,
                groupTag = "Imported"
            )
        }

        if (newEntities.isNotEmpty()) {
            contactDao.insertContacts(newEntities)
        }
        return newEntities.size
    }
}

