package com.evostackr.smscampaignmanager.ui.campaign

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evostackr.smscampaignmanager.data.local.entity.CampaignEntity
import com.evostackr.smscampaignmanager.data.local.entity.RecipientEntity
import com.evostackr.smscampaignmanager.data.repository.CampaignRepository
import com.evostackr.smscampaignmanager.data.repository.TemplateRepository
import com.evostackr.smscampaignmanager.service.SmsSendingService
import com.evostackr.smscampaignmanager.util.CsvParser
import com.evostackr.smscampaignmanager.util.ParsedContact
import com.evostackr.smscampaignmanager.util.ContactsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateCampaignUiState(
    val campaignName: String = "",
    val rawTextRecipients: String = "",
    val parsedContacts: List<ParsedContact> = emptyList(),
    val removeDuplicates: Boolean = true,
    val invalidCount: Int = 0,
    val duplicateCount: Int = 0,
    val messageText: String = "",
    val delaySeconds: Int = 30,
    val isConfirmDialogOpen: Boolean = false,
    val importSummaryMessage: String? = null,
    val isImportingContacts: Boolean = false
)

@HiltViewModel
class CampaignViewModel @Inject constructor(
    application: Application,
    private val campaignRepository: CampaignRepository,
    private val templateRepository: TemplateRepository
) : AndroidViewModel(application) {

    val searchQuery = MutableStateFlow("")
    val selectedStatusFilter = MutableStateFlow("ALL")

    val allCampaigns: StateFlow<List<CampaignEntity>> = combine(
        searchQuery,
        selectedStatusFilter
    ) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        campaignRepository.searchAndFilterCampaigns(query, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _createUiState = MutableStateFlow(CreateCampaignUiState())
    val createUiState: StateFlow<CreateCampaignUiState> = _createUiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateStatusFilter(filter: String) {
        selectedStatusFilter.value = filter
    }

    fun importDeviceContacts() {
        viewModelScope.launch {
            _createUiState.update { it.copy(isImportingContacts = true) }
            val contacts = ContactsManager.getDeviceContacts(getApplication())
            addManualContacts(contacts)
            _createUiState.update {
                it.copy(
                    isImportingContacts = false,
                    importSummaryMessage = if (contacts.isNotEmpty()) "Imported ${contacts.size} device contacts" else "No contacts found or permission missing"
                )
            }
        }
    }

    fun updateCampaignName(name: String) {
        _createUiState.update { it.copy(campaignName = name) }
    }

    fun updateRawText(text: String) {
        _createUiState.update { it.copy(rawTextRecipients = text) }
        reparseRawText()
    }

    fun setRemoveDuplicates(remove: Boolean) {
        _createUiState.update { it.copy(removeDuplicates = remove) }
        reparseRawText()
    }

    fun updateMessageText(message: String) {
        _createUiState.update { it.copy(messageText = message) }
    }

    fun updateDelaySeconds(seconds: Int) {
        _createUiState.update { it.copy(delaySeconds = seconds.coerceIn(5, 600)) }
    }

    fun parseCsvFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                    val result = CsvParser.parseCsv(stream, _createUiState.value.removeDuplicates)
                    _createUiState.update { state ->
                        state.copy(
                            parsedContacts = (state.parsedContacts + result.validContacts).distinctBy { it.phoneNumber },
                            invalidCount = state.invalidCount + result.invalidCount,
                            duplicateCount = state.duplicateCount + result.duplicateCount,
                            importSummaryMessage = "Imported ${result.validContacts.size} contacts (${result.invalidCount} invalid, ${result.duplicateCount} duplicates skipped)"
                        )
                    }
                }
            } catch (e: Exception) {
                _createUiState.update { it.copy(importSummaryMessage = "Failed to parse CSV file: ${e.message}") }
            }
        }
    }

    fun addManualContacts(contacts: List<ParsedContact>) {
        _createUiState.update { state ->
            val merged = if (state.removeDuplicates) {
                (state.parsedContacts + contacts).distinctBy { it.phoneNumber }
            } else {
                state.parsedContacts + contacts
            }
            state.copy(parsedContacts = merged)
        }
    }

    private fun reparseRawText() {
        val raw = _createUiState.value.rawTextRecipients
        if (raw.isBlank()) return
        val result = CsvParser.parseRawText(raw, _createUiState.value.removeDuplicates)
        _createUiState.update { state ->
            state.copy(
                parsedContacts = result.validContacts,
                invalidCount = result.invalidCount,
                duplicateCount = result.duplicateCount
            )
        }
    }

    fun clearRecipients() {
        _createUiState.update { it.copy(parsedContacts = emptyList(), rawTextRecipients = "", invalidCount = 0, duplicateCount = 0) }
    }

    fun showConfirmDialog() {
        _createUiState.update { it.copy(isConfirmDialogOpen = true) }
    }

    fun hideConfirmDialog() {
        _createUiState.update { it.copy(isConfirmDialogOpen = false) }
    }

    // ── Campaign start ────────────────────────────────────────────────────────

    fun startCampaign(onSuccess: (Long) -> Unit) {
        val state = _createUiState.value
        if (state.campaignName.isBlank() || state.parsedContacts.isEmpty() || state.messageText.isBlank()) return

        viewModelScope.launch {
            val campaignId = campaignRepository.createCampaignWithRecipients(
                name = state.campaignName,
                messageTemplate = state.messageText,
                delaySeconds = state.delaySeconds,
                recipients = state.parsedContacts
            )
            // Save as recent template if title provided
            templateRepository.saveTemplate(
                title = "Template - ${state.campaignName}",
                content = state.messageText
            )
            hideConfirmDialog()
            _createUiState.value = CreateCampaignUiState() // reset form
            SmsSendingService.startCampaign(getApplication(), campaignId)
            onSuccess(campaignId)
        }
    }

    fun pauseCampaign(campaignId: Long) {
        SmsSendingService.pauseCampaign(getApplication())
    }

    fun resumeCampaign(campaignId: Long) {
        SmsSendingService.startCampaign(getApplication(), campaignId)
    }

    fun cancelCampaign(campaignId: Long) {
        SmsSendingService.cancelCampaign(getApplication())
    }

    fun getRecipientsForCampaign(campaignId: Long): Flow<List<RecipientEntity>> {
        return campaignRepository.getRecipientsForCampaign(campaignId)
    }

    fun getCampaignFlow(campaignId: Long): Flow<CampaignEntity?> {
        return campaignRepository.getCampaignFlow(campaignId)
    }

    fun retryFailedRecipients(campaignId: Long) {
        viewModelScope.launch {
            campaignRepository.retryFailedRecipients(campaignId)
            SmsSendingService.startCampaign(getApplication(), campaignId)
        }
    }
}
