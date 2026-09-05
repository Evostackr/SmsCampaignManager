package com.evostackr.smscampaignmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaigns")
data class CampaignEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val messageTemplate: String,
    val delaySeconds: Int = 30,
    val status: String = STATUS_QUEUED, // DRAFT, QUEUED, IN_PROGRESS, PAUSED, COMPLETED, CANCELLED, FAILED
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    val totalRecipients: Int = 0,
    val sentCount: Int = 0,
    val failedCount: Int = 0,
    val nextAllowedSendTime: Long = 0L
) {
    companion object {
        const val STATUS_DRAFT = "DRAFT"
        const val STATUS_QUEUED = "QUEUED"
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_PAUSED = "PAUSED"
        const val STATUS_PAUSED_DAILY_LIMIT = "PAUSED_DAILY_LIMIT"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_CANCELLED = "CANCELLED"
        const val STATUS_FAILED = "FAILED"
    }
}
