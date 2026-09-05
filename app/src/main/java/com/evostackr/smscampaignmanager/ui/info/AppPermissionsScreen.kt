package com.evostackr.smscampaignmanager.ui.info

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.evostackr.smscampaignmanager.util.PermissionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPermissionsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var hasSmsPermission by remember {
        mutableStateOf(PermissionManager.hasAllPermissions(context, PermissionManager.SMS_PERMISSIONS))
    }
    var hasContactsPermission by remember {
        mutableStateOf(PermissionManager.hasAllPermissions(context, PermissionManager.CONTACTS_PERMISSIONS))
    }
    var hasNotificationPermission by remember {
        mutableStateOf(PermissionManager.hasAllPermissions(context, PermissionManager.NOTIFICATION_PERMISSIONS))
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasSmsPermission = permissions.values.all { it }
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasContactsPermission = permissions.values.all { it }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasNotificationPermission = permissions.values.all { it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Permissions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Permission Manager", fontWeight = FontWeight.Bold)
                        Text("Review and manage permissions required for SMSCampaignManager", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PermissionCard(
                title = "SMS Sending & Reception",
                permissionName = "android.permission.SEND_SMS",
                isMandatory = true,
                isGranted = hasSmsPermission,
                explanation = "Mandatory for sending single and bulk campaign messages via your SIM card and updating delivery status.",
                icon = Icons.Default.Sms,
                onGrantClick = { smsPermissionLauncher.launch(PermissionManager.SMS_PERMISSIONS.toTypedArray()) }
            )

            PermissionCard(
                title = "Address Book Contacts",
                permissionName = "android.permission.READ_CONTACTS",
                isMandatory = false,
                isGranted = hasContactsPermission,
                explanation = "Optional. Allows picking recipient contact numbers directly from your Android phone address book.",
                icon = Icons.Default.Contacts,
                onGrantClick = { contactsPermissionLauncher.launch(PermissionManager.CONTACTS_PERMISSIONS.toTypedArray()) }
            )

            PermissionCard(
                title = "Foreground Notifications",
                permissionName = "android.permission.POST_NOTIFICATIONS",
                isMandatory = false,
                isGranted = hasNotificationPermission,
                explanation = "Optional (Android 13+). Keeps you updated on real-time campaign dispatch progress and completion.",
                icon = Icons.Default.Notifications,
                onGrantClick = { notificationPermissionLauncher.launch(PermissionManager.NOTIFICATION_PERMISSIONS.toTypedArray()) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Open System Settings Button
            OutlinedButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open System App Settings")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LegalFooter()
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    permissionName: String,
    isMandatory: Boolean,
    isGranted: Boolean,
    explanation: String,
    icon: ImageVector,
    onGrantClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(if (isMandatory) "Mandatory Permission" else "Optional Permission", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isGranted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (isGranted) "Granted" else "Not Granted",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isGranted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (!isGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Grant Permission", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

