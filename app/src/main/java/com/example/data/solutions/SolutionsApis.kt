package com.example.data.solutions

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/*
 * Interfaces for the data only a DisCo, TCN or NERC backend can supply. Each has a Demo
 * implementation the UI labels "Demo data"; swap in a real implementation when an API exists.
 */

data class GridPulse(
    val frequencyHz: Double,
    val generationMw: Int,
    val peakMw: Int,
    val takenAt: Long,
    val isDemo: Boolean
) {
    /** TCN's normal band is 49.75-50.25 Hz; grid collapses happen well below 49.5 Hz. */
    val status: String
        get() = when {
            generationMw < 1000 || frequencyHz < 48.75 -> "Collapse risk"
            frequencyHz < 49.75 || frequencyHz > 50.25 -> "Unstable"
            else -> "Stable"
        }
}

interface GridPulseSource {
    suspend fun latest(): GridPulse
}

// The demo grid pulse source was removed: national grid figures now come from NISO
// (com.example.data.cloud.NisoGridFeed), daily, never simulated.

// Field crews: no DisCo publishes crew positions, so the tracker shows residents' crowdsourced
// sightings instead (com.example.data.cloud.CommunityFeed, Firestore crew_sightings).

data class OutageDiagnosis(
    val kind: String,
    val explanation: String,
    val expectedBackAt: Long?,
    val isDemo: Boolean
)

interface OutageStatusSource {
    suspend fun diagnose(feeder: String, lightIsOn: Boolean?, hasOpenReport: Boolean): OutageDiagnosis
}

/**
 * Demo rules: an open fault report you raised means "fault"; otherwise outages during the
 * evening peak (6-11 pm) look like load shedding with a 2-hour slot. Real data would come from
 * the DisCo's feeder schedule.
 */
class DemoOutageStatusSource : OutageStatusSource {
    override suspend fun diagnose(feeder: String, lightIsOn: Boolean?, hasOpenReport: Boolean): OutageDiagnosis {
        val now = System.currentTimeMillis()
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            lightIsOn == true -> OutageDiagnosis("Supply on", "Your log says the light is on.", null, true)
            hasOpenReport -> OutageDiagnosis(
                "Unplanned fault",
                "You have an open fault report. Load shedding is not scheduled for $feeder right now, so this is treated as a fault.",
                null, true
            )
            hour in 18..23 -> OutageDiagnosis(
                "Load shedding",
                "Evening peak: $feeder is in a load-shedding slot to stay within the TCN allocation.",
                now + 2 * SupplyMath.HOUR_MS, true
            )
            else -> OutageDiagnosis(
                "Unplanned fault",
                "No load-shedding slot is scheduled for $feeder now. Report it so a crew is sent.",
                null, true
            )
        }
    }
}

object Geo {
    /** Great-circle distance in km. */
    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = (lat2 - lat1) * PI / 180
        val dLon = (lon2 - lon1) * PI / 180
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1 * PI / 180) * cos(lat2 * PI / 180) * sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    /** ETA in minutes at a city average of ~22 km/h. */
    fun etaMinutes(km: Double): Int = ((km / 22.0) * 60).roundToInt().coerceAtLeast(3)
}

object Refs {
    fun make(prefix: String): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return prefix + "-" + (1..5).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}
