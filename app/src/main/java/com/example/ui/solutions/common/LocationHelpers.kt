package com.example.ui.solutions.common

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.location.LocationListenerCompat

/** Where a report is pinned: GPS fix, or what the user typed when GPS isn't available. */
data class PinnedLocation(
    val latitude: Double?,
    val longitude: Double?,
    val label: String,
    val accuracyMeters: Float? = null
) {
    val hasCoordinates: Boolean get() = latitude != null && longitude != null

    fun describe(): String =
        if (hasCoordinates) {
            val acc = accuracyMeters?.let { " (±${it.toInt()} m)" } ?: ""
            "%.5f, %.5f%s".format(latitude, longitude, acc) + if (label.isNotBlank()) " - $label" else ""
        } else label

    fun mapsLink(): String? =
        if (hasCoordinates) "https://maps.google.com/?q=$latitude,$longitude" else null

    companion object {
        /** Accepts "6.4281, 3.4219" typed by hand; anything else is kept as a landmark. */
        fun fromManual(text: String): PinnedLocation {
            val parts = text.split(",").map { it.trim() }
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                    return PinnedLocation(lat, lng, "typed coordinates")
                }
            }
            return PinnedLocation(null, null, text.trim())
        }
    }
}

/**
 * Plain LocationManager (no Play Services needed): uses a recent last-known fix, otherwise
 * asks GPS / network for one fresh fix and gives up after [timeoutMs].
 */
object LocationFetcher {
    private const val FRESH_MS = 2 * 60 * 1000L

    @SuppressLint("MissingPermission")
    fun current(context: Context, timeoutMs: Long = 15_000L, onResult: (Location?) -> Unit) {
        if (!BrightPermissions.hasAny(context, BrightPermissions.LOCATION)) {
            onResult(null)
            return
        }
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm == null) {
            onResult(null)
            return
        }
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
        val best = providers.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
        if (best != null && System.currentTimeMillis() - best.time < FRESH_MS) {
            onResult(best)
            return
        }
        val live = providers.firstOrNull { it != LocationManager.PASSIVE_PROVIDER }
        if (live == null) {
            onResult(best)
            return
        }
        val handler = Handler(Looper.getMainLooper())
        var done = false
        lateinit var listener: LocationListenerCompat
        val finish: (Location?) -> Unit = { loc ->
            if (!done) {
                done = true
                handler.removeCallbacksAndMessages(null)
                runCatching { lm.removeUpdates(listener) }
                onResult(loc ?: best)
            }
        }
        listener = LocationListenerCompat { loc -> finish(loc) }
        try {
            lm.requestLocationUpdates(live, 0L, 0f, listener, Looper.getMainLooper())
            handler.postDelayed({ finish(null) }, timeoutMs)
        } catch (e: Exception) {
            finish(null)
        }
    }
}
