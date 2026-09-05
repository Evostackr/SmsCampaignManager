package com.evostackr.smscampaignmanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contact_groups",
    indices = [Index(value = ["name"], unique = true)]
)
data class ContactGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val colorHex: String? = null
)

