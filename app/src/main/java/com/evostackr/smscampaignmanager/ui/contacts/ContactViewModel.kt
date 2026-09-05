package com.evostackr.smscampaignmanager.ui.contacts

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evostackr.smscampaignmanager.data.local.dao.ImportedContactDao
import com.evostackr.smscampaignmanager.data.local.entity.ContactEntity
import com.evostackr.smscampaignmanager.data.repository.ContactRepository
import com.evostackr.smscampaignmanager.util.ContactImporter
import com.evostackr.smscampaignmanager.util.PhoneValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    application: Application,
    private val contactRepository: ContactRepository,
    private val importedContactDao: ImportedContactDao
) : AndroidViewModel(application) {

    val searchQuery = MutableStateFlow("")
    val selectedGroupTag = MutableStateFlow("ALL")

    val allGroups: StateFlow<List<String>> = contactRepository.allGroupTags.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val contacts: StateFlow<List<ContactEntity>> = combine(
        searchQuery,
        selectedGroupTag,
        contactRepository.allContacts
    ) { query, group, list ->
        list.filter { c ->
            val matchesQuery = c.name.contains(query, ignoreCase = true) || c.phoneNumber.contains(query)
            val matchesGroup = when (group) {
                "ALL" -> true
                "FAVORITES" -> c.isFavorite
                else -> c.groupTag?.equals(group, ignoreCase = true) == true
            }
            matchesQuery && matchesGroup
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun selectGroup(group: String) {
        selectedGroupTag.value = group
    }

    fun addContact(name: String, phone: String, groupTag: String?, email: String?) {
        val sanitized = PhoneValidator.sanitizePhoneNumber(phone)
        if (!PhoneValidator.isValidPhoneNumber(sanitized)) {
            _statusMessage.value = "Invalid phone number format"
            return
        }
        viewModelScope.launch {
            contactRepository.insertContact(
                ContactEntity(
                    name = name,
                    phoneNumber = sanitized,
                    groupTag = if (groupTag.isNullOrBlank()) null else groupTag,
                    email = if (email.isNullOrBlank()) null else email
                )
            )
            _statusMessage.value = "Contact added successfully"
        }
    }

    fun importCsvOrExcelFile(uri: Uri, isExcel: Boolean) {
        viewModelScope.launch {
            val imported = ContactImporter.importFromUri(getApplication(), uri, isExcel)
            if (imported.isNotEmpty()) {
                importedContactDao.insertAll(imported)

                // Also save into main contact table
                val contactEntities = imported.map {
                    ContactEntity(
                        name = it.name,
                        phoneNumber = it.phoneNumber,
                        groupTag = it.tag ?: "IMPORTED_${it.source}",
                        email = null,
                        notes = it.notes
                    )
                }
                contactRepository.insertContacts(contactEntities)
                _statusMessage.value = "Successfully imported ${imported.size} contacts from ${if (isExcel) "Excel" else "CSV"}"
            } else {
                _statusMessage.value = "No valid contacts found in file."
            }
        }
    }

    fun toggleFavorite(contact: ContactEntity) {
        viewModelScope.launch {
            contactRepository.updateContact(contact.copy(isFavorite = !contact.isFavorite))
        }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            contactRepository.deleteContact(contact)
        }
    }

    fun mergeDuplicates() {
        viewModelScope.launch {
            val count = contactRepository.mergeDuplicates()
            _statusMessage.value = "Merged $count duplicate contacts"
        }
    }

    fun importDeviceContacts() {
        viewModelScope.launch {
            val count = contactRepository.importDeviceContacts(getApplication())
            if (count > 0) {
                _statusMessage.value = "Imported $count contacts automatically"
            }
        }
    }
}

