package com.martinservold.hidenotifications.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.martinservold.hidenotifications.service.NotificationBlockerService

fun isNotificationAccessGranted(context: Context): Boolean {
    val component = ComponentName(context, NotificationBlockerService::class.java)
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        ?: return false
    return flat.split(":").any { it == component.flattenToString() }
}
