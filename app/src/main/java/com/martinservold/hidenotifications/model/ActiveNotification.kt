package com.martinservold.hidenotifications.model

import android.graphics.Bitmap

/** In-memory snapshot of a notification currently visible in the shade, for our own list UI. */
data class ActiveNotification(
    val key: String,
    val packageName: String,
    val appName: String,
    val appIcon: Bitmap?,
    val title: String,
    val text: String,
    val postTime: Long
)
