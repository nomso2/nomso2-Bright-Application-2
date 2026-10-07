package com.example.ui.solutions.common

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.solutions.SolutionsDatabase
import com.example.data.solutions.SolutionsPrefs
import com.example.data.solutions.SupplyEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Notification channels and posts for the siren, surge warning and quick Light On/Off log. */
object SolutionsNotifier {
    const val CHANNEL_SIREN = "bright_light_back_alert"
    const val CHANNEL_SURGE = "bright_surge_warning"
    const val CHANNEL_QUICK_LOG = "bright_quick_supply_log"
    const val CHANNEL_UPDATES = "bright_solution_updates"

    private const val ID_SIREN = 3001
    private const val ID_SURGE = 3002
    private const val ID_QUICK_LOG = 3003

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        val alarmAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val siren = NotificationChannel(CHANNEL_SIREN, "Light is back alert", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "A gentle sound when power returns, so you can switch off the generator."
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), alarmAttrs)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 300, 400)
        }
        val surge = NotificationChannel(CHANNEL_SURGE, "Surge warning", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Reminder to unplug delicate appliances before power is restored."
            enableVibration(true)
        }
        val quick = NotificationChannel(CHANNEL_QUICK_LOG, "Light On / Off quick log", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Keeps Light On and Light Off buttons in your notification shade."
            setShowBadge(false)
        }
        val updates = NotificationChannel(CHANNEL_UPDATES, "Supply summaries & news", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Weekly band audit, shortfall alerts, tariff news and report updates."
        }
        nm.createNotificationChannels(listOf(siren, surge, quick, updates))
    }

    private fun canPost(context: Context): Boolean =
        BrightPermissions.hasAny(context, BrightPermissions.NOTIFICATIONS) &&
            NotificationManagerCompat.from(context).areNotificationsEnabled()

    private fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context, 0, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun broadcast(context: Context, action: String, code: Int): PendingIntent = PendingIntent.getBroadcast(
        context, code, Intent(context, SolutionsReceiver::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    /** Returns false when notifications are off (the caller still plays the in-app siren). */
    @android.annotation.SuppressLint("MissingPermission")
    fun postSiren(context: Context, text: String): Boolean {
        ensureChannels(context)
        if (!canPost(context)) return false
        val n = NotificationCompat.Builder(context, CHANNEL_SIREN)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Your light is back")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .build()
        NotificationManagerCompat.from(context).notify(ID_SIREN, n)
        return true
    }

    @android.annotation.SuppressLint("MissingPermission")
    fun postSurge(context: Context, text: String): Boolean {
        ensureChannels(context)
        if (!canPost(context)) return false
        val n = NotificationCompat.Builder(context, CHANNEL_SURGE)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Unplug your appliances")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .build()
        NotificationManagerCompat.from(context).notify(ID_SURGE, n)
        return true
    }

    /** Ongoing notification with Light ON / Light OFF buttons that log straight into the tracker. */
    @android.annotation.SuppressLint("MissingPermission")
    fun showQuickLog(context: Context, lightIsOn: Boolean?): Boolean {
        ensureChannels(context)
        if (!canPost(context)) return false
        val state = when (lightIsOn) {
            true -> "Light is ON"
            false -> "Light is OFF"
            null -> "Tap when your light changes"
        }
        val n = NotificationCompat.Builder(context, CHANNEL_QUICK_LOG)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Bright supply log")
            .setContentText(state)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppIntent(context))
            .addAction(0, "Light ON", broadcast(context, SolutionsReceiver.ACTION_LIGHT_ON, 1))
            .addAction(0, "Light OFF", broadcast(context, SolutionsReceiver.ACTION_LIGHT_OFF, 2))
            .build()
        NotificationManagerCompat.from(context).notify(ID_QUICK_LOG, n)
        return true
    }

    /** Summaries and alerts from the background checks (band audit, shortfall, news, ticket stages). */
    @android.annotation.SuppressLint("MissingPermission")
    fun postUpdate(context: Context, id: Int, title: String, text: String): Boolean {
        ensureChannels(context)
        if (!canPost(context)) return false
        val n = NotificationCompat.Builder(context, CHANNEL_UPDATES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .build()
        NotificationManagerCompat.from(context).notify(id, n)
        return true
    }

    fun hideQuickLog(context: Context) {
        NotificationManagerCompat.from(context).cancel(ID_QUICK_LOG)
    }

    /** Inexact alarm (no exact-alarm permission needed); Android may deliver it a few minutes late. */
    fun scheduleSurgeWarning(context: Context, atMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = broadcast(context, SolutionsReceiver.ACTION_SURGE_ALARM, 3)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
        SolutionsPrefs(context).surgeAlertAt = atMillis
    }

    fun cancelSurgeWarning(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(broadcast(context, SolutionsReceiver.ACTION_SURGE_ALARM, 3))
        SolutionsPrefs(context).surgeAlertAt = 0L
    }
}

/** Shared by the in-app buttons and the notification buttons, so the siren logic lives in one place. */
object SupplyLogger {
    suspend fun log(context: Context, isOn: Boolean, source: String) {
        val dao = SolutionsDatabase.get(context).dao()
        val last = dao.lastSupplyEvent()
        if (last != null && last.isOn == isOn) return // ignore double taps
        dao.insertSupplyEvent(SupplyEventEntity(isOn = isOn, source = source))
        val prefs = SolutionsPrefs(context)
        if (prefs.quickLogNotification) SolutionsNotifier.showQuickLog(context, isOn)
        if (isOn && prefs.sirenArmed) {
            SolutionsNotifier.postSiren(context, "You can switch off your generator now.")
        }
        if (isOn && prefs.isEnabled(26) && prefs.surgeOnReturn) {
            SolutionsNotifier.postSurge(context, "Power just came back. Wait about 2 minutes for the voltage to settle before plugging TVs, fridges and laptops back in.")
        }
    }
}

class SolutionsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        when (intent.action) {
            ACTION_SURGE_ALARM -> {
                SolutionsPrefs(app).surgeAlertAt = 0L
                SolutionsNotifier.postSurge(app, "Power is due back soon. Unplug TVs, fridges, laptops and chargers until the voltage settles.")
            }
            ACTION_LIGHT_ON, ACTION_LIGHT_OFF -> {
                val pending = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        SupplyLogger.log(app, intent.action == ACTION_LIGHT_ON, "NOTIFICATION")
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_LIGHT_ON = "com.example.solutions.LIGHT_ON"
        const val ACTION_LIGHT_OFF = "com.example.solutions.LIGHT_OFF"
        const val ACTION_SURGE_ALARM = "com.example.solutions.SURGE_ALARM"
    }
}
