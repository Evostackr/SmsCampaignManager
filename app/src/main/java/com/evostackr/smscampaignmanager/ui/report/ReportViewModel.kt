package com.evostackr.smscampaignmanager.ui.report

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evostackr.smscampaignmanager.SmsApplication
import com.evostackr.smscampaignmanager.data.local.entity.SmsLogEntity
import com.evostackr.smscampaignmanager.data.repository.CampaignRepository
import com.evostackr.smscampaignmanager.data.repository.SmsRepository
import com.evostackr.smscampaignmanager.util.CsvExporter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class ReportUiState(
    val totalSent: Int = 0,
    val totalFailed: Int = 0,
    val totalPending: Int = 0,
    val successRatePercentage: Int = 100,
    val logs: List<SmsLogEntity> = emptyList(),
    val exportStatusMessage: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    application: Application,
    private val smsRepository: SmsRepository,
    private val campaignRepository: CampaignRepository
) : AndroidViewModel(application) {

    val logs: StateFlow<List<SmsLogEntity>> = smsRepository.allLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val uiState: StateFlow<ReportUiState> = combine(
        smsRepository.allLogs,
        campaignRepository.totalFailedCount,
        campaignRepository.totalPendingQueueCount
    ) { logList, failed, pending ->
        val successCount = logList.count { it.result == SmsLogEntity.RESULT_SUCCESS }
        val totalAttempts = successCount + failed
        val rate = if (totalAttempts > 0) ((successCount.toDouble() / totalAttempts.toDouble()) * 100).toInt() else 100

        ReportUiState(
            totalSent = successCount,
            totalFailed = failed,
            totalPending = pending,
            successRatePercentage = rate,
            logs = logList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportUiState()
    )

    fun exportReportToUri(uri: Uri) {
        viewModelScope.launch {
            try {
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { stream ->
                    CsvExporter.exportLogsToCsv(stream, uiState.value.logs)
                }
            } catch (e: Exception) {
                // handle export error
            }
        }
    }
}

