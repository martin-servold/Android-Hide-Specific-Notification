package com.martinservold.hidenotifications.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.martinservold.hidenotifications.data.NotificationRule
import com.martinservold.hidenotifications.data.TitleMatchType
import com.martinservold.hidenotifications.repository.RuleRepository
import kotlinx.coroutines.launch

@Composable
fun RulesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ruleRepository = remember { RuleRepository.getInstance(context) }
    val rules by ruleRepository.rules.collectAsState(initial = emptyList())

    if (rules.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No block rules yet.\nLong-press a notification on the Notifications tab to create one.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(rules, key = { it.id }) { rule ->
                RuleRow(
                    rule = rule,
                    onToggleEnabled = { enabled -> scope.launch { ruleRepository.setEnabled(rule.id, enabled) } },
                    onDelete = { scope.launch { ruleRepository.removeRule(rule) } }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun RuleRow(rule: NotificationRule, onToggleEnabled: (Boolean) -> Unit, onDelete: () -> Unit) {
    val contentColor = if (rule.enabled) LocalContentColor.current else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(rule.appName, style = MaterialTheme.typography.titleSmall, color = contentColor)
            Text(
                text = describeScope(rule),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
            Text(
                text = if (rule.dismissCount == 1) "Dismissed 1 notification" else "Dismissed ${rule.dismissCount} notifications",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = rule.enabled, onCheckedChange = onToggleEnabled)
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Remove rule")
        }
    }
}

private fun describeScope(rule: NotificationRule): String {
    val title = rule.titleMatch ?: return "Blocking all notifications"
    return when (rule.matchType) {
        TitleMatchType.EXACT -> "Blocking notifications titled \"$title\""
        TitleMatchType.CONTAINS -> "Blocking notifications containing \"$title\""
    }
}
