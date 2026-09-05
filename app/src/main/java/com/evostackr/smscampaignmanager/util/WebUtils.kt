package com.evostackr.smscampaignmanager.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WebUtils {
    const val URL_PRIVACY_POLICY = "https://evostackr.in/apps/smscampaignmanager/privacy-policy"
    const val URL_TERMS_CONDITIONS = "https://evostackr.in/apps/smscampaignmanager/terms-and-conditions"
    const val URL_ABOUT = "https://evostackr.in/apps/smscampaignmanager/about"
    const val URL_CONTACT = "https://evostackr.in/apps/smscampaignmanager/contact"

    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No web browser available to open link", Toast.LENGTH_SHORT).show()
        }
    }
}
