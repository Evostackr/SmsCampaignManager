package com.evostackr.smscampaignmanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipients",
    foreignKeys = [
        ForeignKey(
            entity = CampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["campaignId"]), Index(value = ["phoneNumber"]), Index(value = ["campaignId", "status"])]
)
data class RecipientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val campaignId: Long,
    val name: String? = null,
    val phoneNumber: String,
    val company: String? = null,
    val date: String? = null,
    val time: String? = null,
    val amount: String? = null,
    val city: String? = null,
    val custom: String? = null,
    val status: String = STATUS_PENDING, // PENDING, SENDING, SENT, FAILED, SKIPPED
    val retryCount: Int = 0,
    val sentTime: Long? = null,
    val errorReason: String? = null
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_SENDING = "SENDING"
        const val STATUS_SENT = "SENT"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_SKIPPED = "SKIPPED"
    }
}

