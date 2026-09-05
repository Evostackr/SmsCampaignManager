package com.evostackr.smscampaignmanager.data.repository

import com.evostackr.smscampaignmanager.data.local.dao.TemplateDao
import com.evostackr.smscampaignmanager.data.local.entity.TemplateEntity
import kotlinx.coroutines.flow.Flow

class TemplateRepository(
    private val templateDao: TemplateDao
) {
    val allTemplates: Flow<List<TemplateEntity>> = templateDao.getAllTemplates()
    val favoriteTemplates: Flow<List<TemplateEntity>> = templateDao.getFavoriteTemplates()
    val recentlyUsedTemplates: Flow<List<TemplateEntity>> = templateDao.getRecentlyUsedTemplates()

    suspend fun initializeSeedData() {
        com.evostackr.smscampaignmanager.util.TemplateSeeder.seedBuiltInTemplates(templateDao)
    }

    fun searchAndFilterTemplates(query: String?, category: String?, sortBy: String?): Flow<List<TemplateEntity>> {
        return templateDao.searchAndFilterTemplates(query, category, sortBy)
    }

    suspend fun saveTemplate(title: String, content: String, isFavorite: Boolean = false): Long {
        val template = TemplateEntity(
            title = title,
            content = content,
            isFavorite = isFavorite
        )
        return templateDao.insertTemplate(template)
    }

    suspend fun updateTemplate(template: TemplateEntity) {
        if (!template.isBuiltIn) {
            templateDao.updateTemplate(template)
        }
    }

    suspend fun toggleFavorite(template: TemplateEntity) {
        templateDao.updateTemplate(template.copy(isFavorite = !template.isFavorite))
    }

    suspend fun duplicateTemplate(template: TemplateEntity) {
        val duplicate = template.copy(
            id = 0,
            title = "${template.title} (Copy)",
            isBuiltIn = false,
            createdAt = System.currentTimeMillis()
        )
        templateDao.insertTemplate(duplicate)
    }

    suspend fun deleteTemplate(template: TemplateEntity) {
        templateDao.deleteTemplate(template)
    }

    suspend fun markTemplateUsed(id: Long) {
        templateDao.markTemplateUsed(id)
    }
}

