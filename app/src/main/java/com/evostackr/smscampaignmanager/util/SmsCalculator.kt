package com.evostackr.smscampaignmanager.util

import kotlin.math.ceil

object SmsCalculator {
    /**
     * Calculates the number of SMS segments required for a given message text.
     * Standard GSM 7-bit encoding allows 160 chars per SMS (153 for multi-part).
     * Unicode (UCS-2) encoding allows 70 chars per SMS (67 for multi-part).
     */
    fun calculateSegments(message: String): Int {
        if (message.isEmpty()) return 0
        val isUnicode = message.any { it.code > 127 }
        return if (isUnicode) {
            if (message.length <= 70) 1 else ceil(message.length.toDouble() / 67).toInt()
        } else {
            if (message.length <= 160) 1 else ceil(message.length.toDouble() / 153).toInt()
        }
    }

    /**
     * Estimates total SMS messages required for a campaign.
     */
    fun estimateTotalSms(recipientCount: Int, message: String): Int {
        val segments = calculateSegments(message)
        return recipientCount * segments
    }
}

