package com.evostackr.smscampaignmanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["phoneNumber"], unique = true),
        Index(value = ["name"]),
        Index(value = ["groupTag"])
    ]
)
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val email: String? = null,
    val birthday: String? = null, // YYYY-MM-DD
    val groupTag: String? = null, // e.g. "VIP", "Pending", "Customers"
    val isFavorite: Boolean = false,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

