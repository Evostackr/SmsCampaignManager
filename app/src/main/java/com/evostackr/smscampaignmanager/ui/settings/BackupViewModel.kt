package com.evostackr.smscampaignmanager.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity
import com.evostackr.smscampaignmanager.data.repository.BackupRepository
import com.evostackr.smscampaignmanager.data.repository.SettingsRepository
import com.evostackr.smscampaignmanager.util.CorruptedBackupException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    application: Application,
    private val backupRepository: BackupRepository,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    val settings: StateFlow<SettingsEntity?> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun createBackup(outputStream: OutputStream) {
        viewModelScope.launch {
            try {
                val bytes = backupRepository.createEncryptedBackup(outputStream)
                _statusMessage.value = "Encrypted backup created (${bytes / 1024} KB)!"
            } catch (e: Exception) {
                _statusMessage.value = "Backup error: ${e.message}"
            }
        }
    }

    fun restoreBackup(inputStream: InputStream) {
        viewModelScope.launch {
            try {
                val success = backupRepository.restoreEncryptedBackup(inputStream)
                if (success) {
                    _statusMessage.value = "Backup restored successfully!"
                } else {
                    _statusMessage.value = "Failed to restore backup."
                }
            } catch (e: CorruptedBackupException) {
                _statusMessage.value = "Integrity Error: ${e.message}"
            } catch (e: Exception) {
                _statusMessage.value = "Restore error: ${e.message}"
            }
        }
    }

    fun recoverFromSmsHistory() {
        viewModelScope.launch {
            val stats = backupRepository.recoverFromDeviceSmsHistory()
            _statusMessage.value = "Recovered ${stats.sentThisMonth} sent SMS from system log!"
        }
    }

    fun setupAutoBackup(frequency: String, folderUri: String) {
        viewModelScope.launch {
            backupRepository.scheduleAutoBackup(frequency, folderUri)
            _statusMessage.value = "Auto-backup scheduled ($frequency)"
        }
    }

    fun startFresh() {
        viewModelScope.launch {
            val current = settingsRepository.getOrInitSettings()
            settingsRepository.updateSettings(
                current.copy(
                    recoverySource = SettingsEntity.SOURCE_FRESH_START,
                    isRecoveryWizardPending = false
                )
            )
        }
    }
}

