package com.martinservold.hidenotifications.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Warns that OEM battery optimization may pause the notification-listener service in the
 * background, which is the most common way apps like this silently stop working.
 */
@Composable
fun BatteryOptimizationBanner(onFix: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .clickable(onClick = onFix)
            .padding(16.dp)
    ) {
        Text(
            "Battery optimization is on for this app",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
        Text(
            "The system may stop this app in the background, which would stop it from blocking notifications. Tap to fix.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
