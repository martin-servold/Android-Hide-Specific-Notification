package com.martinservold.hidenotifications.repository

import com.martinservold.hidenotifications.model.ActiveNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Bridges [com.martinservold.hidenotifications.service.NotificationBlockerService] and the UI:
 * the service publishes the notifications it currently sees, the UI observes them to build the
 * live list screen. Both run in the same process, so a plain in-memory singleton is enough.
 */
object NotificationRepository {
    private val _activeNotifications = MutableStateFlow<List<ActiveNotification>>(emptyList())
    val activeNotifications: StateFlow<List<ActiveNotification>> = _activeNotifications

    fun onPosted(notification: ActiveNotification) {
        _activeNotifications.update { current ->
            current.filterNot { it.key == notification.key } + notification
        }
    }

    fun onRemoved(key: String) {
        _activeNotifications.update { current -> current.filterNot { it.key == key } }
    }

    fun removeAllFor(packageName: String) {
        _activeNotifications.update { current -> current.filterNot { it.packageName == packageName } }
    }
}
