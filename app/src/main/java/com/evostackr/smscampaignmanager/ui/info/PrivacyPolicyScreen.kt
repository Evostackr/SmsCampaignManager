package com.evostackr.smscampaignmanager.ui.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
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
            // Policy Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Privacy Commitment", fontWeight = FontWeight.Bold)
                        Text("Last Updated: August 1, 2026", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Policy Sections
            PolicySection(
                title = "1. Zero Telemetry & On-Device Data Storage",
                content = "SMSCampaignManager (developed by Evostackr) is designed with a strict Privacy-First architecture. All message templates, campaign databases, contacts, and delivery logs are stored 100% locally on your Android device using SQLCipher AES-256 database encryption. We do NOT operate remote tracking servers, analytics services, or cloud databases. No personal data, phone numbers, or SMS message content ever leaves your device."
            )

            PolicySection(
                title = "2. Required Permissions Rationale",
                content = "To perform its core functions as a bulk messaging manager, the application requests the following Android permissions:\n\n" +
                        "• SEND_SMS / READ_SMS: Required strictly to dispatch campaign messages through your device's cellular SIM card and track delivery receipts.\n" +
                        "• READ_CONTACTS: Required to allow selecting recipients directly from your address book for custom SMS campaigns.\n" +
                        "• POST_NOTIFICATIONS (Android 13+): Required to show ongoing campaign dispatch progress and completion notifications.\n" +
                        "• READ_EXTERNAL_STORAGE: Required to import CSV and Excel (.xlsx) recipient contact lists selected by the user."
            )

            PolicySection(
                title = "3. Encrypted Backups & Security",
                content = "Local backups created via the app are encrypted using AES-256 encryption. Backup files are saved solely to the local folder or directory chosen by the user. Evostackr does not have access to your passphrase or your backup files."
            )

            PolicySection(
                title = "4. User Data Control & Complete Deletion",
                content = "You maintain complete ownership of your data. You can wipe all stored campaigns, logs, and contacts at any time from Settings -> Data & Privacy -> Clear Local Database. Uninstalling the application immediately removes all encrypted local databases from your device."
            )

            PolicySection(
                title = "5. Third-Party Services",
                content = "SMSCampaignManager does not incorporate third-party advertising SDKs, tracking frameworks, or analytics engines. All operations run locally on your device."
            )

            PolicySection(
                title = "6. Contact Developer",
                content = "If you have questions regarding this Privacy Policy or data security, please contact the developer team at support@evostackr.in."
            )

            Spacer(modifier = Modifier.height(16.dp))

            LegalFooter()
        }
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
        }
    }
}

