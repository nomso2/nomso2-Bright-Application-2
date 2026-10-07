package com.example.ui.solutions

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.solutions.common.ActionButton
import com.example.ui.solutions.common.BrightPermissions
import com.example.ui.solutions.common.ChoiceRow
import com.example.ui.solutions.common.DemoDataBadge
import com.example.ui.solutions.common.FeatureHeader
import com.example.ui.solutions.common.Fmt
import com.example.ui.solutions.common.InfoNote
import com.example.ui.solutions.common.NumberField
import com.example.ui.solutions.common.SectionCard
import com.example.ui.solutions.common.SirenPlayer
import com.example.ui.solutions.common.SolutionIntents
import com.example.ui.solutions.common.SolutionsNotifier
import com.example.ui.solutions.common.StatRow
import com.example.ui.solutions.common.SupplyLogger
import com.example.ui.solutions.common.TextInput
import com.example.ui.solutions.common.rememberPermissionAction
import com.example.ui.solutions.common.rememberSolutionsDao
import com.example.ui.solutions.common.rememberSolutionsPrefs
import kotlinx.coroutines.launch
import java.util.Calendar

// SOLUTION 26: SURGE RETURN WARNING
private val SURGE_DEVICES = listOf("TV", "Fridge / freezer", "Laptop", "Phone chargers", "Wi-Fi router", "Air conditioner", "Pumping machine", "Sound system")

