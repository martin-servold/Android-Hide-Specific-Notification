package com.martinservold.hidenotifications.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM notification_rules ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NotificationRule>>

    @Query("SELECT * FROM notification_rules")
    suspend fun getAll(): List<NotificationRule>

    @Insert
    suspend fun insert(rule: NotificationRule): Long

    @Delete
    suspend fun delete(rule: NotificationRule)

    @Query("DELETE FROM notification_rules WHERE id = :ruleId")
    suspend fun deleteById(ruleId: Long)

    @Query("UPDATE notification_rules SET dismissCount = dismissCount + :amount WHERE id = :ruleId")
    suspend fun incrementDismissCount(ruleId: Long, amount: Int = 1)

    @Query("UPDATE notification_rules SET enabled = :enabled WHERE id = :ruleId")
    suspend fun setEnabled(ruleId: Long, enabled: Boolean)
}
