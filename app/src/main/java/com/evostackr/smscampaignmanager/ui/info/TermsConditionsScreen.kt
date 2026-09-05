package com.evostackr.smscampaignmanager.ui.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsConditionsScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold) },
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Terms of Service Agreement", fontWeight = FontWeight.Bold)
                        Text("SMSCampaignManager by Evostackr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TermCard("1. Acceptance of Terms", "By installing and using SMSCampaignManager (developed by Evostackr), you agree to be bound by these Terms and Conditions. If you do not agree to these terms, do not use the application.")

            TermCard("2. User Responsibilities & Anti-Spam Policy", "You are solely responsible for all SMS content composed and sent via the application. You agree NOT to use the application for sending spam, unsolicited commercial messages, harassing content, deceptive phishing, or illegal communications. You must comply with all local telecom regulations and obtain consent from recipients prior to messaging.")

            TermCard("3. Cellular Carrier Charges & Billing", "SMS messages dispatched through SMSCampaignManager are sent using your device's active cellular SIM card plan. You are entirely responsible for any SMS charges, carrier fees, roaming costs, or plan overages billed by your mobile network operator.")

            TermCard("4. Carrier Limitations & Delivery Guarantees", "Evostackr does not guarantee 100% SMS delivery, as final message transmission depends on cellular network coverage, SIM carrier limits, signal strength, and recipient device states.")

            TermCard("5. Limitation of Liability", "To the maximum extent permitted by law, Evostackr shall not be liable for any indirect, incidental, or consequential damages resulting from message failure, carrier charges, data loss, or improper usage of the software.")

            TermCard("6. Service Changes & Updates", "Evostackr reserves the right to modify or update features within SMSCampaignManager to enhance security, performance, or compliance with Google Play Store guidelines.")

            Spacer(modifier = Modifier.height(16.dp))

            LegalFooter()
        }
    }
}

@Composable
private fun TermCard(title: String, content: String) {
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

