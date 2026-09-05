package com.evostackr.smscampaignmanager.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RecoveryWizardDialog(
    onRestoreBackup: (Uri) -> Unit,
    onRecoverSmsHistory: () -> Unit,
    onStartFresh: () -> Unit
) {
    val context = LocalContext.current

    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onRestoreBackup(it) }
    }

    AlertDialog(
        onDismissRequest = { /* Force explicit decision */ },
        icon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("App Recovery & Initialization Wizard", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Welcome! Detect app state after launch or reset. Please select how you wish to set up your app data:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: Restore Encrypted Backup
                OutlinedCard(
                    onClick = { restoreFileLauncher.launch("application/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Restore Encrypted Backup", fontWeight = FontWeight.Bold)
                            Text("Select an existing .enc or .json backup file", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Recover System SMS Log Stats
                OutlinedCard(
                    onClick = onRecoverSmsHistory,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Recover SMS Statistics", fontWeight = FontWeight.Bold)
                            Text("Reconstruct current month sent SMS from device log", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onStartFresh) {
                Text("Start Fresh")
            }
        }
    )
}

