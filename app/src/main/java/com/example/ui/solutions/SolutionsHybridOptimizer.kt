package com.example.ui.solutions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.solutions.SupplyEventEntity
import com.example.data.solutions.SupplyMath
import com.example.ui.solutions.common.ActionButton
import com.example.ui.solutions.common.BrightPermissions
import com.example.ui.solutions.common.FeatureHeader
import com.example.ui.solutions.common.InfoNote
import com.example.ui.solutions.common.LocationFetcher
import com.example.ui.solutions.common.NumberField
import com.example.ui.solutions.common.SectionCard
import com.example.ui.solutions.common.StatRow
import com.example.ui.solutions.common.rememberSolutionsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Locale

/** Today's hourly solar radiation (W/m2) and cloud cover (%) from Open-Meteo (free, no API key). */
data class SolarForecast(val hours: List<Int>, val radiation: List<Double>, val cloud: List<Int>, val place: String)

interface WeatherSource {
    suspend fun today(lat: Double, lng: Double, place: String): SolarForecast?
}

class OpenMeteoWeatherSource : WeatherSource {
    override suspend fun today(lat: Double, lng: Double, place: String): SolarForecast? = withContext(Dispatchers.IO) {
        try {
            val url = URL(String.format(Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&hourly=shortwave_radiation,cloud_cover&forecast_days=1&timezone=auto",
                lat, lng))
            val conn = (url.openConnection() as HttpURLConnection).apply { connectTimeout = 10_000; readTimeout = 10_000 }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val hourly = JSONObject(body).getJSONObject("hourly")
            val times = hourly.getJSONArray("time")
            val rad = hourly.getJSONArray("shortwave_radiation")
            val cloud = hourly.getJSONArray("cloud_cover")
            val n = times.length()
            SolarForecast(
                hours = (0 until n).map { times.getString(it).substringAfter("T").substringBefore(":").toInt() },
                radiation = (0 until n).map { rad.optDouble(it, 0.0) },
                cloud = (0 until n).map { cloud.optInt(it, 0) },
                place = place
            )
        } catch (e: Exception) {
            null
        }
    }
}

/** Share of logged days on which the light was OFF at each hour (0-23); null when under 3 days logged. */
internal fun outageRiskByHour(events: List<SupplyEventEntity>, now: Long): List<Double>? {
    if (events.isEmpty()) return null
    val first = SupplyMath.startOfDay(events.minOf { it.timestamp })
    val today = SupplyMath.startOfDay(now)
    val days = mutableListOf<Long>()
    val cal = Calendar.getInstance().apply { timeInMillis = first }
    while (cal.timeInMillis < today && days.size < 14) {
        days += cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    if (days.size < 3) return null
    val on = SupplyMath.onIntervals(events, now)
    return (0..23).map { h ->
        days.count { d ->
            val t = d + h * SupplyMath.HOUR_MS + 30 * 60_000L
            on.none { (s, e) -> t in s until e }
        }.toDouble() / days.size
    }
}

// SOLUTION 29: HYBRID ENERGY OPTIMIZER
@Composable
fun HybridEnergyOptimizerFeature() {
    val context = LocalContext.current
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val events by dao.supplyEvents().collectAsState(initial = emptyList())
    val weather = remember { OpenMeteoWeatherSource() }
    var battery by remember { mutableStateOf("5") }
    var soc by remember { mutableStateOf("60") }
    var panels by remember { mutableStateOf("2") }
    var forecast by remember { mutableStateOf<SolarForecast?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun fetch(lat: Double, lng: Double, place: String) {
        loading = true
        error = null
        scope.launch {
            val f = weather.today(lat, lng, place)
            loading = false
            if (f == null) error = "Couldn't reach the weather service. Check your internet and try again." else forecast = f
        }
    }

    val now = System.currentTimeMillis()
    val risk = remember(events) { outageRiskByHour(events, now) }
    val hourNow = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val kwp = panels.toDoubleOrNull() ?: 0.0
    val batKwh = battery.toDoubleOrNull() ?: 0.0
    val socPct = (soc.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(29, "Hybrid Energy Optimizer", "Combines today's solar forecast for your location with your own outage pattern to tell you when to charge from the grid and when to run on sun.")
        SectionCard {
            NumberField("Battery capacity", battery, { battery = it }, suffix = "kWh")
            NumberField("Battery charge now", soc, { soc = it }, suffix = "%")
            NumberField("Solar panels", panels, { panels = it }, suffix = "kWp")
        }
        ActionButton("Get today's solar forecast", Icons.Default.WbSunny, {
            if (BrightPermissions.hasAny(context, BrightPermissions.LOCATION)) {
                loading = true
                LocationFetcher.current(context) { loc ->
                    if (loc != null) fetch(loc.latitude, loc.longitude, "your location") else fetch(6.5244, 3.3792, "Lagos (default)")
                }
            } else fetch(6.5244, 3.3792, "Lagos (default - allow location in #2 for your area)")
        })
        if (loading) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp); Text("Checking the sky...", fontSize = 16.sp)
        }
        error?.let { InfoNote(it, isWarning = true) }

        val f = forecast
        if (f != null) {
            val solarKwh = f.radiation.sum() / 1000.0 * kwp * 0.75
            val peakIdx = f.radiation.indices.maxByOrNull { f.radiation[it] }
            val avgCloud = f.cloud.filterIndexed { i, _ -> f.hours[i] in 9..16 }.average().takeIf { !it.isNaN() } ?: 0.0
            SectionCard {
                StatRow("Forecast for", f.place)
                StatRow("Solar you can expect today", String.format(Locale.US, "%.1f kWh", solarKwh))
                StatRow("Daytime cloud cover", "${avgCloud.toInt()}%")
                peakIdx?.let { StatRow("Strongest sun", "${f.hours[it]}:00") }
                Text("Weather: Open-Meteo.com", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        SectionCard {
            Text("Advice", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            val next3 = risk?.let { r -> (1..3).map { r[(hourNow + it) % 24] }.maxOrNull() }
            if (risk == null) {
                Text("Log Light ON / OFF for at least 3 days (in #12) so Bright can learn your outage pattern.", fontSize = 16.sp)
            } else {
                StatRow("Chance of outage in next 3 h", "${((next3 ?: 0.0) * 100).toInt()}%",
                    if ((next3 ?: 0.0) >= 0.5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            }
            val gridOn = events.lastOrNull()?.isOn == true
            val tips = mutableListOf<String>()
            if (next3 != null && next3 >= 0.5 && socPct < 80 && gridOn) tips += "Charge your batteries from the grid now - an outage is likely within 2-3 hours."
            if (f != null) {
                val solarKwh = f.radiation.sum() / 1000.0 * kwp * 0.75
                val missing = batKwh * (1 - socPct / 100)
                if (solarKwh >= missing && hourNow < 15) tips += "Today's sun can refill the battery (needs ${String.format(Locale.US, "%.1f", missing)} kWh). Run heavy loads between 11:00 and 14:00 on solar."
                else if (missing > 0) tips += "Solar alone won't fill the battery today - top up from the grid while it's on."
            }
            if (risk != null) {
                val worst = risk.indices.maxByOrNull { risk[it] }
                worst?.let { tips += "Your light is most often off around $it:00 - plan to be on battery then." }
            }
            if (tips.isEmpty()) tips += "Nothing urgent. Get the forecast for tailored advice."
            tips.forEach { Text("- $it", fontSize = 16.sp) }
        }
    }
}
