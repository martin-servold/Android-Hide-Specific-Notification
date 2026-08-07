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
import com.martinservold.hidenotifications.data.TitleMatchType
import com.martinservold.hidenotifications.model.ActiveNotification

private enum class BlockScope {
    EXACT_TITLE,
    CONTAINS_TITLE,
    WHOLE_APP
}

/**
 * Shown when the user long-presses a notification in our in-app list. Lets them choose
 * how broadly to block: an exact title match, a looser "title contains this" match (so
 * the rule survives apps that vary the title slightly), or every notification from the app.
 */
@Composable
fun BlockNotificationDialog(
    notification: ActiveNotification,
    onDismiss: () -> Unit,
    onConfirm: (titleMatch: String?, matchType: TitleMatchType) -> Unit
) {
    var scope by remember { mutableStateOf(BlockScope.EXACT_TITLE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Block notification") },
        text = {
            Column {
                Text("From ${notification.appName}")
                BlockOptionRow(
                    label = "Only notifications titled exactly \"${notification.title}\"",
                    selected = scope == BlockScope.EXACT_TITLE,
                    onClick = { scope = BlockScope.EXACT_TITLE }
                )
                BlockOptionRow(
                    label = "Notifications with \"${notification.title}\" in the title",
                    selected = scope == BlockScope.CONTAINS_TITLE,
                    onClick = { scope = BlockScope.CONTAINS_TITLE }
                )
                BlockOptionRow(
                    label = "All notifications from ${notification.appName}",
                    selected = scope == BlockScope.WHOLE_APP,
                    onClick = { scope = BlockScope.WHOLE_APP }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when (scope) {
                    BlockScope.EXACT_TITLE -> onConfirm(notification.title, TitleMatchType.EXACT)
                    BlockScope.CONTAINS_TITLE -> onConfirm(notification.title, TitleMatchType.CONTAINS)
                    BlockScope.WHOLE_APP -> onConfirm(null, TitleMatchType.EXACT)
                }
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

@Composable
private fun BlockOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.padding(top = 12.dp)) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, modifier = Modifier.padding(start = 8.dp, top = 12.dp))
    }
}
