package com.evostackr.smscampaignmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.evostackr.smscampaignmanager.data.local.entity.CampaignEntity
import com.evostackr.smscampaignmanager.data.repository.CampaignRepository
import com.evostackr.smscampaignmanager.data.repository.SettingsRepository
import com.evostackr.smscampaignmanager.service.SmsSendingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MidnightResetReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var campaignRepository: CampaignRepository

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MidnightResetReceiver", "Midnight Reset triggered")

        CoroutineScope(Dispatchers.IO).launch {
            // Check and reset counters if needed
            settingsRepository.checkAndResetMonthlyCounterIfNeeded()
            
            // Re-fetch to get updated autoResume settings
            val settings = settingsRepository.getOrInitSettings()
            
            if (settings.autoResumeEnabled) {
                // Find all paused campaigns
                val pausedCampaigns = campaignRepository.getCampaignsByStatusDirect(CampaignEntity.STATUS_PAUSED_DAILY_LIMIT)
                for (campaign in pausedCampaigns) {
                    Log.d("MidnightResetReceiver", "Auto-resuming campaign ID: ${campaign.id}")
                    campaignRepository.updateCampaignStatus(campaign.id, CampaignEntity.STATUS_QUEUED)
                    SmsSendingService.startCampaign(context, campaign.id)
                }
            }
        }
    }
}
