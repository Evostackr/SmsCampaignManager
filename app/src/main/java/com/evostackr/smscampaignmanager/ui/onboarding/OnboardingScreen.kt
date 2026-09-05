package com.evostackr.smscampaignmanager.ui.onboarding

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.evostackr.smscampaignmanager.R
import com.evostackr.smscampaignmanager.util.PermissionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // Check permission statuses when launching screen or returning
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.updatePermissionStatuses(context, activity)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Standard Android Permission Launchers using official Activity Result API
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        viewModel.handlePermissionResult(PermissionType.SMS, allGranted, activity)
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.handlePermissionResult(PermissionType.CONTACTS, isGranted, activity)
    }

    val notificationsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.handlePermissionResult(PermissionType.NOTIFICATIONS, isGranted, activity)
    }

    // Auto-skip Notifications page if on older Android (< Android 13)
    LaunchedEffect(uiState.currentPage) {
        if (uiState.currentPage == 4 && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            viewModel.nextPage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Step ${uiState.currentPage + 1} of ${uiState.totalPages}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (uiState.currentPage > 0) {
                        IconButton(onClick = { viewModel.previousPage() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                        }
                    }
                },
                actions = {
                    if (uiState.currentPage < uiState.totalPages - 1) {
                        TextButton(onClick = { viewModel.nextPage() }) {
                            Text(
                                "Skip",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            OnboardingBottomNavigation(
                currentPage = uiState.currentPage,
                totalPages = uiState.totalPages,
                onNextClick = {
                    if (uiState.currentPage == uiState.totalPages - 1) {
                        viewModel.finishOnboarding(onNavigateToDashboard)
                    } else {
                        viewModel.nextPage()
                    }
                },
                onDotClick = { page -> viewModel.goToPage(page) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = uiState.currentPage,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "OnboardingPageTransition"
            ) { page ->
                when (page) {
                    0 -> WelcomePage(onGetStartedClick = { viewModel.nextPage() })
                    1 -> FeaturesPage()
                    2 -> PermissionPage(
                        title = "Send SMS",
                        description = "SMSCampaignManager requires SMS permission to send messages that you create.\n\nWe never send messages without your action.",
                        icon = Icons.AutoMirrored.Filled.Send,
                        status = uiState.smsStatus,
                        showSuccessAnimation = uiState.showSmsSuccessAnimation,
                        permissionExplanation = PermissionManager.getPermissionExplanation(Manifest.permission.SEND_SMS),
                        onGrantPermission = {
                            smsPermissionLauncher.launch(PermissionManager.SMS_PERMISSIONS.toTypedArray())
                        },
                        onRetry = {
                            smsPermissionLauncher.launch(PermissionManager.SMS_PERMISSIONS.toTypedArray())
                        },
                        onContinueAnyway = { viewModel.nextPage() },
                        onOpenSettings = { PermissionManager.openAppSettings(context) }
                    )
                    3 -> PermissionPage(
                        title = "Read Contacts",
                        description = "SMSCampaignManager requires Contacts permission to import and address recipients directly from your address book.\n\nYour contacts are stored locally and never uploaded anywhere.",
                        icon = Icons.Default.Contacts,
                        status = uiState.contactsStatus,
                        showSuccessAnimation = uiState.showContactsSuccessAnimation,
                        permissionExplanation = PermissionManager.getPermissionExplanation(Manifest.permission.READ_CONTACTS),
                        onGrantPermission = {
                            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        },
                        onRetry = {
                            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        },
                        onContinueAnyway = { viewModel.nextPage() },
                        onOpenSettings = { PermissionManager.openAppSettings(context) }
                    )
                    4 -> PermissionPage(
                        title = "Notifications",
                        description = "SMSCampaignManager uses notifications to keep you updated on active bulk campaign dispatch progress, delivery status, and warnings.",
                        icon = Icons.Default.Notifications,
                        status = uiState.notificationsStatus,
                        showSuccessAnimation = uiState.showNotificationsSuccessAnimation,
                        permissionExplanation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            PermissionManager.getPermissionExplanation(Manifest.permission.POST_NOTIFICATIONS)
                        } else "Notifications enabled automatically on this Android version.",
                        onGrantPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.nextPage()
                            }
                        },
                        onRetry = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.nextPage()
                            }
                        },
                        onContinueAnyway = { viewModel.nextPage() },
                        onOpenSettings = { PermissionManager.openAppSettings(context) }
                    )
                    5 -> PrivacySecurityPage()
                    6 -> GetStartedPage(
                        smsStatus = uiState.smsStatus,
                        contactsStatus = uiState.contactsStatus,
                        notificationsStatus = uiState.notificationsStatus,
                        onFinishClick = { viewModel.finishOnboarding(onNavigateToDashboard) }
                    )
                }
            }
        }
    }
}

