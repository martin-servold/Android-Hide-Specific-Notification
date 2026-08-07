package com.martinservold.hidenotifications.service

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.martinservold.hidenotifications.data.NotificationRule
import com.martinservold.hidenotifications.model.ActiveNotification
import com.martinservold.hidenotifications.repository.NotificationRepository
import com.martinservold.hidenotifications.repository.RuleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Mirrors currently active notifications into [NotificationRepository] for the app's own
 * list UI, and auto-cancels any notification that matches a saved [NotificationRule] -
 * including future reposts, so a hidden notification stays hidden.
 */
class NotificationBlockerService : NotificationListenerService() {

    private var serviceJob: Job = SupervisorJob()
    private lateinit var serviceScope: CoroutineScope
    private lateinit var ruleRepository: RuleRepository

    @Volatile
    private var cachedRules: List<NotificationRule> = emptyList()

    override fun onCreate() {
        super.onCreate()
        serviceJob = SupervisorJob()
        serviceScope = CoroutineScope(serviceJob)
        ruleRepository = RuleRepository.getInstance(applicationContext)
        ruleRepository.rules
            .onEach { cachedRules = it }
            .launchIn(serviceScope)
    }

    override fun onDestroy() {
        instance = null
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        serviceScope.launch {
            activeNotifications?.forEach { sbn -> handlePosted(sbn) }
        }
        serviceScope.launch {
            while (isActive) {
                delay(RECONCILE_INTERVAL_MS)
                reconcileActiveNotifications()
            }
        }
    }

    override fun onListenerDisconnected() {
        instance = null
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        handlePosted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationRepository.onRemoved(sbn.key)
    }

    private fun handlePosted(sbn: StatusBarNotification) {
        if (sbn.packageName == applicationContext.packageName) return

        val title = sbn.notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = sbn.notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()

        val matchedRule = ruleRepository.findMatch(cachedRules, sbn.packageName, title)
        if (matchedRule != null) {
            cancelNotification(sbn.key)
            NotificationRepository.onRemoved(sbn.key)
            serviceScope.launch { ruleRepository.incrementDismissCount(matchedRule.id) }
            return
        }

        val isGroupSummary = sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
        if (isGroupSummary) {
            // Exists only to bundle a group; the shade hides/merges it too, so don't show it as its own row.
            NotificationRepository.onRemoved(sbn.key)
            return
        }

        val (appName, icon) = resolveAppInfo(sbn.packageName)
        NotificationRepository.onPosted(
            ActiveNotification(
                key = sbn.key,
                packageName = sbn.packageName,
                appName = appName,
                appIcon = icon,
                title = title,
                text = text,
                postTime = sbn.postTime
            )
        )
    }

    /** Prunes anything from our in-app list that the OS no longer considers active, in case a removal event was missed. */
    private fun reconcileActiveNotifications() {
        val validKeys = activeNotifications?.map { it.key }?.toSet() ?: return
        NotificationRepository.retainOnly(validKeys)
    }

    private fun resolveAppInfo(packageName: String): Pair<String, Bitmap?> {
        val pm = applicationContext.packageManager
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val label = pm.getApplicationLabel(appInfo).toString()
            val icon = drawableToBitmap(pm.getApplicationIcon(appInfo))
            label to icon
        } catch (e: Exception) {
            packageName to null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap
        val width = drawable.intrinsicWidth.coerceAtLeast(1)
        val height = drawable.intrinsicHeight.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        private const val RECONCILE_INTERVAL_MS = 60_000L

        @Volatile
        private var instance: NotificationBlockerService? = null

        /** Immediately dismisses an already-posted notification, e.g. right after a rule is created. */
        fun cancelNow(key: String) {
            instance?.cancelNotification(key)
        }
    }
}
