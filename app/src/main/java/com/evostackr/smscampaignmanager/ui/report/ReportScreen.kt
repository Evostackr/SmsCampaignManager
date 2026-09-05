package com.evostackr.smscampaignmanager.ui.report

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.evostackr.smscampaignmanager.data.local.entity.SmsLogEntity
import com.evostackr.smscampaignmanager.ui.components.StatCard
import com.evostackr.smscampaignmanager.ui.theme.StatusFailed
import com.evostackr.smscampaignmanager.ui.theme.StatusPending
import com.evostackr.smscampaignmanager.ui.theme.StatusSuccess
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.ui.platform.LocalContext
import com.evostackr.smscampaignmanager.ui.components.AnalyticsChart
import com.evostackr.smscampaignmanager.util.PdfExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US)

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportReportToUri(it) }
    }

    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    PdfExporter.exportLogsToPdf(stream, uiState.logs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { exportPdfLauncher.launch("sms_campaign_report.pdf") }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export Report PDF")
                    }
                    IconButton(onClick = { exportCsvLauncher.launch("sms_campaign_full_report.csv") }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export Report CSV")
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
            // Metrics overview
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Total Dispatched",
                    value = "${uiState.totalSent}",
                    icon = Icons.Default.Assessment,
                    iconColor = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(
                    title = "Success Rate",
                    value = "${uiState.successRatePercentage}%",
                    icon = Icons.Default.Assessment,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Analytics Chart
            AnalyticsChart(
                successCount = uiState.totalSent,
                failedCount = uiState.totalFailed,
                pendingCount = uiState.totalPending
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Detailed Activity Logs (${uiState.logs.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No SMS log history available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 30.dp)
                ) {
                    items(uiState.logs, key = { it.id }) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(log.phoneNumber, fontWeight = FontWeight.Bold)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (log.result == SmsLogEntity.RESULT_SUCCESS) StatusSuccess.copy(alpha = 0.15f) else StatusFailed.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = log.result,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (log.result == SmsLogEntity.RESULT_SUCCESS) StatusSuccess else StatusFailed
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(log.message, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dateFormat.format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

