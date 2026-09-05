package com.evostackr.smscampaignmanager.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.evostackr.smscampaignmanager.ui.info.LegalFooter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToBackupSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToDisclaimer: () -> Unit = {},
    onNavigateToContact: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToFaq: () -> Unit = {},
    onNavigateToLicenses: () -> Unit = {},
    onNavigateToPermissions: () -> Unit = {},
    onNavigateToDataPrivacy: () -> Unit = {},
    onNavigateToVersion: () -> Unit = {},
    onNavigateToChangelog: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settings by viewModel.settingsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Security & Local Processing Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("100% Local & Encrypted", fontWeight = FontWeight.Bold)
                        Text(
                            "Database is encrypted with SQLCipher. No phone numbers or messages leave your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))



            Text("Dispatch & Quota Settings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))

            // Monthly Limit Setup
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Monthly SMS Limit", fontWeight = FontWeight.SemiBold)
                    Text("Prevents sending beyond this count each month.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Limit: ${settings.monthlyLimit} SMS", fontWeight = FontWeight.Bold)
                        Row {
                            OutlinedButton(onClick = { viewModel.updateMonthlyLimit(settings.monthlyLimit - 25) }) { Text("-25") }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(onClick = { viewModel.updateMonthlyLimit(settings.monthlyLimit + 25) }) { Text("+25") }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Daily Limit Setup
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Daily SMS Limit", fontWeight = FontWeight.SemiBold)
                    Text("Auto-pauses campaign for the day when limit is reached.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Limit: ${settings.dailyLimit} SMS", fontWeight = FontWeight.Bold)
                        Row {
                            OutlinedButton(onClick = { viewModel.updateDailyLimit(settings.dailyLimit - 25) }) { Text("-25") }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(onClick = { viewModel.updateDailyLimit(settings.dailyLimit + 25) }) { Text("+25") }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-Resume at Midnight", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = settings.autoResumeEnabled,
                            onCheckedChange = { viewModel.toggleAutoResume(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Delay Mode Setup
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sending Delay Mode", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = settings.delayMode == com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity.MODE_FIXED,
                                onClick = { viewModel.updateDelayMode(com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity.MODE_FIXED) }
                            )
                            Text("Fixed", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = settings.delayMode == com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity.MODE_RANDOM,
                                onClick = { viewModel.updateDelayMode(com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity.MODE_RANDOM) }
                            )
                            Text("Random", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = settings.delayMode == com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity.MODE_NONE,
                                onClick = { viewModel.updateDelayMode(com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity.MODE_NONE) }
                            )
                            Text("None", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (settings.delayMode) {
                        com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity.MODE_RANDOM -> {
                            Text("Randomly picks a delay between min and max", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Minimum Delay: ${settings.minDelaySeconds} sec", style = MaterialTheme.typography.bodySmall)
                            Slider(
                                value = settings.minDelaySeconds.toFloat(),
                                onValueChange = { viewModel.updateMinDelay(it.toInt()) },
                                valueRange = 1f..3600f,
                                steps = 3598
                            )
                            Text("Maximum Delay: ${settings.maxDelaySeconds} sec", style = MaterialTheme.typography.bodySmall)
                            Slider(
                                value = settings.maxDelaySeconds.toFloat(),
                                onValueChange = { viewModel.updateMaxDelay(it.toInt()) },
                                valueRange = 1f..3600f,
                                steps = 3598
                            )
                        }
                        com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity.MODE_FIXED -> {
                            Text("Fixed Delay: ${settings.defaultDelaySeconds} seconds", style = MaterialTheme.typography.bodySmall)
                            Slider(
                                value = settings.defaultDelaySeconds.toFloat(),
                                onValueChange = { viewModel.updateDefaultDelay(it.toInt()) },
                                valueRange = 1f..3600f,
                                steps = 3598
                            )
                        }
                        com.evostackr.smscampaignmanager.data.local.entity.SettingsEntity.MODE_NONE -> {
                            Text("Immediately sends the next SMS. Recommended only for testing.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Max Retries Setup
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Max Retry Count for Failed SMS", fontWeight = FontWeight.SemiBold)
                    Text("Retries: ${settings.maxRetryCount} times", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = settings.maxRetryCount.toFloat(),
                        onValueChange = { viewModel.updateMaxRetries(it.toInt()) },
                        valueRange = 0f..5f,
                        steps = 4
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("App Preferences & Data", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))

            // Notifications Toggle
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Foreground Notifications", fontWeight = FontWeight.SemiBold)
                        Text("Show ongoing campaign progress notification", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingsNavigationCard(
                title = "Encrypted Backup & Recovery",
                subtitle = "AES-256 local backups & SMS log recovery",
                icon = Icons.Default.Backup,
                onClick = onNavigateToBackupSettings
            )

            SettingsNavigationCard(
                title = "App Permissions & Setup",
                subtitle = "Review & grant SMS, Contacts & Notification access",
                icon = Icons.Default.Security,
                onClick = onNavigateToPermissions
            )

            SettingsNavigationCard(
                title = "Re-play Onboarding Setup",
                subtitle = "Walk through guided onboarding tutorial & permissions",
                icon = Icons.Default.RocketLaunch,
                onClick = onNavigateToOnboarding
            )

            SettingsNavigationCard(
                title = "Data & Privacy Controls",
                subtitle = "Clear cache, local storage & wipe encrypted DB",
                icon = Icons.Default.Lock,
                onClick = onNavigateToDataPrivacy
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("In-App Information", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))

            SettingsNavigationCard(
                title = "About Us",
                subtitle = "Evostackr product mission & branding details",
                icon = Icons.Default.Info,
                onClick = onNavigateToAbout
            )

            SettingsNavigationCard(
                title = "Help & User Tutorials",
                subtitle = "Learn CSV import, scheduling & campaign workflows",
                icon = Icons.Default.Help,
                onClick = onNavigateToHelp
            )

            SettingsNavigationCard(
                title = "Frequently Asked Questions (FAQ)",
                subtitle = "Instant answers to common app questions",
                icon = Icons.Default.QuestionAnswer,
                onClick = onNavigateToFaq
            )

            SettingsNavigationCard(
                title = "Contact Us & Bug Report",
                subtitle = "Reach out to Evostackr developer team",
                icon = Icons.Default.Email,
                onClick = onNavigateToContact
            )

            SettingsNavigationCard(
                title = "Open Source Licenses",
                subtitle = "Third-party open-source libraries",
                icon = Icons.Default.Code,
                onClick = onNavigateToLicenses
            )

            SettingsNavigationCard(
                title = "App Version & System Info",
                subtitle = "Version 1.0.0 (Build 1) package info",
                icon = Icons.Default.Verified,
                onClick = onNavigateToVersion
            )

            SettingsNavigationCard(
                title = "Changelog & History",
                subtitle = "See latest improvements & bug fixes",
                icon = Icons.Default.History,
                onClick = onNavigateToChangelog
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Reset Monthly Counter Button
            OutlinedButton(
                onClick = { viewModel.resetMonthlyCounter() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset Monthly Counter (${settings.sentThisMonth} sent)")
            }

            Spacer(modifier = Modifier.height(24.dp))

            LegalFooter()

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}



@Composable
private fun SettingsNavigationCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
