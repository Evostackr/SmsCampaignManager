package com.evostackr.smscampaignmanager.ui.campaign

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.evostackr.smscampaignmanager.data.local.entity.CampaignEntity
import com.evostackr.smscampaignmanager.ui.components.CampaignItem
import com.evostackr.smscampaignmanager.ui.components.EmptyStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignListScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: CampaignViewModel = hiltViewModel()
) {
    val campaigns by viewModel.allCampaigns.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredCampaigns = campaigns.filter { campaign ->
        val matchesQuery = campaign.name.contains(searchQuery, ignoreCase = true) ||
                campaign.messageTemplate.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "ACTIVE" -> campaign.status == CampaignEntity.STATUS_IN_PROGRESS || campaign.status == CampaignEntity.STATUS_QUEUED
            "COMPLETED" -> campaign.status == CampaignEntity.STATUS_COMPLETED
            "PAUSED" -> campaign.status == CampaignEntity.STATUS_PAUSED
            "CANCELLED" -> campaign.status == CampaignEntity.STATUS_CANCELLED || campaign.status == CampaignEntity.STATUS_FAILED
            else -> true
        }

        matchesQuery && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campaigns", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Campaign")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by campaign name or message...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "ACTIVE", "COMPLETED", "PAUSED", "CANCELLED").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredCampaigns.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.AutoMirrored.Filled.Send,
                        title = if (campaigns.isEmpty()) "No Campaigns Created" else "No Matching Campaigns",
                        description = if (campaigns.isEmpty()) "Compose and schedule your first bulk SMS campaign with custom recipient groups." else "Try adjusting your search query or filter selection.",
                        actionLabel = "Create Campaign",
                        onActionClick = onNavigateToCreate
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredCampaigns, key = { it.id }) { campaign ->
                        CampaignItem(
                            campaign = campaign,
                            onClick = { onNavigateToDetail(campaign.id) },
                            onPauseClick = { viewModel.pauseCampaign(campaign.id) },
                            onResumeClick = { viewModel.resumeCampaign(campaign.id) },
                            onCancelClick = { viewModel.cancelCampaign(campaign.id) }
                        )
                    }
                }
            }
        }
    }
}

