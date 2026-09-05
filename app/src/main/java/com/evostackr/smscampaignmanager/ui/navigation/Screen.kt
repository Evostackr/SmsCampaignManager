package com.evostackr.smscampaignmanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val navLabel: String = title,
    val icon: ImageVector? = null,
    val selectedIcon: ImageVector? = icon
) {
    object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        navLabel = "Dashboard",
        icon = Icons.Outlined.Dashboard,
        selectedIcon = Icons.Filled.Dashboard
    )

    object Campaigns : Screen(
        route = "campaigns",
        title = "Campaigns",
        navLabel = "Campaigns",
        icon = Icons.AutoMirrored.Outlined.Send,
        selectedIcon = Icons.AutoMirrored.Filled.Send
    )

    object Contacts : Screen(
        route = "contacts",
        title = "Contacts",
        navLabel = "Contacts",
        icon = Icons.Outlined.People,
        selectedIcon = Icons.Filled.People
    )

    object CreateCampaign : Screen("create_campaign", "New Campaign", icon = Icons.Default.Add)

    object CampaignDetail : Screen("campaign_detail/{campaignId}", "Campaign Detail") {
        fun createRoute(campaignId: Long) = "campaign_detail/$campaignId"
    }

    object Templates : Screen(
        route = "templates",
        title = "Templates",
        navLabel = "Templates",
        icon = Icons.Outlined.Description,
        selectedIcon = Icons.Filled.Description
    )

    object Reports : Screen(
        route = "reports",
        title = "Reports",
        navLabel = "Reports",
        icon = Icons.Outlined.Assessment,
        selectedIcon = Icons.Filled.Assessment
    )

    object Settings : Screen(
        route = "settings",
        title = "Settings",
        navLabel = "Settings",
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings
    )

    object BackupSettings : Screen("backup_settings", "Backup & Recovery Settings")

    // Splash & System Routes
    object Splash : Screen("splash", "Splash")
    object Onboarding : Screen("onboarding", "Onboarding")

    // Legal & Information Routes
    object About : Screen("about", "About Us")
    object PrivacyPolicy : Screen("privacy_policy", "Privacy Policy")
    object Terms : Screen("terms", "Terms & Conditions")
    object Disclaimer : Screen("disclaimer", "Disclaimer")
    object Contact : Screen("contact", "Contact Us")
    object Help : Screen("help", "Help & Support")
    object Faq : Screen("faq", "FAQ")
    object Licenses : Screen("licenses", "Open Source Licenses")
    object AppPermissions : Screen("app_permissions", "App Permissions")
    object DataPrivacy : Screen("data_privacy", "Data & Privacy")
    object VersionInfo : Screen("version_info", "App Version")
    object Changelog : Screen("changelog", "Changelog")

    companion object {
        val primaryNavItems = listOf(
            Dashboard,
            Campaigns,
            Contacts
        )

        val overflowNavItems = listOf(
            Templates,
            Reports,
            Settings
        )

        val allTopLevelScreens = listOf(
            Dashboard,
            Campaigns,
            Contacts,
            Templates,
            Reports,
            Settings
        )
    }
}

