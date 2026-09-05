package com.evostackr.smscampaignmanager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.evostackr.smscampaignmanager.ui.campaign.CampaignDetailScreen
import com.evostackr.smscampaignmanager.ui.campaign.CampaignListScreen
import com.evostackr.smscampaignmanager.ui.campaign.CreateCampaignScreen
import com.evostackr.smscampaignmanager.ui.contacts.ContactsScreen
import com.evostackr.smscampaignmanager.ui.dashboard.DashboardScreen
import com.evostackr.smscampaignmanager.ui.info.*
import com.evostackr.smscampaignmanager.ui.report.ReportScreen
import com.evostackr.smscampaignmanager.ui.settings.BackupSettingsScreen
import com.evostackr.smscampaignmanager.ui.settings.SettingsScreen
import com.evostackr.smscampaignmanager.ui.template.TemplateScreen
import com.evostackr.smscampaignmanager.ui.splash.SplashScreen
import com.evostackr.smscampaignmanager.ui.onboarding.OnboardingScreen

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    AppNavigationWrapper(
        navController = navController,
        currentRoute = currentRoute
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = { isCompleted ->
                        val targetRoute = if (isCompleted) Screen.Dashboard.route else Screen.Onboarding.route
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToCreateCampaign = { navController.navigate(Screen.CreateCampaign.route) },
                    onNavigateToCampaignDetail = { campaignId -> navController.navigate(Screen.CampaignDetail.createRoute(campaignId)) }
                )
            }

            composable(Screen.Campaigns.route) {
                CampaignListScreen(
                    onNavigateToCreate = { navController.navigate(Screen.CreateCampaign.route) },
                    onNavigateToDetail = { campaignId -> navController.navigate(Screen.CampaignDetail.createRoute(campaignId)) }
                )
            }

            composable(Screen.CreateCampaign.route) {
                CreateCampaignScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onCampaignCreated = { navController.navigate(Screen.Campaigns.route) { popUpTo(Screen.Dashboard.route) } }
                )
            }

            composable(
                route = Screen.CampaignDetail.route,
                arguments = listOf(navArgument("campaignId") { type = NavType.LongType })
            ) { backStackEntry ->
                val campaignId = backStackEntry.arguments?.getLong("campaignId") ?: 0L
                CampaignDetailScreen(
                    campaignId = campaignId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Contacts.route) {
                ContactsScreen()
            }

            composable(Screen.Templates.route) {
                TemplateScreen()
            }

            composable(Screen.Reports.route) {
                ReportScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToBackupSettings = { navController.navigate(Screen.BackupSettings.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) },
                    onNavigateToTerms = { navController.navigate(Screen.Terms.route) },
                    onNavigateToDisclaimer = { navController.navigate(Screen.Disclaimer.route) },
                    onNavigateToContact = { navController.navigate(Screen.Contact.route) },
                    onNavigateToHelp = { navController.navigate(Screen.Help.route) },
                    onNavigateToFaq = { navController.navigate(Screen.Faq.route) },
                    onNavigateToLicenses = { navController.navigate(Screen.Licenses.route) },
                    onNavigateToPermissions = { navController.navigate(Screen.AppPermissions.route) },
                    onNavigateToDataPrivacy = { navController.navigate(Screen.DataPrivacy.route) },
                    onNavigateToVersion = { navController.navigate(Screen.VersionInfo.route) },
                    onNavigateToChangelog = { navController.navigate(Screen.Changelog.route) },
                    onNavigateToOnboarding = { navController.navigate(Screen.Onboarding.route) }
                )
            }

            composable(Screen.BackupSettings.route) {
                BackupSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Legal & Information Screens
            composable(Screen.About.route) { AboutScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(Screen.PrivacyPolicy.route) { PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(Screen.Terms.route) { TermsConditionsScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(Screen.Disclaimer.route) { DisclaimerScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(Screen.Contact.route) { ContactUsScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(Screen.Help.route) { HelpSupportScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(Screen.Faq.route) { FaqScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(Screen.Licenses.route) { OpenSourceLicensesScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(Screen.AppPermissions.route) { AppPermissionsScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(Screen.DataPrivacy.route) { DataPrivacyScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(Screen.VersionInfo.route) { VersionInfoScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(Screen.Changelog.route) { ChangelogScreen(onNavigateBack = { navController.popBackStack() }) }
        }
    }
}
