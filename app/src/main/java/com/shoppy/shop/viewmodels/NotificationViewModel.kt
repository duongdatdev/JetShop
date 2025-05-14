package com.shoppy.shop.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppy.shop.models.NotificationData
import com.shoppy.shop.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    // Observable flow of all notifications
    val notifications: StateFlow<List<NotificationData>> = notificationRepository
        .getNotificationsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Observable flow of unread notifications count
    val unreadNotificationsCount: StateFlow<Int> = notificationRepository
        .getUnreadNotificationsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Mark a notification as read
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                notificationRepository.markAsRead(notificationId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Mark all notifications as read
    fun markAllAsRead() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                notificationRepository.markAllAsRead()
            } finally {
                _isLoading.value = false
            }
        }
    }
} 