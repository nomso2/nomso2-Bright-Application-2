package com.example.ui.solutions.refund

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.solutions.SupplyMath
import com.example.model.UserProfile
import com.example.ui.solutions.common.BrightPermissions
import com.example.ui.solutions.common.FeatureHeader
import com.example.ui.solutions.common.Fmt
import com.example.ui.solutions.common.InfoNote
import com.example.ui.solutions.common.NumberField
import com.example.ui.solutions.common.SectionCard
import com.example.ui.solutions.common.SolutionsNotifier
import com.example.ui.solutions.common.StatRow
import com.example.ui.solutions.common.rememberPermissionAction
import com.example.ui.solutions.common.rememberSolutionsDao
import com.example.ui.solutions.common.rememberSolutionsPrefs
import com.example.ui.theme.extendedColors
import kotlinx.coroutines.delay

/** What the tracker worked out from the log; shared with the claim section. */
data class RefundSummary(
    val periodStart: Long,
    val periodEnd: Long,
    val promisedHours: Int,
    val avgHours: Double,
    val suppliedTotal: Double,
    val promisedTotal: Double,
    val daysCounted: Int,
    val shortDays: Int,
    val consecutiveShort: Int,
    val deliveredBand: String,
    val owedNaira: Double,
    val dailyLines: List<String>
) {
    val isShort: Boolean get() = daysCounted > 0 && avgHours < promisedHours
}

/**
 * SOLUTION 12 (replaces the static "Rebates & Refunds"): wasted-time refund tracker.
 * Basis: NERC Order NERC/334/2022 and Addendum NERC/2024/003 - Band A promises at least
 * 20 hours a day; customers who get less are owed the gap between the tariff paid and the
 * tariff for the service actually delivered, and a feeder below 20 hours for 7 consecutive
 * days should be downgraded.
 */
