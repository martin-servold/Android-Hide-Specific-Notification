package com.martinservold.hidenotifications.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.martinservold.hidenotifications.model.ActiveNotification
import com.martinservold.hidenotifications.repository.NotificationRepository
import com.martinservold.hidenotifications.repository.RuleRepository
import com.martinservold.hidenotifications.service.NotificationBlockerService
import com.martinservold.hidenotifications.ui.components.BlockNotificationDialog
import com.martinservold.hidenotifications.ui.components.NotificationListItem
import com.martinservold.hidenotifications.util.isNotificationAccessGranted
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var accessGranted by remember { mutableStateOf(isNotificationAccessGranted(context)) }

    DisposableEffectRecheckOnResume(lifecycleOwner) {
        accessGranted = isNotificationAccessGranted(context)
    }

    if (!accessGranted) {
        PermissionPrompt(
            onOpenSettings = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        )
    } else {
        LiveNotificationsList()
    }
}

@Composable
private fun DisposableEffectRecheckOnResume(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onResume: () -> Unit
) {
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@Composable
private fun PermissionPrompt(onOpenSettings: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Notification access needed",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "To show and block notifications, grant this app access to your notifications " +
                    "in the system settings screen that opens next.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onOpenSettings) {
                Text("Open notification access settings")
            }
        }
    }
}

@Composable
private fun LiveNotificationsList() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ruleRepository = remember { RuleRepository.getInstance(context) }
    val notifications by NotificationRepository.activeNotifications.collectAsState()
    var pendingBlock by remember { mutableStateOf<ActiveNotification?>(null) }

    if (notifications.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No active notifications.\nThey'll show up here as they arrive.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(notifications.sortedByDescending { it.postTime }, key = { it.key }) { notification ->
                NotificationListItem(notification = notification, onLongPress = { pendingBlock = it })
                HorizontalDivider()
            }
        }
    }

    pendingBlock?.let { notification ->
        BlockNotificationDialog(
            notification = notification,
            onDismiss = { pendingBlock = null },
            onConfirm = { titleMatch ->
                val immediateDismissCount = if (titleMatch == null) {
                    notifications.count { it.packageName == notification.packageName }
                } else {
                    1
                }
                scope.launch {
                    val ruleId = ruleRepository.addRule(
                        packageName = notification.packageName,
                        appName = notification.appName,
                        titleMatch = titleMatch
                    )
                    ruleRepository.incrementDismissCount(ruleId, immediateDismissCount)
                }
                if (titleMatch == null) {
                    NotificationBlockerService.cancelAllNow(notification.packageName)
                    NotificationRepository.removeAllFor(notification.packageName)
                } else {
                    NotificationBlockerService.cancelNow(notification.key)
                    NotificationRepository.onRemoved(notification.key)
                }
                pendingBlock = null
            }
        )
    }
}
