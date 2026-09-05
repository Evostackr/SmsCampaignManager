package com.evostackr.smscampaignmanager.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

enum class PermissionState {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED,
    NOT_REQUESTED
}

/**
 * PermissionManager
 *
 * Centralized utility for checking, observing, requesting, and managing
 * Android runtime permissions across Jetpack Compose screens and feature components.
 */
object PermissionManager {

    val SMS_PERMISSIONS = listOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_PHONE_STATE
    )

    val CONTACTS_PERMISSIONS = listOf(
        Manifest.permission.READ_CONTACTS
    )

    val NOTIFICATION_PERMISSIONS: List<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyList()
        }

    /** Check permission state for a single permission. */
    fun checkPermission(context: Context, permission: String): PermissionState {
        val status = ContextCompat.checkSelfPermission(context, permission)
        return if (status == PackageManager.PERMISSION_GRANTED) {
            PermissionState.GRANTED
        } else {
            PermissionState.DENIED
        }
    }

    /** Check if all specified permissions are granted. */
    fun hasAllPermissions(context: Context, permissions: List<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /** Check if permission is permanently denied. */
    fun isPermanentlyDenied(activity: Activity?, permission: String): Boolean {
        if (activity == null) return false
        val status = ContextCompat.checkSelfPermission(activity, permission)
        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        return status == PackageManager.PERMISSION_DENIED && !shouldShowRationale
    }

    /** Open application settings screen in Android Settings. */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /** Get human readable explanation for a permission. */
    fun getPermissionExplanation(permission: String): String {
        return when (permission) {
            Manifest.permission.SEND_SMS ->
                "SMS sending permission is required to dispatch single or bulk campaign messages from your SIM card."
            Manifest.permission.READ_CONTACTS ->
                "Contacts permission allows you to import and select recipients directly from your address book."
            Manifest.permission.READ_PHONE_STATE ->
                "Phone state permission is required to detect SIM card status and select active SIM slots."
            Manifest.permission.POST_NOTIFICATIONS ->
                "Notifications permission keeps you updated on real-time campaign dispatch progress and status."
            else ->
                "This permission is required for full functionality of the SMS Campaign Manager."
        }
    }
}
