package com.evostackr.smscampaignmanager.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.evostackr.smscampaignmanager.ui.components.CampaignItem
import com.evostackr.smscampaignmanager.ui.components.PermissionBanner
import com.evostackr.smscampaignmanager.ui.components.SimInfoCard
import com.evostackr.smscampaignmanager.ui.components.StatCard
import com.evostackr.smscampaignmanager.ui.theme.BrandError
import com.evostackr.smscampaignmanager.ui.theme.BrandPrimary
import com.evostackr.smscampaignmanager.ui.theme.BrandSuccess
import com.evostackr.smscampaignmanager.ui.theme.BrandWarning
import com.evostackr.smscampaignmanager.util.PermissionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCreateCampaign: () -> Unit,
    onNavigateToCampaignDetail: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var hasSmsPermission by remember {
        mutableStateOf(
            PermissionManager.hasAllPermissions(context, PermissionManager.SMS_PERMISSIONS)
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        hasSmsPermission = allGranted
        if (allGranted) viewModel.refreshSimState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SMSCampaignManager",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Text(
                            text = "Professional Bulk SMS & Campaign Management",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateCampaign,
                icon = { Icon(Icons.Default.Add, contentDescription = "New Campaign") },
                text = { Text("New Campaign", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
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
            // Permission check banner
            if (!hasSmsPermission) {
                PermissionBanner(
                    permissionName = "SEND SMS",
                    explanation = "SMS sending capability is required to dispatch campaigns from your SIM card.",
                    onGrantClick = { permissionLauncher.launch(PermissionManager.SMS_PERMISSIONS.toTypedArray()) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // SIM Status Card
            SimInfoCard(
                simState = uiState.simState
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stat Cards Grid
            Text(
                text = "Dispatch Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Sent Today",
                    value = uiState.sentToday.toString(),
                    icon = Icons.AutoMirrored.Filled.Send,
                    iconColor = BrandPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Sent This Month",
                    value = uiState.sentThisMonth.toString(),
                    icon = Icons.Default.CheckCircle,
                    iconColor = BrandSuccess,
                    subtitle = "Limit: ${uiState.monthlyLimit}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Failed Messages",
                    value = uiState.failedCount.toString(),
                    icon = Icons.Default.Error,
                    iconColor = BrandError,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Remaining Quota",
                    value = uiState.remainingThisMonth.toString(),
                    icon = Icons.Default.HourglassEmpty,
                    iconColor = BrandWarning,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Active Campaign Section
            Text(
                text = "Active Campaign Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val activeCampaign = uiState.activeCampaign
            if (activeCampaign != null) {
                CampaignItem(
                    campaign = activeCampaign,
                    onClick = { onNavigateToCampaignDetail(activeCampaign.id) },
                    onPauseClick = { viewModel.pauseActiveCampaign() },
                    onResumeClick = { viewModel.resumeActiveCampaign() },
                    onCancelClick = { viewModel.cancelActiveCampaign() }
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "No active campaign running",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Tap 'New Campaign' below to compose and dispatch bulk SMS.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

