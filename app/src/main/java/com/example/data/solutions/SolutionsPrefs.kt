package com.example.data.solutions

import android.content.Context
import android.content.SharedPreferences

/** Small settings for the solutions (no secrets are stored here). */
class SolutionsPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /** Main on/off switch for each of the 25 settings-based solutions (see SolutionSettingsCatalog). */
    fun isEnabled(number: Int): Boolean = prefs.getBoolean("enabled_$number", true)

    fun setEnabled(number: Int, on: Boolean) = prefs.edit().putBoolean("enabled_$number", on).apply()

    /** The siren (#27) is armed exactly when its setting is on. */
    var sirenArmed: Boolean
        get() = isEnabled(27)
        set(v) = setEnabled(27, v)

    /** ALARM, RINGTONE or NOTIFICATION. */
    var sirenSound: String
        get() = prefs.getString("siren_sound", "NOTIFICATION") ?: "NOTIFICATION"
        set(v) = prefs.edit().putString("siren_sound", v).apply()

    /** Volume for the in-app alert sound, 10-100 (gentle by default). */
    var sirenVolume: Int
        get() = prefs.getInt("siren_volume", 50)
        set(v) = prefs.edit().putInt("siren_volume", v.coerceIn(10, 100)).apply()

    /** #12: log Light ON / OFF automatically from the phone's charger (power-presence proxy). */
    var autoTrackOutages: Boolean
        get() = prefs.getBoolean("auto_track_outages", true)
        set(v) = prefs.edit().putBoolean("auto_track_outages", v).apply()

    /** Last charger state the auto-tracker saw: 1 plugged, 0 unplugged, -1 unknown. */
    var lastPlugged: Int
        get() = prefs.getInt("last_plugged", -1)
        set(v) = prefs.edit().putInt("last_plugged", v).apply()

    var shortfallAlerts: Boolean
        get() = prefs.getBoolean("shortfall_alerts", true)
        set(v) = prefs.edit().putBoolean("shortfall_alerts", v).apply()

    var lastShortfallAlert: Long
        get() = prefs.getLong("last_shortfall_alert", 0L)
        set(v) = prefs.edit().putLong("last_shortfall_alert", v).apply()

    /** Band code chosen in refund settings; blank = use the profile's band. */
    var refundBand: String
        get() = prefs.getString("refund_band", "") ?: ""
        set(v) = prefs.edit().putString("refund_band", v).apply()

    /** Promised hours/day, cached for background checks that can't see the profile. */
    var promisedHours: Int
        get() = prefs.getInt("promised_hours", 20)
        set(v) = prefs.edit().putInt("promised_hours", v).apply()

    var weeklyAuditSummary: Boolean
        get() = prefs.getBoolean("weekly_audit_summary", true)
        set(v) = prefs.edit().putBoolean("weekly_audit_summary", v).apply()

    var lastAuditSummary: Long
        get() = prefs.getLong("last_audit_summary", 0L)
        set(v) = prefs.edit().putLong("last_audit_summary", v).apply()

    /** #26: warn the moment power returns, before plugging appliances back in. */
    var surgeOnReturn: Boolean
        get() = prefs.getBoolean("surge_on_return", true)
        set(v) = prefs.edit().putBoolean("surge_on_return", v).apply()

    var newsNotifications: Boolean
        get() = prefs.getBoolean("news_notifications", true)
        set(v) = prefs.edit().putBoolean("news_notifications", v).apply()

    var notifiedNewsIds: Set<String>
        get() = prefs.getStringSet("notified_news", emptySet()) ?: emptySet()
        set(v) = prefs.edit().putStringSet("notified_news", v).apply()

    var stageNotifications: Boolean
        get() = prefs.getBoolean("stage_notifications", true)
        set(v) = prefs.edit().putBoolean("stage_notifications", v).apply()

    /** #8: switch Bat-Signal on automatically below this battery %, 0 = never. */
    var batSignalThreshold: Int
        get() = prefs.getInt("bat_signal_threshold", 15)
        set(v) = prefs.edit().putInt("bat_signal_threshold", v).apply()

    var quickLogNotification: Boolean
        get() = prefs.getBoolean("quick_log_notification", false)
        set(v) = prefs.edit().putBoolean("quick_log_notification", v).apply()

    var linkedMeter: String
        get() = prefs.getString("linked_meter", "") ?: ""
        set(v) = prefs.edit().putString("linked_meter", v).apply()

    var linkedMeterDisco: String
        get() = prefs.getString("linked_meter_disco", "") ?: ""
        set(v) = prefs.edit().putString("linked_meter_disco", v).apply()

    var unitsPerMonthKwh: Float
        get() = prefs.getFloat("units_month_kwh", 150f)
        set(v) = prefs.edit().putFloat("units_month_kwh", v).apply()

    var tariffPaid: Float
        get() = prefs.getFloat("tariff_paid", 209.5f)
        set(v) = prefs.edit().putFloat("tariff_paid", v).apply()

    var tariffDelivered: Float
        get() = prefs.getFloat("tariff_delivered", 63f)
        set(v) = prefs.edit().putFloat("tariff_delivered", v).apply()

    var surgeChecklist: Set<String>
        get() = prefs.getStringSet("surge_checklist", emptySet()) ?: emptySet()
        set(v) = prefs.edit().putStringSet("surge_checklist", v).apply()

    var surgeAlertAt: Long
        get() = prefs.getLong("surge_alert_at", 0L)
        set(v) = prefs.edit().putLong("surge_alert_at", v).apply()

    var invitesSent: Int
        get() = prefs.getInt("invites_sent", 0)
        set(v) = prefs.edit().putInt("invites_sent", v).apply()

    var readNewsIds: Set<String>
        get() = prefs.getStringSet("read_news", emptySet()) ?: emptySet()
        set(v) = prefs.edit().putStringSet("read_news", v).apply()

    /** True once the first-launch permission explanation has been shown. */
    var firstRunPermissionsAsked: Boolean
        get() = prefs.getBoolean("first_run_permissions_asked", false)
        set(v) = prefs.edit().putBoolean("first_run_permissions_asked", v).apply()

    var ussdCode: String
        get() = prefs.getString("ussd_code", "") ?: ""
        set(v) = prefs.edit().putString("ussd_code", v).apply()

    companion object {
        const val FILE = "bright_solutions_prefs"
    }
}
