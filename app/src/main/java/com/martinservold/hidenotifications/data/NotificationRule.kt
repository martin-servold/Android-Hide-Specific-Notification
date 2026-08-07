package com.martinservold.hidenotifications.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TitleMatchType {
    EXACT,
    CONTAINS
}

/**
 * A rule describing notifications that should be auto-dismissed.
 *
 * When [titleMatch] is null the rule blocks every notification from [packageName].
 * When it is set, [matchType] decides whether a notification's title must equal it
 * exactly or merely contain it, so a rule keeps matching even if the app varies the
 * title slightly (e.g. an appended count or timestamp).
 */
@Entity(tableName = "notification_rules")
data class NotificationRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val titleMatch: String?,
    val matchType: TitleMatchType = TitleMatchType.EXACT,
    val createdAt: Long = System.currentTimeMillis(),
    val dismissCount: Int = 0,
    val enabled: Boolean = true
)
