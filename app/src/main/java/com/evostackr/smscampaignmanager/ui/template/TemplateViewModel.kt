package com.evostackr.smscampaignmanager.ui.template

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evostackr.smscampaignmanager.SmsApplication
import com.evostackr.smscampaignmanager.data.local.entity.TemplateEntity
import com.evostackr.smscampaignmanager.data.repository.TemplateRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TemplateViewModel @Inject constructor(
    application: Application,
    private val templateRepository: TemplateRepository
) : AndroidViewModel(application) {

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val selectedSort = MutableStateFlow("RECENT") // "RECENT" or "ALPHABETICAL"

    val allTemplates: StateFlow<List<TemplateEntity>> = combine(
        searchQuery,
        selectedCategory,
        selectedSort
    ) { query, category, sort ->
        Triple(query, category, sort)
    }.flatMapLatest { (query, category, sort) ->
        templateRepository.searchAndFilterTemplates(query, category, sort)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val builtInCategories = listOf("All", "Marketing", "Business", "Payment Reminder", "Appointment Reminder", "Greetings", "Festival Wishes", "OTP", "Delivery", "Education", "Healthcare", "Personal")

    fun createTemplate(title: String, content: String, isFavorite: Boolean = false) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            templateRepository.saveTemplate(title, content, isFavorite)
        }
    }

    fun toggleFavorite(template: TemplateEntity) {
        viewModelScope.launch {
            templateRepository.toggleFavorite(template)
        }
    }

    fun duplicateTemplate(template: TemplateEntity) {
        viewModelScope.launch {
            templateRepository.duplicateTemplate(template)
        }
    }

    fun deleteTemplate(template: TemplateEntity) {
        if (template.isBuiltIn) return
        viewModelScope.launch {
            templateRepository.deleteTemplate(template)
        }
    }

    fun markUsed(id: Long) {
        viewModelScope.launch {
            templateRepository.markTemplateUsed(id)
        }
    }
}

