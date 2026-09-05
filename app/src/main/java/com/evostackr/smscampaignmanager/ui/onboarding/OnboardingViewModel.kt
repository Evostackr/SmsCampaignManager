package com.evostackr.smscampaignmanager.ui.onboarding

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evostackr.smscampaignmanager.data.repository.SettingsRepository
import com.evostackr.smscampaignmanager.util.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingPermissionStatus {
    PENDING,
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED
}

enum class PermissionType {
    SMS,
    CONTACTS,
    NOTIFICATIONS
}

data class OnboardingUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 7,
    val smsStatus: OnboardingPermissionStatus = OnboardingPermissionStatus.PENDING,
    val contactsStatus: OnboardingPermissionStatus = OnboardingPermissionStatus.PENDING,
    val notificationsStatus: OnboardingPermissionStatus = OnboardingPermissionStatus.PENDING,
    val showSmsSuccessAnimation: Boolean = false,
    val showContactsSuccessAnimation: Boolean = false,
    val showNotificationsSuccessAnimation: Boolean = false,
    val isCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /**
     * Called on page load or screen resume to reflect existing system permissions.
     * Keeps PENDING state if permission was never granted and not yet processed by the user.
     */
    fun updatePermissionStatuses(context: Context, activity: Activity?) {
        val smsGranted = PermissionManager.hasAllPermissions(context, PermissionManager.SMS_PERMISSIONS)
        val contactsGranted = PermissionManager.hasAllPermissions(context, PermissionManager.CONTACTS_PERMISSIONS)
        val notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionManager.hasAllPermissions(context, PermissionManager.NOTIFICATION_PERMISSIONS)
        } else {
            true
        }

        var shouldAdvance = false

        _uiState.update { current ->
            val newSmsStatus = if (smsGranted) OnboardingPermissionStatus.GRANTED else current.smsStatus
            val newContactsStatus = if (contactsGranted) OnboardingPermissionStatus.GRANTED else current.contactsStatus
            val newNotificationsStatus = if (notificationsGranted) OnboardingPermissionStatus.GRANTED else current.notificationsStatus

            // If we are on the specific permission page and the user just granted it (e.g. from Settings), auto-advance
            if (current.currentPage == 2 && newSmsStatus == OnboardingPermissionStatus.GRANTED && current.smsStatus != OnboardingPermissionStatus.GRANTED) {
                shouldAdvance = true
            }
            if (current.currentPage == 3 && newContactsStatus == OnboardingPermissionStatus.GRANTED && current.contactsStatus != OnboardingPermissionStatus.GRANTED) {
                shouldAdvance = true
            }
            if (current.currentPage == 4 && newNotificationsStatus == OnboardingPermissionStatus.GRANTED && current.notificationsStatus != OnboardingPermissionStatus.GRANTED) {
                shouldAdvance = true
            }

            current.copy(
                smsStatus = newSmsStatus,
                contactsStatus = newContactsStatus,
                notificationsStatus = newNotificationsStatus,
                showSmsSuccessAnimation = if (current.currentPage == 2 && shouldAdvance) true else current.showSmsSuccessAnimation,
                showContactsSuccessAnimation = if (current.currentPage == 3 && shouldAdvance) true else current.showContactsSuccessAnimation,
                showNotificationsSuccessAnimation = if (current.currentPage == 4 && shouldAdvance) true else current.showNotificationsSuccessAnimation
            )
        }

        if (shouldAdvance) {
            viewModelScope.launch {
                delay(650)
                nextPage()
            }
        }
    }

    /**
     * Processes the result from the official Android Runtime Permission dialog launcher.
     */
    fun handlePermissionResult(permissionType: PermissionType, isGranted: Boolean, activity: Activity?) {
        viewModelScope.launch {
            when (permissionType) {
                PermissionType.SMS -> {
                    if (isGranted) {
                        _uiState.update {
                            it.copy(
                                smsStatus = OnboardingPermissionStatus.GRANTED,
                                showSmsSuccessAnimation = true
                            )
                        }
                        delay(650)
                        nextPage()
                    } else {
                        val shouldShowRationale = activity?.let {
                            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.SEND_SMS)
                        } ?: false

                        // If rationale is true, user denied once (regular denial).
                        // If rationale is false after launching, user selected "Don't ask again" (permanently denied).
                        val newStatus = if (shouldShowRationale) {
                            OnboardingPermissionStatus.DENIED
                        } else {
                            OnboardingPermissionStatus.PERMANENTLY_DENIED
                        }

                        _uiState.update {
                            it.copy(
                                smsStatus = newStatus,
                                showSmsSuccessAnimation = false
                            )
                        }
                    }
                }
                PermissionType.CONTACTS -> {
                    if (isGranted) {
                        _uiState.update {
                            it.copy(
                                contactsStatus = OnboardingPermissionStatus.GRANTED,
                                showContactsSuccessAnimation = true
                            )
                        }
                        delay(650)
                        nextPage()
                    } else {
                        val shouldShowRationale = activity?.let {
                            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_CONTACTS)
                        } ?: false

                        val newStatus = if (shouldShowRationale) {
                            OnboardingPermissionStatus.DENIED
                        } else {
                            OnboardingPermissionStatus.PERMANENTLY_DENIED
                        }

                        _uiState.update {
                            it.copy(
                                contactsStatus = newStatus,
                                showContactsSuccessAnimation = false
                            )
                        }
                    }
                }
                PermissionType.NOTIFICATIONS -> {
                    if (isGranted) {
                        _uiState.update {
                            it.copy(
                                notificationsStatus = OnboardingPermissionStatus.GRANTED,
                                showNotificationsSuccessAnimation = true
                            )
                        }
                        delay(650)
                        nextPage()
                    } else {
                        val shouldShowRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && activity != null) {
                            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
                        } else false

                        val newStatus = if (shouldShowRationale) {
                            OnboardingPermissionStatus.DENIED
                        } else {
                            OnboardingPermissionStatus.PERMANENTLY_DENIED
                        }

                        _uiState.update {
                            it.copy(
                                notificationsStatus = newStatus,
                                showNotificationsSuccessAnimation = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun nextPage() {
        if (_uiState.value.currentPage < _uiState.value.totalPages - 1) {
            _uiState.update { it.copy(currentPage = it.currentPage + 1) }
        }
    }

    fun previousPage() {
        if (_uiState.value.currentPage > 0) {
            _uiState.update { it.copy(currentPage = it.currentPage - 1) }
        }
    }

    fun goToPage(pageIndex: Int) {
        if (pageIndex in 0 until _uiState.value.totalPages) {
            _uiState.update { it.copy(currentPage = pageIndex) }
        }
    }

    fun finishOnboarding(onNavigateToDashboard: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
            _uiState.update { it.copy(isCompleted = true) }
            onNavigateToDashboard()
        }
    }
}