@Composable
fun RefundTrackerFeature(userProfile: UserProfile) {
    val context = LocalContext.current
    val dao = rememberSolutionsDao()
    val prefs = rememberSolutionsPrefs()
    val events by dao.supplyEvents().collectAsState(initial = emptyList())
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            now = System.currentTimeMillis()
        }
    }

    var units by remember { mutableStateOf(prefs.unitsPerMonthKwh.toString()) }
    var tariffPaid by remember { mutableStateOf(prefs.tariffPaid.toString()) }
    var tariffDelivered by remember { mutableStateOf(prefs.tariffDelivered.toString()) }
    var quickLog by remember { mutableStateOf(prefs.quickLogNotification) }

    val promised = userProfile.feederBand.minimumHours
    val days30 = remember(events, now) { SupplyMath.hoursByDay(events, 30, now) }
    // Only count days since logging started, so empty days before install don't look like blackouts.
    val firstLog = events.minOfOrNull { it.timestamp }
    val counted = days30.dropLast(1).filter { firstLog != null && it.dayStart >= SupplyMath.startOfDay(firstLog) }
    val last7 = counted.takeLast(7)
    val avg7 = if (last7.isEmpty()) 0.0 else last7.sumOf { it.hours } / last7.size
    val avgAll = if (counted.isEmpty()) 0.0 else counted.sumOf { it.hours } / counted.size
    val consecutive = SupplyMath.consecutiveShortDays(counted + days30.last(), promised)
    val today = days30.last().hours

    val unitsInPeriod = (units.toDoubleOrNull() ?: 0.0) * counted.size / 30.0
    val deliveredBand = SupplyMath.deliveredBand(avgAll)
    val owed = if (counted.isNotEmpty() && avgAll < promised)
        SupplyMath.estimateOwedNaira(unitsInPeriod, tariffPaid.toDoubleOrNull() ?: 0.0, tariffDelivered.toDoubleOrNull() ?: 0.0)
    else 0.0

    val summary = RefundSummary(
        periodStart = counted.firstOrNull()?.dayStart ?: SupplyMath.startOfDay(now),
        periodEnd = now,
        promisedHours = promised,
        avgHours = avgAll,
        suppliedTotal = counted.sumOf { it.hours },
        promisedTotal = counted.size * promised.toDouble(),
        daysCounted = counted.size,
        shortDays = counted.count { it.hours < promised },
        consecutiveShort = consecutive,
        deliveredBand = deliveredBand,
        owedNaira = owed,
        dailyLines = counted.map { "${Fmt.date(it.dayStart)}: ${Fmt.hours(it.hours)} supplied (promise ${promised}h)" }
    )

    val enableQuickLog = rememberPermissionAction(
        permissions = BrightPermissions.NOTIFICATIONS,
        title = "Light On/Off from your notifications",
        rationale = "Bright can keep Light ON / Light OFF buttons in your notification shade, so logging takes one tap without opening the app."
    ) {
        if (SolutionsNotifier.showQuickLog(context, events.lastOrNull()?.isOn)) {
            prefs.quickLogNotification = true
            quickLog = true
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(12, "Wasted-Time Refund Tracker", "Log when your light goes on and off. Bright counts your supply hours against your band's promise and works out what the DisCo owes you.")
        SectionCard { SupplyLogButtons(events) }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("Light On/Off buttons in notifications", fontSize = 16.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = quickLog,
                onCheckedChange = { on ->
                    if (on) enableQuickLog() else {
                        prefs.quickLogNotification = false
                        quickLog = false
                        SolutionsNotifier.hideQuickLog(context)
                    }
                },
                modifier = Modifier.semantics { contentDescription = "Light On and Off buttons in notifications" }
            )
        }

        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                HoursRing(today, promised)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${userProfile.feederBand.code} promise: ${promised}h/day", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    StatRow("7-day average", if (last7.isEmpty()) "-" else Fmt.hours(avg7),
                        if (avg7 >= promised) MaterialTheme.extendedColors.success else MaterialTheme.colorScheme.error)
                    StatRow("Days logged", "${counted.size}")
                    StatRow("Short days", "${summary.shortDays}")
                    StatRow("Delivered like", if (counted.isEmpty()) "-" else deliveredBand)
                }
            }
            if (counted.isNotEmpty()) DailyHoursChart(days30.filter { firstLog != null && it.dayStart >= SupplyMath.startOfDay(firstLog) }, promised)
            if (counted.isEmpty()) Text("Averages start after your first full day of logging.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (consecutive >= 7) {
            InfoNote("$consecutive days in a row below ${promised}h. Under NERC's rule a feeder that misses its band for 7 consecutive days should be downgraded - and you are owed compensation.", isWarning = true)
        } else if (counted.isNotEmpty() && avg7 < promised) {
            InfoNote("Shortfall: your 7-day average is ${Fmt.hours(avg7)}, below the ${promised}h promise. ${7 - consecutive} more short day(s) in a row hits the downgrade rule.", isWarning = true)
        }

        SectionCard {
            Text("What you're owed (estimate)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            NumberField("Units you buy per month", units, { units = it; it.toFloatOrNull()?.let { v -> prefs.unitsPerMonthKwh = v } }, suffix = "kWh")
            NumberField("Tariff you pay (${userProfile.feederBand.code})", tariffPaid, { tariffPaid = it; it.toFloatOrNull()?.let { v -> prefs.tariffPaid = v } }, suffix = "₦/kWh")
            NumberField("Tariff for the band you actually got", tariffDelivered, { tariffDelivered = it; it.toFloatOrNull()?.let { v -> prefs.tariffDelivered = v } }, suffix = "₦/kWh")
            Text("Check both tariffs on your DisCo's current tariff table; Band A was ₦209.50/kWh from July 2024.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            StatRow("Supplied vs promised", "${Fmt.hours(summary.suppliedTotal)} / ${Fmt.hours(summary.promisedTotal)}")
            StatRow("Estimated owed", Fmt.naira(owed), if (owed > 0) MaterialTheme.extendedColors.success else MaterialTheme.colorScheme.onSurface)
            Text(
                "Estimate = units bought in the logged period x (tariff paid - tariff for the band delivered). Prepaid is credited as kWh token; postpaid as a bill adjustment. Your DisCo makes the final calculation.",
                fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box { RefundClaimsSection(userProfile, summary) }
    }
}
