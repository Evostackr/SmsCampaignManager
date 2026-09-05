package com.evostackr.smscampaignmanager.ui.campaign

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.evostackr.smscampaignmanager.ui.template.TemplateViewModel
import com.evostackr.smscampaignmanager.util.ParsedContact
import com.evostackr.smscampaignmanager.util.PermissionManager
import com.evostackr.smscampaignmanager.util.SmsCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCampaignScreen(
    onNavigateBack: () -> Unit,
    onCampaignCreated: (Long) -> Unit,
    campaignViewModel: CampaignViewModel = hiltViewModel(),
    templateViewModel: TemplateViewModel = hiltViewModel()
) {
    val uiState by campaignViewModel.createUiState.collectAsState()
    val templates by templateViewModel.allTemplates.collectAsState()
    val context = LocalContext.current

    var showTemplateSheet by remember { mutableStateOf(false) }

    // CSV File Picker launcher
    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { campaignViewModel.parseCsvFromUri(it) }
    }

    // Single Contact Picker launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let { contactUri ->
            try {
                val cursor = context.contentResolver.query(contactUri, null, null, null, null)
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                        val name = if (nameIndex >= 0) c.getString(nameIndex) else null
                        val contactId = if (idIndex >= 0) c.getString(idIndex) else null

                        if (contactId != null) {
                            val phoneCursor = context.contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                arrayOf(contactId),
                                null
                            )
                            phoneCursor?.use { pc ->
                                if (pc.moveToFirst()) {
                                    val numberIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    if (numberIndex >= 0) {
                                        val number = pc.getString(numberIndex)
                                        campaignViewModel.addManualContacts(listOf(ParsedContact(name = name, phoneNumber = number)))
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    val segments = SmsCalculator.calculateSegments(uiState.messageText)
    val estimatedTotalSms = SmsCalculator.estimateTotalSms(uiState.parsedContacts.size, uiState.messageText)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create SMS Campaign",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Campaign Identity
            Text(
                text = "1. Campaign Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.campaignName,
                onValueChange = { campaignViewModel.updateCampaignName(it) },
                label = { Text("Campaign Name") },
                placeholder = { Text("e.g. July Promo Campaign") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Recipients & Import Options
            Text(
                text = "2. Recipients (${uiState.parsedContacts.size} Selected)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Import Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { csvPickerLauncher.launch("text/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import CSV")
                }
                OutlinedButton(
                    onClick = { contactPickerLauncher.launch(null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Contacts")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Device Contacts Import Button
            OutlinedButton(
                onClick = { campaignViewModel.importDeviceContacts() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isImportingContacts,
                shape = RoundedCornerShape(10.dp)
            ) {
                if (uiState.isImportingContacts) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importing Device Contacts...")
                } else {
                    Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import All Device Contacts")
                }
            }

            if (uiState.importSummaryMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = uiState.importSummaryMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Manual Raw Phone Numbers Input
            OutlinedTextField(
                value = uiState.rawTextRecipients,
                onValueChange = { campaignViewModel.updateRawText(it) },
                label = { Text("Paste Phone Numbers / Raw Text") },
                placeholder = { Text("Enter numbers separated by comma, space or newline\ne.g. +1234567890, +9876543210") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Duplicate toggle & stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uiState.removeDuplicates,
                        onCheckedChange = { campaignViewModel.setRemoveDuplicates(it) }
                    )
                    Text("Remove Duplicates", style = MaterialTheme.typography.bodyMedium)
                }

                if (uiState.parsedContacts.isNotEmpty()) {
                    TextButton(onClick = { campaignViewModel.clearRecipients() }) {
                        Text("Clear All", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (uiState.duplicateCount > 0 || uiState.invalidCount > 0) {
                Text(
                    text = "Skipped: ${uiState.duplicateCount} duplicates, ${uiState.invalidCount} invalid format",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Message Template Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3. Message Template",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                TextButton(onClick = { showTemplateSheet = true }) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Insert Template")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Message text box (OutlinedTextField)
            OutlinedTextField(
                value = uiState.messageText,
                onValueChange = { campaignViewModel.updateMessageText(it) },
                label = { Text("SMS Message") },
                placeholder = { Text("Hello {name}, your special offer is ready...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val variables = listOf("{name}", "{phone}", "{company}", "{date}", "{time}", "{amount}", "{city}", "{custom}")
                    items(variables.size) { index ->
                        val variable = variables[index]
                        AssistChip(
                            onClick = { campaignViewModel.updateMessageText(uiState.messageText + variable) },
                            label = { Text("+ $variable") }
                        )
                    }
                }
                Text(
                    text = "${uiState.messageText.length} chars ($segments SMS / recipient)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Delay & Estimation
            Text(
                text = "4. Dispatch Settings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Delay between SMS: ${uiState.delaySeconds} seconds", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = uiState.delaySeconds.toFloat(),
                onValueChange = { campaignViewModel.updateDelaySeconds(it.toInt()) },
                valueRange = 5f..600f,
                steps = 11
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Campaign Cost & Time Estimate",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total Recipients: ${uiState.parsedContacts.size}")
                    Text("Total SMS Required: $estimatedTotalSms SMS")
                    val totalSeconds = uiState.parsedContacts.size * uiState.delaySeconds
                    val minutes = totalSeconds / 60
                    Text("Estimated Time: ~$minutes minutes")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Start Campaign Button
            Button(
                onClick = { campaignViewModel.showConfirmDialog() },
                enabled = uiState.campaignName.isNotBlank() && uiState.parsedContacts.isNotEmpty() && uiState.messageText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Campaign",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    var showRationaleDialog by remember { mutableStateOf(false) }
    var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            campaignViewModel.startCampaign { newId ->
                onCampaignCreated(newId)
            }
        } else {
            val activity = context as? android.app.Activity
            if (PermissionManager.isPermanentlyDenied(activity, android.Manifest.permission.SEND_SMS)) {
                showPermanentlyDeniedDialog = true
            } else {
                showRationaleDialog = true
            }
        }
    }

    val submitCampaignAction = {
        val requiredPerms = PermissionManager.SMS_PERMISSIONS
        if (PermissionManager.hasAllPermissions(context, requiredPerms)) {
            campaignViewModel.startCampaign { newId ->
                onCampaignCreated(newId)
            }
        } else {
            smsPermissionLauncher.launch(requiredPerms.toTypedArray())
        }
    }

    // Confirmation Modal Dialog
    if (uiState.isConfirmDialogOpen) {
        AlertDialog(
            onDismissRequest = { campaignViewModel.hideConfirmDialog() },
            title = {
                Text(
                    "Confirm SMS Campaign Launch",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Are you sure you want to start sending SMS for '${uiState.campaignName}'?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Recipients: ${uiState.parsedContacts.size}")
                    Text("• Total SMS Quota: $estimatedTotalSms")
                    Text("• Delay: ${uiState.delaySeconds} seconds between messages")
                    Text("• Carrier: User SIM card default")
                }
            },
            confirmButton = {
                Button(
                    onClick = { submitCampaignAction() }
                ) {
                    Text("Confirm & Start")
                }
            },
            dismissButton = {
                TextButton(onClick = { campaignViewModel.hideConfirmDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRationaleDialog) {
        com.evostackr.smscampaignmanager.ui.components.PermissionRationaleDialog(
            permissionName = "SEND SMS",
            explanation = PermissionManager.getPermissionExplanation(android.Manifest.permission.SEND_SMS),
            onGrantClick = { smsPermissionLauncher.launch(PermissionManager.SMS_PERMISSIONS.toTypedArray()) },
            onDismiss = { showRationaleDialog = false }
        )
    }

    if (showPermanentlyDeniedDialog) {
        com.evostackr.smscampaignmanager.ui.components.PermissionPermanentlyDeniedDialog(
            permissionName = "SEND SMS",
            explanation = PermissionManager.getPermissionExplanation(android.Manifest.permission.SEND_SMS),
            onOpenSettingsClick = { PermissionManager.openAppSettings(context) },
            onDismiss = { showPermanentlyDeniedDialog = false }
        )
    }

    // Template Selector Sheet/Dialog
    if (showTemplateSheet) {
        AlertDialog(
            onDismissRequest = { showTemplateSheet = false },
            title = { Text("Select SMS Template") },
            text = {
                if (templates.isEmpty()) {
                    Text("No saved templates. Create one in the Templates tab.")
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        templates.forEach { tmpl ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = {
                                    campaignViewModel.updateMessageText(tmpl.content)
                                    showTemplateSheet = false
                                }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(tmpl.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        if (tmpl.isBuiltIn) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.padding(start = 8.dp)
                                            ) {
                                                Text(
                                                    "Built-in",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(tmpl.content, maxLines = 2)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTemplateSheet = false }) {
                    Text("Close")
                }
            }
        )
    }
}
