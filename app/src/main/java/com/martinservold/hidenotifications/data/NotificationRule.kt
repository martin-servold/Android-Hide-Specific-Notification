package com.martinservold.hidenotifications.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A rule describing notifications that should be auto-dismissed.
 *
 * When [titleMatch] is null the rule blocks every notification from [packageName].
 * When it is set, only notifications whose title exactly matches are blocked,
 * so a specific notification can be hidden without silencing the whole app.
 */
@Entity(tableName = "notification_rules")
data class NotificationRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val titleMatch: String?,
    val createdAt: Long = System.currentTimeMillis()
)
