package com.evostackr.smscampaignmanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.evostackr.smscampaignmanager.ui.theme.StatusFailed
import com.evostackr.smscampaignmanager.ui.theme.StatusSuccess

@Composable
fun AnalyticsChart(
    successCount: Int,
    failedCount: Int,
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val total = (successCount + failedCount + pendingCount).coerceAtLeast(1)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Delivery Performance & Analytics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barWidth = canvasWidth / 4f
                val maxBarHeight = canvasHeight * 0.8f

                val successHeight = (successCount.toFloat() / total) * maxBarHeight
                val failedHeight = (failedCount.toFloat() / total) * maxBarHeight
                val pendingHeight = (pendingCount.toFloat() / total) * maxBarHeight

                // Draw Success Bar
                drawRect(
                    color = StatusSuccess,
                    topLeft = Offset(barWidth * 0.5f, canvasHeight - successHeight),
                    size = Size(barWidth * 0.6f, successHeight)
                )

                // Draw Failed Bar
                drawRect(
                    color = StatusFailed,
                    topLeft = Offset(barWidth * 1.7f, canvasHeight - failedHeight),
                    size = Size(barWidth * 0.6f, failedHeight)
                )

                // Draw Pending Bar
                drawRect(
                    color = primaryColor,
                    topLeft = Offset(barWidth * 2.9f, canvasHeight - pendingHeight),
                    size = Size(barWidth * 0.6f, pendingHeight)
                )

                // Base axis line
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, canvasHeight),
                    end = Offset(canvasWidth, canvasHeight),
                    strokeWidth = 2f
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                LegendItem(color = StatusSuccess, label = "Sent ($successCount)")
                LegendItem(color = StatusFailed, label = "Failed ($failedCount)")
                LegendItem(color = primaryColor, label = "Pending ($pendingCount)")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

