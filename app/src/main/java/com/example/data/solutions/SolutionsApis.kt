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

class DemoGridPulseSource(private val random: Random = Random.Default) : GridPulseSource {
    override suspend fun latest(): GridPulse {
        val freq = 49.6 + random.nextDouble() * 0.7
        val gen = 3600 + random.nextInt(1600)
        return GridPulse(
            frequencyHz = (freq * 100).roundToInt() / 100.0,
            generationMw = gen,
            peakMw = 5801,
            takenAt = System.currentTimeMillis(),
            isDemo = true
        )
    }
}

data class FieldCrew(
    val name: String,
    val vehicleReg: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val isDemo: Boolean
)

interface CrewDirectory {
    suspend fun crewsNear(latitude: Double, longitude: Double): List<FieldCrew>
}

/** Places three fictional crews a short drive from the given point. */
class DemoCrewDirectory : CrewDirectory {
    override suspend fun crewsNear(latitude: Double, longitude: Double): List<FieldCrew> = listOf(
        FieldCrew("Crew A (demo) - Tunde B.", "LND-482-XY", "", latitude + 0.012, longitude + 0.008, true),
        FieldCrew("Crew B (demo) - Amaka O.", "KJA-219-KA", "", latitude - 0.025, longitude + 0.018, true),
        FieldCrew("Crew C (demo) - Musa I.", "ABJ-731-GW", "", latitude + 0.04, longitude - 0.03, true)
    )
}

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
