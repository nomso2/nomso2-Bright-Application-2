package com.example.ui.solutions

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.solutions.MeterWaitlistEntity
import com.example.data.solutions.SupplyMath
import com.example.data.solutions.TokenQueueEntity
import com.example.model.DisCoContacts
import com.example.model.UserProfile
import com.example.ui.solutions.common.ActionButton
import com.example.ui.solutions.common.ChoiceRow
import com.example.ui.solutions.common.DemoDataBadge
import com.example.ui.solutions.common.DiscoContactButtons
import com.example.ui.solutions.common.EvidencePdf
import com.example.ui.solutions.common.FeatureHeader
import com.example.ui.solutions.common.Fmt
import com.example.ui.solutions.common.InfoNote
import com.example.ui.solutions.common.NumberField
import com.example.ui.solutions.common.SectionCard
import com.example.ui.solutions.common.SolutionIntents
import com.example.ui.solutions.common.StatRow
import com.example.ui.solutions.common.TextInput
import com.example.ui.solutions.common.customerBlock
import com.example.ui.solutions.common.rememberSolutionsDao
import com.example.ui.solutions.common.rememberSolutionsPrefs
import com.example.ui.solutions.refund.DailyHoursChart
import com.example.ui.solutions.refund.RefundTrackerFeature
import com.example.ui.solutions.refund.SupplyLogButtons
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// SOLUTION 11: AUTOMATED BAND AUDITOR
@Composable
fun AutomatedBandAuditorFeature(userProfile: UserProfile) {
    val context = LocalContext.current
    val dao = rememberSolutionsDao()
    val events by dao.supplyEvents().collectAsState(initial = emptyList())
    val now = System.currentTimeMillis()
    val promised = userProfile.feederBand.minimumHours
    val days = SupplyMath.hoursByDay(events, 30, now)
    val firstLog = events.minOfOrNull { it.timestamp }
    val counted = days.dropLast(1).filter { firstLog != null && it.dayStart >= SupplyMath.startOfDay(firstLog) }
    val avg = if (counted.isEmpty()) 0.0 else counted.sumOf { it.hours } / counted.size
    val below = counted.count { it.hours < promised }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(11, "Automated Band Auditor", "Counts the hours of supply you actually get each day (from your Light On/Off log) against your band, and builds a NERC evidence report.")
        SectionCard { SupplyLogButtons(events) }
        SectionCard {
            StatRow("Your band", "${userProfile.feederBand.code} (min ${promised}h/day)")
            StatRow("Days audited", "${counted.size}")
            StatRow("Average supply", if (counted.isEmpty()) "-" else Fmt.hours(avg),
                if (avg >= promised) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            StatRow("Days below promise", "$below")
            StatRow("Actually delivered", if (counted.isEmpty()) "-" else SupplyMath.deliveredBand(avg))
            if (counted.isNotEmpty()) DailyHoursChart(counted, promised)
        }
        if (counted.isNotEmpty() && avg < promised) {
            InfoNote("You pay for ${userProfile.feederBand.code} but receive ${SupplyMath.deliveredBand(avg)} service. Claim compensation in #12.", isWarning = true)
        }
        ActionButton("Create NERC evidence report (PDF)", Icons.Default.PictureAsPdf, {
            val lines = mutableListOf<String>()
            lines += customerBlock(userProfile).lines()
            lines += ""
            lines += "Audit period: ${counted.firstOrNull()?.let { Fmt.date(it.dayStart) } ?: "-"} to ${Fmt.date(now)}"
            lines += "Band promise: ${promised} h/day. Average delivered: ${Fmt.hours(avg)}. Days below promise: $below of ${counted.size}."
            lines += "Service delivered matches ${SupplyMath.deliveredBand(avg)}."
            lines += ""
            lines += counted.map { "${Fmt.date(it.dayStart)}  ${Fmt.hours(it.hours)}" + if (it.hours < promised) "  BELOW" else "" }
            lines += ""
            lines += "Source: customer's Light On/Off log recorded in the Bright app."
            val file = EvidencePdf.write(context, "bright_band_audit.pdf", "Band audit - meter ${userProfile.meterNumber}", lines)
            if (file != null) SolutionIntents.shareFile(context, file, "application/pdf", "Band audit report")
            else Toast.makeText(context, "Couldn't create the PDF", Toast.LENGTH_SHORT).show()
        }, enabled = counted.isNotEmpty())
    }
}

// SOLUTION 12: AUTOMATED REFUND LEDGER -> wasted-time refund tracker
@Composable
fun AutomatedRefundLedgerFeature(userProfile: UserProfile) = RefundTrackerFeature(userProfile)

