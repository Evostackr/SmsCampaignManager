package com.evostackr.smscampaignmanager.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import com.evostackr.smscampaignmanager.MainActivity
import com.evostackr.smscampaignmanager.data.local.entity.CampaignEntity
import com.evostackr.smscampaignmanager.data.local.entity.RecipientEntity
import com.evostackr.smscampaignmanager.data.local.entity.SmsLogEntity
import com.evostackr.smscampaignmanager.data.repository.CampaignRepository
import com.evostackr.smscampaignmanager.data.repository.SettingsRepository
import com.evostackr.smscampaignmanager.data.repository.SmsRepository
import com.evostackr.smscampaignmanager.util.PhoneValidator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SmsSendingService : Service() {

    companion object {
        const val CHANNEL_ID = "sms_campaign_dispatch_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.evostackr.smscampaignmanager.ACTION_START"
        const val ACTION_PAUSE = "com.evostackr.smscampaignmanager.ACTION_PAUSE"
        const val ACTION_RESUME = "com.evostackr.smscampaignmanager.ACTION_RESUME"
        const val ACTION_CANCEL = "com.evostackr.smscampaignmanager.ACTION_CANCEL"

        const val EXTRA_CAMPAIGN_ID = "extra_campaign_id"

        fun startCampaign(context: Context, campaignId: Long) {
            val intent = Intent(context, SmsSendingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_CAMPAIGN_ID, campaignId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseCampaign(context: Context) {
            val intent = Intent(context, SmsSendingService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resumeCampaign(context: Context) {
            val intent = Intent(context, SmsSendingService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun cancelCampaign(context: Context) {
            val intent = Intent(context, SmsSendingService::class.java).apply {
                action = ACTION_CANCEL
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Inject
    lateinit var campaignRepository: CampaignRepository
    @Inject
    lateinit var settingsRepository: SettingsRepository
    @Inject
    lateinit var smsRepository: SmsRepository

    private var activeJob: Job? = null
    private var activeCampaignId: Long = -1L
    private var isPaused = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val campaignId = intent?.getLongExtra(EXTRA_CAMPAIGN_ID, -1L) ?: -1L

        startForegroundNotification("Campaign Service", 0, 0)

        when (action) {
            ACTION_START -> {
                if (campaignId != -1L) {
                    activeCampaignId = campaignId
                    isPaused = false
                    startForegroundNotification("Initializing campaign...", 0, 100)
                    startSendingLoop(campaignId)
                }
            }
            ACTION_PAUSE -> {
                isPaused = true
                serviceScope.launch {
                    if (activeCampaignId != -1L) {
                        campaignRepository.updateCampaignStatus(activeCampaignId, CampaignEntity.STATUS_PAUSED)
                    }
                    updateNotification("Campaign Paused", 0, 0, isPaused = true)
                }
            }
            ACTION_RESUME -> {
                if (activeCampaignId != -1L && isPaused) {
                    isPaused = false
                    startSendingLoop(activeCampaignId)
                }
            }
            ACTION_CANCEL -> {
                serviceScope.launch {
                    if (activeCampaignId != -1L) {
                        campaignRepository.updateCampaignStatus(activeCampaignId, CampaignEntity.STATUS_CANCELLED)
                    }
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }

    private fun startSendingLoop(campaignId: Long) {
        activeJob?.cancel()
        activeJob = serviceScope.launch {
            campaignRepository.updateCampaignStatus(campaignId, CampaignEntity.STATUS_IN_PROGRESS)

            while (isActive && !isPaused) {
                // Check settings and monthly limits
                val settings = settingsRepository.getOrInitSettings()
                if (settings.sentThisMonth >= settings.monthlyLimit) {
                    // Monthly Limit reached!
                    campaignRepository.updateCampaignStatus(campaignId, CampaignEntity.STATUS_PAUSED)
                    updateNotification("Monthly limit reached (${settings.monthlyLimit} SMS)", 0, 0, isPaused = true)
                    break
                }
                
                if (settings.sentToday >= settings.dailyLimit) {
                    // Daily Limit reached!
                    campaignRepository.updateCampaignStatus(campaignId, CampaignEntity.STATUS_PAUSED_DAILY_LIMIT)
                    updateNotification("Daily SMS limit reached. Campaign will continue tomorrow.", 0, 0, isPaused = true)
                    break
                }

                val campaign = campaignRepository.getCampaignById(campaignId)
                if (campaign == null || campaign.status == CampaignEntity.STATUS_CANCELLED) {
                    break
                }
                
                val now = System.currentTimeMillis()
                if (now < campaign.nextAllowedSendTime) {
                    var remainingSeconds = ((campaign.nextAllowedSendTime - now) / 1000).toInt()
                    val sentSoFar = campaign.sentCount + campaign.failedCount
                    while (remainingSeconds > 0 && isActive && !isPaused) {
                        updateNotification("Next SMS in $remainingSeconds seconds", sentSoFar, campaign.totalRecipients)
                        delay(1000)
                        remainingSeconds--
                    }
                    if (isPaused || !isActive) {
                        break
                    }
                }

                val recipient = campaignRepository.getNextPendingRecipient(campaignId)
                if (recipient == null) {
                    // All pending recipients completed!
                    campaignRepository.updateCampaignStatus(campaignId, CampaignEntity.STATUS_COMPLETED)
                    updateNotification(
                        "SMS Campaign Completed (${campaign.sentCount}/${campaign.totalRecipients})",
                        campaign.totalRecipients,
                        campaign.totalRecipients
                    )
                    delay(3000)
                    stopSelf()
                    break
                }

                // Update notification progress
                val sentSoFar = campaign.sentCount + campaign.failedCount
                updateNotification("Sending SMS to ${recipient.phoneNumber}", sentSoFar, campaign.totalRecipients)

                // Dispatch SMS
                val maxRetries = settings.maxRetryCount
                val sendSuccess = dispatchSms(campaign, recipient, maxRetries)

                if (sendSuccess) {
                    campaignRepository.markRecipientSent(campaignId, recipient.id)
                    settingsRepository.incrementMonthlySent(1)
                    settingsRepository.incrementDailySent(1)
                    smsRepository.logMessage(
                        phoneNumber = recipient.phoneNumber,
                        message = formatMessage(campaign.messageTemplate, recipient),
                        result = SmsLogEntity.RESULT_SUCCESS,
                        campaignId = campaignId,
                        messageType = SmsLogEntity.TYPE_SMS
                    )
                } else {
                    val newRetryCount = recipient.retryCount + 1
                    val errorReason = "SMS Send Failed after $newRetryCount attempts"
                    campaignRepository.markRecipientFailed(campaignId, recipient.id, errorReason, newRetryCount)
                    smsRepository.logMessage(
                        phoneNumber = recipient.phoneNumber,
                        message = formatMessage(campaign.messageTemplate, recipient),
                        result = SmsLogEntity.RESULT_FAILED,
                        campaignId = campaignId,
                        errorMessage = errorReason,
                        messageType = SmsLogEntity.TYPE_SMS,
                        retryCount = newRetryCount
                    )
                }

                // Calculate and save next allowed send time
                val baseDelay = when (settings.delayMode) {
                    com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity.MODE_NONE -> 0
                    com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity.MODE_RANDOM -> kotlin.random.Random.nextInt(settings.minDelaySeconds, settings.maxDelaySeconds + 1)
                    else -> settings.defaultDelaySeconds
                }
                
                if (baseDelay > 0) {
                    val delayMs = baseDelay * 1000L
                    campaignRepository.updateNextAllowedSendTime(campaignId, System.currentTimeMillis() + delayMs)
                }
            }
        }
    }

    // ─── SMS Dispatch ──────────────────────────────────────────────────────────

    private suspend fun dispatchSms(campaign: CampaignEntity, recipient: RecipientEntity, maxRetries: Int): Boolean {
        val sanitizedPhone = PhoneValidator.sanitizePhoneNumber(recipient.phoneNumber)
        if (!PhoneValidator.isValidPhoneNumber(sanitizedPhone)) {
            return false
        }

        val messageText = formatMessage(campaign.messageTemplate, recipient)
        val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            applicationContext.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        var currentAttempt = recipient.retryCount
        var success = false

        while (currentAttempt <= maxRetries && !success) {
            try {
                campaignRepository.markRecipientSending(recipient.id)
                val parts = smsManager.divideMessage(messageText)

                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(sanitizedPhone, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(sanitizedPhone, null, messageText, null, null)
                }
                success = true
            } catch (e: Exception) {
                currentAttempt++
                if (currentAttempt <= maxRetries) {
                    delay(3000L * currentAttempt) // retry backoff
                }
            }
        }
        return success
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private fun formatMessage(template: String, recipient: RecipientEntity): String {
        val nameReplacement = recipient.name ?: "Customer"
        return template
            .replace("{name}", nameReplacement, ignoreCase = true)
            .replace("{phone}", recipient.phoneNumber, ignoreCase = true)
            .replace("{company}", recipient.company ?: "", ignoreCase = true)
            .replace("{date}", recipient.date ?: "", ignoreCase = true)
            .replace("{time}", recipient.time ?: "", ignoreCase = true)
            .replace("{amount}", recipient.amount ?: "", ignoreCase = true)
            .replace("{city}", recipient.city ?: "", ignoreCase = true)
            .replace("{custom}", recipient.custom ?: "", ignoreCase = true)
    }

    private fun startForegroundNotification(title: String, progress: Int, total: Int) {
        val notification = buildNotification(title, progress, total, isPaused = false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(title: String, progress: Int, total: Int, isPaused: Boolean = false) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(title, progress, total, isPaused))
    }

    private fun buildNotification(title: String, progress: Int, total: Int, isPaused: Boolean): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = PendingIntent.getService(
            this, 1,
            Intent(this, SmsSendingService::class.java).apply { action = ACTION_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val resumeIntent = PendingIntent.getService(
            this, 2,
            Intent(this, SmsSendingService::class.java).apply { action = ACTION_RESUME },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = PendingIntent.getService(
            this, 3,
            Intent(this, SmsSendingService::class.java).apply { action = ACTION_CANCEL },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("Campaign: $title")
            .setContentText(if (total > 0) "Progress: $progress / $total sent" else title)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (total > 0) {
            builder.setProgress(total, progress, false)
        }

        if (isPaused) {
            builder.addAction(android.R.drawable.ic_media_play, "Resume", resumeIntent)
        } else {
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", pauseIntent)
        }
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelIntent)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SMS Campaign Dispatcher",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live campaign dispatch progress and controls"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
