package com.martinservold.hidenotifications.repository

import android.content.Context
import com.martinservold.hidenotifications.data.AppDatabase
import com.martinservold.hidenotifications.data.NotificationRule
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for block rules, shared by the UI and the
 * notification-listener service.
 */
class RuleRepository private constructor(context: Context) {
    private val dao = AppDatabase.getInstance(context).ruleDao()

    val rules: Flow<List<NotificationRule>> = dao.observeAll()

    suspend fun addRule(packageName: String, appName: String, titleMatch: String?): Long =
        dao.insert(NotificationRule(packageName = packageName, appName = appName, titleMatch = titleMatch))

    suspend fun removeRule(rule: NotificationRule) = dao.delete(rule)

    suspend fun snapshot(): List<NotificationRule> = dao.getAll()

    suspend fun incrementDismissCount(ruleId: Long, amount: Int = 1) = dao.incrementDismissCount(ruleId, amount)

    /** The rule (if any) that covers [packageName]/[title]. */
    fun findMatch(rules: List<NotificationRule>, packageName: String, title: String?): NotificationRule? =
        rules.firstOrNull { rule ->
            rule.packageName == packageName && (rule.titleMatch == null || rule.titleMatch == title)
        }

    companion object {
        @Volatile
        private var instance: RuleRepository? = null

        fun getInstance(context: Context): RuleRepository =
            instance ?: synchronized(this) {
                instance ?: RuleRepository(context.applicationContext).also { instance = it }
            }
    }
}