// SOLUTION 13: COMMUNITY CONSUMPTION CALCULATOR
@Composable
fun CommunityConsumptionCalculatorFeature(userProfile: UserProfile) {
    val prefs = rememberSolutionsPrefs()
    val neighbours = remember { mutableStateListOf<Double>() }
    var entry by remember { mutableStateOf("") }
    var tariff by remember { mutableStateOf(prefs.tariffPaid.toString()) }
    var bill by remember { mutableStateOf("") }

    val sorted = neighbours.sorted()
    val median = when {
        sorted.isEmpty() -> 0.0
        sorted.size % 2 == 1 -> sorted[sorted.size / 2]
        else -> (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2
    }
    val avg = if (sorted.isEmpty()) 0.0 else sorted.average()
    val t = tariff.toDoubleOrNull() ?: 0.0
    val fair = median * t
    val billed = bill.toDoubleOrNull() ?: 0.0
    val over = billed - fair

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(13, "Community Consumption Calculator", "Add your metered neighbours' monthly units (from their token receipts). Bright uses the median to show what an unmetered home on your street should fairly pay.")
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Neighbour's monthly units", entry, { entry = it }, Modifier.weight(1f), suffix = "kWh")
                IconButton(onClick = {
                    entry.toDoubleOrNull()?.takeIf { it > 0 }?.let { neighbours.add(it); entry = "" }
                }, modifier = Modifier.heightIn(min = 48.dp)) { Icon(Icons.Default.Add, contentDescription = "Add neighbour reading") }
            }
            neighbours.forEachIndexed { i, v ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Home ${i + 1}: ${v.toInt()} kWh", fontSize = 16.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { neighbours.removeAt(i) }) { Icon(Icons.Default.Delete, contentDescription = "Remove home ${i + 1}") }
                }
            }
            if (neighbours.size < 3) Text("Add at least 3 homes for a fair comparison.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SectionCard {
            NumberField("Tariff (${userProfile.feederBand.code})", tariff, { tariff = it }, suffix = "₦/kWh")
            NumberField("Your estimated bill", bill, { bill = it }, suffix = "₦")
            StatRow("Median use", "${median.toInt()} kWh")
            StatRow("Average use", "${avg.toInt()} kWh")
            StatRow("Fair monthly bill", Fmt.naira(fair))
            if (billed > 0 && neighbours.size >= 3) {
                StatRow(if (over > 0) "Over-billed by" else "Under fair level by", Fmt.naira(kotlin.math.abs(over)),
                    if (over > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
        }
        if (billed > 0 && neighbours.size >= 3 && over > 0) {
            InfoNote("NERC requires estimated bills for unmetered customers to follow its estimated-billing methodology, and you have a right to contest any bill.")
            DiscoContactButtons(userProfile, "Estimated bill dispute - meter ${userProfile.meterNumber}",
                "I dispute my estimated bill of ${Fmt.naira(billed)}. ${neighbours.size} metered homes on my street use a median of ${median.toInt()} kWh/month, which at ₦$tariff/kWh is ${Fmt.naira(fair)}. Please review and refund the difference of ${Fmt.naira(over)}.\n${customerBlock(userProfile)}",
                showCall = false)
        }
    }
}

// SOLUTION 14: METER WAITLIST TRACKER & HALL OF SHAME
private val DEMO_WAITS = mapOf("KAEDC" to 64, "YEDC" to 58, "JED" to 51, "EEDC" to 47, "BEDC" to 42, "IBEDC" to 35, "PHED" to 31, "KEDCO" to 29, "AEDC" to 24, "IE" to 18, "EKEDC" to 15)

@Composable
fun MeterWaitlistTrackerFeature(userProfile: UserProfile) {
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val entries by dao.meterWaitlist().collectAsState(initial = emptyList())
    var disco by remember { mutableStateOf(userProfile.discoCode) }
    var paidOn by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var ref by remember { mutableStateOf("") }
    val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.UK).apply { isLenient = false } }
    val parsed = runCatching { fmt.parse(paidOn)?.time }.getOrNull()?.takeIf { it <= System.currentTimeMillis() }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(14, "Meter Waitlist Tracker & Hall of Shame", "Log the day you paid for a meter. Bright counts the days past the 10-day installation limit and drafts the complaint.")
        SectionCard {
            ChoiceRow(DisCoContacts.all.map { it.code }, disco) { disco = it }
            TextInput("Date paid (DD/MM/YYYY)", paidOn, { paidOn = it.take(10) }, keyboardType = KeyboardType.Number)
            NumberField("Amount paid", amount, { amount = it }, suffix = "₦")
            TextInput("Payment reference / receipt no.", ref, { ref = it })
            ActionButton("Start tracking", Icons.Default.Save, {
                scope.launch {
                    dao.insertMeterWait(MeterWaitlistEntity(discoCode = disco, paidOn = parsed!!, amountNaira = amount.toDoubleOrNull(), paymentReference = ref))
                    paidOn = ""; amount = ""; ref = ""
                }
            }, enabled = parsed != null)
            if (paidOn.length == 10 && parsed == null) Text("Enter a real past date as DD/MM/YYYY.", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }
        entries.forEach { e ->
            val end = e.installedOn ?: System.currentTimeMillis()
            val days = ((end - e.paidOn) / SupplyMath.DAY_MS).toInt()
            val late = days - 10
            SectionCard {
                StatRow("${e.discoCode} - paid ${Fmt.date(e.paidOn)}", if (e.installedOn != null) "Installed" else "$days days")
                if (e.paymentReference.isNotBlank()) StatRow("Reference", e.paymentReference)
                if (e.installedOn == null && late > 0) InfoNote("$late days past the 10-day limit.", isWarning = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (e.installedOn == null) OutlinedButton(onClick = {
                        scope.launch { dao.updateMeterWait(e.copy(installedOn = System.currentTimeMillis())) }
                    }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Mark installed") }
                    OutlinedButton(onClick = { scope.launch { dao.deleteMeterWait(e) } }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Remove") }
                }
                if (e.installedOn == null && late > 0) {
                    DiscoContactButtons(userProfile, "Meter not installed - paid ${Fmt.date(e.paidOn)}",
                        "I paid for a meter on ${Fmt.date(e.paidOn)} (ref ${e.paymentReference.ifBlank { "-" }}${e.amountNaira?.let { ", " + Fmt.naira(it) } ?: ""}). It is now $days days with no installation, $late days past the limit. Please install it or give a date.\n${customerBlock(userProfile)}",
                        showCall = false)
                }
            }
        }
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Hall of Shame - average wait", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                DemoDataBadge()
            }
            val mine = entries.groupBy { it.discoCode }.mapValues { (_, l) ->
                l.map { ((it.installedOn ?: System.currentTimeMillis()) - it.paidOn) / SupplyMath.DAY_MS }.average().toInt()
            }
            (DEMO_WAITS + mine).entries.sortedByDescending { it.value }.forEachIndexed { i, (code, d) ->
                StatRow("${i + 1}. $code" + if (mine.containsKey(code)) " (incl. yours)" else "", "$d days",
                    if (d > 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
            Text("Community averages are demo figures until reports are pooled on a server; your own entries are real.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// SOLUTION 15: OFFLINE TOKEN VENDING VIA USSD
private val BANK_USSD = listOf("GTBank" to "*737#", "First Bank" to "*894#", "UBA" to "*919#", "Zenith" to "*966#", "Access" to "*901#")

@Composable
fun OfflineTokenVendingFeature(userProfile: UserProfile) {
    val context = LocalContext.current
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val queue by dao.tokenQueue().collectAsState(initial = emptyList())
    var meter by remember { mutableStateOf(userProfile.meterNumber) }
    var amount by remember { mutableStateOf("5000") }
    var bank by remember { mutableStateOf(BANK_USSD.first().first) }
    val tokenInputs = remember { mutableStateOf(mapOf<Long, String>()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(15, "Offline Token Vending via USSD", "No internet? Queue the purchase, then buy through your bank's USSD menu (Pay Bills > Electricity) and save the 20-digit token here.")
        SectionCard {
            TextInput("Meter number", meter, { meter = it.filter(Char::isDigit).take(13) }, keyboardType = KeyboardType.Number)
            NumberField("Amount", amount, { amount = it }, suffix = "₦")
            ChoiceRow(BANK_USSD.map { it.first }, bank) { bank = it }
            val code = BANK_USSD.first { it.first == bank }.second
            ActionButton("Queue & dial $bank $code", Icons.Default.Dialpad, {
                val amt = amount.toDoubleOrNull() ?: return@ActionButton
                scope.launch { dao.insertToken(TokenQueueEntity(meterNumber = meter, amountNaira = amt, bankName = bank)) }
                SolutionIntents.copy(context, "Meter number", meter)
                SolutionIntents.dialUssd(context, code)
            }, enabled = meter.length >= 11 && (amount.toDoubleOrNull() ?: 0.0) > 0)
            Text("Your meter number is copied so you can paste it into the bank menu. USSD charges are set by your bank and network.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        queue.forEach { item ->
            SectionCard {
                StatRow("${Fmt.naira(item.amountNaira)} via ${item.bankName}", Fmt.dateTime(item.createdAt))
                StatRow("Meter", item.meterNumber)
                val token = item.token
                if (token != null) {
                    Text(token.chunked(4).joinToString("-"), fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { SolutionIntents.copy(context, "Token", token) }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null); Text(" Copy token")
                        }
                        OutlinedButton(onClick = { scope.launch { dao.deleteToken(item) } }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Remove") }
                    }
                } else {
                    val typed = tokenInputs.value[item.id] ?: ""
                    TextInput("Paste 20-digit token", typed, { v ->
                        tokenInputs.value = tokenInputs.value + (item.id to v.filter(Char::isDigit).take(20))
                    }, keyboardType = KeyboardType.Number)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            scope.launch { dao.updateToken(item.copy(token = typed)) }
                        }, enabled = typed.length == 20, modifier = Modifier.heightIn(min = 48.dp)) { Text("Save token") }
                        OutlinedButton(onClick = { scope.launch { dao.deleteToken(item) } }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Cancel") }
                    }
                    if (typed.isNotEmpty() && typed.length != 20) Text("STS tokens are exactly 20 digits (${typed.length}/20).", fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
