package com.evostackr.smscampaignmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.evostackr.smscampaignmanager.data.local.entity.CampaignEntity
import com.evostackr.smscampaignmanager.ui.theme.StatusFailed
import com.evostackr.smscampaignmanager.ui.theme.StatusPaused
import com.evostackr.smscampaignmanager.ui.theme.StatusPending
import com.evostackr.smscampaignmanager.ui.theme.StatusSuccess
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CampaignItem(
    campaign: CampaignEntity,
    onClick: () -> Unit,
    onPauseClick: (() -> Unit)? = null,
    onResumeClick: (() -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val progress = if (campaign.totalRecipients > 0) {
        (campaign.sentCount + campaign.failedCount).toFloat() / campaign.totalRecipients.toFloat()
    } else 0f

    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
    val dateStr = dateFormat.format(Date(campaign.createdAt))

    val statusColor = when (campaign.status) {
        CampaignEntity.STATUS_IN_PROGRESS -> StatusPending
        CampaignEntity.STATUS_COMPLETED -> StatusSuccess
        CampaignEntity.STATUS_PAUSED -> StatusPaused
        CampaignEntity.STATUS_CANCELLED, CampaignEntity.STATUS_FAILED -> StatusFailed
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = campaign.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = campaign.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = campaign.messageTemplate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sent: ${campaign.sentCount}/${campaign.totalRecipients} (${(progress * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick actions if campaign is active
            if (campaign.status == CampaignEntity.STATUS_IN_PROGRESS || campaign.status == CampaignEntity.STATUS_PAUSED) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (campaign.status == CampaignEntity.STATUS_IN_PROGRESS && onPauseClick != null) {
                        OutlinedButton(
                            onClick = onPauseClick,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause")
                        }
                    } else if (campaign.status == CampaignEntity.STATUS_PAUSED && onResumeClick != null) {
                        Button(
                            onClick = onResumeClick,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume")
                        }
                    }

                    if (onCancelClick != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = onCancelClick,
                            colors = ButtonDefaults.textButtonColors(contentColor = StatusFailed)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

