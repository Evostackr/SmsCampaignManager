package com.evostackr.smscampaignmanager.service

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log

class SmsStatusReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_SMS_SENT = "com.evostackr.smscampaignmanager.SMS_SENT"
        const val ACTION_SMS_DELIVERED = "com.evostackr.smscampaignmanager.SMS_DELIVERED"

        const val EXTRA_RECIPIENT_ID = "extra_recipient_id"
        const val EXTRA_CAMPAIGN_ID = "extra_campaign_id"
        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val recipientId = intent.getLongExtra(EXTRA_RECIPIENT_ID, -1L)
        val campaignId = intent.getLongExtra(EXTRA_CAMPAIGN_ID, -1L)
        val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""

        when (action) {
            ACTION_SMS_SENT -> {
                val resultCode = resultCode
                val success = resultCode == Activity.RESULT_OK
                val errorMessage = if (success) null else getSmsErrorMessage(resultCode)
                Log.d("SmsStatusReceiver", "SMS Sent to $phoneNumber: success=$success, code=$resultCode")
            }
            ACTION_SMS_DELIVERED -> {
                Log.d("SmsStatusReceiver", "SMS Delivered to $phoneNumber")
            }
        }
    }

    private fun getSmsErrorMessage(resultCode: Int): String {
        return when (resultCode) {
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "Generic Failure (No SIM or Radio Error)"
            SmsManager.RESULT_ERROR_NO_SERVICE -> "No Cellular Service"
            SmsManager.RESULT_ERROR_NULL_PDU -> "Null PDU Error"
            SmsManager.RESULT_ERROR_RADIO_OFF -> "Radio Off (Airplane Mode Active)"
            SmsManager.RESULT_ERROR_LIMIT_EXCEEDED -> "SMS Rate Limit Exceeded"
            SmsManager.RESULT_ERROR_FDN_CHECK_FAILURE -> "FDN Check Failure"
            else -> "Send Failed (Code $resultCode)"
        }
    }
}