@Composable
fun SurgeReturnWarningFeature() {
    val context = LocalContext.current
    val prefs = rememberSolutionsPrefs()
    var time by remember { mutableStateOf("") }
    var lead by remember { mutableStateOf("10 min") }
    var scheduledAt by remember { mutableLongStateOf(prefs.surgeAlertAt.takeIf { it > System.currentTimeMillis() } ?: 0L) }
    var checked by remember { mutableStateOf(prefs.surgeChecklist) }

    val restoreAt: Long? = run {
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull()
        val m = parts.getOrNull(1)?.toIntOrNull()
        if (parts.size != 2 || h == null || m == null || h !in 0..23 || m !in 0..59) null else {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            if (cal.timeInMillis <= System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.timeInMillis
        }
    }
    val leadMin = lead.substringBefore(" ").toIntOrNull() ?: 10

    val schedule = rememberPermissionAction(
        permissions = BrightPermissions.NOTIFICATIONS,
        title = "Allow surge alerts",
        rationale = "Bright needs notifications to warn you to unplug appliances before power returns."
    ) {
        val at = (restoreAt ?: return@rememberPermissionAction) - leadMin * 60_000L
        val fireAt = maxOf(at, System.currentTimeMillis() + 5_000L)
        SolutionsNotifier.scheduleSurgeWarning(context, fireAt)
        scheduledAt = fireAt
        Toast.makeText(context, "Surge warning set for ${Fmt.time(fireAt)}", Toast.LENGTH_SHORT).show()
    }
    val test = rememberPermissionAction(
        permissions = BrightPermissions.NOTIFICATIONS,
        title = "Allow surge alerts",
        rationale = "Bright needs notifications to show the surge warning."
    ) {
        if (!SolutionsNotifier.postSurge(context, "Test: unplug delicate appliances before power is restored.")) {
            Toast.makeText(context, "Notifications are turned off for Bright in Settings", Toast.LENGTH_LONG).show()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(26, "Surge Return Warning", "Tell Bright when power is due back (from your DisCo's schedule or #5) and it warns you to unplug delicate appliances before the surge.")
        SectionCard {
            TextInput("Power expected back at (HH:MM, 24-hour)", time, { time = it.take(5) }, keyboardType = KeyboardType.Number)
            Text("Warn me before", fontSize = 16.sp)
            ChoiceRow(listOf("5 min", "10 min", "15 min", "30 min"), lead) { lead = it }
            ActionButton("Set surge warning", Icons.Default.Alarm, schedule, enabled = restoreAt != null)
            if (time.length == 5 && restoreAt == null) Text("Use 24-hour time, e.g. 18:30.", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }
        if (scheduledAt > 0) {
            InfoNote("Warning set for ${Fmt.dateTime(scheduledAt)}. Android may deliver it a few minutes late to save battery.")
            ActionButton("Cancel warning", Icons.Default.AlarmOff, {
                SolutionsNotifier.cancelSurgeWarning(context); scheduledAt = 0L
            }, outlined = true)
        }
        ActionButton("Test the alert now", Icons.Default.NotificationsActive, test, outlined = true)
        SectionCard {
            Text("Unplug checklist", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            SURGE_DEVICES.forEach { d ->
                CheckRow(d, d in checked) { on ->
                    checked = if (on) checked + d else checked - d
                    prefs.surgeChecklist = checked
                }
            }
            if (checked.isNotEmpty()) {
                ActionButton("Reset checklist", null, { checked = emptySet(); prefs.surgeChecklist = emptySet() }, outlined = true)
            }
        }
    }
}

// SOLUTION 27: 'GRID IS BACK' AUDIO SIREN
@Composable
fun GridIsBackAudioSirenFeature() {
    val context = LocalContext.current
    val prefs = rememberSolutionsPrefs()
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val events by dao.supplyEvents().collectAsState(initial = emptyList())
    var playing by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(27, "Light is back alert", "When 'Light ON' is noted - by you, by the notification button, by your charger or by a neighbour - Bright plays a gentle sound so you can switch off the generator.")
        StatRow("Supply now", when (events.lastOrNull()?.isOn) { true -> "ON"; false -> "OFF"; null -> "Not logged" })
        if (playing) {
            ActionButton("Stop the sound", Icons.Default.VolumeOff, { SirenPlayer.stop(); playing = false }, danger = true)
        } else {
            ActionButton("Play the sound", Icons.Default.VolumeUp, { SirenPlayer.play(context, 6); playing = true }, outlined = true)
        }
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Neighbour reports", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                DemoDataBadge()
            }
            Text("When neighbour reports are pooled on a server, their 'Light ON' will trigger your siren. Try it:", fontSize = 14.sp)
            ActionButton("Simulate neighbour: light is back", null, {
                scope.launch {
                    SupplyLogger.log(context, true, "NEIGHBOUR_DEMO")
                    if (prefs.sirenArmed) { SirenPlayer.play(context, 10); playing = true }
                    else Toast.makeText(context, "Noted. Turn this tool on to hear the sound.", Toast.LENGTH_SHORT).show()
                }
            }, outlined = true)
        }
    }
}

// SOLUTION 28: APPLIANCE LOAD BUDGETER
private data class Appliance(val name: String, val watts: Int, val defaultHours: Double)

private val APPLIANCES = listOf(
    Appliance("LED bulb", 10, 6.0), Appliance("Ceiling / standing fan", 75, 8.0), Appliance("TV (LED 43\")", 100, 5.0),
    Appliance("Fridge", 150, 12.0), Appliance("Chest freezer", 200, 12.0), Appliance("Laptop", 65, 6.0),
    Appliance("Phone charger", 10, 3.0), Appliance("Wi-Fi router", 12, 24.0), Appliance("Pressing iron", 1000, 0.5),
    Appliance("Microwave", 1200, 0.3), Appliance("Washing machine", 500, 1.0), Appliance("Water pump (1 hp)", 750, 0.5),
    Appliance("Air conditioner (1.5 hp)", 1100, 6.0)
)

@Composable
fun ApplianceLoadBudgeterFeature() {
    val prefs = rememberSolutionsPrefs()
    val qty = remember { mutableStateMapOf<String, Int>() }
    val hours = remember { mutableStateMapOf<String, String>() }
    var inverter by remember { mutableStateOf("3500") }
    var tariff by remember { mutableStateOf(prefs.tariffPaid.toString()) }
    var budget by remember { mutableStateOf("30000") }

    val chosen = APPLIANCES.filter { (qty[it.name] ?: 0) > 0 }
    val peakW = chosen.sumOf { it.watts * (qty[it.name] ?: 0) }
    val dailyKwh = chosen.sumOf { it.watts * (qty[it.name] ?: 0) * (hours[it.name]?.toDoubleOrNull() ?: it.defaultHours) } / 1000.0
    val t = tariff.toDoubleOrNull() ?: 0.0
    val monthly = dailyKwh * 30 * t
    val inv = inverter.toDoubleOrNull() ?: 0.0
    val b = budget.toDoubleOrNull() ?: 0.0
    val biggest = chosen.maxByOrNull { it.watts * (qty[it.name] ?: 0) * (hours[it.name]?.toDoubleOrNull() ?: it.defaultHours) }
    val heavy = chosen.filter { it.watts >= 700 }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(28, "Appliance Load Budgeter", "Pick what you own and how long it runs. Bright totals your load, monthly units and cost, and tells you what to stagger.")
        SectionCard {
            APPLIANCES.forEach { a ->
                val q = qty[a.name] ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(a.name, fontSize = 16.sp, fontWeight = if (q > 0) FontWeight.Bold else FontWeight.Normal)
                        Text("${a.watts} W", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { if (q > 0) qty[a.name] = q - 1 }) { Icon(Icons.Default.Remove, contentDescription = "One less ${a.name}") }
                    Text("$q", fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                    IconButton(onClick = { qty[a.name] = q + 1 }) { Icon(Icons.Default.Add, contentDescription = "One more ${a.name}") }
                }
                if (q > 0) NumberField("${a.name}: hours per day", hours[a.name] ?: a.defaultHours.toString(), { hours[a.name] = it }, suffix = "h")
            }
        }
        SectionCard {
            NumberField("Inverter / generator capacity", inverter, { inverter = it }, suffix = "W")
            NumberField("Tariff", tariff, { tariff = it }, suffix = "₦/kWh")
            NumberField("Monthly budget", budget, { budget = it }, suffix = "₦")
        }
        SectionCard {
            StatRow("Peak load (all on at once)", "$peakW W", if (inv > 0 && peakW > inv) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            StatRow("Daily use", String.format(java.util.Locale.US, "%.2f kWh", dailyKwh))
            StatRow("Monthly units", String.format(java.util.Locale.US, "%.0f kWh", dailyKwh * 30))
            StatRow("Monthly cost", Fmt.naira(monthly), if (b > 0 && monthly > b) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
        }
        if (inv > 0 && peakW > inv * 0.8) {
            InfoNote(
                "Peak $peakW W is over 80% of your ${inv.toInt()} W capacity. " +
                    (if (heavy.isNotEmpty()) "Never run ${heavy.joinToString(" and ") { it.name.lowercase() }} at the same time." else "Spread loads across the day."),
                isWarning = true
            )
        }
        if (b > 0 && monthly > b && biggest != null && t > 0) {
            val q = qty[biggest.name] ?: 1
            val cutHours = (monthly - b) / (biggest.watts * q / 1000.0 * 30 * t)
            InfoNote("Over budget by ${Fmt.naira(monthly - b)}. Cutting the ${biggest.name.lowercase()} by about ${String.format(java.util.Locale.US, "%.1f", cutHours)} h/day brings you back within budget.", isWarning = true)
        } else if (chosen.isNotEmpty() && b > 0) {
            InfoNote("Within budget - ${Fmt.naira(b - monthly)} to spare each month.")
        }
    }
}

// SOLUTION 29: HYBRID ENERGY OPTIMIZER -> SolutionsHybridOptimizer.kt
// SOLUTION 30: TARIFF FLASH NEWS FEED -> SolutionsTariffNews.kt
