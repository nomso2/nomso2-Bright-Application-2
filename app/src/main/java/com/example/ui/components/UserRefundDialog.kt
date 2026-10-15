package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.database.AppDatabase
import com.example.data.meter.CompensationTokens
import com.example.data.solutions.SolutionsDatabase
import com.example.data.solutions.SolutionsPrefs
import com.example.data.solutions.SupplyMath
import com.example.model.UserProfile
import com.example.ui.meter.CompensationTokensSection
import com.example.ui.solutions.common.SolutionIntents
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

/**
 * User Refund: the one place for refunds.
 *  (a) Band refund calculator: hours short (from the band auditor's Light On/Off log, or typed)
 *      x average load in kW x tariff paid per kWh. Updates live as values change.
 *  (b) Late-repair compensation tokens linked to overdue complaints.
 *  (c) A refund claim letter built from (a) and (b), with Copy and Send.
 */
@Composable
fun UserRefundDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { SolutionsPrefs(context) }
    val supplyDao = remember { SolutionsDatabase.get(context).dao() }
    val events by supplyDao.supplyEvents().collectAsState(initial = emptyList())
    val complaintsFlow = remember(userProfile.meterNumber) {
        AppDatabase.getDatabase(context).complaintDao()
            .getComplaintsForMeter(userProfile.meterNumber)
            .map { list -> list.map { it.toDomain() } }
    }
    val complaints by complaintsFlow.collectAsState(initial = emptyList())

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            now = System.currentTimeMillis()
        }
    }

    // Band auditor logic (same as solution #11): count only days since logging started.
    val promised = userProfile.feederBand.minimumHours
    val days30 = remember(events, now) { SupplyMath.hoursByDay(events, 30, now) }
    val firstLog = events.minOfOrNull { it.timestamp }
    val counted = days30.dropLast(1).filter { firstLog != null && it.dayStart >= SupplyMath.startOfDay(firstLog) }
    val loggedHoursShort = counted.sumOf { max(0.0, promised - it.hours) }
    val avgSupplied = if (counted.isEmpty()) 0.0 else counted.sumOf { it.hours } / counted.size

    var manualHoursShort by remember { mutableStateOf("") }
    var avgKw by remember { mutableStateOf(prefs.avgLoadKw.toString()) }
    var tariff by remember { mutableStateOf(prefs.tariffPaid.toString()) }

    val hoursShort = manualHoursShort.toDoubleOrNull() ?: loggedHoursShort
    val kw = avgKw.toDoubleOrNull() ?: 0.0
    val tariffPerKwh = tariff.toDoubleOrNull() ?: 0.0
    val kwhShort = hoursShort * kw
    val bandRefundNaira = kwhShort * tariffPerKwh

    val compensation = remember(complaints, now) { CompensationTokens.entries(context, complaints, now) }

    val letter = buildRefundLetter(
        profile = userProfile,
        daysCounted = counted.size,
        avgSupplied = avgSupplied,
        hoursShort = hoursShort,
        hoursFromLog = manualHoursShort.toDoubleOrNull() == null,
        avgKw = kw,
        tariff = tariffPerKwh,
        kwhShort = kwhShort,
        refundNaira = bandRefundNaira,
        compensationLines = compensation.map {
            "- Fault ref ${it.complaintId}: ${it.hoursLate}h past the ${it.slaHours}h limit, ${it.kwhOwedText}, status ${it.status.label}"
        }
    )

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("user_refund_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("User Refund", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Meter ${userProfile.meterNumber} • ${userProfile.discoCode} • ${userProfile.feederBand.code}",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("user_refund_close")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // (a) Band refund calculator
                RefundSection("Band refund calculator") {
                    Text(
                        "Refund = hours short × average load (kW) × tariff (₦/kWh). Hours short come from your Light On/Off log against ${userProfile.feederBand.code}'s ${promised}h a day.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    RefundRow("Days logged", "${counted.size}")
                    RefundRow("Average supply", if (counted.isEmpty()) "-" else String.format(Locale.US, "%.1f h/day", avgSupplied))
                    RefundRow("Hours short (from log)", String.format(Locale.US, "%.1f h", loggedHoursShort))
                    if (counted.isEmpty()) {
                        Text(
                            "No complete days logged yet. Log Light On/Off in Bright tools, or type the hours short below.",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    RefundNumberField(
                        label = "Hours short (optional, overrides the log)",
                        value = manualHoursShort,
                        onChange = { manualHoursShort = it },
                        tag = "refund_hours_short"
                    )
                    RefundNumberField(
                        label = "Average load while supply is on (kW)",
                        value = avgKw,
                        onChange = { v -> avgKw = v; v.toFloatOrNull()?.let { prefs.avgLoadKw = it } },
                        tag = "refund_avg_kw"
                    )
                    RefundNumberField(
                        label = "Tariff you pay (₦/kWh)",
                        value = tariff,
                        onChange = { v -> tariff = v; v.toFloatOrNull()?.let { prefs.tariffPaid = it } },
                        tag = "refund_tariff"
                    )
                    RefundRow("Energy short", String.format(Locale.US, "%.1f kWh", kwhShort))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Band refund owed", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        Text(
                            naira(bandRefundNaira),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("refund_band_total")
                        )
                    }
                    Text(
                        "An estimate. Your DisCo makes the final calculation.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // (b) Late-repair compensation tokens
                RefundSection("Late-repair compensation tokens") {
                    CompensationTokensSection(
                        meterNumber = userProfile.meterNumber,
                        customerName = userProfile.customerName,
                        discoCode = userProfile.discoCode,
                        showHeading = false
                    )
                }

                // (c) Refund claim letter
                RefundSection("Refund claim letter") {
                    Text(
                        letter,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp)
                            .testTag("refund_letter_text")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { SolutionIntents.copy(context, "Refund claim letter", letter) },
                            modifier = Modifier.weight(1f).testTag("refund_letter_copy")
                        ) { Text("Copy", fontSize = 16.sp) }
                        OutlinedButton(
                            onClick = {
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Refund claim: meter ${userProfile.meterNumber}")
                                    putExtra(Intent.EXTRA_TEXT, letter)
                                }
                                context.startActivity(Intent.createChooser(send, "Send refund claim"))
                            },
                            modifier = Modifier.weight(1f).testTag("refund_letter_send")
                        ) { Text("Send", fontSize = 16.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RefundSection(title: String, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        content()
    }
}

@Composable
private fun RefundRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun RefundNumberField(label: String, value: String, onChange: (String) -> Unit, tag: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> onChange(v.filter { it.isDigit() || it == '.' }.take(10)) },
        label = { Text(label, fontSize = 16.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().testTag(tag)
    )
}

private fun naira(v: Double): String =
    "₦" + NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 2; minimumFractionDigits = 2 }.format(v)

private fun buildRefundLetter(
    profile: UserProfile,
    daysCounted: Int,
    avgSupplied: Double,
    hoursShort: Double,
    hoursFromLog: Boolean,
    avgKw: Double,
    tariff: Double,
    kwhShort: Double,
    refundNaira: Double,
    compensationLines: List<String>
): String = buildString {
    val date = SimpleDateFormat("d MMMM yyyy", Locale.UK).format(Date())
    appendLine("Refund claim")
    appendLine("Date: $date")
    appendLine()
    appendLine("To: ${profile.discoCode.ifBlank { "My DisCo" }} customer care")
    appendLine("From: ${profile.customerName.ifBlank { "Customer" }}")
    appendLine("Meter: ${profile.meterNumber}")
    if (profile.streetAddress.isNotBlank()) appendLine("Address: ${profile.streetAddress}")
    appendLine("Band: ${profile.feederBand.code} (${profile.feederBand.minimumHours}h supply a day)")
    appendLine()
    appendLine("1. Band shortfall")
    if (hoursFromLog) {
        appendLine("Days logged: $daysCounted. Average supply: ${String.format(Locale.US, "%.1f", avgSupplied)}h a day.")
    }
    appendLine("Hours short: ${String.format(Locale.US, "%.1f", hoursShort)}h${if (hoursFromLog) " (from my supply log)" else ""}")
    appendLine("Average load: ${String.format(Locale.US, "%.2f", avgKw)} kW. Tariff: ${naira(tariff)}/kWh.")
    appendLine("Energy short: ${String.format(Locale.US, "%.1f", kwhShort)} kWh. Refund requested: ${naira(refundNaira)}.")
    appendLine()
    appendLine("2. Late fault repairs (NERC limit: 24h phase fault, 48h transformer)")
    if (compensationLines.isEmpty()) appendLine("None overdue.") else compensationLines.forEach { appendLine(it) }
    appendLine()
    appendLine("Please credit the band refund to my account and issue the compensation tokens owed for the late repairs above to meter ${profile.meterNumber}.")
    append("Thank you.")
}
