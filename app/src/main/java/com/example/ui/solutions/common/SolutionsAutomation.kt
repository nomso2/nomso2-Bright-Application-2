package com.example.ui.solutions.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.solutions.SolutionsDatabase
import com.example.data.solutions.SolutionsPrefs
import com.example.data.solutions.SupplyMath
import com.example.ui.solutions.CuratedTariffNews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Runs the settings-based solutions in the background:
 *  - #12 auto-track outages from the phone's charger (plugged into mains = grid power present),
 *    instantly while Bright's process is alive and every 15 minutes via WorkManager otherwise;
 *  - #12 shortfall alerts, #11 weekly band-audit summary, #30 tariff news notifications.
 */
object SolutionsAutomation {
    private const val WORK_NAME = "bright_solutions_checks"
    @Volatile private var powerReceiverRegistered = false

    /** Call on app start and whenever a setting changes: schedules or cancels the background work. */
    fun sync(context: Context) {
        val app = context.applicationContext
        val prefs = SolutionsPrefs(app)
        val needsWork = (prefs.isEnabled(12) && (prefs.autoTrackOutages || prefs.shortfallAlerts)) ||
            (prefs.isEnabled(11) && prefs.weeklyAuditSummary) ||
            (prefs.isEnabled(30) && prefs.newsNotifications)
        val wm = WorkManager.getInstance(app)
        if (needsWork) {
            wm.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<SolutionsWorker>(15, TimeUnit.MINUTES).build()
            )
        } else {
            wm.cancelUniqueWork(WORK_NAME)
        }
        if (prefs.isEnabled(12) && prefs.autoTrackOutages) registerPowerReceiver(app)
    }

    private fun registerPowerReceiver(app: Context) {
        if (powerReceiverRegistered) return
        powerReceiverRegistered = true
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        ContextCompat.registerReceiver(app, object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val prefs = SolutionsPrefs(context)
                if (!prefs.isEnabled(12) || !prefs.autoTrackOutages) return
                val plugged = intent.action == Intent.ACTION_POWER_CONNECTED
                val pending = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try { recordChargerState(context, plugged) } finally { pending.finish() }
                }
            }
        }, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    /** True when the phone is on a charger (AC, USB or wireless). */
    fun isPlugged(context: Context): Boolean? {
        val sticky = ContextCompat.registerReceiver(
            context, null, IntentFilter(Intent.ACTION_BATTERY_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED
        ) ?: return null
        return sticky.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) > 0
    }

    suspend fun recordChargerState(context: Context, plugged: Boolean) {
        val prefs = SolutionsPrefs(context)
        val previous = prefs.lastPlugged
        prefs.lastPlugged = if (plugged) 1 else 0
        if (previous == -1 || previous != (if (plugged) 1 else 0)) {
            SupplyLogger.log(context, plugged, "AUTO_CHARGER")
        }
    }

    suspend fun runChecks(context: Context) {
        val prefs = SolutionsPrefs(context)
        val now = System.currentTimeMillis()
        if (prefs.isEnabled(12) && prefs.autoTrackOutages) {
            isPlugged(context)?.let { recordChargerState(context, it) }
        }
        val events = SolutionsDatabase.get(context).dao().supplyEvents().first()
        val promised = prefs.promisedHours
        val days = SupplyMath.hoursByDay(events, 30, now)
        val firstLog = events.minOfOrNull { it.timestamp }
        val counted = days.dropLast(1).filter { firstLog != null && it.dayStart >= SupplyMath.startOfDay(firstLog) }

        if (prefs.isEnabled(12) && prefs.shortfallAlerts && counted.isNotEmpty() && now - prefs.lastShortfallAlert > SupplyMath.DAY_MS) {
            val run = SupplyMath.consecutiveShortDays(counted + days.last(), promised)
            val last7 = counted.takeLast(7)
            val avg7 = last7.sumOf { it.hours } / last7.size
            if (run >= 7 || avg7 < promised) {
                prefs.lastShortfallAlert = now
                SolutionsNotifier.postUpdate(
                    context, 4101, "You're owed for missing supply",
                    if (run >= 7) "$run days in a row below ${promised}h. Your feeder should be downgraded and you can claim compensation in Bright."
                    else "Your 7-day average is ${"%.1f".format(avg7)}h, below the ${promised}h promise. Open the refund tracker to claim."
                )
            }
        }
        if (prefs.isEnabled(11) && prefs.weeklyAuditSummary && counted.size >= 3 && now - prefs.lastAuditSummary > 7 * SupplyMath.DAY_MS) {
            val week = counted.takeLast(7)
            val avg = week.sumOf { it.hours } / week.size
            prefs.lastAuditSummary = now
            SolutionsNotifier.postUpdate(
                context, 4102, "Weekly band audit",
                "Last ${week.size} days: ${"%.1f".format(avg)}h a day on average (promise ${promised}h) - delivered like ${SupplyMath.deliveredBand(avg)}."
            )
        }
        if (prefs.isEnabled(30) && prefs.newsNotifications) {
            val fresh = CuratedTariffNews.items().filter { it.id !in prefs.notifiedNewsIds && it.id !in prefs.readNewsIds }
            if (fresh.isNotEmpty()) {
                prefs.notifiedNewsIds = prefs.notifiedNewsIds + fresh.map { it.id }
                SolutionsNotifier.postUpdate(context, 4103, "Tariff news", fresh.first().headline + if (fresh.size > 1) " (+${fresh.size - 1} more)" else "")
            }
        }
    }
}

class SolutionsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        SolutionsAutomation.runChecks(applicationContext)
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }
}
