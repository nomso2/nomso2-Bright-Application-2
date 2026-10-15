package com.example.ui.meter

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.meter.HesRegistry
import com.example.data.meter.Iec62056Reader
import com.example.data.meter.MeterIdentity
import com.example.data.meter.MeterReading
import com.example.data.meter.MeterResult
import com.example.model.MeterManufacturer
import com.example.model.UserProfile
import com.example.ui.solutions.common.BrightPermissions
import com.example.ui.solutions.common.SolutionIntents
import com.example.ui.solutions.common.rememberPermissionAction
import kotlinx.coroutines.launch

/**
 * Dedicated smart meter connection. Real paths: nameplate scan, USB-OTG optical probe
 * (IEC 62056-21), a DisCo HES when configured, compensation tokens owed for overdue fault repairs, and SMS / USSD / call
 * backup. Until a DisCo grants HES access, the cloud path runs a clearly labelled DEMO meter.
 * Phones have no CIU RF or PLC radio, so those methods say so.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MeterConnectHub(userProfile: UserProfile) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var meterNumber by remember { mutableStateOf(MeterIdentity.meterNumber(context, userProfile.meterNumber)) }
    var manufacturer by remember { mutableStateOf(MeterIdentity.manufacturer(context)) }
    var identityNote by remember { mutableStateOf<String?>(null) }
    var showScanner by remember { mutableStateOf(false) }
    var reading by remember { mutableStateOf<MeterReading?>(null) }
    var busy by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var probes by remember { mutableStateOf(Iec62056Reader.findProbes(context)) }
    var ussd by remember { mutableStateOf("") }
    val disco = userProfile.discoCode
    val hesConfigured = manufacturer?.let { HesRegistry.config(context, disco, it) != null } == true

    LaunchedEffect(Unit) { probes = Iec62056Reader.findProbes(context) }

    val scan = rememberPermissionAction(
        permissions = BrightPermissions.CAMERA,
        title = "Camera for the nameplate",
        rationale = "Bright uses the camera only to read the barcode or QR code on your meter's label."
    ) { showScanner = true }

    fun saveIdentity() = MeterIdentity.save(context, meterNumber, manufacturer)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.testTag("meter_connect_hub")) {
        // 1. Identify the meter
        MeterCard("1. Your meter") {
            OutlinedTextField(
                value = meterNumber,
                onValueChange = { meterNumber = it.filter(Char::isDigit).take(13); saveIdentity() },
                label = { Text("Meter number (11-13 digits)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("meter_number_input")
            )
            Button(onClick = scan, modifier = Modifier.fillMaxWidth().testTag("scan_nameplate_button")) {
                Text("Scan nameplate barcode / QR")
            }
            Text("Manufacturer", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MeterManufacturer.entries.forEach { m ->
                    FilterChip(
                        selected = manufacturer == m,
                        onClick = { manufacturer = m; identityNote = "Picked by you."; saveIdentity(); reading = null },
                        label = { Text(m.displayName, fontSize = 12.sp) }
                    )
                }
            }
            identityNote?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }

        // 2. Live panel (real sources or DEMO only)
        TelemetryPanel(reading)
        message?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary) }

        // 3. Connection methods
        MeterCard("2. Optical probe (USB-OTG, IEC 62056-21)", badge = {
            StatusChip(if (probes.isEmpty()) "No probe" else "Probe found", ok = probes.isNotEmpty())
        }) {
            Text("Plug an optical (IR) probe into the phone with an OTG adapter and place it on the meter's optical port. Bright reads it in IEC 62056-21 mode C (starts at 300 baud, then switches).",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            probes.firstOrNull()?.let { Text(Iec62056Reader.describe(it), fontSize = 13.sp) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { probes = Iec62056Reader.findProbes(context) }) { Text("Look for probe") }
                Button(
                    enabled = probes.isNotEmpty() && busy == null,
                    onClick = {
                        val p = probes.firstOrNull()
                        if (p != null) busy = "optical"
                        if (p != null) scope.launch {
                            when (val r = Iec62056Reader.read(context, p, meterNumber)) {
                                is MeterResult.Ok -> { reading = r.value; message = "Read from the meter by optical probe." }
                                is MeterResult.Fail -> message = r.message
                            }
                            busy = null
                        }
                    }
                ) { Text(if (busy == "optical") "Reading..." else "Read meter") }
            }
        }

        MeterCard("3. DisCo HES / MDMS (cloud)", badge = {
            if (hesConfigured) StatusChip("HES connected", ok = true) else DemoBadge("DEMO MODE")
        }) {
            if (manufacturer == null) {
                Text("Pick your meter's manufacturer above first.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (hesConfigured) {
                Text("Using your DisCo's HES for ${manufacturer!!.displayName}. Readings are live from the server.", fontSize = 13.sp)
            } else {
                Text("Needs DisCo HES access. Until your DisCo issues an endpoint and key (add them in the manufacturers tab), this runs a DEMO ${manufacturer!!.displayName} meter with simulated values.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            manufacturer?.let { m ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(enabled = busy == null, onClick = {
                        busy = "hes"
                        scope.launch {
                            when (val r = HesRegistry.connectorFor(context, disco, m).readTelemetry(meterNumber)) {
                                is MeterResult.Ok -> { reading = r.value; message = if (r.value.source.isDemo) "DEMO reading (simulated)." else "Read from your DisCo's HES." }
                                is MeterResult.Fail -> message = r.message
                            }
                            busy = null
                        }
                    }) { Text(if (hesConfigured) "Read from HES" else "Read DEMO meter") }
                    OutlinedButton(enabled = busy == null, onClick = {
                        busy = "ping"
                        scope.launch {
                            val c = HesRegistry.connectorFor(context, disco, m)
                            message = when (val r = c.ping()) {
                                is MeterResult.Ok -> if (c.isDemo) "DEMO ping: ${r.value} ms (simulated latency)" else "App-to-server latency: ${r.value} ms (not meter latency)"
                                is MeterResult.Fail -> r.message
                            }
                            busy = null
                        }
                    }) { Text("Ping") }
                }
            }
        }

        // 4. Compensation tokens (owed by the DisCo for overdue fault repairs; no buying or recharging)
        MeterCard("4. Compensation tokens") {
            CompensationTokensSection(
                meterNumber = userProfile.meterNumber,
                customerName = userProfile.customerName,
                discoCode = userProfile.discoCode,
                showHeading = false
            )
        }

        // 5. SMS / USSD / call backup (only numbers Bright already holds from DisCo websites)
        MeterCard("5. SMS / USSD / call backup") {
            val contact = SolutionIntents.discoFor(userProfile)
            if (contact == null) {
                Text("Add your DisCo in your profile to see its official contacts.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("${contact.name}: numbers from its official website.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val body = "Meter $meterNumber${manufacturer?.let { " (${it.displayName})" } ?: ""}: please confirm my credit balance / compensation token status. ${userProfile.customerName}".trim()
                if (contact.primaryPhone.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { SolutionIntents.dial(context, contact.primaryPhone) }) { Text("Call") }
                        OutlinedButton(onClick = { SolutionIntents.sms(context, listOf(contact.primaryPhone), body) }) { Text("SMS") }
                        contact.whatsapp.firstOrNull()?.let { w ->
                            OutlinedButton(onClick = { SolutionIntents.whatsApp(context, w, body) }) { Text("WhatsApp") }
                        }
                    }
                }
            }
            Text("Your DisCo's balance USSD code (Bright doesn't list codes it hasn't verified; use the one on your receipt or DisCo SMS):",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(
                    value = ussd,
                    onValueChange = { ussd = it.filter { c -> c.isDigit() || c == '*' || c == '#' }.take(30) },
                    label = { Text("e.g. *xxx#") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(enabled = ussd.startsWith("*") && ussd.endsWith("#"), onClick = { SolutionIntents.dialUssd(context, ussd) }) { Text("Dial") }
            }
        }

        // 6. Radios a phone doesn't have
        MeterCard("6. CIU RF (433 / 868 MHz) and PLC", badge = { StatusChip("Needs RF bridge", ok = false) }) {
            Text("Keypad units (CIUs) talk to the meter by RF on 433 or 868 MHz, or over the power line (PLC). Phones have neither radio.",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            KeyValue("CIU RF 433 / 868 MHz", "Needs a Bright RF bridge (not available yet)", MaterialTheme.colorScheme.onSurfaceVariant)
            KeyValue("PLC", "Needs a Bright RF bridge (not available yet)", MaterialTheme.colorScheme.onSurfaceVariant)
            manufacturer?.let { m ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    OutlinedButton(enabled = busy == null, onClick = {
                        busy = "ciu"
                        scope.launch {
                            (com.example.data.meter.DemoHesConnector(m).readTelemetry(meterNumber) as? MeterResult.Ok)?.let {
                                reading = it.value
                                message = "DEMO CIU link: simulated values."
                            }
                            busy = null
                        }
                    }) { Text("Try DEMO CIU link") }
                    DemoBadge()
                }
            }
        }

        // Manual readings from the meter display / vendor app (real, entered by the user)
        ManualMeterReadingsCard()
    }

    if (showScanner) {
        NameplateScannerDialog(
            onResult = { raw ->
                showScanner = false
                val number = meterNumberFromPayload(raw)
                val guess = MeterManufacturer.guessFromText(raw)
                if (number != null) meterNumber = number
                if (guess != null) manufacturer = guess
                identityNote = buildString {
                    append(if (number != null) "Meter number read from the nameplate. " else "No 11-13 digit meter number in that code; type it. ")
                    append(if (guess != null) "Manufacturer read from the code: ${guess.displayName}." else "The code doesn't name the manufacturer; pick it above.")
                }
                saveIdentity()
                Toast.makeText(context, "Scanned", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showScanner = false }
        )
    }
}
