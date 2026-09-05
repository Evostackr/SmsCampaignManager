package com.evostackr.smscampaignmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.evostackr.smscampaignmanager.util.SimStateInfo

@Composable
fun SimInfoCard(
    simState: SimStateInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (simState.isAirplaneModeOn) Icons.Default.AirplanemodeActive else Icons.Default.SimCard,
                    contentDescription = "SIM Info",
                    tint = if (simState.isAirplaneModeOn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Carrier SIM Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (simState.isAirplaneModeOn) "Airplane Mode Active (SMS Blocked)" else simState.primaryCarrierName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (simState.isAirplaneModeOn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (simState.simList.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    simState.simList.forEach { sim ->
                        AssistChip(
                            onClick = {},
                            label = { Text("${sim.displayName} (${sim.carrierName})") },
                            leadingIcon = { Icon(Icons.Default.SimCard, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }
    }
}

