package com.martinservold.hidenotifications.repository

import android.content.Context
import com.martinservold.hidenotifications.data.AppDatabase
import com.martinservold.hidenotifications.data.NotificationRule
import com.martinservold.hidenotifications.data.TitleMatchType
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for block rules, shared by the UI and the
 * notification-listener service.
 */
class RuleRepository private constructor(context: Context) {
    private val dao = AppDatabase.getInstance(context).ruleDao()

    val rules: Flow<List<NotificationRule>> = dao.observeAll()

    suspend fun addRule(
        packageName: String,
        appName: String,
        titleMatch: String?,
        matchType: TitleMatchType = TitleMatchType.EXACT
    ): Long = dao.insert(
        NotificationRule(packageName = packageName, appName = appName, titleMatch = titleMatch, matchType = matchType)
    )

    suspend fun removeRule(rule: NotificationRule) = dao.delete(rule)

    suspend fun removeRuleById(ruleId: Long) = dao.deleteById(ruleId)

    suspend fun setEnabled(ruleId: Long, enabled: Boolean) = dao.setEnabled(ruleId, enabled)

    suspend fun snapshot(): List<NotificationRule> = dao.getAll()

    suspend fun incrementDismissCount(ruleId: Long, amount: Int = 1) = dao.incrementDismissCount(ruleId, amount)

    /** The enabled rule (if any) that covers [packageName]/[title]. */
    fun findMatch(rules: List<NotificationRule>, packageName: String, title: String?): NotificationRule? =
        rules.firstOrNull { rule ->
            rule.enabled && rule.packageName == packageName && titleMatches(rule.titleMatch, rule.matchType, title)
        }

    /** True if a rule with this [titleMatch]/[matchType] scope would cover a notification titled [title]. */
    fun titleMatches(titleMatch: String?, matchType: TitleMatchType, title: String?): Boolean {
        if (titleMatch == null) return true
        val actual = title ?: return false
        return when (matchType) {
            TitleMatchType.EXACT -> actual == titleMatch
            TitleMatchType.CONTAINS -> actual.contains(titleMatch, ignoreCase = true)
        }
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
