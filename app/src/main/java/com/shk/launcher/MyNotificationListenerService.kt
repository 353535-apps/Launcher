package com.shk.launcher

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationRepository {
    // Expose read-only flow of current notifications
    private val _notifications = MutableStateFlow<List<StatusBarNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    internal fun update(list: List<StatusBarNotification>) {
        _notifications.value = list
    }
}

class MyNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() {
        // initial load
        NotificationRepository.update(activeNotifications.toList())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        NotificationRepository.update(activeNotifications.toList())
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationRepository.update(activeNotifications.toList())
    }
}