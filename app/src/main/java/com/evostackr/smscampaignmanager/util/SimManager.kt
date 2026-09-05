package com.evostackr.smscampaignmanager.util

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

data class SimInfo(
    val slotIndex: Int,
    val carrierName: String,
    val displayName: String,
    val subscriptionId: Int,
    val isNetworkRoaming: Boolean
)

data class SimStateInfo(
    val hasSim: Boolean,
    val isAirplaneModeOn: Boolean,
    val simList: List<SimInfo>,
    val primaryCarrierName: String
)

object SimManager {

    fun getSimState(context: Context): SimStateInfo {
        val isAirplaneModeOn = try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0
            ) != 0
        } catch (e: Exception) {
            false
        }

        val simList = mutableListOf<SimInfo>()
        var primaryCarrier = "No SIM Detected"

        try {
            val hasReadPhoneStatePermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED

            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

            if (hasReadPhoneStatePermission) {
                val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                try {
                    val activeSubscriptions: List<SubscriptionInfo>? = subscriptionManager?.activeSubscriptionInfoList
                    if (!activeSubscriptions.isNullOrEmpty()) {
                        for (sub in activeSubscriptions) {
                            simList.add(
                                SimInfo(
                                    slotIndex = sub.simSlotIndex,
                                    carrierName = sub.carrierName?.toString() ?: "Carrier",
                                    displayName = sub.displayName?.toString() ?: "SIM ${sub.simSlotIndex + 1}",
                                    subscriptionId = sub.subscriptionId,
                                    isNetworkRoaming = sub.dataRoaming == SubscriptionManager.DATA_ROAMING_ENABLE
                                )
                            )
                        }
                        if (simList.isNotEmpty()) {
                            primaryCarrier = simList[0].carrierName
                        }
                    }
                } catch (e: Exception) {
                    // Ignored - fallback to telephonyManager
                }
            }

            if (simList.isEmpty() && telephonyManager != null) {
                val networkOperatorName = try { telephonyManager.networkOperatorName } catch (e: Exception) { "" }
                val simState = try { telephonyManager.simState } catch (e: Exception) { TelephonyManager.SIM_STATE_UNKNOWN }
                val hasSimCard = simState == TelephonyManager.SIM_STATE_READY
                if (hasSimCard && !networkOperatorName.isNullOrEmpty()) {
                    primaryCarrier = networkOperatorName
                }
            }

            val simState = try { telephonyManager?.simState } catch (e: Exception) { TelephonyManager.SIM_STATE_UNKNOWN }
            val hasSim = simList.isNotEmpty() || (simState == TelephonyManager.SIM_STATE_READY)

            return SimStateInfo(
                hasSim = hasSim,
                isAirplaneModeOn = isAirplaneModeOn,
                simList = simList,
                primaryCarrierName = if (isAirplaneModeOn) "Airplane Mode Active" else primaryCarrier
            )
        } catch (e: Exception) {
            return SimStateInfo(
                hasSim = false,
                isAirplaneModeOn = isAirplaneModeOn,
                simList = emptyList(),
                primaryCarrierName = "No SIM Detected"
            )
        }
    }
}

