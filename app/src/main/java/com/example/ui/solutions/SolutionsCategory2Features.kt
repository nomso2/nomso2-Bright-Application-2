package com.example.ui.solutions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.solutions.FaultReportEntity
import com.example.data.solutions.GridPulse
import com.example.data.solutions.Refs
import com.example.model.DisCoContacts
import com.example.model.FeederBand
import com.example.model.UserProfile
import com.example.ui.solutions.common.ActionButton
import com.example.ui.solutions.common.ChoiceRow
import com.example.ui.solutions.common.DemoDataBadge
import com.example.ui.solutions.common.DiscoContactButtons
import com.example.ui.solutions.common.FeatureHeader
import com.example.ui.solutions.common.Fmt
import com.example.ui.solutions.common.InfoNote
import com.example.ui.solutions.common.LocationPinner
import com.example.ui.solutions.common.PhotoThumb
import com.example.ui.solutions.common.PinnedLocation
import com.example.ui.solutions.common.SectionCard
import com.example.ui.solutions.common.SolutionIntents
import com.example.ui.solutions.common.StatRow
import com.example.ui.solutions.common.TextInput
import com.example.ui.solutions.common.customerBlock
import com.example.ui.solutions.common.rememberSolutionsDao
import com.example.ui.solutions.common.rememberSolutionsPrefs
import com.example.ui.solutions.common.rememberTakePhotoAction
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// SOLUTION 6: OFFLINE USSD / SMS DATA BRIDGE
@Composable
fun OfflineUssdBridgeFeature(userProfile: UserProfile) {
    val context = LocalContext.current
    val dao = rememberSolutionsDao()
    val prefs = rememberSolutionsPrefs()
    val scope = rememberCoroutineScope()
    var fault by remember { mutableStateOf("No light") }
    var note by remember { mutableStateOf("") }
    var ussd by remember { mutableStateOf(prefs.ussdCode) }

    val sms = "FAULT ${fault.uppercase()} | MTR ${userProfile.meterNumber} | TR ${userProfile.transformerId} | " +
        "${userProfile.streetAddress}, ${userProfile.lga}" + if (note.isNotBlank()) " | $note" else ""

    fun logIt(source: String) = scope.launch {
        dao.insertReport(FaultReportEntity(reference = Refs.make("SMS"), source = source, tier = 1, title = fault, details = sms, transformerId = userProfile.transformerId))
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(6, "Offline USSD / SMS Data Bridge", "No data? Report by plain SMS or USSD. Works on any network with airtime - no internet needed.")
        SectionCard {
            Text("What's wrong?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            ChoiceRow(listOf("No light", "Low voltage", "One phase", "Sparking", "Meter error", "Fallen pole"), fault) { fault = it }
            TextInput("Extra detail (optional)", note, { note = it.take(80) })
            Text("SMS preview (${sms.length} chars)", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(sms, fontSize = 16.sp)
        }
        DiscoContactButtons(userProfile, "Fault: $fault", sms, onUsed = { logIt("SMS") }, showCall = false)
        SectionCard {
            Text("USSD", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Bright's own USSD short code is not live yet. Save your DisCo's or bank's code here and dial it in one tap.", fontSize = 14.sp)
            TextInput("USSD code, e.g. *123#", ussd, { ussd = it.filter { c -> c.isDigit() || c == '*' || c == '#' } }, keyboardType = KeyboardType.Phone)
            ActionButton("Dial $ussd", Icons.Default.Dialpad, {
                prefs.ussdCode = ussd
                if (SolutionIntents.dialUssd(context, ussd)) logIt("USSD")
            }, enabled = ussd.startsWith("*") && ussd.endsWith("#"))
        }
    }
}

// SOLUTION 7: TAMPER CROWDSOURCING
@Composable
fun TamperCrowdsourcingFeature(userProfile: UserProfile) {
    val context = LocalContext.current
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val reports by dao.reports().collectAsState(initial = emptyList())
    var kind by remember { mutableStateOf("Cable cut") }
    var pin by remember { mutableStateOf<PinnedLocation?>(null) }
    var photo by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf<String?>(null) }
    val takePhoto = rememberTakePhotoAction { f -> if (f != null) photo = f.absolutePath }

    val dayAgo = System.currentTimeMillis() - 24 * 3_600_000L
    val recent = reports.filter { it.source == "TAMPER" && it.transformerId == userProfile.transformerId && it.createdAt > dayAgo }
    val flagged = recent.size >= 2

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(7, "Tamper Crowdsourcing", "Report meter bypass, cable theft or vandalism with a photo and pin. Two reports on one transformer in 24 hours raises a theft flag.")
        if (flagged) InfoNote("Potential vandalism / cable theft event on ${userProfile.transformerId}: ${recent.size} reports in 24 h.", isWarning = true)
        SectionCard {
            ChoiceRow(listOf("Cable cut", "Meter bypassed", "Transformer parts stolen", "Meter smashed", "Pillar opened", "Other"), kind) { kind = it }
        }
        ActionButton(if (photo == null) "Take evidence photo" else "Retake photo", Icons.Default.AddAPhoto, takePhoto, outlined = photo != null)
        photo?.let { PhotoThumb(it, "Tamper evidence photo", Modifier.fillMaxWidth().heightIn(max = 180.dp)) }
        LocationPinner(pin) { pin = it }
        ActionButton("Save tamper report", Icons.Default.Save, {
            val ref = Refs.make("TMP")
            scope.launch {
                dao.insertReport(FaultReportEntity(
                    reference = ref, source = "TAMPER", tier = 2, title = "Tamper: $kind",
                    details = pin?.describe() ?: "", transformerId = userProfile.transformerId,
                    latitude = pin?.latitude, longitude = pin?.longitude, photoPath = photo, isHazard = kind == "Cable cut"
                ))
                saved = ref
            }
        })
        saved?.let { ref ->
            InfoNote("Saved as $ref. Theft is a crime - you can also call the police.")
            ActionButton("Call 112", Icons.Default.Call, { SolutionIntents.dial(context, SolutionIntents.EMERGENCY_NUMBER) }, outlined = true)
            DiscoContactButtons(userProfile, "Vandalism report $ref",
                "Vandalism report $ref: $kind.\nLocation: ${pin?.mapsLink() ?: pin?.label ?: "-"}\n${customerBlock(userProfile)}", showCall = false)
        }
    }
}

// SOLUTION 8: LOW-POWER BAT-SIGNAL MODE
@Composable
fun LowPowerBatSignalFeature(isBatSignalMode: Boolean, onToggleBatSignal: (Boolean) -> Unit, userProfile: UserProfile) {
    val context = LocalContext.current
    val battery = remember { batteryPercent(context) }
    val disco = SolutionIntents.discoFor(userProfile)
    val ping = "SOS NO LIGHT. MTR ${userProfile.meterNumber}. TR ${userProfile.transformerId}. ${userProfile.streetAddress}, ${userProfile.lga}. Phone battery ${battery ?: "?"}%."

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(8, "Low-Power 'Bat-Signal' Mode", "A black, text-only screen that sends your outage ping by SMS in one tap - for when your phone is nearly dead.")
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("Bat-Signal mode", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Switch(checked = isBatSignalMode, onCheckedChange = onToggleBatSignal, modifier = Modifier.semantics { contentDescription = "Bat-Signal mode" })
        }
        StatRow("Phone battery", battery?.let { "$it%" } ?: "Unknown")
        if (isBatSignalMode) {
            Column(
                Modifier.fillMaxWidth().background(Color.Black, RoundedCornerShape(12.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pure black + white on purpose: OLED pixels are off on black.
                Text("BAT-SIGNAL", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text(ping, color = Color.White, fontSize = 16.sp)
                val smsTo = disco?.let { (it.whatsapp + it.phones).firstOrNull { n -> n.filter(Char::isDigit).length == 11 } }
                Button(
                    onClick = { SolutionIntents.sms(context, listOfNotNull(smsTo), ping) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                ) { Text("SEND SMS PING", fontWeight = FontWeight.Black) }
                if (disco != null && disco.primaryPhone.isNotBlank()) {
                    Button(
                        onClick = { SolutionIntents.dial(context, disco.primaryPhone) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) { Text("CALL ${disco.code}", fontWeight = FontWeight.Black) }
                }
            }
            ActionButton("Turn on phone Battery Saver", Icons.Default.BatterySaver, {
                try {
                    context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (e: Exception) {
                    Toast.makeText(context, "Open Settings > Battery", Toast.LENGTH_SHORT).show()
                }
            }, outlined = true)
        }
    }
}

private fun batteryPercent(context: Context): Int? {
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager ?: return null
    val v = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    return if (v in 0..100) v else null
}

// SOLUTION 9: UNIVERSAL METER SYNC (BARCODE & QR)
@Composable
fun UniversalMeterSyncFeature(userProfile: UserProfile) {
    val context = LocalContext.current
    val prefs = rememberSolutionsPrefs()
    var meter by remember { mutableStateOf(prefs.linkedMeter.ifBlank { userProfile.meterNumber }) }
    var disco by remember { mutableStateOf(prefs.linkedMeterDisco.ifBlank { userProfile.discoCode }) }
    var band by remember { mutableStateOf(userProfile.feederBand.code) }
    var status by remember { mutableStateOf<String?>(null) }
    var linked by remember { mutableStateOf(prefs.linkedMeter) }

    val scan = rememberTakePhotoAction { file ->
        if (file == null) return@rememberTakePhotoAction
        status = "Reading barcode..."
        try {
            val image = InputImage.fromFilePath(context, Uri.fromFile(file))
            BarcodeScanning.getClient().process(image)
                .addOnSuccessListener { codes ->
                    val digits = codes.mapNotNull { it.rawValue }
                        .map { raw -> Regex("\\d{11,13}").find(raw.replace(" ", ""))?.value }
                        .firstOrNull { it != null }
                    if (digits != null) {
                        meter = digits
                        status = "Found meter number $digits. Check it, then link."
                    } else status = "No meter number found in that photo. Hold the camera closer to the barcode, or type it."
                    file.delete()
                }
                .addOnFailureListener {
                    status = "Couldn't read the barcode. Type the number instead."
                    file.delete()
                }
        } catch (e: Exception) {
            status = "Couldn't open the photo. Type the number instead."
        }
    }

    val valid = meter.length in 11..13 && meter.all { it.isDigit() }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(9, "Universal Meter Sync", "Scan the barcode or QR sticker on your prepaid meter (or type the number) to link it with your DisCo and band.")
        ActionButton("Scan meter barcode / QR", Icons.Default.QrCodeScanner, scan)
        status?.let { InfoNote(it) }
        TextInput("Meter number (11-13 digits)", meter, { meter = it.filter(Char::isDigit).take(13) }, keyboardType = KeyboardType.Number)
        if (meter.isNotEmpty() && !valid) Text("Prepaid meter numbers are 11 to 13 digits.", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        Text("DisCo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        ChoiceRow(DisCoContacts.all.map { it.code }, disco) { disco = it }
        Text("Feeder band", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        ChoiceRow(FeederBand.entries.map { it.code }, band) { band = it }
        ActionButton("Link this meter", Icons.Default.Save, {
            prefs.linkedMeter = meter
            prefs.linkedMeterDisco = disco
            linked = meter
            Toast.makeText(context, "Meter $meter linked to $disco", Toast.LENGTH_SHORT).show()
        }, enabled = valid)
        if (linked.isNotBlank()) {
            SectionCard {
                StatRow("Linked meter", linked)
                StatRow("DisCo", prefs.linkedMeterDisco)
                StatRow("Band", band)
                DisCoContacts.forCode(prefs.linkedMeterDisco)?.let { Text(it.name, fontSize = 14.sp) }
            }
        }
    }
}

// SOLUTION 10: NATIONAL GRID PULSE MONITOR
// Real figures from NISO's public homepage, published once a day. No live frequency is shown
// because no public live feed exists.
@Composable
fun NationalGridPulseMonitorFeature() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<com.example.model.GridTelemetry?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun load() {
        loading = true
        scope.launch {
            data = com.example.data.cloud.NisoGridFeed.refresh(context)
            loading = false
        }
    }
    LaunchedEffect(Unit) {
        data = com.example.data.cloud.NisoGridFeed.cached(context)
        load()
    }
    val d = data

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(10, "National Grid Pulse Monitor", "Yesterday's national grid figures as published by NISO. Daily figures, not real-time.")
        if (d == null || !d.hasData) {
            InfoNote(if (loading) "Fetching today's figures from NISO..." else "No NISO figures saved yet. Connect to the internet and tap Refresh.")
        } else {
            val freqOk = d.peakFrequencyHz?.let { it in 49.75..50.25 } ?: true
            SectionCard {
                StatRow("Figures for", d.reportDate)
                StatRow("Source", "NISO (niso.org.ng), daily")
                d.peakGenerationMw?.let { StatRow("Peak generation", "${"%,.2f".format(it)} MW at ${d.peakTime}") }
                d.peakFrequencyHz?.let {
                    StatRow("Frequency at peak", "$it Hz (target 50.00)", if (freqOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
                d.offPeakGenerationMw?.let { StatRow("Off-peak generation", "${"%,.2f".format(it)} MW at ${d.offPeakTime}") }
                d.offPeakFrequencyHz?.let { StatRow("Frequency at off-peak", "$it Hz") }
                d.energyGeneratedMwh?.let { StatRow("Energy generated", "${"%,.2f".format(it)} MWh") }
                d.energySentOutMwh?.let { StatRow("Energy sent out", "${"%,.2f".format(it)} MWh") }
                if (d.fetchedAt > 0) StatRow("Checked", Fmt.dateTime(d.fetchedAt))
            }
            if (d.isFromCache) InfoNote("Couldn't reach NISO just now. These are the last saved figures (for ${d.reportDate}).", isWarning = true)
            Text("NISO's normal band is 49.75-50.25 Hz. Bright doesn't show a live frequency: there is no public real-time feed.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        ActionButton(if (loading) "Refreshing..." else "Refresh now", Icons.Default.Refresh, { load() }, outlined = true, enabled = !loading)
        ActionButton("Open NISO website", Icons.Default.OpenInNew, {
            try {
                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(com.example.data.cloud.NisoGridFeed.SOURCE_URL)))
            } catch (e: Exception) { }
        }, outlined = true)
    }
}
