package com.evostackr.smscampaignmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_sms")
data class ScheduledSmsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipientPhone: String,
    val recipientName: String? = null,
    val message: String,
    val scheduledTime: Long,
    val repeatInterval: String = REPEAT_ONE_TIME, // ONE_TIME, DAILY, WEEKLY, MONTHLY
    val status: String = STATUS_PENDING, // PENDING, EXECUTED, CANCELLED, FAILED
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val REPEAT_ONE_TIME = "ONE_TIME"
        const val REPEAT_DAILY = "DAILY"
        const val REPEAT_WEEKLY = "WEEKLY"
        const val REPEAT_MONTHLY = "MONTHLY"

        const val STATUS_PENDING = "PENDING"
        const val STATUS_EXECUTED = "EXECUTED"
        const val STATUS_CANCELLED = "CANCELLED"
        const val STATUS_FAILED = "FAILED"
    }
}
