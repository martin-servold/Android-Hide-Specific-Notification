package com.martinservold.hidenotifications.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.martinservold.hidenotifications.model.ActiveNotification

/**
 * Shown when the user long-presses a notification in our in-app list. Lets them choose
 * whether to block just this specific notification (by title) or every notification
 * from that app.
 */
@Composable
fun BlockNotificationDialog(
    notification: ActiveNotification,
    onDismiss: () -> Unit,
    onConfirm: (titleMatch: String?) -> Unit
) {
    var blockWholeApp by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Block notification") },
        text = {
            Column {
                Text("From ${notification.appName}")
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    RadioButton(selected = !blockWholeApp, onClick = { blockWholeApp = false })
                    Text(
                        "Only notifications titled \"${notification.title}\"",
                        modifier = Modifier.padding(start = 8.dp, top = 12.dp)
                    )
                }
                Row {
                    RadioButton(selected = blockWholeApp, onClick = { blockWholeApp = true })
                    Text(
                        "All notifications from ${notification.appName}",
                        modifier = Modifier.padding(start = 8.dp, top = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(if (blockWholeApp) null else notification.title)
            }) {
                Text("Block")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
