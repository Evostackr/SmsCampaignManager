package com.evostackr.smscampaignmanager.ui.contacts

import android.Manifest
import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.evostackr.smscampaignmanager.data.local.entity.ContactEntity
import com.evostackr.smscampaignmanager.ui.components.PermissionPermanentlyDeniedDialog
import com.evostackr.smscampaignmanager.ui.components.PermissionRationaleDialog
import com.evostackr.smscampaignmanager.util.PermissionManager
import com.evostackr.smscampaignmanager.util.PermissionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val contacts by viewModel.contacts.collectAsState()
    val groups by viewModel.allGroups.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGroup by viewModel.selectedGroupTag.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }

    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newGroup by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }

    // File Import Launchers
    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importCsvOrExcelFile(it, isExcel = false) }
    }

    val excelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importCsvOrExcelFile(it, isExcel = true) }
    }

    // Permission Launcher using Activity Result API
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.importDeviceContacts()
        } else {
            if (PermissionManager.isPermanentlyDenied(activity, Manifest.permission.READ_CONTACTS)) {
                showPermanentlyDeniedDialog = true
            } else {
                showRationaleDialog = true
            }
        }
    }

    LaunchedEffect(Unit) {
        val state = PermissionManager.checkPermission(context, Manifest.permission.READ_CONTACTS)
        if (state == PermissionState.GRANTED) {
            viewModel.importDeviceContacts()
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts & Groups", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { csvPickerLauncher.launch("text/*") }) {
                        Icon(Icons.Default.FileOpen, contentDescription = "Import CSV")
                    }
                    IconButton(onClick = { excelPickerLauncher.launch("*/*") }) {
                        Icon(Icons.Default.TableChart, contentDescription = "Import Excel")
                    }
                    IconButton(onClick = { viewModel.mergeDuplicates() }) {
                        Icon(Icons.AutoMirrored.Filled.MergeType, contentDescription = "Merge Duplicates")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search by name or number...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            // Import Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { csvPickerLauncher.launch("*/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import CSV")
                }

                OutlinedButton(
                    onClick = { excelPickerLauncher.launch("*/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import Excel")
                }
            }

            // Group Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedGroup == "ALL",
                    onClick = { viewModel.selectGroup("ALL") },
                    label = { Text("All (${contacts.size})") }
                )
                FilterChip(
                    selected = selectedGroup == "FAVORITES",
                    onClick = { viewModel.selectGroup("FAVORITES") },
                    label = { Text("Favorites") }
                )
                groups.forEach { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { viewModel.selectGroup(group) },
                        label = { Text(group) }
                    )
                }
            }

            if (statusMessage != null) {
                Text(
                    text = statusMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Contacts List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts, key = { it.id }) { contact ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contact.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(contact.phoneNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (contact.groupTag != null) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(contact.groupTag!!, style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            Row {
                                IconButton(onClick = { viewModel.toggleFavorite(contact) }) {
                                    Icon(
                                        if (contact.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (contact.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteContact(contact) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Contact") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text("Phone Number") }, singleLine = true)
                    OutlinedTextField(value = newGroup, onValueChange = { newGroup = it }, label = { Text("Group Tag (Optional)") }, singleLine = true)
                    OutlinedTextField(value = newEmail, onValueChange = { newEmail = it }, label = { Text("Email (Optional)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank() && newPhone.isNotBlank()) {
                        viewModel.addContact(newName, newPhone, newGroup, newEmail)
                        showAddDialog = false
                        newName = ""
                        newPhone = ""
                        newGroup = ""
                        newEmail = ""
                    }
                }) {
                    Text("Save Contact")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRationaleDialog) {
        PermissionRationaleDialog(
            permissionName = "Read Contacts",
            explanation = "SMS Campaign Manager needs access to your contacts to let you send messages using contact names.",
            onGrantClick = {
                showRationaleDialog = false
                contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            },
            onDismiss = { showRationaleDialog = false }
        )
    }

    if (showPermanentlyDeniedDialog) {
        PermissionPermanentlyDeniedDialog(
            permissionName = "Read Contacts",
            explanation = "Contacts permission is required to import your phone contacts.",
            onOpenSettingsClick = {
                showPermanentlyDeniedDialog = false
                PermissionManager.openAppSettings(context)
            },
            onDismiss = { showPermanentlyDeniedDialog = false }
        )
    }
}

