package com.evostackr.smscampaignmanager.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val restoreRequest = OneTimeWorkRequestBuilder<CampaignRestoreWorker>().build()
            val resetRequest = OneTimeWorkRequestBuilder<MonthlyResetWorker>().build()
            com.evostackr.smscampaignmanager.util.AlarmManagerHelper.scheduleMidnightReset(context)

            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(restoreRequest)
            workManager.enqueue(resetRequest)
        }
    }
}

