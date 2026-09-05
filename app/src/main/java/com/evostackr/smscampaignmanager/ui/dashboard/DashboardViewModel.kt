package com.evostackr.smscampaignmanager.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evostackr.smscampaignmanager.SmsApplication
import com.evostackr.smscampaignmanager.data.local.entity.CampaignEntity
import com.evostackr.smscampaignmanager.data.repository.CampaignRepository
import com.evostackr.smscampaignmanager.data.repository.SettingsRepository
import com.evostackr.smscampaignmanager.data.repository.SmsRepository
import com.evostackr.smscampaignmanager.service.SmsSendingService
import com.evostackr.smscampaignmanager.util.SimManager
import com.evostackr.smscampaignmanager.util.SimStateInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class DashboardUiState(
    val monthlyLimit: Int = 100,
    val sentThisMonth: Int = 0,
    val remainingThisMonth: Int = 100,
    val sentToday: Int = 0,
    val failedCount: Int = 0,
    val pendingQueueCount: Int = 0,
    val activeCampaign: CampaignEntity? = null,
    val estimatedCompletionTime: String? = null,
    val simState: SimStateInfo = SimStateInfo(hasSim = false, isAirplaneModeOn = false, simList = emptyList(), primaryCarrierName = "Checking SIM...")
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    application: Application,
    private val campaignRepository: CampaignRepository,
    private val settingsRepository: SettingsRepository,
    private val smsRepository: SmsRepository
) : AndroidViewModel(application) {

    private val _simState = MutableStateFlow(SimManager.getSimState(application))
    val simState: StateFlow<SimStateInfo> = _simState.asStateFlow()

    private fun getTodayStartTimestamp(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        settingsRepository.settingsFlow,
        smsRepository.getSentCountSince(getTodayStartTimestamp()),
        campaignRepository.totalFailedCount,
        campaignRepository.totalPendingQueueCount,
        campaignRepository.activeCampaign
    ) { settings, todaySent, failed, pending, active ->
        val limit = settings?.monthlyLimit ?: 100
        val monthSent = settings?.sentThisMonth ?: 0
        val remaining = (limit - monthSent).coerceAtLeast(0)

        val remainingPending = active?.let { (it.totalRecipients - (it.sentCount + it.failedCount)).coerceAtLeast(0) } ?: 0
        val estSeconds = if (active != null && remainingPending > 0) remainingPending * active.delaySeconds else 0
        val estText = if (estSeconds > 0) {
            val mins = estSeconds / 60
            val secs = estSeconds % 60
            if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
        } else null

        DashboardUiState(
            monthlyLimit = limit,
            sentThisMonth = monthSent,
            remainingThisMonth = remaining,
            sentToday = todaySent,
            failedCount = failed,
            pendingQueueCount = pending,
            activeCampaign = active,
            estimatedCompletionTime = estText,
            simState = _simState.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun refreshSimState() {
        _simState.value = SimManager.getSimState(getApplication())
    }

    fun pauseActiveCampaign() {
        SmsSendingService.pauseCampaign(getApplication())
    }

    fun resumeActiveCampaign() {
        SmsSendingService.resumeCampaign(getApplication())
    }

    fun cancelActiveCampaign() {
        SmsSendingService.cancelCampaign(getApplication())
    }
}

