package com.evostackr.smscampaignmanager.ui.settings

import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)

    // Storage Access Framework folder picker
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { folderUri ->
            context.contentResolver.takePersistableUriPermission(
                folderUri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.setupAutoBackup(settings?.backupFrequency ?: SettingsEntity.FREQUENCY_DAILY, folderUri.toString())
        }
    }

    // Export & Restore Launchers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let { context.contentResolver.openOutputStream(it)?.use { stream -> viewModel.createBackup(stream) } }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { context.contentResolver.openInputStream(it)?.use { stream -> viewModel.restoreBackup(stream) } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Encrypted Backup & Recovery", fontWeight = FontWeight.Bold) },
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
            // Security Card Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("AES-256 Encrypted & Local", fontWeight = FontWeight.Bold)
                        Text("All backups are encrypted with AES-256 and kept 100% offline on your device.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Last Backup Stats Card
            OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Backup Storage Info", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if ((settings?.lastBackupTimestamp ?: 0L) > 0L)
                            "Last Backup: ${dateFormat.format(Date(settings!!.lastBackupTimestamp))}"
                        else "Last Backup: Never",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Backup Size: ${((settings?.lastBackupSize ?: 0L) / 1024)} KB",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Folder: ${settings?.backupFolderUri ?: "Default internal storage"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Auto-Backup Configuration", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            Spacer(modifier = Modifier.height(8.dp))

            // Storage Folder Selection Button
            OutlinedButton(
                onClick = { folderPickerLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Folder, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (settings?.backupFolderUri != null) "Change Storage Folder" else "Choose Storage Folder (SAF)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Manual & Restore Action Buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { exportLauncher.launch("sms_campaign_backup.enc") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Backup Now")
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedButton(
                    onClick = { restoreLauncher.launch("*/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restore Backup")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // System SMS Log Recovery Button
            OutlinedButton(
                onClick = { viewModel.recoverFromSmsHistory() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recover Monthly Stats from Device SMS Log")
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(statusMessage!!, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

