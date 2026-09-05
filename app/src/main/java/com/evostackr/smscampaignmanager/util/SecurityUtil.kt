package com.evostackr.smscampaignmanager.util

import android.content.Context
import android.util.Base64
import java.security.SecureRandom

object SecurityUtil {
    private const val PREFS_NAME = "secure_sms_db_prefs"
    private const val KEY_DB_PASSPHRASE = "db_passphrase_b64"

    fun getOrCreatePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var passphraseBase64 = prefs.getString(KEY_DB_PASSPHRASE, null)

        if (passphraseBase64 == null) {
            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)
            passphraseBase64 = Base64.encodeToString(randomBytes, Base64.NO_WRAP)
            prefs.edit().putString(KEY_DB_PASSPHRASE, passphraseBase64).apply()
        }

        return try {
            Base64.decode(passphraseBase64, Base64.NO_WRAP)
        } catch (e: Exception) {
            "SMS_CAMPAIGN_SECURE_LOCAL_DB_KEY_2026".toByteArray()
        }
    }
}

