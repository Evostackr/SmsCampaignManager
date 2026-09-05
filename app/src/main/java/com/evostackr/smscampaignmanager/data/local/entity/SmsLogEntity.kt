package com.evostackr.smscampaignmanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sms_logs",
    indices = [Index(value = ["campaignId"]), Index(value = ["timestamp"]), Index(value = ["result"]), Index(value = ["messageType"])]
)
data class SmsLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val result: String, // SUCCESS, FAILED, CANCELLED
    val campaignId: Long? = null,
    val errorMessage: String? = null,
    val messageType: String = TYPE_SMS,
    val simUsed: Int? = null,
    val retryCount: Int = 0
) {
    companion object {
        const val RESULT_SUCCESS = "SUCCESS"
        const val RESULT_FAILED = "FAILED"
        const val RESULT_CANCELLED = "CANCELLED"
        const val TYPE_SMS = "SMS"
    }
}
