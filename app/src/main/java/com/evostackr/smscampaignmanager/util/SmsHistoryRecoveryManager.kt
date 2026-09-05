package com.evostackr.smscampaignmanager.util

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

data class RecoveryStats(
    val sentThisMonth: Int,
    val sentToday: Int,
    val recoverySource: String
)

object SmsHistoryRecoveryManager {

    suspend fun recoverMonthlyStatsFromDeviceSms(context: Context): RecoveryStats = withContext(Dispatchers.IO) {
        val hasReadSmsPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasReadSmsPermission) {
            return@withContext RecoveryStats(0, 0, "FRESH_START")
        }

        var sentThisMonth = 0
        var sentToday = 0

        val calendar = Calendar.getInstance()

        // Today start timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStartMs = calendar.timeInMillis

        // Month start timestamp
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val monthStartMs = calendar.timeInMillis

        try {
            val projection = arrayOf(Telephony.Sms.DATE)
            val selection = "${Telephony.Sms.DATE} >= ?"
            val selectionArgs = arrayOf(monthStartMs.toString())

            context.contentResolver.query(
                Telephony.Sms.Sent.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${Telephony.Sms.DATE} DESC"
            )?.use { cursor ->
                val dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE)

                while (cursor.moveToNext()) {
                    sentThisMonth++
                    val timestamp = if (dateIndex >= 0) cursor.getLong(dateIndex) else 0L
                    if (timestamp >= todayStartMs) {
                        sentToday++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext RecoveryStats(
            sentThisMonth = sentThisMonth,
            sentToday = sentToday,
            recoverySource = "SMS_HISTORY_ESTIMATE"
        )
    }
}

