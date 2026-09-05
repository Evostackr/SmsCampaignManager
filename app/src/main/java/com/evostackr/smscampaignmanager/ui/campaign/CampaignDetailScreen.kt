package com.evostackr.smscampaignmanager.ui.campaign

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evostackr.smscampaignmanager.data.local.entity.CampaignEntity
import com.evostackr.smscampaignmanager.data.local.entity.RecipientEntity
import com.evostackr.smscampaignmanager.ui.theme.StatusFailed
import com.evostackr.smscampaignmanager.ui.theme.StatusPaused
import com.evostackr.smscampaignmanager.ui.theme.StatusPending
import com.evostackr.smscampaignmanager.ui.theme.StatusSuccess
import com.evostackr.smscampaignmanager.util.CsvExporter

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailScreen(
    campaignId: Long,
    onNavigateBack: () -> Unit,
    viewModel: CampaignViewModel = hiltViewModel()
) {
    val campaign by viewModel.getCampaignFlow(campaignId).collectAsState(initial = null)
    val recipients by viewModel.getRecipientsForCampaign(campaignId).collectAsState(initial = emptyList())
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    val filteredRecipients = recipients.filter { rec ->
        val matchesQuery = (rec.name?.contains(searchQuery, ignoreCase = true) ?: false) ||
                rec.phoneNumber.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedStatusFilter) {
            "SENT" -> rec.status == RecipientEntity.STATUS_SENT
            "PENDING" -> rec.status == RecipientEntity.STATUS_PENDING
            "FAILED" -> rec.status == RecipientEntity.STATUS_FAILED
            "RETRIED" -> rec.retryCount > 0
            else -> true
        }
        matchesQuery && matchesFilter
    }

    // Export CSV document creator launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    CsvExporter.exportRecipientsToCsv(stream, recipients)
                }
            } catch (e: Exception) {
                // export error
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(campaign?.name ?: "Campaign Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { exportLauncher.launch("${campaign?.name ?: "campaign"}_report.csv") }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (campaign != null) {
                val current = campaign!!
                val progress = if (current.totalRecipients > 0) {
                    (current.sentCount + current.failedCount).toFloat() / current.totalRecipients.toFloat()
                } else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Status: ${current.status}", fontWeight = FontWeight.Bold)
                            Text("Delay: ${current.delaySeconds}s")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total: ${current.totalRecipients}")
                            Text("Sent: ${current.sentCount}", color = StatusSuccess)
                            Text("Failed: ${current.failedCount}", color = StatusFailed)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (current.status == CampaignEntity.STATUS_IN_PROGRESS) {
                                OutlinedButton(onClick = { viewModel.pauseCampaign(campaignId) }) {
                                    Icon(Icons.Default.Pause, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pause")
                                }
                            } else if (current.status == CampaignEntity.STATUS_PAUSED || current.status == CampaignEntity.STATUS_QUEUED) {
                                Button(onClick = { viewModel.resumeCampaign(campaignId) }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Resume")
                                }
                            }

                            if (current.failedCount > 0) {
                                OutlinedButton(onClick = { viewModel.retryFailedRecipients(campaignId) }) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry Failed (${current.failedCount})")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search & Filters for Recipients
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or number...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "SENT", "PENDING", "FAILED", "RETRIED").forEach { status ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = status },
                        label = { Text(status) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 30.dp)
            ) {
                items(filteredRecipients, key = { it.id }) { recipient ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(recipient.name ?: recipient.phoneNumber, fontWeight = FontWeight.Bold)
                                if (recipient.name != null) {
                                    Text(recipient.phoneNumber, style = MaterialTheme.typography.bodySmall)
                                }
                                if (recipient.errorReason != null) {
                                    Text(recipient.errorReason!!, style = MaterialTheme.typography.labelSmall, color = StatusFailed)
                                }
                            }

                            val color = when (recipient.status) {
                                RecipientEntity.STATUS_SENT -> StatusSuccess
                                RecipientEntity.STATUS_FAILED -> StatusFailed
                                RecipientEntity.STATUS_SENDING -> StatusPending
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = color.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = recipient.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

