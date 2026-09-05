package com.evostackr.smscampaignmanager.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * AppNavigationWrapper
 *
 * Production-quality responsive navigation container:
 * - Phone UI (< 600dp): Modern 5-tab Bottom Navigation Bar with a "More" BottomSheet
 * - Tablet UI (>= 600dp): Clean vertical Navigation Rail with all 7 items
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationWrapper(
    navController: NavHostController,
    currentRoute: String?,
    content: @Composable (PaddingValues) -> Unit
) {
    var showMoreSheet by remember { mutableStateOf(false) }

    // Check if current route is one of the top-level screens
    val isTopLevelRoute = Screen.allTopLevelScreens.any { it.route == currentRoute }

    // Is current route an overflow item (Templates, Reports, Settings)?
    val isOverflowSelected = Screen.overflowNavItems.any { it.route == currentRoute }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen && isTopLevelRoute) {
            // ── Tablet Layout: Navigation Rail ─────────────────────────────────
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                    header = {
                        Spacer(modifier = Modifier.height(16.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                ) {
                    Screen.allTopLevelScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = {
                                navigateToTopLevel(navController, currentRoute, screen.route)
                            },
                            icon = {
                                val vector = if (isSelected) (screen.selectedIcon ?: screen.icon) else screen.icon
                                vector?.let { Icon(it, contentDescription = screen.title) }
                            },
                            label = {
                                Text(
                                    text = screen.navLabel,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 11.sp
                                )
                            },
                            alwaysShowLabel = true
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    content(PaddingValues(0.dp))
                }
            }
        } else {
            // ── Phone Layout: 5-Tab Bottom Navigation + BottomSheet ────────────
            Scaffold(
                bottomBar = {
                    if (isTopLevelRoute) {
                        ProductionBottomBar(
                            currentRoute = currentRoute,
                            isOverflowSelected = isOverflowSelected,
                            onPrimaryClick = { route ->
                                navigateToTopLevel(navController, currentRoute, route)
                            },
                            onMoreClick = {
                                showMoreSheet = true
                            }
                        )
                    }
                }
            ) { innerPadding ->
                content(innerPadding)
            }
        }
    }

    // ── "More" Modal Bottom Sheet ──────────────────────────────────────────────
    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            MoreNavigationSheetContent(
                currentRoute = currentRoute,
                onItemClick = { route ->
                    showMoreSheet = false
                    navigateToTopLevel(navController, currentRoute, route)
                },
                onDismiss = { showMoreSheet = false }
            )
        }
    }
}

/**
 * Modern Material 3 Bottom Navigation Bar
 * Height: 78dp, rounded top corners (24dp), equal width items, single-line labels.
 */
@Composable
fun ProductionBottomBar(
    currentRoute: String?,
    isOverflowSelected: Boolean,
    onPrimaryClick: (String) -> Unit,
    onMoreClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 4 Primary Navigation Items
            Screen.primaryNavItems.forEach { screen ->
                val isSelected = currentRoute == screen.route
                BottomNavItemCell(
                    modifier = Modifier.weight(1f),
                    label = screen.navLabel,
                    icon = screen.icon ?: Icons.Default.Circle,
                    selectedIcon = screen.selectedIcon ?: screen.icon ?: Icons.Default.Circle,
                    isSelected = isSelected,
                    onClick = { onPrimaryClick(screen.route) }
                )
            }

            // 5th Item: "More" Tab
            BottomNavItemCell(
                modifier = Modifier.weight(1f),
                label = "More",
                icon = Icons.Outlined.MoreHoriz,
                selectedIcon = Icons.Filled.MoreHoriz,
                isSelected = isOverflowSelected,
                onClick = onMoreClick
            )
        }
    }
}

/**
 * Individual Navigation Cell with M3 Pill Indicator & Animated Label
 */
@Composable
private fun BottomNavItemCell(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "iconColor"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "labelColor"
    )

    val pillColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "pillColor"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 32.dp),
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Indicator Pill around 24dp Icon
        Box(
            modifier = Modifier
                .height(32.dp)
                .width(56.dp)
                .clip(CircleShape)
                .background(pillColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Single Line Label — No Text Wrapping / Clipping
        Text(
            text = label,
            color = labelColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Content of the "More" Modal Bottom Sheet
 */
@Composable
private fun MoreNavigationSheetContent(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "More Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Quick access to tools & system settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close sheet"
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Cards for Templates, Reports, Settings
        OverflowMenuItemCard(
            screen = Screen.Templates,
            subtitle = "Create & manage reusable SMS campaign templates",
            isSelected = currentRoute == Screen.Templates.route,
            onClick = { onItemClick(Screen.Templates.route) }
        )

        OverflowMenuItemCard(
            screen = Screen.Reports,
            subtitle = "View message delivery stats, logs & analytics charts",
            isSelected = currentRoute == Screen.Reports.route,
            onClick = { onItemClick(Screen.Reports.route) }
        )

        OverflowMenuItemCard(
            screen = Screen.Settings,
            subtitle = "Manage app settings, dispatch limits & local backups",
            isSelected = currentRoute == Screen.Settings.route,
            onClick = { onItemClick(Screen.Settings.route) }
        )
    }
}

/**
 * Styled Card Item for the More Bottom Sheet
 */
@Composable
private fun OverflowMenuItemCard(
    screen: Screen,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val vector = if (isSelected) (screen.selectedIcon ?: screen.icon) else screen.icon
                    vector?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = screen.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Single Top-Level Navigation Helper
 */
private fun navigateToTopLevel(
    navController: NavHostController,
    currentRoute: String?,
    targetRoute: String
) {
    if (currentRoute != targetRoute) {
        navController.navigate(targetRoute) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

