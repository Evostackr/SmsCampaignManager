package com.evostackr.smscampaignmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "imported_contacts")
data class ImportedContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val tag: String? = null,
    val company: String? = null,
    val notes: String? = null,
    val source: String = "CSV" // "CSV" or "EXCEL"
)