// ── 1. Welcome Page ─────────────────────────────────────────────────────────
@Composable
private fun WelcomePage(onGetStartedClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "SMS Campaign Manager Logo",
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(24.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to\nSMS Campaign Manager",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ) {
            Text(
                text = "An Evostackr Product",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "The ultimate professional solution for bulk SMS campaigns, SIM card dispatch, contact group management, and real-time dispatch analytics.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onGetStartedClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Begin Guided Setup", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

// ── 2. Features Page ────────────────────────────────────────────────────────
@Composable
private fun FeaturesPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Powerful Features",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Everything you need to launch and monitor high-volume mobile campaigns.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        FeatureCard(
            icon = Icons.AutoMirrored.Filled.Send,
            title = "Bulk SMS Dispatch",
            description = "Send personalized bulk SMS campaigns seamlessly using your native phone SIM subscriptions."
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureCard(
            icon = Icons.Default.Group,
            title = "Contacts & File Import",
            description = "Organize contacts into custom groups, import device address books, or load external CSV and Excel spreadsheets directly."
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureCard(
            icon = Icons.Default.Analytics,
            title = "Real-Time Reports & Logs",
            description = "Track exact sent, delivered, failed status, carrier responses, and monthly rate limits with interactive charts."
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureCard(
            icon = Icons.Default.Schedule,
            title = "Scheduled Deliveries & Templates",
            description = "Queue campaigns for precise automated background delivery and manage reusable message templates."
        )
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── 3, 4, 5. Permission Page ────────────────────────────────────────────────
@Composable
private fun PermissionPage(
    title: String,
    description: String,
    icon: ImageVector,
    status: OnboardingPermissionStatus,
    showSuccessAnimation: Boolean,
    permissionExplanation: String,
    onGrantPermission: () -> Unit,
    onRetry: () -> Unit,
    onContinueAnyway: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Icon & Permission Header
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        when (status) {
                            OnboardingPermissionStatus.GRANTED -> MaterialTheme.colorScheme.primaryContainer
                            OnboardingPermissionStatus.DENIED -> MaterialTheme.colorScheme.errorContainer
                            OnboardingPermissionStatus.PERMANENTLY_DENIED -> MaterialTheme.colorScheme.errorContainer
                            OnboardingPermissionStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = when (status) {
                        OnboardingPermissionStatus.GRANTED -> MaterialTheme.colorScheme.primary
                        OnboardingPermissionStatus.DENIED -> MaterialTheme.colorScheme.error
                        OnboardingPermissionStatus.PERMANENTLY_DENIED -> MaterialTheme.colorScheme.error
                        OnboardingPermissionStatus.PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Permission Status Badge Component
            PermissionStatusBadge(status = status)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Success Animated Card or Rationale Box
            if (status == OnboardingPermissionStatus.GRANTED || showSuccessAnimation) {
                SuccessPermissionCard()
            } else if (status == OnboardingPermissionStatus.DENIED) {
                DeniedRationaleCard(
                    explanation = permissionExplanation,
                    onRetry = onRetry,
                    onContinueAnyway = onContinueAnyway
                )
            } else if (status == OnboardingPermissionStatus.PERMANENTLY_DENIED) {
                PermanentlyDeniedCard(
                    onOpenSettings = onOpenSettings,
                    onContinueAnyway = onContinueAnyway
                )
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (status) {
                OnboardingPermissionStatus.PENDING -> {
                    Button(
                        onClick = onGrantPermission,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grant Permission", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    OutlinedButton(
                        onClick = onContinueAnyway,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Skip", fontWeight = FontWeight.SemiBold)
                    }
                }
                OnboardingPermissionStatus.GRANTED -> {
                    Button(
                        onClick = onContinueAnyway,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
                OnboardingPermissionStatus.DENIED -> {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry Permission", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    OutlinedButton(
                        onClick = onContinueAnyway,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continue Anyway", fontWeight = FontWeight.SemiBold)
                    }
                }
                OnboardingPermissionStatus.PERMANENTLY_DENIED -> {
                    Button(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    OutlinedButton(
                        onClick = onContinueAnyway,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Skip for Now", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionStatusBadge(status: OnboardingPermissionStatus) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = when (status) {
            OnboardingPermissionStatus.GRANTED -> Color(0x224CAF50)
            OnboardingPermissionStatus.DENIED -> Color(0x22F44336)
            OnboardingPermissionStatus.PERMANENTLY_DENIED -> Color(0x22F44336)
            OnboardingPermissionStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (status) {
                    OnboardingPermissionStatus.GRANTED -> Icons.Default.CheckCircle
                    OnboardingPermissionStatus.DENIED -> Icons.Default.Cancel
                    OnboardingPermissionStatus.PERMANENTLY_DENIED -> Icons.Default.Warning
                    OnboardingPermissionStatus.PENDING -> Icons.Default.HourglassEmpty
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = when (status) {
                    OnboardingPermissionStatus.GRANTED -> Color(0xFF4CAF50)
                    OnboardingPermissionStatus.DENIED -> Color(0xFFF44336)
                    OnboardingPermissionStatus.PERMANENTLY_DENIED -> Color(0xFFF44336)
                    OnboardingPermissionStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when (status) {
                    OnboardingPermissionStatus.GRANTED -> "Granted"
                    OnboardingPermissionStatus.DENIED -> "Denied"
                    OnboardingPermissionStatus.PERMANENTLY_DENIED -> "Permanently Denied"
                    OnboardingPermissionStatus.PENDING -> "Pending"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = when (status) {
                    OnboardingPermissionStatus.GRANTED -> Color(0xFF4CAF50)
                    OnboardingPermissionStatus.DENIED -> Color(0xFFF44336)
                    OnboardingPermissionStatus.PERMANENTLY_DENIED -> Color(0xFFF44336)
                    OnboardingPermissionStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun SuccessPermissionCard() {
    var scaleState by remember { mutableStateOf(0.6f) }
    val animatedScale by animateFloatAsState(
        targetValue = scaleState,
        animationSpec = tween(durationMillis = 400),
        label = "SuccessScale"
    )

    LaunchedEffect(Unit) {
        scaleState = 1.0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x184CAF50))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Permission Granted!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    "Advancing automatically to the next page...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DeniedRationaleCard(
    explanation: String,
    onRetry: () -> Unit,
    onContinueAnyway: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Why Permission is Needed", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry")
                }
                OutlinedButton(
                    onClick = onContinueAnyway,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Continue Anyway")
                }
            }
        }
    }
}

@Composable
private fun PermanentlyDeniedCard(
    onOpenSettings: () -> Unit,
    onContinueAnyway: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Permission Permanently Denied", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "You have previously selected 'Don't ask again'. To enable this permission, open Android Settings.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open Settings")
                }
                OutlinedButton(
                    onClick = onContinueAnyway,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Skip for Now")
                }
            }
        }
    }
}

// ── 6. Privacy & Security Page ──────────────────────────────────────────────
@Composable
private fun PrivacySecurityPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Privacy & Security First",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your data and contact privacy are completely protected.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrivacyGuaranteeItem(
            icon = Icons.Default.Lock,
            title = "100% On-Device Storage",
            description = "All campaign messages, imported contact numbers, and logs stay strictly on your local device."
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrivacyGuaranteeItem(
            icon = Icons.Default.Shield,
            title = "Encrypted Local Database",
            description = "Room database is secured using SQLCipher encryption to protect your records against unauthorized extraction."
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrivacyGuaranteeItem(
            icon = Icons.Default.VerifiedUser,
            title = "User Action Explicit Dispatch",
            description = "SMSCampaignManager never sends messages in the background without your explicit action and confirmation."
        )
    }
}

@Composable
private fun PrivacyGuaranteeItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── 7. Get Started Page ─────────────────────────────────────────────────────
@Composable
private fun GetStartedPage(
    smsStatus: OnboardingPermissionStatus,
    contactsStatus: OnboardingPermissionStatus,
    notificationsStatus: OnboardingPermissionStatus,
    onFinishClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "You're All Set!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Setup is complete. You can now launch campaigns and manage recipients easily.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Permission Setup Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Permission Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    SummaryPermissionRow(name = "SMS Sending", status = smsStatus)
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryPermissionRow(name = "Contacts Read", status = contactsStatus)
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryPermissionRow(name = "Notifications", status = notificationsStatus)
                }
            }
        }

        Button(
            onClick = onFinishClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun SummaryPermissionRow(name: String, status: OnboardingPermissionStatus) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.bodyMedium)
        PermissionStatusBadge(status = status)
    }
}

// ── Bottom Page Indicator & Navigation ──────────────────────────────────────
@Composable
private fun OnboardingBottomNavigation(
    currentPage: Int,
    totalPages: Int,
    onNextClick: () -> Unit,
    onDotClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Page Indicator Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalPages) { index ->
                val isSelected = index == currentPage
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(if (isSelected) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                )
            }
        }

        IconButton(
            onClick = onNextClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = if (currentPage == totalPages - 1) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next Page",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
