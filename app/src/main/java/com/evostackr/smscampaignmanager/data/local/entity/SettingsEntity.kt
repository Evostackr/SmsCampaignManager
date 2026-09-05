package com.evostackr.smscampaignmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val monthlyLimit: Int = 100,
    val defaultDelaySeconds: Int = 30,
    val maxRetryCount: Int = 3,
    val darkTheme: Boolean? = null, // null for system default
    val notificationsEnabled: Boolean = true,
    val sentThisMonth: Int = 0,
    val lastResetMonthYear: String = "", // e.g. "2026-07"
    val dailyLimit: Int = 100,
    val sentToday: Int = 0,
    val lastResetDay: String = "", // e.g. "2026-08-04"
    val autoResumeEnabled: Boolean = true,
    val delayMode: String = MODE_FIXED,
    val minDelaySeconds: Int = 30,
    val maxDelaySeconds: Int = 90,
    val voiceEnabled: Boolean = true,
    val autoSuggestTemplates: Boolean = true,
    val undoWindowSeconds: Int = 10,
    val backupFolderUri: String? = null,
    val autoBackupEnabled: Boolean = true,
    val backupFrequency: String = FREQUENCY_DAILY,
    val lastBackupTimestamp: Long = 0L,
    val lastBackupSize: Long = 0L,
    val recoverySource: String = SOURCE_FRESH_START,
    val isRecoveryWizardPending: Boolean = false,
    val isOnboardingCompleted: Boolean = false
) {
    companion object {
        const val MODE_FIXED = "FIXED"
        const val MODE_RANDOM = "RANDOM"
        const val MODE_NONE = "NONE"

        const val FREQUENCY_DAILY = "DAILY"
        const val FREQUENCY_WEEKLY = "WEEKLY"
        const val FREQUENCY_MANUAL = "MANUAL"

        const val SOURCE_FRESH_START = "FRESH_START"
        const val SOURCE_BACKUP_RESTORE = "BACKUP_RESTORE"
        const val SOURCE_SMS_HISTORY = "SMS_HISTORY_ESTIMATE"
    }
}
