package com.martinservold.hidenotifications.util

import android.content.Context
import android.os.PowerManager

/** False means the OS may pause this app's background work (including the notification listener) to save power. */
fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}
